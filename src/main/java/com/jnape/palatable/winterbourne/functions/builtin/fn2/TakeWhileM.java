package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Limit a {@link StreamT} to the first group of contiguous emitted elements that satisfy the predicate by emitting up
 * to, but not including or beyond, the first element for which the predicate evaluates to <code>false</code>.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The {@link StreamT} element type
 * @see TakeM
 * @see FilterM
 * @see DropWhileM
 */
public final class TakeWhileM<M extends MonadRec<?, M>, A>
        implements Fn2<Fn1<? super A, Boolean>, StreamT<M, A>, StreamT<M, A>> {

    private static final TakeWhileM<?, ?> INSTANCE = new TakeWhileM<>();

    private TakeWhileM() {
    }

    @Override
    public StreamT<M, A> checkedApply(Fn1<? super A, Boolean> predicate, StreamT<M, A> as) {
        return streamT(() -> as.runStreamT().fmap(m -> m
                               .filter(t -> t._1().match(constantly(true), predicate))
                               .fmap(t -> t.fmap(takeWhileM(predicate)))),
                       as.pure(UNIT).runStreamT()::pure);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> TakeWhileM<M, A> takeWhileM() {
        return (TakeWhileM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>>
    takeWhileM(Fn1<? super A, Boolean> predicate) {
        return $(takeWhileM(), predicate);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A>
    takeWhileM(Fn1<? super A, Boolean> predicate, StreamT<M, A> as) {
        return $(takeWhileM(predicate), as);
    }
}
