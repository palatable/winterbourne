package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;
import com.jnape.palatable.winterbourne.functions.builtin.fn3.ZipWithM;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.ZipWithM.zipWithM;

/**
 * Zip together two {@link StreamT}s into a single {@link StreamT} of <code>{@link Tuple2}&lt;A, B&gt;</code>. If
 * the input {@link StreamT}s differ in size, the resulting {@link StreamT} contains only as many pairs as
 * the smallest input {@link StreamT} emits.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The first input {@link StreamT} element type, and the type of the first tuple slot in the output
 *            {@link StreamT}
 * @param <B> The second input {@link StreamT} element type, and the type of the second tuple slot in the output
 *            {@link StreamT}
 * @see ZipWithM
 */
public final class ZipM<M extends MonadRec<?, M>, A, B>
        implements Fn2<StreamT<M, A>, StreamT<M, B>, StreamT<M, Tuple2<A, B>>> {

    private static final ZipM<?, ?, ?> INSTANCE = new ZipM<>();

    private ZipM() {
    }

    @Override
    public StreamT<M, Tuple2<A, B>> checkedApply(StreamT<M, A> as, StreamT<M, B> bs) {
        return zipWithM(tupler(), as, bs);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B> ZipM<M, A, B> zipM() {
        return (ZipM<M, A, B>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B> Fn1<StreamT<M, B>, StreamT<M, Tuple2<A, B>>> zipM(
            StreamT<M, A> as) {
        return $(zipM(), as);
    }

    public static <M extends MonadRec<?, M>, A, B> StreamT<M, Tuple2<A, B>> zipM(
            StreamT<M, A> as, StreamT<M, B> bs) {
        return $(zipM(as), bs);
    }
}
