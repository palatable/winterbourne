package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import java.util.Map.Entry;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn2.curried;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Applies a {@link Fn2} pairwise to the emitted elements of two {@link StreamT}s and emits the results until one of the
 * two source {@link StreamT}s terminates.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The first input {@link StreamT} element type
 * @param <B> The second input {@link StreamT} element type
 * @param <C> The output {@link StreamT} element type
 */
public final class ZipWithM<M extends MonadRec<?, M>, A, B, C>
        implements Fn3<Fn2<? super A, ? super B, ? extends C>, StreamT<M, A>, StreamT<M, B>, StreamT<M, C>> {

    private static final ZipWithM<?, ?, ?, ?> INSTANCE = new ZipWithM<>();

    private ZipWithM() {
    }

    @Override
    public StreamT<M, C> checkedApply(Fn2<? super A, ? super B, ? extends C> fn, StreamT<M, A> as, StreamT<M, B> bs) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();

        Fn2<Entry<Maybe<A>, StreamT<M, A>>, Entry<Maybe<B>, StreamT<M, B>>, Tuple2<Maybe<C>, StreamT<M, C>>> zipper =
                curried(into((maybeA, as_) -> into((maybeB, bs_) -> maybeA.match(
                        __ -> tuple(Maybe.<C>nothing(), tuple(as_, bs_.cons(mUnit.pure(maybeB)))),
                        a -> maybeB.<Tuple2<Maybe<C>, Tuple2<StreamT<M, A>, StreamT<M, B>>>>match(
                                __ -> tuple(nothing(), tuple(as_.cons(mUnit.pure(just(a))), bs_)),
                                b -> tuple(just(fn.apply(a, b)), tuple(as_, bs_))))
                        .fmap(into(zipWithM(fn))))));

        return streamT(() -> maybeT(as.runStreamT()).zip(maybeT(bs.runStreamT()).fmap(zipper.flip())).runMaybeT(),
                       Pure.of(mUnit));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B, C> ZipWithM<M, A, B, C> zipWithM() {
        return (ZipWithM<M, A, B, C>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B, C> Fn2<StreamT<M, A>, StreamT<M, B>, StreamT<M, C>> zipWithM(
            Fn2<? super A, ? super B, ? extends C> fn) {
        return ZipWithM.<M, A, B, C>zipWithM().apply(fn);
    }

    public static <M extends MonadRec<?, M>, A, B, C> Fn1<StreamT<M, B>, StreamT<M, C>> zipWithM(
            Fn2<? super A, ? super B, ? extends C> fn, StreamT<M, A> as) {
        return $(zipWithM(fn), as);
    }

    public static <M extends MonadRec<?, M>, A, B, C> StreamT<M, C> zipWithM(
            Fn2<? super A, ? super B, ? extends C> fn, StreamT<M, A> as, StreamT<M, B> bs) {
        return $(zipWithM(fn, as), bs);
    }
}
