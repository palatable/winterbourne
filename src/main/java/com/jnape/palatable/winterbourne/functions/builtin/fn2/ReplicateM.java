package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;

/**
 * Produce a {@link StreamT} of a value wrapped in a monadic effect <code>n</code> times.
 *
 * @param <A> the output {@link StreamT} element type
 * @param <M> the output {@link StreamT} element type
 */
public final class ReplicateM<M extends MonadRec<?, M>, A> implements Fn2<NonZero, MonadRec<A, M>, StreamT<M, A>> {

    private static final ReplicateM<?, ?> INSTANCE = new ReplicateM<>();

    private ReplicateM() {
    }

    @Override
    public StreamT<M, A> checkedApply(NonZero n, MonadRec<A, M> ma) throws Throwable {
        return takeM(n, repeatM(ma));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> ReplicateM<M, A> replicateM() {
        return (ReplicateM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<MonadRec<A, M>, StreamT<M, A>> replicateM(NonZero n) {
        return $(replicateM(), n);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> replicateM(NonZero n, MonadRec<A, M> a) {
        return $(replicateM(n), a);
    }
}

