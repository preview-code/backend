package previewcode.backend.services.actiondsl;

import io.vavr.*;
import previewcode.backend.services.actiondsl.ActionDSL.Action;

import java.util.function.Function;

public class WithSyntax {

    public static class W1<A> {
        private final Action<A> a;

        public W1(Action<A> a) {
            this.a = a;
        }

        public <B> W2<A, B> and(Action<B> b) {
            return new W2<>(a,b);
        }
    }


    public static class W2<A, B> {
        private final Action<A> a;
        private final Action<B> b;

        public W2(Action<A> a, Action<B> b) {
            this.a = a;
            this.b = b;
        }

        public <C> W3<A,B,C> and(Action<C> c) {
            return new W3<>(a, b, c);
        }

        public <R> Action<R> apply(Function<? super A, Function<? super B, ? extends R>> f) {
            return b.ap(a.map(f));
        }

        public <R> Action<R> apply(Function2<? super A,? super B,? extends R> f) {
            Action<R> r = b.ap(a.map(f::apply));
            return r;
        }

        public <R> Action<R> then(Function<? super A, Function<? super B, ? extends Action<R>>> f) {
            return apply(f).then(x -> x);
        }

        public <R> Action<R> then(Function2<? super A,? super B, ? extends Action<R>> f) {
            Action<Action<R>> r = apply(f);
            Action<R> r2 = r.then(x -> x);
            return apply(f).then(x -> x);
        }
    }

    public static class W3<A, B, C> {
        private final Action<A> a;
        private final Action<B> b;
        private final Action<C> c;

        public W3(Action<A> a, Action<B> b, Action<C> c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public <D> W4<A,B,C,D> and(Action<D> d) {
            return new W4<>(a,b,c,d);
        }

        public <R> Action<R> apply(Function3<? super A,? super B,? super C,? extends R> f) {
            return c.ap(b.ap(a.map(a -> f.curried().apply(a))));
        }

        public <R> Action<R> apply(Function<? super A, Function<? super B, Function<? super C, ? extends R>>> f) {
            return c.ap(b.ap(a.map(f)));
        }

        public <R> Action<R> then(Function<? super A, Function<? super B, Function<? super C, ? extends Action<R>>>> f) {
            return apply(f).then(x -> x);
        }

        public <R> Action<R> then(Function3<? super A,? super B,? super C,? extends Action<R>> f) {
            return apply(f).then(x -> x);
        }
    }

    public static class W4<A, B, C, D> {
        private final Action<A> a;
        private final Action<B> b;
        private final Action<C> c;
        private final Action<D> d;

        public W4(Action<A> a, Action<B> b, Action<C> c, Action<D> d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        public <E> W5<A,B,C,D,E> and(Action<E> e) {
            return new W5<>(a,b,c,d,e);
        }

        public <R> Action<R> apply(Function4<? super A,? super B,? super C,? super D,? extends R> f) {
            return d.ap(c.ap(b.ap(a.map(a -> f.curried().apply(a)))));
        }

        public <R> Action<R> apply(Function<? super A, Function<? super B, Function<? super C, Function<? super D, ? extends R>>>> f) {
            return d.ap(c.ap(b.ap(a.map(f))));
        }

        public <R> Action<R> then(Function<? super A, Function<? super B, Function<? super C, Function<? super D, ? extends Action<R>>>>> f) {
            return apply(f).then(x -> x);
        }

        public <R> Action<R> then(Function4<? super A,? super B,? super C,? super D,? extends Action<R>> f) {
            return apply(f).then(x -> x);
        }
    }

    public static class W5<A, B, C, D, E> {
        private final Action<A> a;
        private final Action<B> b;
        private final Action<C> c;
        private final Action<D> d;
        private final Action<E> e;

        public W5(Action<A> a, Action<B> b, Action<C> c, Action<D> d, Action<E> e) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
        }

        public <R> Action<R> apply(Function5<? super A,? super B,? super C,? super D,? super E,? extends R> f) {
            return e.ap(d.ap(c.ap(b.ap(a.map(a -> f.curried().apply(a))))));
        }

        public <R> Action<R> apply(Function<? super A, Function<? super B, Function<? super C, Function<? super D, Function<? super E, ? extends R>>>>> f) {
            return e.ap(d.ap(c.ap(b.ap(a.map(f)))));
        }

        public <R> Action<R> then(Function<? super A, Function<? super B, Function<? super C, Function<? super D, Function<? super E, ? extends Action<R>>>>>> f) {
            return apply(f).then(x -> x);
        }

        public <R> Action<R> then(Function5<? super A,? super B,? super C,? super D,? super E,? extends Action<R>> f) {
            return apply(f).then(x -> x);
        }
    }
}
