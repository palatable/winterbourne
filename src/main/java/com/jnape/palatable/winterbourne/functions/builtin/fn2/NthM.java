package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;

/**
 * Retrieve the element effect of an {@link StreamT} at offset index <code>n</code>. If the offset index <code>n</code>
 * is greater than or equal to the size of the {@link StreamT}, the result is a {@link Monad#pure(Object) pure} effect
 * of {@link Maybe#nothing()}.
 *
 * @param <A>   the {@link StreamT} element type
 * @param <M>   the {@link StreamT} effect type
 * @param <MMA> the narrowed nth result type
 */
public final class NthM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn2<NonZero, StreamT<M, A>, MMA> {

    private static final NthM<?, ?, ?> INSTANCE = new NthM<>();

    private NthM() {
    }

    @Override
    public MMA checkedApply(NonZero n, StreamT<M, A> as) {
        return headM(dropM(n, as));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> NthM<M, A, MMA> nthM() {
        return (NthM<M, A, MMA>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> Fn1<StreamT<M, A>, MMA>
    nthM(NonZero n) {
        return $(nthM(), n);
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> MMA
    nthM(NonZero n, StreamT<M, A> as) {
        return $(nthM(n), as);
    }
}
