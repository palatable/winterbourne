package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.iterateT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.suspended;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;

/**
 * Lazily limit the <code>IterateT</code> to the first group of contiguous elements that satisfy the predicate by
 * iterating up to, but not including, the first element for which the predicate evaluates to <code>false</code>.
 *
 * @param <M> the {@link IterateT} effect type
 * @param <A> The {@link IterateT} element type
 * @see TakeM
 * @see FilterM
 * @see DropWhileM
 */
public final class TakeWhileM<M extends MonadRec<?, M>, A> implements
        Fn2<Fn1<? super A, ? extends Boolean>, IterateT<M, A>, IterateT<M, A>> {

    private static final TakeWhileM<?, ?> INSTANCE = new TakeWhileM<>();

    @Override
    public IterateT<M, A> checkedApply(Fn1<? super A, ? extends Boolean> predicate, IterateT<M, A> mas) {
        MonadRec<Maybe<Tuple2<A, IterateT<M, A>>>, M> head = mas.runIterateT();
        return iterateT(maybeT(head)
                                .filter(predicate.diMapL(Tuple2::_1))
                                .fmap(t -> t.fmap(as -> suspended(() -> takeWhileM(predicate, as).runIterateT(),
                                                                  Pure.of(head))))
                                .runMaybeT());
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> TakeWhileM<M, A> takeWhileM() {
        return (TakeWhileM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<IterateT<M, A>, IterateT<M, A>> takeWhileM(
            Fn1<? super A, ? extends Boolean> predicate) {
        return TakeWhileM.<M, A>takeWhileM().apply(predicate);
    }

    public static <M extends MonadRec<?, M>, A> IterateT<M, A> takeWhileM(Fn1<? super A, ? extends Boolean> predicate,
                                                                          IterateT<M, A> as) {
        return TakeWhileM.<M, A>takeWhileM(predicate).apply(as);
    }
}
