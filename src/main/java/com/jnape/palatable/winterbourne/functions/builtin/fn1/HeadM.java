package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;

/**
 * Retrieve the first emitted element of a {@link StreamT}, wrapped in {@link Maybe} and in the effect <code>M</code>.
 * If the {@link StreamT} does not emit any elements, {@link Maybe#nothing()} wrapped in the effect <code>M</code>.
 *
 * @param <M>   the {@link StreamT} effect type
 * @param <A>   the {@link StreamT} element type
 * @param <MMA> the narrowed head result type
 */
public final class HeadM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn1<StreamT<M, A>, MMA> {

    private static final HeadM<?, ?, ?> INSTANCE = new HeadM<>();

    private HeadM() {
    }

    @Override
    public MMA checkedApply(StreamT<M, A> as) {
        return awaitM(as).fmap(m -> m.fmap(Tuple2::_1)).coerce();
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> HeadM<M, A, MMA> headM() {
        return (HeadM<M, A, MMA>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> MMA headM(StreamT<M, A> as) {
        return $(headM(), as);
    }
}
