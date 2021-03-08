package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;

/**
 * Returns the a {@link StreamT} of all the elisions and emissions of the given {@link StreamT} except for the first
 * emitted element, which is skipped. If the input {@link StreamT} is empty or a singleton, the result is an empty
 * {@link StreamT}
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> the {@link StreamT} element type
 */
public final class TailM<M extends MonadRec<?, M>, A> implements Fn1<StreamT<M, A>, StreamT<M, A>> {

    private static final TailM<?, ?> INSTANCE = new TailM<>();

    private TailM() {
    }

    @Override
    public StreamT<M, A> checkedApply(StreamT<M, A> as) {
        return dropM(one(), as);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> TailM<M, A> tailM() {
        return (TailM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> tailM(StreamT<M, A> as) {
        return $(tailM(), as);
    }
}
