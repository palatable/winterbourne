package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;

/**
 * Retrieve the last element of an {@link IterateT}, wrapped in a {@link Maybe}. If the {@link IterateT} is empty, the
 * result is {@link Maybe#nothing()}.
 *
 * @param <M>   the IterateT effect type
 * @param <A>   the IterateT element type
 * @param <MMA> the narrowed last result type
 */
public final class LastM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn1<IterateT<M, A>, MMA> {

    private static final LastM<?, ?, ?> INSTANCE = new LastM<>();

    @Override
    public MMA checkedApply(IterateT<M, A> mas) {
        MonadRec<Maybe<Tuple2<A, IterateT<M, A>>>, M> headM = mas.runIterateT();
        return maybeT(headM).trampolineM(into((a, as) -> maybeT(as.runIterateT())
                .<RecursiveResult<Tuple2<A, IterateT<M, A>>, A>>fmap(RecursiveResult::recurse)
                .or(maybeT(headM.pure(just(terminate(a)))))))
                .runMaybeT();
    }

    @SuppressWarnings("unchecked")
    public static <A, M extends MonadRec<?, M>, MMA extends MonadRec<Maybe<A>, M>> LastM<M, A, MMA> lastM() {
        return (LastM<M, A, MMA>) INSTANCE;
    }

    public static <A, M extends MonadRec<?, M>, MMA extends MonadRec<Maybe<A>, M>> MMA lastM(IterateT<M, A> as) {
        return LastM.<A, M, MMA>lastM().apply(as);
    }
}
