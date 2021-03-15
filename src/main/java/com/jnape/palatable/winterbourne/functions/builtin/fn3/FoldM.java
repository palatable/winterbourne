package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldM.gFoldM;

public final class FoldM<M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> implements
        Fn3<Fn2<? super B, ? super Maybe<A>, ? extends MB>, MB, StreamT<M, A>, MB> {

    private static final FoldM<?, ?, ?, ?> INSTANCE = new FoldM<>();

    private FoldM() {
    }

    @Override
    public MB checkedApply(Fn2<? super B, ? super Maybe<A>, ? extends MB> fold,
                           MB acc,
                           StreamT<M, A> streamT) throws Throwable {
        return gFoldM(StreamT::<MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>runStreamT, fold, acc, streamT);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> FoldM<M, A, B, MB> foldM() {
        return (FoldM<M, A, B, MB>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> Fn2<MB, StreamT<M, A>, MB> foldM(
            Fn2<? super B, ? super Maybe<A>, ? extends MB> fold) {
        return FoldM.<M, A, B, MB>foldM().apply(fold);
    }

    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> Fn1<StreamT<M, A>, MB> foldM(
            Fn2<? super B, ? super Maybe<A>, ? extends MB> fold,
            MB acc) {
        return FoldM.<M, A, B, MB>foldM(fold).apply(acc);
    }

    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> MB foldM(
            Fn2<? super B, ? super Maybe<A>, ? extends MB> fold,
            MB acc,
            StreamT<M, A> streamT) {
        return FoldM.foldM(fold, acc).apply(streamT);
    }
}
