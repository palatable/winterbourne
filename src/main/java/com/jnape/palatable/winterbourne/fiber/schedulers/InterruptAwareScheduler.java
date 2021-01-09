package com.jnape.palatable.winterbourne.fiber.schedulers;

import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.winterbourne.fiber.Scheduler;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Map.map;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.unfold;
import static com.jnape.palatable.shoki.interop.Shoki.strictQueue;

public final class InterruptAwareScheduler<F extends MonadRec<?, F>, G extends MonadRec<?, G>>
        implements Scheduler.EnvironmentAwareScheduler<MonadRec<Boolean, F>, F, G> {
    private final Scheduler<F, G> delegate;

    private InterruptAwareScheduler(Scheduler<F, G> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <A, GU extends MonadRec<Unit, ReaderT<MonadRec<Boolean, F>, G, ?>>> GU schedule(
            Collection<?, IterateT<F, A>> fibers) {
        return ReaderT.<MonadRec<Boolean, F>, G, Unit>readerT(isInterrupted -> delegate.schedule(strictQueue(map(f -> unfold(
                f_ -> isInterrupted.flatMap(interrupted -> interrupted
                                                           ? isInterrupted.pure(nothing())
                                                           : f_.runIterateT()),
                isInterrupted.pure(f)), fibers)))).coerce();
    }

    public static <F extends MonadRec<?, F>, G extends MonadRec<?, G>> InterruptAwareScheduler<F, G> interruptAwareScheduler(
            Scheduler<F, G> delegate) {
        return new InterruptAwareScheduler<>(delegate);
    }
}
