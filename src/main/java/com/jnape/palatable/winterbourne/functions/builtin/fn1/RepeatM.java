package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Given a value in monadic effect, return an infinite {@link StreamT} that repeatedly runs the effect.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The {@link StreamT} element type
 */
public class RepeatM<M extends MonadRec<?, M>, A> implements Fn1<MonadRec<A, M>, StreamT<M, A>> {

    private static final RepeatM<?, ?> INSTANCE = new RepeatM<>();

    private RepeatM() {
    }

    @Override
    public StreamT<M, A> checkedApply(MonadRec<A, M> ma) throws Throwable {
        return streamT(() -> ma.fmap(a -> just(tuple(just(a), repeatM(ma)))), Pure.of(ma));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> RepeatM<M, A> repeatM() {
        return (RepeatM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> repeatM(MonadRec<A, M> ma) {
        return $(repeatM(), ma);
    }
}
