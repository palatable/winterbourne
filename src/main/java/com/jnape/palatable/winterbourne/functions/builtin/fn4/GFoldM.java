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

import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldCutM.gFoldCutM;

public final class GFoldM<M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> implements
        Fn4<
                Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>>,
                Fn2<? super B, ? super X, ? extends MB>,
                MB,
                StreamT<M, A>,
                MB> {

    private static final GFoldM<?, ?, ?, ?, ?> INSTANCE = new GFoldM<>();

    private GFoldM() {
    }

    @Override
    public MB checkedApply(Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
                           Fn2<? super B, ? super X, ? extends MB> fold,
                           MB acc,
                           StreamT<M, A> streamT) {
        return gFoldCutM(advance, (b, x) -> fold.apply(b, x).fmap(RecursiveResult::recurse), acc, streamT);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> GFoldM<M, A, B, X, MB> gFoldM() {
        return (GFoldM<M, A, B, X, MB>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>>
    Fn3<Fn2<? super B, ? super X, ? extends MB>, MB, StreamT<M, A>, MB> gFoldM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance) {
        return GFoldM.<M, A, B, X, MB>gFoldM().apply(advance);
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> Fn2<MB, StreamT<M, A>, MB> gFoldM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn2<? super B, ? super X, ? extends MB> foldCut) {
        return GFoldM.<M, A, B, X, MB>gFoldM(advance).apply(foldCut);
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> Fn1<StreamT<M, A>, MB> gFoldM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn2<? super B, ? super X, ? extends MB> foldCut,
            MB acc) {
        return GFoldM.<M, A, B, X, MB>gFoldM(advance, foldCut).apply(acc);
    }

    public static <M extends MonadRec<?, M>, A, B, X, MB extends MonadRec<B, M>> MB gFoldM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn2<? super B, ? super X, ? extends MB> foldCut,
            MB acc,
            StreamT<M, A> streamT) {
        return gFoldM(advance, foldCut, acc).apply(streamT);
    }
}


