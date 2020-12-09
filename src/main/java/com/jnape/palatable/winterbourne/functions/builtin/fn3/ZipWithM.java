package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.HList;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import com.jnape.palatable.winterbourne.functions.builtin.fn2.ZipM;

import static com.jnape.palatable.lambda.functions.Fn1.fn1;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.suspended;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;

/**
 * Zip together two <code>IterateT</code>s by applying a zipping function to the successive elements of each
 * <code>IterateT</code> until one of them runs out of elements. Returns an <code>IterateT</code> containing the
 * results.
 *
 * @param <M> the {@link IterateT} effect type
 * @param <A> The first input {@link IterateT} element type
 * @param <B> The second input {@link IterateT} element type
 * @param <C> The output {@link IterateT} element type
 * @see ZipM
 */
public final class ZipWithM<M extends MonadRec<?, M>, A, B, C>
        implements Fn3<Fn2<? super A, ? super B, ? extends C>, IterateT<M, A>, IterateT<M, B>, IterateT<M, C>> {

    private static final ZipWithM<?, ?, ?, ?> INSTANCE = new ZipWithM<>();

    @Override
    @SuppressWarnings("RedundantTypeArguments")
    public IterateT<M, C> checkedApply(Fn2<? super A, ? super B, ? extends C> zipFn, IterateT<M, A> as,
                                       IterateT<M, B> bs) {
        MonadRec<Maybe<Tuple2<B, IterateT<M, B>>>, M> unwrappedB = bs.runIterateT();
        return suspended(() -> maybeT(unwrappedB)
                                 .zip(maybeT(as.runIterateT())
                                              .fmap(ta -> fn1(tb -> HList.<C, IterateT<M, C>>tuple(
                                                      zipFn.apply(ta._1(), tb._1()),
                                                      ZipWithM.<M, A, B, C>zipWithM(zipFn, ta._2(), tb._2())))))
                                 .runMaybeT(),
                         Pure.of(unwrappedB));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B, C> ZipWithM<M, A, B, C> zipWithM() {
        return (ZipWithM<M, A, B, C>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B, C> Fn2<IterateT<M, A>, IterateT<M, B>, IterateT<M, C>> zipWithM(
            Fn2<? super A, ? super B, ? extends C> zipFn) {
        return ZipWithM.<M, A, B, C>zipWithM().apply(zipFn);
    }

    public static <M extends MonadRec<?, M>, A, B, C> Fn1<IterateT<M, B>, IterateT<M, C>> zipWithM(
            Fn2<? super A, ? super B, ? extends C> zipFn, IterateT<M, A> as) {
        return ZipWithM.<M, A, B, C>zipWithM(zipFn).apply(as);
    }

    public static <M extends MonadRec<?, M>, A, B, C> IterateT<M, C> zipWithM(
            Fn2<? super A, ? super B, ? extends C> zipFn, IterateT<M, A> as, IterateT<M, B> bs) {
        return ZipWithM.<M, A, B, C>zipWithM(zipFn, as).apply(bs);
    }
}
