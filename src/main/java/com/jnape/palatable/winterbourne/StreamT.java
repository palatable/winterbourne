package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.builtin.Lazy;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.MonadT;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.shoki.impl.StrictQueue;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn0.fn0;
import static com.jnape.palatable.lambda.functions.Fn1.withSelf;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.monad.Monad.join;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;

public final class StreamT<M extends MonadRec<?, M>, A> implements MonadT<M, A, StreamT<M, ?>, StreamT<?, ?>> {

    private final Pure<M>                                                                         pureM;
    private final StrictQueue<Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>> spine;

    private StreamT(Pure<M> pureM,
                    StrictQueue<Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>> spine) {
        this.pureM = pureM;
        this.spine = spine;
    }

    public <MStep extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>> MStep runStreamT() {
        Fn1<StrictQueue<Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>>,
                MonadRec<RecursiveResult<StrictQueue<Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>>,
                        Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>>, M>> tickLast = q -> q.head().match(
                fn0(() -> pureM.apply(RecursiveResult.<StrictQueue<Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>>,
                        Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>>terminate(nothing()))),
                f -> f.apply().fmap(maybeStep -> maybeStep.match(
                        fn0(() -> recurse(q.tail())),
                        t -> terminate(just(t.fmap(as -> new StreamT<>(pureM, as.spine.snocAll(q.tail()))))))));

        MonadRec<StrictQueue<Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>>, M> spineM = pureM.apply(spine);
        return spineM
                .trampolineM(tickLast)
                .coerce();
    }

    public <MA extends MonadRec<Maybe<Tuple2<A, StreamT<M, A>>>, M>> MA awaitStreamT() {
        return pureM.<StreamT<M, A>, MonadRec<StreamT<M, A>, M>>apply(this)
                .<Maybe<Tuple2<A, StreamT<M, A>>>>trampolineM(as -> as.runStreamT().fmap(maybeStep -> maybeStep.match(
                        fn0(() -> terminate(nothing())),
                        t -> t.into((maybeA, as_) -> maybeA.match(
                                fn0(() -> recurse(as_)),
                                a -> terminate(just(tuple(a, as_))))))))
                .coerce();
    }

    public <N extends MonadRec<?, N>> StreamT<N, A> mapStreamT(NaturalTransformation<M, N> mToN) {
        return streamT(() -> mToN.apply(runStreamT().fmap(m -> m.fmap(t -> t.fmap(as -> as.mapStreamT(mToN))))),
                       mToN.mapPure(pureM));
    }

    public StreamT<M, A> cons(MonadRec<Maybe<A>, M> ma) {
        return new StreamT<>(pureM, spine.cons(() -> ma.fmap(maybeA -> just(tuple(maybeA, empty(pureM))))));
    }

    public StreamT<M, A> snoc(MonadRec<Maybe<A>, M> ma) {
        return new StreamT<>(pureM, spine.snoc(() -> ma.fmap(maybeA -> just(tuple(maybeA, empty(pureM))))));
    }

    public StreamT<M, A> concat(StreamT<M, A> other) {
        return new StreamT<>(pureM, spine.snocAll(other.spine));
    }

    @Override
    public <B> StreamT<M, B> fmap(Fn1<? super A, ? extends B> fn) {
        return MonadT.super.<B>fmap(fn).coerce();
    }

    @Override
    public <B> StreamT<M, B> pure(B b) {
        return new StreamT<>(pureM, strictQueue(() -> pureM.apply(just(tuple(just(b), empty(pureM))))));
    }

    @Override
    public <B> StreamT<M, B> zip(Applicative<Fn1<? super A, ? extends B>, StreamT<M, ?>> appFn) {
        return streamT(() -> {
            MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M> headM = runStreamT();
            return join(maybeT(headM).zip(
                    maybeT(appFn.<StreamT<M, Fn1<? super A, ? extends B>>>coerce().runStreamT())
                            .fmap(into((maybeF, fs) -> into((maybeA, as) -> maybeA.match(
                                    fn0(() -> maybeT(headM.pure(just(tuple(nothing(), as.zip(fs.cons(headM.pure(maybeF)))))))),
                                    a -> maybeF.match(
                                            fn0(() -> maybeT(headM.pure(just(tuple(nothing(), as.cons(headM.pure(just(a))).zip(fs)))))),
                                            f -> maybeT(fs.<B>fmap(f_ -> f_.apply(a)).cons(headM.pure(just(f.apply(a))))
                                                                .concat(as.zip(appFn))
                                                                .runStreamT()))))))))
                    .runMaybeT();
        }, pureM);
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
        return streamT(
                () -> runStreamT().trampolineM(maybeSpine -> maybeSpine.match(
                        fn0(() -> pureM.apply(RecursiveResult.<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, Maybe<Tuple2<Maybe<B>, StreamT<M, B>>>>terminate(nothing()))),
                        into((maybeA, as) -> maybeA.match(
                                fn0(() -> pureM.apply(RecursiveResult.<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, Maybe<Tuple2<Maybe<B>, StreamT<M, B>>>>terminate(just(tuple(nothing(), as.flatMap(f)))))),
                                a -> f.apply(a).<StreamT<M, B>>coerce().runStreamT()
                                        .flatMap(maybeNext -> maybeNext.match(
                                                fn0(() -> as.runStreamT().fmap(RecursiveResult::recurse)),
                                                bSpine -> pureM.apply(terminate(just(bSpine.fmap(bs_ -> bs_.concat(as.flatMap(f)))))))))))),
                pureM);
    }

