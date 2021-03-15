package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.UnfoldM.unfoldM;

/**
 * Given a {@link Pure}, produce a <code>{@link StreamT}&lt;M, Natural&gt;</code> starting with {@link Natural#zero()}
 * and increasing by {@link Natural#one()} on each emission.
 *
 * @param <M> the effect type
 */
public final class NaturalsM<M extends MonadRec<?, M>> implements Fn1<Pure<M>, StreamT<M, Natural>> {

    private static final NaturalsM<?> INSTANCE = new NaturalsM<>();

    private NaturalsM() {
    }

    @Override
    public StreamT<M, Natural> checkedApply(Pure<M> pureM) {
        return unfoldM(n -> pureM.apply(just(tuple(just(n), n.inc()))),
                       pureM.<Natural, MonadRec<Natural, M>>apply(zero()));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>> NaturalsM<M> naturalsM() {
        return (NaturalsM<M>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>> StreamT<M, Natural> naturalsM(Pure<M> pureM) {
        return $(naturalsM(), pureM);
    }
}
