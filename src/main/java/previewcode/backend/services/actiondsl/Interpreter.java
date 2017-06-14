package previewcode.backend.services.actiondsl;

import io.atlassian.fugue.Either;
import io.atlassian.fugue.Try;
import io.atlassian.fugue.Unit;
import io.vavr.collection.List;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static previewcode.backend.services.actiondsl.ActionDSL.*;


public class Interpreter {
    private final Map<Class<? extends Action>, Object> handlers;

    public static class InterpreterBuilder<A, X extends Action<A>> {

        private final Interpreter interpreter;
        private final Class<X> actionClass;

        protected InterpreterBuilder(Interpreter interpreter, Class<X> actionClass) {
            this.interpreter = interpreter;
            this.actionClass = actionClass;
        }

        /**
         * When this action is encountered, simply return {@code value}.
         *
         * @param value The value to return
         * @return The interpreter that returns {@code value} for action {@code X}
         */
        public Interpreter returnA(A value) {
            return apply(action -> value);
        }

        /**
         * When this action is encountered, run the {@code handler} function.
         * @param handler Function that handles actions of type {@code X}
         * @return An interpreter that handles {@code X}
         */
        public Interpreter apply(Function<X, ? extends A> handler) {
            interpreter.handlers.put(actionClass, handler);
            return interpreter;
        }

        /**
         * When this action is encountered, halt the execution with
         * a {@code StoppedException}.
         *
         * @param handler The function to run before raising a {@code StoppedException}.
         * @return An interpreter that halts on {@code X}
         */
        public Interpreter stop(Consumer<X> handler) {
            return apply(x -> {
                handler.accept(x);
                throw new StoppedException();
            });
        }

        /**
         * When this action is encountered, halt the execution with
         * a {@code StoppedException}.
         *
         * @return An interpreter that halts on {@code X}
         */
        public Interpreter stop() {
            return this.stop(action -> {});
        }
    }

    public static class StoppedException extends RuntimeException { }

    public Interpreter() {
        handlers = new HashMap<>();
    }

    /**
     * Compose multiple interpreters.
     *
     * If multiple interpreter handle the same action,
     * the handler of the last interpreter will be called.
     */
    public Interpreter(Interpreter ... interpreters) {
        this();
        List.of(interpreters).forEach(interpreter -> handlers.putAll(interpreter.handlers));
    }

    /**
     * Build an interpreter that recognizes {@code actionClass}
     *
     * @param actionClass The action that needs to be interpreted
     * @param <A> The actions return type
     * @param <X> The action type
     * @return A Builder which can be used to associate a handler with this action.
     */
    public <A, X extends Action<A>> InterpreterBuilder<A, X> on(Class<X> actionClass) {
        return new InterpreterBuilder<>(this, actionClass);
    }


    /**
     * Create a stepped interpreter from the current interpreter.
     * @param action The action to evaluate stepwise
     * @param <A> The result of the action
     */
    public <A> Stepper<A> stepwiseEval(Action<A> action) {
        return new Stepper<>(this, action);
    }

    /**
     * Build an interpreter that does nothing for the given action.
     *
     * The action return type must be {@code Unit} for the
     * interpreter to be able to ignore the action.
     *
     * @param actionClass The action to ignore when evaluating.
     * @return The new interpreter
     */
    public Interpreter ignore(Class<? extends Action<Unit>> actionClass) {
        this.handlers.put(actionClass, toUnit(a -> {}));
        return this;
    }

    /**
     * Evaluate an action with the current interpreter.
     *
     * @param action The action to run
     * @param <A> Represents the result type
     * @return A Try monad encapsulating any errors that occur during evaluation
     */
    public <A> Try<A> evaluate(Action<A> action) {
        return run(action);
    }

    /**
     * Evaluate an action with the current interpreter.
     * This method does not encapsulate errors in {@code Try<A>},
     * which means that any error will be thrown immediately.
     *
     * @param action The action to run
     * @param <A> Represents the result type
     * @return The result of running the action
     * @throws Exception when an error occurs during evaluation
     */
    public <A> A unsafeEvaluate(Action<A> action) throws Exception {
        Either<Exception, A> result = evaluate(action).toEither();

        if (result.isLeft()) {
            throw result.left().get();
        } else {
            return result.right().get();
        }
    }

    /**
     * Evaluate an action and build a response from the action result.
     * @param action The action to evaluate
     * @return The response built from the action result.
     * @throws Exception when an error occurs during evaluation
     */
    public Response evaluateToResponse(Action<?> action) throws Exception {
        return Response.ok().entity(unsafeEvaluate(action)).build();
    }

