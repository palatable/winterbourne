package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.adt.coproduct.CoProduct2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.monad.MonadRec;

public abstract class StepT<M extends MonadRec<?, M>, A>
        implements CoProduct2<StepT.Emission<M, A>, StepT.Elision<M, A>, StepT<M, A>> {

    private StepT() {
    }

    public static <M extends MonadRec<?, M>, A> Emission<M, A> emission(A value, StreamT<M, A> rest) {
        return new Emission<>(value, rest);
    }

    public static <M extends MonadRec<?, M>, A> Elision<M, A> elision(StreamT<M, A> rest) {
        return new Elision<>(rest);
    }

    public static final class Emission<M extends MonadRec<?, M>, A> extends StepT<M, A> {

        private final A             value;
        private final StreamT<M, A> rest;

        private Emission(A value, StreamT<M, A> rest) {
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
        public <R> R match(Fn1<? super Emission<M, A>, ? extends R> aFn, Fn1<? super Elision<M, A>, ? extends R> bFn) {
            return aFn.apply(this);
        }
    }

    public static final class Elision<M extends MonadRec<?, M>, A> extends StepT<M, A> {
        private final StreamT<M, A> rest;

        private Elision(StreamT<M, A> rest) {
            this.rest = rest;
        }

        public StreamT<M, A> rest() {
            return rest;
        }

        @Override
        public <R> R match(Fn1<? super Emission<M, A>, ? extends R> aFn, Fn1<? super Elision<M, A>, ? extends R> bFn) {
            return bFn.apply(this);
        }
    }
}
