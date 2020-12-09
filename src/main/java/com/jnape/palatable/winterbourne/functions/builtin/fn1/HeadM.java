package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;

/**
 * Retrieve the head element of an {@link IterateT}, wrapped in an {@link Maybe}. If the {@link IterateT} is empty, the
 * result is {@link Maybe#nothing()}.
 *
 * @param <M>   the {@link IterateT} effect type
 * @param <A>   the {@link IterateT} element type
 * @param <MMA> the narrowed head result type
 */
public final class HeadM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn1<IterateT<M, A>, MMA> {

    private static final HeadM<?, ?, ?> INSTANCE = new HeadM<>();

    @Override
    public MMA checkedApply(IterateT<M, A> as) {
        return maybeT(as.runIterateT()).fmap(Tuple2::_1).runMaybeT();
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> HeadM<M, A, MMA> headM() {
        return (HeadM<M, A, MMA>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> MMA headM(IterateT<M, A> as) {
        return HeadM.<M, A, MMA>headM().apply(as);
    }
}
