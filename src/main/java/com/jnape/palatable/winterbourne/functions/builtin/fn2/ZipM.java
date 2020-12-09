package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import com.jnape.palatable.winterbourne.functions.builtin.fn3.ZipWithM;

import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.ZipWithM.zipWithM;

/**
 * Zip together two <code>IterateT</code>s into a single <code>IterateT</code> of <code>Tuple2&lt;A, B&gt;</code>. If
 * the input <code>IterateT</code>s differ in size, the resulting <code>IterateT</code> contains only as many pairs as
 * the smallest input <code>IterateT</code>'s elements.
 *
 * @param <M> the {@link IterateT} effect type
 * @param <A> The first input {@link IterateT} element type, and the type of the first tuple slot in the output
 *            {@link IterateT}
 * @param <B> The second input {@link IterateT} element type, and the type of the second tuple slot in the output
 *            {@link IterateT}
 * @see ZipWithM
 */
public final class ZipM<M extends MonadRec<?, M>, A, B> implements Fn2<IterateT<M, A>, IterateT<M, B>, IterateT<M, Tuple2<A, B>>> {

    private static final ZipM<?, ?, ?> INSTANCE = new ZipM<>();

    @Override
    public IterateT<M, Tuple2<A, B>> checkedApply(IterateT<M, A> as, IterateT<M, B> bs) throws Throwable {
        return zipWithM(tupler(), as, bs);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B> ZipM<M, A, B> zipM() {
        return (ZipM<M, A, B>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B> Fn1<IterateT<M, B>, IterateT<M, Tuple2<A, B>>> zipM(
            IterateT<M, A> as) {
        return ZipM.<M, A, B>zipM().apply(as);
    }

    public static <M extends MonadRec<?, M>, A, B> IterateT<M, Tuple2<A, B>> zipM(IterateT<M, A> as,
                                                                                  IterateT<M, B> bs) {
        return ZipM.<M, A, B>zipM(as).apply(bs);
    }
}
