package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.GForEachM.gForEachM;

public final class AwaitAllM<M extends MonadRec<?, M>, MU extends MonadRec<Unit, M>>
        implements Fn1<StreamT<M, ?>, MU> {

    private static final AwaitAllM<?, ?> INSTANCE = new AwaitAllM<>();

    private AwaitAllM() {
    }

    @Override
    public MU checkedApply(StreamT<M, ?> streamT) throws Throwable {
        Pure<M> pureM = Pure.of(streamT.pure(UNIT)
                                        .<MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M>>runStreamT());
        return gForEachM(awaitM(), constantly(pureM.apply(UNIT)), streamT);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, MU extends MonadRec<Unit, M>> AwaitAllM<M, MU> awaitAllM() {
        return (AwaitAllM<M, MU>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, MU extends MonadRec<Unit, M>> MU awaitAllM(StreamT<M, ?> streamT) {
        return AwaitAllM.<M, MU>awaitAllM().apply(streamT);
    }
}
