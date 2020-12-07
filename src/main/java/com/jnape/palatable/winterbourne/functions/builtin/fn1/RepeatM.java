package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.suspended;

/**
 * Given a value in monadic effect, return an infinite {@link IterateT} that repeatedly runs the effect.
 *
 * @param <M> the {@link IterateT} effect type
 * @param <A> The {@link IterateT} element type
 */
public final class RepeatM<M extends MonadRec<?, M>, A> implements Fn1<MonadRec<A, M>, IterateT<M, A>> {

    private static final RepeatM<?, ?> INSTANCE = new RepeatM<>();

    private RepeatM() {
    }

    @Override
    public IterateT<M, A> checkedApply(MonadRec<A, M> ma) throws Throwable {
        return suspended(() -> repeatM(ma).runIterateT(), Pure.of(ma)).cons(ma);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> RepeatM<M, A> repeatM() {
        return (RepeatM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> IterateT<M, A> repeatM(MonadRec<A, M> ma) {
        return RepeatM.<M, A>repeatM().apply(ma);
    }
}
