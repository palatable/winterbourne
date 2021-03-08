package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.DropWhile;
import com.jnape.palatable.lambda.functions.builtin.fn2.TakeWhile;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Apply a predicate to each element in a <code>{@link StreamT}&lt;M, A&gt;</code>, returning a
 * <code>{@link StreamT}&lt;M, A&gt;</code> which emits the elements for which the predicate returns true and elides
 * elements for which the predicate returns false.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> the {@link StreamT} element type
 * @see TakeWhile
 * @see DropWhile
 */
public final class FilterM<M extends MonadRec<?, M>, A>
        implements Fn2<Fn1<? super A, ? extends Boolean>, StreamT<M, A>, StreamT<M, A>> {

    private static final FilterM<?, ?> INSTANCE = new FilterM<>();

    private FilterM() {
    }

    @Override
    public StreamT<M, A> checkedApply(Fn1<? super A, ? extends Boolean> predicate, StreamT<M, A> as) {
        return streamT(() -> as.runStreamT().fmap(m -> m.fmap(t -> t.biMap(mHead -> mHead.filter(predicate),
                                                                           filterM(predicate)))),
                       as.pure(UNIT).runStreamT()::pure);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> FilterM<M, A> filterM() {
        return (FilterM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>> filterM(
            Fn1<? super A, ? extends Boolean> predicate) {
        return $(filterM(), predicate);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> filterM(
            Fn1<? super A, ? extends Boolean> predicate, StreamT<M, A> as) {
        return $(filterM(predicate), as);
    }
}
