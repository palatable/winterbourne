package com.jnape.palatable.winterbourne.functions.builtin.fn4;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.Fn4;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Maps each element of a <code>{@link StreamT}&lt;A&gt;</code> into type <code>B</code> while simultaneously folding
 * each <code>A</code> into an accumulator <code>Acc</code>. As the supplied <code>StreamT</code> ends, a termination
 * function produces a <code>MonadRec&lt;Unit, M&gt;</code> from the folded accumulator which will run as part of the
 * end effect of the resulting {@link StreamT}.
 *
 * @param <M>   the {@link StreamT} effect type
 * @param <A>   the {@link StreamT} element type
 * @param <B>   the return {@link StreamT} element type
 * @param <Acc> the accumulator type
 */
public final class FoldMapM<M extends MonadRec<?, M>, A, B, Acc> implements Fn4<
        Fn1<Acc, MonadRec<Unit, M>>, Fn2<A, Acc, MonadRec<Tuple2<B, Acc>, M>>,
        MonadRec<Acc, M>, StreamT<M, A>, StreamT<M, B>> {

    private static final FoldMapM<?, ?, ?, ?> INSTANCE = new FoldMapM<>();

    private FoldMapM() {
    }

    @Override
    public StreamT<M, B> checkedApply(
            Fn1<Acc, MonadRec<Unit, M>> terminateFn, Fn2<A, Acc, MonadRec<Tuple2<B, Acc>, M>> foldMap,
            MonadRec<Acc, M> accM, StreamT<M, A> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return streamT(
                () -> as.runStreamT().flatMap(m -> m.match(
                        __ -> accM.flatMap(terminateFn).fmap(constantly(nothing())),
                        t -> t.fmap(foldMapM(terminateFn, foldMap).flip())
                              .into((mHead, tailFn) -> mHead.match(
                                      __ -> mUnit.pure(just(tuple(nothing(), $(tailFn, accM)))),
                                      a -> accM.flatMap($(foldMap, a)).fmap(accT -> just(accT.biMap(
                                              Maybe::just, tailFn.diMapL(mUnit::pure)))))))),
                Pure.of(accM));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B, Acc> FoldMapM<M, A, B, Acc> foldMapM() {
        return (FoldMapM<M, A, B, Acc>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B, Acc>
    Fn3<Fn2<A, Acc, MonadRec<Tuple2<B, Acc>, M>>, MonadRec<Acc, M>, StreamT<M, A>, StreamT<M, B>> foldMapM(
            Fn1<Acc, MonadRec<Unit, M>> terminateFn) {
        return FoldMapM.<M, A, B, Acc>foldMapM().apply(terminateFn);
    }

    public static <M extends MonadRec<?, M>, A, B, Acc> Fn2<MonadRec<Acc, M>, StreamT<M, A>, StreamT<M, B>> foldMapM(
            Fn1<Acc, MonadRec<Unit, M>> terminateFn, Fn2<A, Acc, MonadRec<Tuple2<B, Acc>, M>> foldMap) {
        return FoldMapM.<M, A, B, Acc>foldMapM(terminateFn).apply(foldMap);
    }

    public static <M extends MonadRec<?, M>, A, B, Acc> Fn1<StreamT<M, A>, StreamT<M, B>> foldMapM(
            Fn1<Acc, MonadRec<Unit, M>> terminateFn, Fn2<A, Acc, MonadRec<Tuple2<B, Acc>, M>> foldMap,
            MonadRec<Acc, M> acc) {
        return $(foldMapM(terminateFn, foldMap), acc);
    }

    public static <M extends MonadRec<?, M>, A, B, Acc> StreamT<M, B> foldMapM(
            Fn1<Acc, MonadRec<Unit, M>> terminateFn, Fn2<A, Acc, MonadRec<Tuple2<B, Acc>, M>> foldMap,
            MonadRec<Acc, M> acc, StreamT<M, A> as) {
        return $(foldMapM(terminateFn, foldMap, acc), as);
    }
}
