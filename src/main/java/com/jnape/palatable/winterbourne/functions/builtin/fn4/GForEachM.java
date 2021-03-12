package com.jnape.palatable.winterbourne.functions.builtin.fn4;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldCutM.gFoldCutM;

public final class GForEachM<M extends MonadRec<?, M>, A, X, MU extends MonadRec<Unit, M>> implements
        Fn3<
                Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>>,
                Fn1<? super X, ? extends MonadRec<Unit, M>>,
                StreamT<M, A>,
                MU> {

    private static final GForEachM<?, ?, ?, ?> INSTANCE = new GForEachM<>();

    private GForEachM() {
    }

    @Override
    public MU checkedApply(Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
                           Fn1<? super X, ? extends MonadRec<Unit, M>> forEach,
                           StreamT<M, A> streamT) throws Throwable {
        Pure<M> pureM = Pure.of(streamT.pure(UNIT)
                                        .<MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M>>runStreamT());
        return gFoldCutM(advance,
                         (__, x) -> forEach.apply(x).fmap(RecursiveResult::recurse),
                         pureM.apply(UNIT),
                         streamT);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, X, MU extends MonadRec<Unit, M>> GForEachM<M, A, X, MU> gForEachM() {
        return (GForEachM<M, A, X, MU>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, X, MU extends MonadRec<Unit, M>>
    Fn2<Fn1<? super X, ? extends MonadRec<Unit, M>>, StreamT<M, A>, MU> gForEachM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance) {
        return GForEachM.<M, A, X, MU>gForEachM().apply(advance);
    }

    public static <M extends MonadRec<?, M>, A, X, MU extends MonadRec<Unit, M>> Fn1<StreamT<M, A>, MU> gForEachM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn1<? super X, ? extends MonadRec<Unit, M>> forEach) {
        return GForEachM.<M, A, X, MU>gForEachM(advance).apply(forEach);
    }

    public static <M extends MonadRec<?, M>, A, X, MU extends MonadRec<Unit, M>> MU gForEachM(
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance,
            Fn1<? super X, ? extends MonadRec<Unit, M>> forEach,
            StreamT<M, A> streamT) {
        return GForEachM.<M, A, X, MU>gForEachM(advance, forEach).apply(streamT);
    }
}
