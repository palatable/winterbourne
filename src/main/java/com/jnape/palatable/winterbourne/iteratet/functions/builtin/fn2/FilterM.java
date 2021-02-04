package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.functions.builtin.fn4.IfThenElse.ifThenElse;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.iterateT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.singleton;

/**
 * Lazily apply a predicate to each element in an <code>IterateT</code>, returning an <code>IterateT</code> of just the
 * elements for which the predicate evaluated to <code>true</code>.
 *
 * @param <M> the IterateT effect type
 * @param <A> A type contravariant to the input IterateT element type
 * @see TakeWhileM
 * @see DropWhileM
 */
public final class FilterM<M extends MonadRec<?, M>, A>
        implements Fn2<Fn1<? super A, ? extends Boolean>, IterateT<M, A>, IterateT<M, A>> {

    private static final FilterM<?, ?> INSTANCE = new FilterM<>();

    @Override
    public IterateT<M, A> checkedApply(Fn1<? super A, ? extends Boolean> predicate,
                                       IterateT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<A, IterateT<M, A>>>, M> unwrapped = as.runIterateT();
        return iterateT(unwrapped)
                .flatMap(ifThenElse(predicate,
                                    a -> singleton(unwrapped.pure(a)),
                                    __ -> empty(Pure.of(unwrapped))));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> FilterM<M, A> filterM() {
        return (FilterM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<IterateT<M, A>, IterateT<M, A>> filterM(
            Fn1<? super A, ? extends Boolean> predicate) {
        return FilterM.<M, A>filterM().apply(predicate);
    }

    public static <M extends MonadRec<?, M>, A> IterateT<M, A> filterM(Fn1<? super A, ? extends Boolean> predicate,
                                                                       IterateT<M, A> as) {
        return FilterM.<M, A>filterM(predicate).apply(as);
    }
}
