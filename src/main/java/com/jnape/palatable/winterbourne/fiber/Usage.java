package com.jnape.palatable.winterbourne.fiber;

import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.Functor;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.lambda.monad.transformer.builtin.IdentityT;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import com.jnape.palatable.winterbourne.NaturalTransformation;
import com.jnape.palatable.winterbourne.fiber.schedulers.RoundRobinScheduler;

import java.util.concurrent.atomic.AtomicLong;

import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IdentityT.liftIdentityT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.singleton;
import static com.jnape.palatable.shoki.impl.StrictStack.strictStack;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;

public interface Usage {

    interface FiberState {

        //
    }

    static void main(String[] args) {
        Scheduler<IdentityT<IO<?>, ?>, IO<?>> scheduler = RoundRobinScheduler.<IO<?>>roundRobinScheduler(pureIO())
                .mapFiberM(new NaturalTransformation<IdentityT<IO<?>, ?>, IO<?>>() {
                    @Override
                    public <A, GA extends Functor<A, IO<?>>> GA apply(Functor<A, IdentityT<IO<?>, ?>> fa) {
                        return fa.<IdentityT<IO<?>, A>>coerce().<IO<Identity<A>>>runIdentityT().fmap(Identity::runIdentity).coerce();
                    }
                });

        IterateT<IdentityT<IO<?>, ?>, Long> sharedState = repeatM(liftIdentityT().<Long, IO<?>, IdentityT<IO<?>, Long>>apply(io(new AtomicLong()::incrementAndGet)));

        Fn1<String, IterateT<IdentityT<IO<?>, ?>, Unit>> mkThread = name ->
                sharedState
                        .flatMap(l -> singleton(liftIdentityT().apply(io(() -> {
                            if (l % 50_000 == 0)
                                System.out.println(name + "\t" + Thread.currentThread() + ": " + l);
                        }))));

        IterateT<IdentityT<IO<?>, ?>, Unit> thread1 = mkThread.apply("foo");
        IterateT<IdentityT<IO<?>, ?>, Unit> thread2 = mkThread.apply("bar");
        IterateT<IdentityT<IO<?>, ?>, Unit> thread3 = mkThread.apply("baz");


        IO<Unit> scheduled = scheduler.schedule(strictStack(thread1, thread2, thread3));
        scheduled.unsafePerformIO();
    }
}
