package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FilterM.filterM;

/**
 * Retrieve the first element emitted by a <code>{@link StreamT}&lt;M, A&gt;</code> which matches a predicate, wrapped
 * in a {@link Maybe} and the effect <code>M</code>. If no elements match the predicate, the result is
 * {@link Maybe#nothing()} wrapped in the effect <code>M</code>.
 *
 * @param <A>   the {@link StreamT} element type
 * @param <M>   the {@link StreamT} effect type
 * @param <MMA> the narrowed find result type
 */
public final class FindM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn2<Fn1<? super A, ? extends Boolean>, StreamT<M, A>, MMA> {

    private static final FindM<?, ?, ?> INSTANCE = new FindM<>();

    private FindM() {
    }

    @Override
    public MMA checkedApply(Fn1<? super A, ? extends Boolean> predicate, StreamT<M, A> as) {
        return headM(filterM(predicate, as));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> FindM<M, A, MMA> findM() {
        return (FindM<M, A, MMA>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> Fn1<StreamT<M, A>, MMA>
    findM(Fn1<? super A, ? extends Boolean> predicate) {
        return $(findM(), predicate);
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> MMA
    findM(Fn1<? super A, ? extends Boolean> predicate, StreamT<M, A> as) {
        return $(findM(predicate), as);
    }
}
