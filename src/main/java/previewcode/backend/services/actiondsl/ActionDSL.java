package previewcode.backend.services.actiondsl;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import previewcode.backend.services.actiondsl.WithSyntax.*;

import java.util.function.Consumer;
import java.util.function.Function;

public class ActionDSL {

    /**
     * An action that does nothing.
     * Used mainly in testing.
     *
     * @param <A>
     */
    public static class NoOp<A> extends Action<A> { }

    /**
     * Represents an action that a service can take.
     * Actions can be combined with several methods to
     * support any kind of control flow over actions.
     *
     * @param <A> Represents the result type of the action
     */
    public static abstract class Action<A> {

        /**
         * Discard the result of the current action.
         * @return An action with the same effect that returns {@code Unit}.
         */
        public Action<Unit> toUnit() {
            return this.map(x -> unit);
        }

        /**
         * Discard the result of the current action, and instead return {@code value}.
         * @param value The value to return
         * @param <B> Type of the new value
         * @return An action with the same effect that returns {@code value}
         */
        public <B> Action<B> pure(B value) {
            return this.map(x -> value);
        }

        /**
         * Given a function {@code A -> B}, map this function over an Action.
         * In other words, map will lift a pure function over an Action.         *
         *
         * @param f The function to lift over the current action.
         * @param <B> The resulting action return type
         * @return The transformed action
         */
        public <B> Action<B> map(Function<? super A, ? extends B> f) {
            return new Apply<>(new Return<>(f), this);
        }


        /**
         * This method enables combination of multiple independent action results.
         *
         * {@code ap} is quite a 'low level' operator, so most of the time it will
         * be more convenient to use one of the following (which use {@code ap} internally):
         * <br>
         *     {@link ActionDSL#sequence(Seq)}, <br>
     *         {@link ActionDSL#traverse(Function)} or <br>
     *         {@link ActionDSL#traverse(Seq, Function)}
         * <br>
         * <br>
         * Example:
         * <pre>
         * {@code
         *
         *  Action<Integer> four = pure(4);
         *  Action<Integer> three = pure(3);
         *  Action<Integer> two = pure(2);
         *  Action<Integer> one = pure(1);
         *
         *  // Result: 10
         *  Action<Integer> applied = one.ap(two.ap(three.ap(four.map(d -> c -> b -> a -> a+b+c+d))));
         * }
         * </pre>
         */
        public <B> Action<B> ap(Action<Function<? super A, ? extends B>> f) {
            return new Apply<>(f ,this);
        }


        /**
         * Compose functions that return Actions.
         *
         * This allows for sequential computation on Actions:
         *
         * <pre>
         * {@code
         *
         *  | Compute Action<A>
         *  |
         *  |      | Take <A> and run the next Action<B>
         *  |      |
         *  |      |       | Take <B> and run the next Action<C>
         *  |      |       |
         *  |      |       |       | Take the resulting <C> and perform the last action
         *  a.then(f).then(g).then(h)
         * }
         * </pre>
         *
         *
         * @param f Function that takes the result of the current action, and returns the next action to run.
         * @param <B> The type of the final action
         * @return The composed action
         */
        public <B> Action<B> then(Function<? super A, ? extends Action<B>> f) {
            return new Suspend<>(f, this);
        }


