package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.builtin.fn1.Magnetize;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Eq.eq;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.MagnetizeByM.magnetizeByM;

/**
 * {@link Magnetize} a <code>{@link StreamT}&lt;M, A&gt;</code> using value equality as the magnetizing function.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> the {@link StrictQueue} element type
 */
public final class MagnetizeM<M extends MonadRec<?, M>, A> implements Fn1<StreamT<M, A>, StreamT<M, StrictQueue<A>>> {

    private static final MagnetizeM<?, ?> INSTANCE = new MagnetizeM<>();

    private MagnetizeM() {
    }

    @Override
    public StreamT<M, StrictQueue<A>> checkedApply(StreamT<M, A> mas) throws Throwable {
        return magnetizeByM(eq(), mas);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> MagnetizeM<M, A> magnetizeM() {
        return (MagnetizeM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, StrictQueue<A>> magnetizeM(StreamT<M, A> as) {
        return $(magnetizeM(), as);
    }
}