package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.monad.Monad.join;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.iterateT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;

/**
 * Given an {@link IterateT}, return an infinite {@link IterateT} that repeatedly cycles its elements, in order.
 *
 * @param <A> The {@link IterateT} element type
 * @param <M> The {@link IterateT} effect type
 */
public final class CycleM<M extends MonadRec<?, M>, A> implements Fn1<IterateT<M, A>, IterateT<M, A>> {

    private static final CycleM<?, ?> INSTANCE = new CycleM<>();

    private CycleM() {
    }

    @Override
    public IterateT<M, A> checkedApply(IterateT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<A, IterateT<M, A>>>, M> headM = as.runIterateT();
        return join(repeatM(headM.pure(iterateT(headM))));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> CycleM<M, A> cycleM() {
        return (CycleM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> IterateT<M, A> cycleM(IterateT<M, A> as) {
        return CycleM.<M, A>cycleM().apply(as);
    }
}