        public <B> Action<B> then(Action<B> next) {
            return then(x -> next);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    public static class Return<A> extends Action<A> {
        public final A value;

        public Return(A value) {
            this.value = value;
        }

        @Override
        public <B> Action<B> map(Function<? super A, ? extends B> f) {
            return new Return<>(f.apply(this.value));
        }

        @Override
        public <B> Action<B> ap(Action<Function<? super A, ? extends B>> f) {
            return f.map(fAB -> fAB.apply(value));
        }

        @Override
        public <B> Action<B> then(Function<? super A, ? extends Action<B>> f) {
            return f.apply(this.value);
        }
    }

    public static class Apply<A, X> extends Action<A> {
        public final Action<Function<? super X, ? extends A>> f;
        public final Action<X> action;

        public Apply(Action<Function<? super X, ? extends A>> f, Action<X> action) {
            this.f = f;
            this.action = action;
        }

        @Override
        public <B> Action<B> map(Function<? super A, ? extends B> f) {
            return new Apply<>(this.f.map(f::compose), action);
        }

        @Override
        public <B> Action<B> ap(Action<Function<? super A, ? extends B>> f) {
            return new Apply<>(this.f.ap(f.map(fBC -> fBC::compose)), action);
        }
    }

    public static class Suspend<A, X> extends Action<A> {
        public final Action<X> action;
        public final Function<? super X, ? extends Action<A>> f;

        public Suspend(Function<? super X, ? extends Action<A>> f, Action<X> action) {
            this.action = action;
            this.f = f;
        }

        @Override
        public <B> Action<B> map(Function<? super A, ? extends B> f) {
            return new Suspend<>(this.f.andThen(next -> next.map(f)), this.action);
        }

        @Override
        public <B> Action<B> then(Function<? super A, ? extends Action<B>> f) {
            Action<A> next = this.action.then(this.f);
            return new Suspend<>(f, next);
        }
    }

    /**
     * Lift a value into the context of an Action.
     * The result will be an Action that when evaluated performs no effect.
     * Therefore this action is 'pure', and will only return {@code value}.
     *
     * @param value The value to lift
     * @param <A> Type of the lifted value
     * @return A pure action
     */
    public static <A> Action<A> pure(A value) {
        return new Return<>(value);
    }

    /**
     * Take a sequence of actions and turn them into a single action.
     *
     * @param actions A sequence of independent actions
     * @param <A> The type of each action
     * @return A single action that returns if all sequenced actions have completed.
     */
    public static <A> Action<List<A>> sequence(List<Action<A>> actions) {
        Function<A, Function<? super List<A>, ? extends List<A>>> cons = x -> xs -> xs.append(x);
        return actions.map(a -> a.map(cons)).foldLeft(pure(List.empty()), Action::ap);
    }

    /**
     * Given a function that generates an action, and a sequence of values,
     * map each value to an action and then sequence these actions.
     *
     * @param xs The sequence of values to map
     * @param f Function to turn each value into an action
     * @param <A> Type of values
     * @param <B> Result type of the actions
     * @return  A single action that returns if all sequenced actions have completed.
     */
    public static <A, B> Action<List<B>> traverse(List<A> xs, Function<? super A, ? extends Action<B>> f) {
        return sequence(xs.map(f));
    }

    /**
     * Curried version of {@link #traverse(Seq, Function)}.
     */
    public static <A, B> Function<List<A>, Action<List<B>>> traverse(Function<? super A, ? extends Action<B>> f) {
        return xs -> sequence(xs.map(f));
    }


    public static <A> W1<A> with(Action<A> a) {
        return new W1<A>(a);
    }

    /**
     * Action that returns Unit.
     * This is analogous to a method that does nothing and returns void.
     */
    public static final Action<Unit> returnU = new Return<>(Unit.VALUE);

    /**
     * The Unit data type represents a result of a computation with no information.
     * Serves the same purpose as Java's `void`, but since void/{@link Void} is not a type and has no values,
     * it's impossible to represent {@code Action<void>}.
     *
     * Instead, an action that returns nothing is represented as {@code Action<Unit>}.
     */
    public static final Unit unit = Unit.VALUE;

    /**
     * Takes a consumer, which is essentially a {@code Function<A, void>},
     * and represent it as a {@code Function<A, Unit>}.
     */
    public static <A> Function<A, Unit> toUnit(Consumer<A> f) {
        return a -> {
            f.accept(a);
            return unit;
        };
    }

    public static Interpreter interpret() {
        return new Interpreter();
    }
}

