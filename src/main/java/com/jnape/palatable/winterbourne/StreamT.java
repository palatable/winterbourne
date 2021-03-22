package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functions.specialized.Lift;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.builtin.Lazy;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.MonadT;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.winterbourne.functions.builtin.fn2.UnfoldM;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn0.fn0;
import static com.jnape.palatable.lambda.functions.Fn1.withSelf;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.builtin.fn3.FoldLeft.foldLeft;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.Eval.once;
import static com.jnape.palatable.winterbourne.Eval.value;
import static com.jnape.palatable.winterbourne.Step.elided;
import static com.jnape.palatable.winterbourne.Step.emitted;
import static com.jnape.palatable.winterbourne.Step.exhausted;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitAllM.awaitAllM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ForEachM.forEachM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.UnfoldM.unfoldM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.FoldCutM.foldCutM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.FoldM.foldM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.GForEachM.gForEachM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldCutM.gFoldCutM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldM.gFoldM;

public final class StreamT<M extends MonadRec<?, M>, A> implements MonadT<M, A, StreamT<M, ?>, StreamT<?, ?>> {

    private final MonadRec<Step<A, Eval<StreamT<M, A>>>, M> spine;

    private StreamT(MonadRec<Step<A, Eval<StreamT<M, A>>>, M> spine) {
        this.spine = spine;
    }

    public <MStep extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>> MStep runStreamT() {
        return spine
                .<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>>fmap(step -> step.match(
                        em -> just(tuple(just(em.head()), em.tail().value())),
                        el -> just(tuple(nothing(), el.tail().value())),
                        ex -> nothing()
                )).coerce();
    }

    public <MA extends MonadRec<Maybe<Tuple2<A, StreamT<M, A>>>, M>> MA awaitStreamT() {
        return awaitM(this);
    }

    public <N extends MonadRec<?, N>> StreamT<N, A> mapStreamT(NaturalTransformation<M, N> mToN) {
        return streamT(() -> mToN.apply(runStreamT().fmap(m -> m.fmap(t -> t.fmap(as -> as.mapStreamT(mToN))))),
                       mToN.mapPure(spine::pure));
    }

    public StreamT<M, A> cons(MonadRec<Maybe<A>, M> ma) {
        return new StreamT<>(ma.fmap(maybeA -> maybeA.match(__ -> elided(value(this)), a -> emitted(a, value(this)))));
    }

    public StreamT<M, A> snoc(MonadRec<Maybe<A>, M> ma) {
        return concat(new StreamT<>(ma.fmap(maybe -> maybe.match(
                __ -> exhausted(),
                a -> emitted(a, value(empty(spine::pure)))))));
    }

    public StreamT<M, A> concat(StreamT<M, A> other) {
        return concat(value(other));
    }

    public StreamT<M, A> concat(Eval<StreamT<M, A>> otherEval) {
        return new StreamT<>(spine.flatMap(step -> step.match(
                em -> spine.pure(em.fmap(tailEval -> tailEval.zip(otherEval.fmap(back -> front -> front.concat(back))))),
                el -> spine.pure(elided(el.tail().zip(otherEval.fmap(back -> front -> front.concat(back))))),
                ex -> otherEval.value().spine
        )));
    }

    @Override
    public <B> StreamT<M, B> fmap(Fn1<? super A, ? extends B> fn) {
        return MonadT.super.<B>fmap(fn).coerce();
    }

    @Override
    public <B> StreamT<M, B> pure(B b) {
        return new StreamT<>(spine.pure(emitted(b, value(empty(spine::pure)))));
    }

