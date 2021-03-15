package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.winterbourne.StreamT.streamT;

public final class UnfoldM<M extends MonadRec<?, M>, A, B> implements
        Fn2<Fn1<? super B, ? extends MonadRec<Maybe<Tuple2<Maybe<A>, B>>, M>>, MonadRec<B, M>, StreamT<M, A>> {

    private static final UnfoldM<?, ?, ?> INSTANCE = new UnfoldM<>();

    private UnfoldM() {
    }

    @Override
    public StreamT<M, A> checkedApply(Fn1<? super B, ? extends MonadRec<Maybe<Tuple2<Maybe<A>, B>>, M>> unfold,
                                      MonadRec<B, M> seedM) throws Throwable {
        Pure<M> pureM = Pure.of(seedM);
        return Fn1.<MonadRec<B, M>, StreamT<M, A>>withSelf((g, mb) -> streamT(
                () -> mb.flatMap(b -> unfold.apply(b)
                        .fmap(m -> m.fmap(t -> t.fmap(b_ -> g.apply(pureM.<B, MonadRec<B, M>>apply(b_)))))),
                pureM))
                .apply(seedM);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B> UnfoldM<M, A, B> unfoldM() {
        return (UnfoldM<M, A, B>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B> Fn1<MonadRec<B, M>, StreamT<M, A>> unfoldM(
            Fn1<? super B, ? extends MonadRec<Maybe<Tuple2<Maybe<A>, B>>, M>> unfold) {
        return UnfoldM.<M, A, B>unfoldM().apply(unfold);
    }

    public static <M extends MonadRec<?, M>, A, B> StreamT<M, A> unfoldM(
            Fn1<? super B, ? extends MonadRec<Maybe<Tuple2<Maybe<A>, B>>, M>> unfold,
            MonadRec<B, M> seedM) {
        return unfoldM(unfold).apply(seedM);
    }
}
