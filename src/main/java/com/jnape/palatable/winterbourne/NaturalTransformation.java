package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.functions.specialized.Lift;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.functor.Functor;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.MonadT;

public interface NaturalTransformation<F extends Functor<?, F>, G extends Functor<?, G>> {
    <A, GA extends Functor<A, G>> GA apply(Functor<A, F> fa);

    default <H extends Functor<?, H>> NaturalTransformation<F, H> andThen(NaturalTransformation<G, H> gToH) {
        return new NaturalTransformation<>() {
            @Override
            public <A, GA extends Functor<A, H>> GA apply(Functor<A, F> fa) {
                return gToH.apply(NaturalTransformation.this.apply(fa));
            }
        };
    }

    default Pure<G> mapPure(Pure<F> pureF) {
        return new Pure<>() {
            @Override
            public <A> Functor<A, ? extends G> checkedApply(A a) {
                return NaturalTransformation.this.apply(pureF.apply(a));
            }
        };
    }

    default <E extends Functor<?, E>> NaturalTransformation<E, G> compose(NaturalTransformation<E, F> eToF) {
        return eToF.andThen(this);
    }

    static <F extends Functor<?, F>> NaturalTransformation<F, F> identity() {
        return new NaturalTransformation<>() {
            @Override
            public <A, GA extends Functor<A, F>> GA apply(Functor<A, F> fa) {
                return fa.coerce();
            }
        };
    }

    static <F extends MonadRec<?, F>, G extends MonadT<F, ?, G, T>, T extends MonadT<?, ?, ?, T>> NaturalTransformation<F, G> fromLift(
            Lift<T> lift) {
        return new NaturalTransformation<>() {
            @Override
            public <A, GA extends Functor<A, G>> GA apply(Functor<A, F> fa) {
                return lift.<A, F, MonadT<F, A, G, T>>apply(fa.coerce()).coerce();
            }
        };
    }
}
