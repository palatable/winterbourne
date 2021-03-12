package com.jnape.palatable.winterbourne.functions.builtin.fn4;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.Fn4;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;

public final class GFoldCutM<M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> implements
        Fn4<
                Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>>,
                Fn2<? super B, ? super X, ? extends MonadRec<RecursiveResult<B, B>, M>>,
                MB,
                StreamT<M, A>,
                MB> {

    private static final GFoldCutM<?, ?, ?, ?, ?> INSTANCE = new GFoldCutM<>();

    private GFoldCutM() {
    }

    @Override
    public MB checkedApply(Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
                           Fn2<? super B, ? super X, ? extends MonadRec<RecursiveResult<B, B>, M>> fold,
                           MB acc,
                           StreamT<M, A> streamT) {
        return acc.fmap(tupler(streamT))
                .trampolineM(into((as, b) -> maybeT(advance.apply(as))
                        .flatMap(into((a, aas) -> maybeT(fold.apply(b, a).fmap(Maybe::just)).fmap(tupler(aas))))
                        .runMaybeT()
                        .fmap(maybeR -> maybeR.match(
                                __ -> terminate(b),
                                into((rest, rr) -> rr.biMapL(tupler(rest)))))))
                .coerce();
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> GFoldCutM<M, A, B, X, MB> gFoldCutM() {
        return (GFoldCutM<M, A, B, X, MB>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>>
    Fn3<Fn2<? super B, ? super X, ? extends MonadRec<RecursiveResult<B, B>, M>>, MB, StreamT<M, A>, MB> gFoldCutM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance) {
        return GFoldCutM.<M, A, B, X, MB>gFoldCutM().apply(advance);
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> Fn2<MB, StreamT<M, A>, MB> gFoldCutM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn2<? super B, ? super X, ? extends MonadRec<RecursiveResult<B, B>, M>> fold) {
        return GFoldCutM.<M, A, B, X, MB>gFoldCutM(advance).apply(fold);
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> Fn1<StreamT<M, A>, MB> gFoldCutM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn2<? super B, ? super X, ? extends MonadRec<RecursiveResult<B, B>, M>> fold,
            MB acc) {
        return GFoldCutM.<M, A, B, X, MB>gFoldCutM(advance, fold).apply(acc);
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> MB gFoldCutM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn2<? super B, ? super X, ? extends MonadRec<RecursiveResult<B, B>, M>> fold,
            MB acc,
            StreamT<M, A> streamT) {
        return gFoldCutM(advance, fold, acc).apply(streamT);
    }
}


