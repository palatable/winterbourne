package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.monad.Monad.join;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;

/**
 * Given a <code>{@link StreamT}&lt;M, A&gt;</code>, return an infinite <code>{@link StreamT}&lt;M, A&gt;</code> that
 * repeatedly cycles its elements, rerunning the effects from the beginning each time the input {@link StreamT} ends.
 *
 * @param <M> The {@link StreamT} effect type
 * @param <A> The {@link StreamT} element type
 */
public final class CycleM<M extends MonadRec<?, M>, A> implements Fn1<StreamT<M, A>, StreamT<M, A>> {

    private static final CycleM<?, ?> INSTANCE = new CycleM<>();

    private CycleM() {
    }

    @Override
    public StreamT<M, A> checkedApply(StreamT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return join(repeatM(mUnit.pure(as)));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> CycleM<M, A> cycleM() {
        return (CycleM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> cycleM(StreamT<M, A> as) {
        return $(cycleM(), as);
    }
}

