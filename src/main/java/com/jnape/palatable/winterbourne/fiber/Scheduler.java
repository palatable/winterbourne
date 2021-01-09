package com.jnape.palatable.winterbourne.fiber;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Lift;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.functor.Functor;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.MonadT;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.winterbourne.NaturalTransformation;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Map.map;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.suspended;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.unfold;
import static com.jnape.palatable.lambda.monad.transformer.builtin.ReaderT.readerT;
import static com.jnape.palatable.shoki.interop.Shoki.strictQueue;

public interface Scheduler<F extends MonadRec<?, F>, G extends MonadRec<?, G>> {

    <A, GU extends MonadRec<Unit, G>> GU schedule(Collection<?, IterateT<F, A>> fibers);

    default <H extends MonadRec<?, H>> Scheduler<F, H> mapM(NaturalTransformation<G, H> gToH) {
        return new Scheduler<>() {
            @Override
            public <A, HU extends MonadRec<Unit, H>> HU schedule(Collection<?, IterateT<F, A>> fibers) {
                return gToH.apply(Scheduler.this.schedule(fibers));
            }
        };
    }

    default <E extends MonadRec<?, E>> Scheduler<E, G> contraMapM(
            NaturalTransformation<IterateT<E, ?>, IterateT<F, ?>> natTrans) {
        return new Scheduler<>() {
            @Override
            public <A, GU extends MonadRec<Unit, G>> GU schedule(Collection<?, IterateT<E, A>> fibers) {
                return Scheduler.this.schedule(strictQueue(map(natTrans::<A, IterateT<F, A>>apply, fibers)));
            }
        };
    }

    default <E extends MonadRec<?, E>> Scheduler<E, G> mapFiberM(NaturalTransformation<E, F> eToF) {
        return contraMapM(new NaturalTransformation<IterateT<E, ?>, IterateT<F, ?>>() {
            @Override
            public <A, GA extends Functor<A, IterateT<F, ?>>> GA apply(Functor<A, IterateT<E, ?>> fa) {
                return mapIterateT(eToF, fa.coerce()).coerce();
            }
        });
    }

    default EnvironmentAwareScheduler<MonadRec<Boolean, F>, F, G> interruptible(Pure<G> pureG) {
        Scheduler<F, ReaderT<MonadRec<Boolean, F>, G, ?>> fReaderTScheduler = liftContraMapM(readerT(checkInterrupt -> {
            NaturalTransformation<IterateT<F, ?>, IterateT<F, ?>> nt = new NaturalTransformation<>() {
                @Override
                public <A, GA extends Functor<A, IterateT<F, ?>>> GA apply(Functor<A, IterateT<F, ?>> fa) {
                    return unfold(f_ -> checkInterrupt.flatMap(interrupted -> interrupted
                                                                              ? checkInterrupt.pure(nothing())
                                                                              : f_.runIterateT()),
                                  checkInterrupt.pure(fa.<IterateT<F, A>>coerce()))
                            .coerce();
                }
            };
            return pureG.apply(nt);
        }));
        return fReaderTScheduler::schedule;
    }

    default <H extends MonadT<G, ?, H, T>, T extends MonadT<?, ?, ?, T>> Scheduler<F, H> liftContraMapM(
            MonadT<G, NaturalTransformation<IterateT<F, ?>, IterateT<F, ?>>, H, T> natTransT) {
        MonadT<G, Scheduler<F, H>, H, T> schedulerT = natTransT
                .fmap(this::contraMapM)
                .fmap(s -> s.mapM(NaturalTransformation.<G, H, T>fromLift(natTransT::lift)));
        return new Scheduler<>() {
            @Override
            public <A, GU extends MonadRec<Unit, H>> GU schedule(Collection<?, IterateT<F, A>> fibers) {
                return schedulerT.flatMap(s -> s.schedule(fibers)).coerce();
            }
        };
    }

    interface Simple<F extends MonadRec<?, F>> extends Scheduler<F, F> {
    }

    interface EnvironmentAwareScheduler<Env, F extends MonadRec<?, F>, G extends MonadRec<?, G>>
            extends Scheduler<F, ReaderT<Env, G, ?>> {

        interface Simple<Env, F extends MonadRec<?, F>> extends EnvironmentAwareScheduler<Env, F, F> {
        }
    }

    private static <A, F extends MonadRec<?, F>, G extends MonadRec<?, G>> IterateT<G, A> mapIterateT(
            NaturalTransformation<F, G> fToG,
            IterateT<F, A> fas) {
        MonadRec<Maybe<Tuple2<A, IterateT<F, A>>>, G> headG = fToG.apply(fas.runIterateT());
        return suspended(() -> headG.fmap(m -> m.fmap(t -> t.fmap(fas_ -> mapIterateT(fToG, fas_)))),
                         Pure.of(headG));
    }
}
