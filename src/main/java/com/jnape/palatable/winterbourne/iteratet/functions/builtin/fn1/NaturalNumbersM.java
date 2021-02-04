package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;

/**
 * Given a {@link Pure}, produce an {@link IterateT} of the positive natural numbers.
 *
 * @param <M> the effect type
 */
public final class NaturalNumbersM<M extends MonadRec<?, M>> implements Fn1<Pure<M>, IterateT<M, Integer>> {

    private static final NaturalNumbersM<?> INSTANCE = new NaturalNumbersM<>();

    private NaturalNumbersM() {
    }

    @Override
    public IterateT<M, Integer> checkedApply(Pure<M> pureM) {
        return IterateT.<M, Integer, Integer>unfold(i -> pureM.apply(just(tuple(i, i + 1))), pureM.apply(1));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>> NaturalNumbersM<M> naturalNumbersM() {
        return (NaturalNumbersM<M>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>> IterateT<M, Integer> naturalNumbersM(Pure<M> pureM) {
        return NaturalNumbersM.<M>naturalNumbersM().apply(pureM);
    }
}
