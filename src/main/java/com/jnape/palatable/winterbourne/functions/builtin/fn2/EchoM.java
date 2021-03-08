package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ReplicateM.replicateM;

/**
 * Repeat each element in an <code>{@link StreamT}</code> <code>n</code> times.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The {@link StreamT} element type
 */
public final class EchoM<M extends MonadRec<?, M>, A> implements Fn2<NonZero, StreamT<M, A>, StreamT<M, A>> {

    private static final EchoM<?, ?> INSTANCE = new EchoM<>();

    private EchoM() {
    }

    @Override
    public StreamT<M, A> checkedApply(NonZero n, StreamT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return as.flatMap(a -> replicateM(n, mUnit.pure(a)));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> EchoM<M, A> echoM() {
        return (EchoM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>> echoM(NonZero n) {
        return $(echoM(), n);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> echoM(NonZero n, StreamT<M, A> as) {
        return $(echoM(n), as);
    }
}