    @SuppressWarnings("unchecked")
    protected <A, X> Try<A> run(Action<A> action) {
        if (action == null) return Try.failure(new NullPointerException("Attempting to run a null action."));

        if (action instanceof Return) {
            return Try.successful(((Return<A>) action).value);
        } else if (action instanceof Suspend) {
            Suspend<A, X> suspendedAction = (Suspend<A, X>) action;
            return run(suspendedAction.action)
                    .flatMap(suspendedAction.f.andThen(this::run));
        } else if (action instanceof Apply) {
            Apply<A, X> applyAction = (Apply<A, X>) action;
            Try<X> applicant = run(applyAction.action);
            Try<Function<? super X, ? extends A>> applier = run(applyAction.f);
            return applicant.flatMap(x -> applier.map(fXA -> fXA.apply(x)));
        } else {
            if (handlers.containsKey(action.getClass())) {
                Function<Action<A>, ? extends A> handler = (Function<Action<A>, ? extends A>) handlers.get(action.getClass());
                try {
                    A result = handler.apply(action);
                    if (result != null) {
                        return Try.successful(result);
                    } else {
                        throw new RuntimeException("Action handler for " + action.getClass().getSimpleName() + " returned null.");
                    }
                } catch (Exception e) {
                    return Try.failure(e);
                }
            } else {
                return Try.failure(new RuntimeException("Unexpected action: " + action));
            }
        }
    }

    /**
     * Specialized interpreter that allows stepwise evaluation of an action.
     * At each step, the Actions that will be evaluated on the next step can
     * be retrieved using {@link Stepper#peek()}.
     * <br>
     * <br>
     * This allows you to easilly inspect which effects an action has at each
     * point in the computation.
     * @param <A>
     */
    public static class Stepper<A>  {

        protected Action<A> currentAction;
        protected final Interpreter evaluator;

        Stepper(Interpreter interpreter, Action<A> currentAction) {
            this.currentAction = currentAction;
            evaluator = interpreter;
        }

        /**
         * Evaluate the next iteration of the action that this Stepper was constructed with.
         * Evaluates all actions that are currently visible with {@link Stepper#peek()}.
         * <br>
         * <br>
         * Note that:
         * <pre>
         * {@code
         *
         * // a equals b
         * List<Action<?>> a = stepper.next();
         * List<Action<?>> b = stepper.peek();
         * }
         * </pre>
         * @return A list of all actions that will be evaluated on the next step.
         * @throws DoneException if there is nothing more to evaluate.
         * @throws Exception when evaluation fails.
         */
        public List<Action<?>> next() throws Exception {
            if (currentAction == null) {
                throw new DoneException();
            } else {
                Try<Either<A, Action<A>>> stepped = unwrap(currentAction);

                if (stepped.isSuccess()) {
                    Either<A, Action<A>> result = stepped.toEither().right().get();
                    if (result.isLeft()) {
                        currentAction = null;
                    } else {
                        currentAction = result.right().get();
                    }
                } else {
                    throw stepped.toEither().left().get();
                }
                return peek();
            }
        }

        /**
         * Continue normal evaluation without pausing.
         * This is equivalent to calling {@link Interpreter#unsafeEvaluate(Action)}
         * with the current state of the Stepper.
         * @return The result of running the entire action.
         * @throws Exception when evaluation fails.
         */
        public A runRemainingSteps() throws Exception {
            return evaluator.unsafeEvaluate(currentAction);
        }

        /**
         * Returns all actions that would be evaluated in the next iteration.
         * Note that not all effects in the action may be visible since sequential
         * computations like {@code x.then(y)} only allow you to see {@code y} after
         * {@code x} was evaluated.
         * <br>
         * <br>
         * Use {@link Stepper#next()} to advance the computation.
         * @return The list of all effects that are visible for the current action.
         */
        public List<Action<?>> peek() {
            return peek(currentAction);
        }

        protected List<Action<?>> peek(Action<?> action) {
            if (action instanceof Return) {
                return List.empty();
            } else if (action instanceof Suspend) {
                return peek(((Suspend) action).action);
            } else if (action instanceof Apply) {
                return peek(((Apply) action).f).appendAll(peek(((Apply) action).action));
            } else {
                return List.of(action);
            }
        }

        protected  <B, X> Try<Either<B, Action<B>>> unwrap(Action<B> action) {
            if (action instanceof Suspend) {
                Suspend<B, X> suspendedAction = (Suspend<B, X>) action;

                return unwrap(suspendedAction.action).map(result -> {
                    if (result.isLeft()) {
                        return Either.right(suspendedAction.f.apply(result.left().get()));
                    } else {
                        return Either.right(new Suspend<>(suspendedAction.f, result.right().get()));
                    }
                });
            } else if (action instanceof Apply) {
                Apply<B, X> applyAction = (Apply<B, X>) action;

                return unwrap(applyAction.action).flatMap(resultOrAction -> unwrap(applyAction.f).map(funOrAction -> {
                    if (resultOrAction.isLeft()) {
                        if (funOrAction.isLeft()) {
                            return Either.left(funOrAction.left().get().apply(resultOrAction.left().get()));
                        } else {
                            return Either.right(new Apply<>(funOrAction.right().get(), pure(resultOrAction.left().get())));
                        }
                    } else {
                        if (funOrAction.isLeft()) {
                            return Either.right(new Apply<>(pure(funOrAction.left().get()), resultOrAction.right().get()));
                        } else {
                            return Either.right(new Apply<>(funOrAction.right().get(), resultOrAction.right().get()));
                        }
                    }
                }));
            } else {
                return evaluator.run(action).map(Either::left);
            }
        }

        public static class DoneException extends RuntimeException { }
    }
}
