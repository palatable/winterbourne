package com.jnape.palatable.winterbourne.fiber.schedulers;

import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.winterbourne.fiber.Scheduler;

import java.util.concurrent.Executor;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Map.map;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Sequence.sequence;
import static com.jnape.palatable.lambda.io.IO.fuse;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pin;

public final class ExecutorPinningScheduler implements Scheduler.EnvironmentAwareScheduler.Simple<Executor, IO<?>> {
    private static final ExecutorPinningScheduler INSTANCE = new ExecutorPinningScheduler();

    private ExecutorPinningScheduler() {
    }

    @Override
    public <A, GU extends MonadRec<Unit, ReaderT<Executor, IO<?>, ?>>> GU schedule(
            Collection<?, IterateT<IO<?>, A>> fibers) {
        IO<Unit> work = sequence(map(f -> fuse(f.forEach(constantly(io(UNIT)))), fibers), IO::io).fmap(constantly(UNIT));
        return ReaderT.<Executor, IO<?>, Unit>readerT(executor -> pin(work, executor)).coerce();
    }

    public static ExecutorPinningScheduler executorPinningScheduler() {
        return INSTANCE;
    }
}
