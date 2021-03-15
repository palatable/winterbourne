package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.winterbourne.functions.builtin.fn3.GForEachM.gForEachM;

public final class ForEachM<M extends MonadRec<?, M>, A, MU extends MonadRec<Unit, M>> implements
        Fn2<Fn1<? super Maybe<A>, ? extends MU>, StreamT<M, A>, MU> {

    private static final ForEachM<?, ?, ?> INSTANCE = new ForEachM<>();

    private ForEachM() {
    }

    @Override
    public MU checkedApply(Fn1<? super Maybe<A>, ? extends MU> forEach,
                           StreamT<M, A> streamT) throws Throwable {
        return gForEachM(StreamT::<MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>runStreamT, forEach, streamT);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MU extends MonadRec<Unit, M>> ForEachM<M, A, MU> forEachM() {
        return (ForEachM<M, A, MU>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MU extends MonadRec<Unit, M>> Fn1<StreamT<M, A>, MU> forEachM(
            Fn1<? super Maybe<A>, ? extends MU> forEach) {
        return ForEachM.<M, A, MU>forEachM().apply(forEach);
    }

    public static <M extends MonadRec<?, M>, A, MU extends MonadRec<Unit, M>> MU forEachM(
            Fn1<? super Maybe<A>, ? extends MU> forEach,
            StreamT<M, A> streamT) {
        return ForEachM.<M, A, MU>forEachM(forEach).apply(streamT);
    }
}
