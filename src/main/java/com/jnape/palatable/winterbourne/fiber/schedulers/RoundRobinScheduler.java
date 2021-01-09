package com.jnape.palatable.winterbourne.fiber.schedulers;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.fiber.Scheduler;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.Fn0.fn0;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.unfold;
import static com.jnape.palatable.shoki.interop.Shoki.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;

public final class RoundRobinScheduler<F extends MonadRec<?, F>> implements Scheduler.Simple<F> {

    private final Pure<F> pureM;

    private RoundRobinScheduler(Pure<F> pureM) {
        this.pureM = pureM;
    }

    @Override
    public <A, MU extends MonadRec<Unit, F>> MU schedule(Collection<?, IterateT<F, A>> fibers) {
        MonadRec<RecursiveResult<StrictQueue<IterateT<F, A>>, Maybe<Tuple2<A, StrictQueue<IterateT<F, A>>>>>, F> terminate =
                pureM.apply(terminate(nothing()));
        return unfold(fiberQueue -> pureM.<StrictQueue<IterateT<F, A>>, MonadRec<StrictQueue<IterateT<F, A>>, F>>apply(fiberQueue)
                              .trampolineM(q -> q.head().match(
                                      constantly(terminate),
                                      nextFiber -> nextFiber.runIterateT()
                                              .fmap(maybeHeadAndTail -> maybeHeadAndTail.match(
                                                      fn0(() -> recurse(q.tail())),
                                                      t -> terminate(just(t.fmap(q.tail()::snoc))))))),
                      pureM.<StrictQueue<IterateT<F, A>>, MonadRec<StrictQueue<IterateT<F, A>>, F>>apply(strictQueue(fibers)))
                .forEach(constantly(pureM.apply(UNIT)));
    }

    public static <M extends MonadRec<?, M>> RoundRobinScheduler<M> roundRobinScheduler(Pure<M> pureM) {
        return new RoundRobinScheduler<>(pureM);
    }

    public static void main(String[] args) {
        RoundRobinScheduler<IO<?>>                                        scheduler     = roundRobinScheduler(pureIO());
        InterruptAwareScheduler<IO<?>, IO<?>>                             ioioInterruptAwareScheduler = InterruptAwareScheduler.interruptAwareScheduler(scheduler);

        IterateT<IO<?>, Unit> workA = repeatM(io(() -> {
            System.out.println("running A");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        IterateT<IO<?>, Unit> workB = repeatM(io(() -> {
            System.out.println("running B");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        ReaderT<MonadRec<Boolean, IO<?>>, IO<?>, Unit> running = ioioInterruptAwareScheduler.schedule(StrictQueue.strictQueue(workA, workB));

        Thread mainThread = Thread.currentThread();
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("interrupting main thread");
                mainThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }){{
            start();
        }};

        running.<IO<Unit>>runReaderT(io(() -> {
            System.out.print("Scheduler: checking interrupt flag...");
            boolean interrupted = Thread.interrupted();
            System.out.println(interrupted ? "interrupted!" : "still running.");
            return interrupted;
        })).unsafePerformIO();

    }
}
