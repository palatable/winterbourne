package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn0.fn0;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;

public final class AwaitT<M extends MonadRec<?, M>, A, MStep extends MonadRec<Maybe<Tuple2<A, StreamT<M, A>>>, M>>
        implements Fn1<StreamT<M, A>, MStep> {

    private static final AwaitT<?, ?, ?> INSTANCE = new AwaitT<>();

    private AwaitT() {
    }

    @Override
    public MStep checkedApply(StreamT<M, A> streamT) {
        Pure<M> pureM = streamT.pure(UNIT).runStreamT()::pure;
        return pureM.<StreamT<M, A>, MonadRec<StreamT<M, A>, M>>apply(streamT)
                .<Maybe<Tuple2<A, StreamT<M, A>>>>trampolineM(as -> as.runStreamT().fmap(maybeStep -> maybeStep.match(
                        fn0(() -> terminate(nothing())),
                        t -> t.into((maybeA, as_) -> maybeA.match(
                                fn0(() -> recurse(as_)),
                                a -> terminate(just(tuple(a, as_))))))))
                .coerce();
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MStep extends MonadRec<Maybe<Tuple2<A, StreamT<M, A>>>, M>>
    AwaitT<M, A, MStep> awaitT() {
        return (AwaitT<M, A, MStep>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MStep extends MonadRec<Maybe<Tuple2<A, StreamT<M, A>>>, M>>
    MStep awaitT(StreamT<M, A> streamT) {
        return AwaitT.<M, A, MStep>awaitT().apply(streamT);
    }
}
