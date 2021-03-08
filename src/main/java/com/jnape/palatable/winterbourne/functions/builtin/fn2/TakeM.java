package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Lazily limit a {@link StreamT} to <code>n</code> emitted elements by returning an {@link StreamT} that stops
 * streaming after the <code>nth</code> element, or the last step of the {@link StreamT}, whichever comes first.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The {@link StreamT} element type
 */
public final class TakeM<M extends MonadRec<?, M>, A> implements Fn2<NonZero, StreamT<M, A>, StreamT<M, A>> {

    private static final TakeM<?, ?> INSTANCE = new TakeM<>();

    private TakeM() {
    }

    @Override
    public StreamT<M, A> checkedApply(NonZero n, StreamT<M, A> as) {
        Pure<M> pureM = as.pure(UNIT).runStreamT()::pure;
        return streamT(() -> as.runStreamT()
                               .fmap(m -> m.fmap(t -> t.into((maybeA, tail) -> tuple(
                                       maybeA,
                                       n.minus(maybeA.match(constantly(zero()), constantly(one()))).orElse(zero())
                                               .match(__ -> empty(pureM), nextN -> takeM(nextN, tail))
                               )))),
                       pureM);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> TakeM<M, A> takeM() {
        return (TakeM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>> takeM(NonZero n) {
        return $(takeM(), n);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> takeM(NonZero n, StreamT<M, A> as) {
        return $(takeM(n), as);
    }
}
