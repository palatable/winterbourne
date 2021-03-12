package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldCutM.gFoldCutM;

public final class FoldCutM<M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> implements
        Fn3<Fn2<? super B, ? super Maybe<A>, ? extends MonadRec<RecursiveResult<B, B>, M>>, MB, StreamT<M, A>, MB> {

    private static final FoldCutM<?, ?, ?, ?> INSTANCE = new FoldCutM<>();

    private FoldCutM() {
    }

    @Override
    public MB checkedApply(Fn2<? super B, ? super Maybe<A>, ? extends MonadRec<RecursiveResult<B, B>, M>> fold,
                           MB acc,
                           StreamT<M, A> streamT) throws Throwable {
        return gFoldCutM(StreamT::<MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>runStreamT, fold, acc, streamT);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> FoldCutM<M, A, B, MB> foldCutM() {
        return (FoldCutM<M, A, B, MB>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> Fn2<MB, StreamT<M, A>, MB> foldCutM(
            Fn2<? super B, ? super Maybe<A>, ? extends MonadRec<RecursiveResult<B, B>, M>> fold) {
        return FoldCutM.<M, A, B, MB>foldCutM().apply(fold);
    }

    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> Fn1<StreamT<M, A>, MB> foldCutM(
            Fn2<? super B, ? super Maybe<A>, ? extends MonadRec<RecursiveResult<B, B>, M>> fold,
            MB acc) {
        return FoldCutM.<M, A, B, MB>foldCutM(fold).apply(acc);
    }

    public static <M extends MonadRec<?, M>, A, B, MB extends MonadRec<B, M>> MB foldCutM(
            Fn2<? super B, ? super Maybe<A>, ? extends MonadRec<RecursiveResult<B, B>, M>> fold,
            MB acc,
            StreamT<M, A> streamT) {
        return FoldCutM.foldCutM(fold, acc).apply(streamT);
    }
}
