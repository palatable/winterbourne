package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;

/**
 * Retrieve the last emitted element of an {@link StreamT}, wrapped in a {@link Maybe} and the effect <code>M</code>.
 * If the {@link StreamT} is empty, the result is {@link Maybe#nothing()} wrapped in the effect <code>M</code>.
 *
 * @param <M>   the StreamT effect type
 * @param <A>   the StreamT element type
 * @param <MMA> the narrowed last result type
 */
public final class LastM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn1<StreamT<M, A>, MMA> {

    private static final LastM<?, ?, ?> INSTANCE = new LastM<>();

    @Override
    public MMA checkedApply(StreamT<M, A> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return maybeT(as.awaitStreamT())
                .trampolineM(into((head, tail) -> maybeT(tail.awaitStreamT())
                        .<RecursiveResult<Tuple2<A, StreamT<M, A>>, A>>fmap(RecursiveResult::recurse)
                        .or(maybeT(mUnit.pure(just(terminate(head)))))))
                .runMaybeT();
    }

    @SuppressWarnings("unchecked")
    public static <A, M extends MonadRec<?, M>, MMA extends MonadRec<Maybe<A>, M>> LastM<M, A, MMA> lastM() {
        return (LastM<M, A, MMA>) INSTANCE;
    }

    public static <A, M extends MonadRec<?, M>, MMA extends MonadRec<Maybe<A>, M>> MMA lastM(StreamT<M, A> as) {
        return $(lastM(), as);
    }
}

