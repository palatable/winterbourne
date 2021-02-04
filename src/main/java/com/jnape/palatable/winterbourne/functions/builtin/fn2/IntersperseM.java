package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.TailM.tailM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.PrependAllM.prependAllM;


/**
 * Produces a {@link StreamT} which emits the given value <code>a</code> between the two members of each pair of
 * successively emitted elements from a given {@link StreamT}. No additional emissions occur when the given
 * {@link StreamT} is empty or a singleton.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> the {@link StreamT} element type
 * @see PrependAllM
 */
public final class IntersperseM<M extends MonadRec<?, M>, A> implements Fn2<A, StreamT<M, A>, StreamT<M, A>> {

    private static final IntersperseM<?, ?> INSTANCE = new IntersperseM<>();

    private IntersperseM() {
    }

    @Override
    public StreamT<M, A> checkedApply(A a, StreamT<M, A> as) throws Throwable {
        return tailM(prependAllM(a, as));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> IntersperseM<M, A> intersperseM() {
        return (IntersperseM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>> intersperseM(A a) {
        return $(intersperseM(), a);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> intersperseM(A a, StreamT<M, A> as) {
        return $(intersperseM(a), as);
    }
}
