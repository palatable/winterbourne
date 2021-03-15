package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.Fn2.curried;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldM.gFoldM;

/**
 * Retrieve the last emitted element of an {@link StreamT}, wrapped in a {@link Maybe} and the effect <code>M</code>.
 * If the {@link StreamT} is empty, the result is {@link Maybe#nothing()} wrapped in the effect <code>M</code>.
 *
 * @param <M>   the {@link StreamT} effect type
 * @param <A>   the {@link StreamT} element type
 * @param <MMA> the narrowed last result type
 */
public final class LastM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn1<StreamT<M, A>, MMA> {

    private static final LastM<?, ?, ?> INSTANCE = new LastM<>();

    private LastM() {
    }

    @Override
    public MMA checkedApply(StreamT<M, A> as) {
        Pure<M> pureM = Pure.of(as.pure(UNIT).<MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M>>runStreamT());
        return gFoldM(awaitM(), curried(constantly(a -> pureM.apply(just(a)))), pureM.apply(nothing()), as);
    }

    @SuppressWarnings("unchecked")
    public static <A, M extends MonadRec<?, M>, MMA extends MonadRec<Maybe<A>, M>> LastM<M, A, MMA> lastM() {
        return (LastM<M, A, MMA>) INSTANCE;
    }

    public static <A, M extends MonadRec<?, M>, MMA extends MonadRec<Maybe<A>, M>> MMA lastM(StreamT<M, A> as) {
        return $(lastM(), as);
    }
}

