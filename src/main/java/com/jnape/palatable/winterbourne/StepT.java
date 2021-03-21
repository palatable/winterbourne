package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.adt.coproduct.CoProduct3;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.monad.MonadRec;

public abstract class StepT<M extends MonadRec<?, M>, A>
        implements CoProduct3<StepT.Emitted<M, A>, StepT.Elided<M, A>, StepT.Exhausted<M, A>, StepT<M, A>> {

    private StepT() {
    }

    public static <M extends MonadRec<?, M>, A> Emitted<M, A> emission(A value, StreamT<M, A> rest) {
        return new Emitted<>(value, rest);
    }

    public static <M extends MonadRec<?, M>, A> Elided<M, A> elision(StreamT<M, A> rest) {
        return new Elided<>(rest);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> Exhausted<M, A> exhausted() {
        return (Exhausted<M, A>) Exhausted.INSTANCE;
    }

    public static final class Emitted<M extends MonadRec<?, M>, A> extends StepT<M, A> {

        private final A             value;
        private final StreamT<M, A> rest;

        private Emitted(A value, StreamT<M, A> rest) {
            this.value = value;
            this.rest  = rest;
        }

        public A value() {
            return value;
        }

        public StreamT<M, A> rest() {
            return rest;
        }

        @Override
        public <R> R match(Fn1<? super Emitted<M, A>, ? extends R> aFn, Fn1<? super Elided<M, A>, ? extends R> bFn,
                           Fn1<? super Exhausted<M, A>, ? extends R> cFn) {
            return aFn.apply(this);
        }
    }

    public static final class Elided<M extends MonadRec<?, M>, A> extends StepT<M, A> {
        private final StreamT<M, A> rest;

        private Elided(StreamT<M, A> rest) {
            this.rest = rest;
        }

        public StreamT<M, A> rest() {
            return rest;
        }

        @Override
        public <R> R match(Fn1<? super Emitted<M, A>, ? extends R> aFn, Fn1<? super Elided<M, A>, ? extends R> bFn,
                           Fn1<? super Exhausted<M, A>, ? extends R> cFn) {
            return bFn.apply(this);
        }
    }

    public static final class Exhausted<M extends MonadRec<?, M>, A> extends StepT<M, A> {
        private static final Exhausted<?, ?> INSTANCE = new Exhausted<>();

        private Exhausted() {
        }

        @Override
        public <R> R match(Fn1<? super Emitted<M, A>, ? extends R> aFn, Fn1<? super Elided<M, A>, ? extends R> bFn,
                           Fn1<? super Exhausted<M, A>, ? extends R> cFn) {
            return cFn.apply(this);
        }
    }
}
