package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.adt.coproduct.CoProduct3;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.Functor;

public abstract class Step<Head, Tail> implements
        CoProduct3<Step.Emitted<Head, Tail>, Step.Elided<Head, Tail>, Step.Exhausted<Head, Tail>, Step<Head, Tail>>,
        Functor<Tail, Step<Head, ?>> {

    private Step() {
    }

    @Override
    public abstract <NewTail> Step<Head, NewTail> fmap(Fn1<? super Tail, ? extends NewTail> fn);

    public static <Head, Tail> Emitted<Head, Tail> emitted(Head head, Tail tail) {
        return new Emitted<>(head, tail);
    }

    public static <Head, Tail> Elided<Head, Tail> elided(Tail tail) {
        return new Elided<>(tail);
    }

    @SuppressWarnings("unchecked")
    public static <Head, Tail> Exhausted<Head, Tail> exhausted() {
        return (Exhausted<Head, Tail>) Exhausted.INSTANCE;
    }

    public static final class Emitted<Head, Tail> extends Step<Head, Tail> {

        private final Head head;
        private final Tail tail;

        private Emitted(Head head, Tail tail) {
            this.head = head;
            this.tail = tail;
        }

        public Head head() {
            return head;
        }

        public Tail tail() {
            return tail;
        }

        @Override
        public <NewTail> Emitted<Head, NewTail> fmap(Fn1<? super Tail, ? extends NewTail> fn) {
            return emitted(head, fn.apply(tail));
        }

        @Override
        public <R> R match(Fn1<? super Emitted<Head, Tail>, ? extends R> aFn,
                           Fn1<? super Elided<Head, Tail>, ? extends R> bFn,
                           Fn1<? super Exhausted<Head, Tail>, ? extends R> cFn) {
            return aFn.apply(this);
        }

        @Override
        public String toString() {
            return "Emitted{" +
                    "head=" + head +
                    ", tail=" + tail +
                    '}';
        }
    }

    public static final class Elided<Head, Tail> extends Step<Head, Tail> {
        private final Tail tail;

        private Elided(Tail tail) {
            this.tail = tail;
        }

        public Tail tail() {
            return tail;
        }

        @Override
        public <NewTail> Elided<Head, NewTail> fmap(Fn1<? super Tail, ? extends NewTail> fn) {
            return elided(fn.apply(tail));
        }

        @Override
        public <R> R match(Fn1<? super Emitted<Head, Tail>, ? extends R> aFn,
                           Fn1<? super Elided<Head, Tail>, ? extends R> bFn,
                           Fn1<? super Exhausted<Head, Tail>, ? extends R> cFn) {
            return bFn.apply(this);
        }

        @Override
        public String toString() {
            return "Elided{" +
                    "tail=" + tail +
                    '}';
        }
    }

    public static final class Exhausted<Head, Tail> extends Step<Head, Tail> {
        private static final Exhausted<?, ?> INSTANCE = new Exhausted<>();

        private Exhausted() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public <NewTail> Exhausted<Head, NewTail> fmap(Fn1<? super Tail, ? extends NewTail> fn) {
            return (Exhausted<Head, NewTail>) this;
        }

        @Override
        public <R> R match(Fn1<? super Emitted<Head, Tail>, ? extends R> aFn,
                           Fn1<? super Elided<Head, Tail>, ? extends R> bFn,
                           Fn1<? super Exhausted<Head, Tail>, ? extends R> cFn) {
            return cFn.apply(this);
        }

        @Override
        public String toString() {
            return "Exhausted{}";
        }
    }
}
