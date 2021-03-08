package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Skip the first <code>n</code> elements emitted by a <code>{@link StreamT}&lt;M, A&gt;</code>.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The {@link StreamT} element type
 */
public final class DropM<M extends MonadRec<?, M>, A> implements Fn2<NonZero, StreamT<M, A>, StreamT<M, A>> {

    private static final DropM<?, ?> INSTANCE = new DropM<>();

    private DropM() {
    }

    @Override
    public StreamT<M, A> checkedApply(NonZero n, StreamT<M, A> as) {
        return streamT(() -> as.runStreamT()
                               .fmap(m -> m.fmap(into((mHead, tail) -> tuple(
                                       nothing(),
                                       mHead.discardL(n.dec()).orElse(n)
                                               .match(constantly(tail), nz -> dropM(nz, tail)))))),
                       as.pure(UNIT).runStreamT()::pure);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> DropM<M, A> dropM() {
        return (DropM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>> dropM(NonZero n) {
        return $(dropM(), n);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> dropM(NonZero n, StreamT<M, A> as) {
        return $(dropM(n), as);
    }
}