    @Override
    public <B> StreamT<M, B> zip(Applicative<Fn1<? super A, ? extends B>, StreamT<M, ?>> appFn) {
        return new StreamT<>(spine.zip(appFn.<StreamT<M, Fn1<? super A, ? extends B>>>coerce().spine.fmap(
                stepFn -> stepA -> stepA.match(
                        emA -> stepFn.match(
                                emFn -> emitted(emFn.head().apply(emA.head()),
                                                emFn.tail().fmap(tailFns -> tailFns
                                                        .<B>fmap(f -> f.apply(emA.head()))
                                                        .concat(emA.tail().fmap(t -> t.zip(new StreamT<>(spine.pure(emFn))))))),
                                elFn -> elided(elFn.tail().fmap(new StreamT<>(spine.pure(stepA))::zip)),
                                __ -> exhausted()),
                        elA -> elided(elA.tail().fmap(tail -> tail.zip(new StreamT<>(spine.pure(stepFn))))),
                        __ -> exhausted()
                ))));
    }

    @Override
    public <B> StreamT<M, B> discardL(Applicative<B, StreamT<M, ?>> appB) {
        return MonadT.super.discardL(appB).coerce();
    }

    @Override
    public <B> StreamT<M, A> discardR(Applicative<B, StreamT<M, ?>> appB) {
        return MonadT.super.discardR(appB).coerce();
    }

    @Override
    public <B> Lazy<StreamT<M, B>> lazyZip(
            Lazy<? extends Applicative<Fn1<? super A, ? extends B>, StreamT<M, ?>>> lazyAppFn) {
        return lazyAppFn.fmap(this::zip);
    }

    @Override
    public <B> StreamT<M, B> flatMap(Fn1<? super A, ? extends Monad<B, StreamT<M, ?>>> f) {
        return new StreamT<>(
                spine.flatMap(step -> step.match(
                        em -> {
                            StreamT<M, B>       front    = f.apply(em.head()).coerce();
                            Eval<StreamT<M, B>> backEval = em.tail().flatMap(s -> once(() -> s.flatMap(f)));

                            return front.spine.flatMap(step_ -> step_.match(
                                    em_ -> spine.pure(em_.fmap(e -> e.zip(backEval.fmap(back -> fr -> fr.concat(back))))),
                                    el_ -> spine.pure(el_.fmap(e -> e.zip(backEval.fmap(back -> fr -> fr.concat(back))))),
                                    ex_ -> backEval.value().spine
                            ));
                        },
                        el -> spine.pure(elided(el.tail().flatMap(s -> once(() -> s.flatMap(f))))),
                        ex -> spine.pure(exhausted())
                )));
    }

    @Override
    public <B> StreamT<M, B> trampolineM(Fn1<? super A, ? extends MonadRec<RecursiveResult<A, B>, StreamT<M, ?>>> fn) {
        return $(withSelf((self, queued) -> streamT(() -> spine.pure(queued)
                         .trampolineM(q -> q.runStreamT().fmap(m -> m.match(
                                 fn0(() -> terminate(nothing())),
                                 t -> t.into((maybeRR, tail) -> maybeRR.match(
                                         fn0(() -> terminate(just(tuple(nothing(), self.apply(tail))))),
                                         rr -> rr.biMap(
                                                 a -> fn.apply(a).<StreamT<M, RecursiveResult<A, B>>>coerce().concat(tail),
                                                 b -> just(tuple(just(b), self.apply(tail))))))
                         ))), spine::pure)),
                 flatMap(fn));
    }

    @Override
    public <B, N extends MonadRec<?, N>> StreamT<N, B> lift(MonadRec<B, N> nb) {
        Pure<N>       pureN = Pure.of(nb);
        StreamT<N, B> empty = empty(pureN);
        return new StreamT<>(nb.fmap(b -> emitted(b, value(empty))));
    }

    public <B, MB extends MonadRec<B, M>> MB foldCut(
            Fn2<? super B, ? super Maybe<A>, ? extends MonadRec<RecursiveResult<B, B>, M>> fn, MB acc) {
        return foldCutM(fn, acc, this);
    }

    public <B, MB extends MonadRec<B, M>> MB foldCutAwait(
            Fn2<? super B, ? super A, ? extends MonadRec<RecursiveResult<B, B>, M>> fn, MB acc) {
        return gFoldCutM(awaitM(), fn, acc, this);
    }

    public <B, MB extends MonadRec<B, M>> MB fold(Fn2<? super B, ? super Maybe<A>, MB> fn, MB acc) {
        return foldM(fn, acc, this);
    }

