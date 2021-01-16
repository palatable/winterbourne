package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.DropWhile;
import com.jnape.palatable.lambda.functions.builtin.fn2.TakeWhile;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Apply a predicate to each element in a <code>{@link StreamT}&lt;M, A&gt;</code>, returning a
 * <code>{@link StreamT}&lt;M, A&gt;</code> which emits the values for which the predicate returns true and skips
 * values for which the predicate returns false.
 *
 * @param <M> the <code>StreamT</code> effect type
 * @param <A> the <code>StreamT</code> element type
 * @see TakeWhile
 * @see DropWhile
 */
public final class FilterM<M extends MonadRec<?, M>, A>
        implements Fn2<Fn1<? super A, Boolean>, StreamT<M, A>, StreamT<M, A>> {

    private static final FilterM<?, ?> INSTANCE = new FilterM<>();

    private FilterM() {
    }

    @Override
    public StreamT<M, A> checkedApply(Fn1<? super A, Boolean> predicate, StreamT<M, A> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return streamT(
                () -> as.runStreamT().fmap(m -> m.fmap(
                        t -> t.biMap(mHead -> mHead.filter(predicate),
                                     filterM(predicate)))),
                Pure.of(mUnit));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> FilterM<M, A> filterM() {
        return (FilterM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>> filterM(
            Fn1<? super A, Boolean> predicate) {
        return $(filterM(), predicate);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> filterM(
            Fn1<? super A, Boolean> predicate, StreamT<M, A> as) {
        return $(filterM(predicate), as);
    }
}