    @Override
    public <B> StreamT<M, B> trampolineM(Fn1<? super A, ? extends MonadRec<RecursiveResult<A, B>, StreamT<M, ?>>> fn) {
        return $(withSelf((self, queued) -> streamT(() -> pureM.<StreamT<M, RecursiveResult<A, B>>, MonadRec<StreamT<M, RecursiveResult<A, B>>, M>>apply(queued)
                         .trampolineM(q -> q.runStreamT().fmap(m -> m.match(
                                 fn0(() -> terminate(nothing())),
                                 t -> t.into((maybeRR, tail) -> maybeRR.match(
                                         fn0(() -> terminate(just(tuple(nothing(), self.apply(tail))))),
                                         rr -> rr.biMap(
                                                 a -> fn.apply(a).<StreamT<M, RecursiveResult<A, B>>>coerce().concat(tail),
                                                 b -> just(tuple(just(b), self.apply(tail))))))
                         ))), pureM)),
                 flatMap(fn));
    }

    @Override
    public <B, N extends MonadRec<?, N>> StreamT<N, B> lift(MonadRec<B, N> nb) {
        Pure<N> pureN = Pure.of(nb);
        return new StreamT<>(pureN, strictQueue(() -> nb.fmap(b -> just(tuple(just(b), empty(pureN))))));
    }

    public <B, MB extends MonadRec<B, M>> MB foldCut(
            Fn2<? super B, ? super Maybe<A>, ? extends MonadRec<RecursiveResult<B, B>, M>> fn, MB acc) {
        return foldCut0(fn, acc, this, StreamT::<MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>runStreamT);
    }

    public <B, MB extends MonadRec<B, M>> MB foldCutAwait(
            Fn2<? super B, ? super A, ? extends MonadRec<RecursiveResult<B, B>, M>> fn, MB acc) {
        return foldCut0(fn, acc, this, StreamT::<MonadRec<Maybe<Tuple2<A, StreamT<M, A>>>, M>>awaitStreamT);
    }

    public <B, MB extends MonadRec<B, M>> MB fold(Fn2<? super B, ? super Maybe<A>, MB> fn, MB acc) {
        return foldCut((b, maybeA) -> fn.apply(b, maybeA).fmap(RecursiveResult::recurse), acc);
    }

    public <B, MB extends MonadRec<B, M>> MB foldAwait(Fn2<? super B, ? super A, MB> fn, MB acc) {
        return foldCutAwait((b, a) -> fn.apply(b, a).fmap(RecursiveResult::recurse), acc);
    }

    public <MU extends MonadRec<Unit, M>> MU forEach(Fn1<? super Maybe<A>, MU> action) {
        return fold((__, a) -> action.apply(a), pureM.apply(UNIT));
    }

    public <MU extends MonadRec<Unit, M>> MU forEachAwait(Fn1<? super A, MU> action) {
        return foldAwait((__, a) -> action.apply(a), pureM.apply(UNIT));
    }

    public <MU extends MonadRec<Unit, M>> MU awaitAll() {
        return forEachAwait(constantly(pureM.apply(UNIT)));
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(
            Fn0<? extends MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>> thunk, Pure<M> pureM) {
        return new StreamT<>(pureM, strictQueue(thunk));
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> empty(Pure<M> pureN) {
        return new StreamT<>(pureN, strictQueue());
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(
            MonadRec<? extends Collection<?, Maybe<A>>, M> mas) {
        return streamT(() -> mas.fmap(as -> as.head().fmap(maybeA -> tuple(maybeA, streamT(mas.pure(as.tail()))))),
                       Pure.of(mas));
    }

    public static <M extends MonadRec<?, M>, A, B> StreamT<M, A> unfold(
            Fn1<? super B, ? extends MonadRec<Maybe<Tuple2<Maybe<A>, B>>, M>> f, MonadRec<B, M> seedM) {
        return streamT(() -> seedM.flatMap(b -> f.apply(b).fmap(m -> m.fmap(t -> t.fmap(b_ -> unfold(f, seedM.pure(b_)))))),
                       Pure.of(seedM));
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(
            Collection<?, ? extends MonadRec<Maybe<A>, M>> mas, Pure<M> pureM) {
        return StreamT.<M, A, Collection<?, ? extends MonadRec<Maybe<A>, M>>>unfold(
                more -> more.head().match(
                        constantly(pureM.apply(Maybe.<Tuple2<Maybe<A>, Collection<?, ? extends MonadRec<Maybe<A>, M>>>>nothing())),
                        ma -> ma.fmap(a -> just(tuple(a, more.tail())))),
                pureM.apply(mas));
    }

    @SafeVarargs
    public static <M extends MonadRec<?, M>, A> StreamT<M, A> streamT(MonadRec<Maybe<A>, M> ma,
                                                                      MonadRec<Maybe<A>, M>... more) {
        return streamT(strictQueue(more).cons(ma), Pure.of(ma));
    }

    private static <A, X, B, M extends MonadRec<?, M>, MB extends MonadRec<B, M>> MB foldCut0(
            Fn2<? super B, ? super X, ? extends MonadRec<RecursiveResult<B, B>, M>> fn, MB acc, StreamT<M, A> streamT,
            Fn1<? super StreamT<M, A>, ? extends MonadRec<Maybe<Tuple2<X, StreamT<M, A>>>, M>> advance) {
        return acc.fmap(tupler(streamT))
                .trampolineM(into((as, b) -> maybeT(advance.apply(as))
                        .flatMap(into((a, aas) -> maybeT(fn.apply(b, a).fmap(Maybe::just)).fmap(tupler(aas))))
                        .runMaybeT()
                        .fmap(maybeR -> maybeR.match(
                                __ -> terminate(b),
                                into((rest, rr) -> rr.biMapL(tupler(rest)))))))
                .coerce();
    }
}