    public <B, MB extends MonadRec<B, M>> MB foldAwait(Fn2<? super B, ? super A, MB> fn, MB acc) {
        return gFoldM(awaitM(), fn, acc, this);
    }

    public <MU extends MonadRec<Unit, M>> MU forEach(Fn1<? super Maybe<A>, MU> action) {
        return forEachM(action, this);
    }

    public <MU extends MonadRec<Unit, M>> MU forEachAwait(Fn1<? super A, MU> action) {
        return gForEachM(awaitM(), action, this);
    }

    public <MU extends MonadRec<Unit, M>> MU awaitAll() {
        return awaitAllM(this);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(
            Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>> thunk, Pure<M> pureM) {
        Step<A, Eval<StreamT<M, A>>> step = elided(once(thunk.fmap(m -> new StreamT<>(m.fmap(maybe -> maybe.match(
                __ -> exhausted(),
                into((maybeA, tail) -> maybeA.match(
                        __ -> elided(value(tail)),
                        a -> emitted(a, value(tail))
                ))
        ))))));
        return new StreamT<>(pureM.apply(step));
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT0(
            MonadRec<Step<A, Eval<StreamT<M, A>>>, M> spine) {
        return new StreamT<>(spine);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> empty(Pure<M> pureN) {
        Step<A, Eval<StreamT<M, A>>> exhausted = exhausted();
        return new StreamT<>(pureN.apply(exhausted));
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(
            MonadRec<? extends Collection<?, Maybe<A>>, M> mas) {
        Fn1<Iterable<Maybe<A>>, Step<A, Eval<StreamT<M, A>>>> fn = foldLeft((step, maybeA) -> step.match(
                em -> em.fmap(tailEval -> tailEval.fmap(tail -> tail.snoc(mas.pure(maybeA)))),
                el -> el.fmap(tailEval -> tailEval.fmap(tail -> tail.snoc(mas.pure(maybeA)))),
                ex -> maybeA.match(__ -> elided(value(empty(mas::pure))), a -> emitted(a, value(empty(mas::pure))))
        ), Step.<A, Eval<StreamT<M, A>>>exhausted());
        return streamT0(mas.fmap(fn));
    }

    public static <M extends MonadRec<?, M>, A, B> StreamT<M, A> unfold(
            Fn1<? super B, ? extends MonadRec<Maybe<Tuple2<Maybe<A>, B>>, M>> f, MonadRec<B, M> seedM) {
        return unfoldM(f, seedM);
    }

    @Override
    public String toString() {
        return "StreamT{" +
                "spine=" + spine +
                '}';
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(
            Collection<?, ? extends MonadRec<Maybe<A>, M>> mas, Pure<M> pureM) {
        return UnfoldM.<M, A, Collection<?, ? extends MonadRec<Maybe<A>, M>>>unfoldM(
                more -> more.head().match(
                        __ -> pureM.apply(nothing()),
                        ma -> ma.fmap(a -> just(tuple(a, more.tail())))),
                pureM.apply(mas));
    }

    @SafeVarargs
    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(MonadRec<Maybe<A>, M> ma,
                                                                      MonadRec<Maybe<A>, M>... more) {
        return streamT(strictQueue(more).cons(ma), Pure.of(ma));
    }

    public static <M extends MonadRec<?, M>> Pure<StreamT<M, ?>> pureStreamT(Pure<M> pureM) {
        return new Pure<>() {
            @Override
            public <A> StreamT<M, A> checkedApply(A a) {
                return streamT(pureM.<Maybe<A>, MonadRec<Maybe<A>, M>>apply(just(a)));
            }
        };
    }

    public static Lift<StreamT<?, ?>> liftStreamT() {
        return new Lift<>() {
            @Override
            public <A, M extends MonadRec<?, M>> StreamT<M, A> checkedApply(MonadRec<A, M> ma) {
                return streamT(ma.<Maybe<A>>fmap(Maybe::just));
            }
        };
    }
}
