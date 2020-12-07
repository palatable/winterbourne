package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.winterbourne.functions.builtin.fn1.LastM.lastM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;

/**
 * Retrieve the element effect of an {@link IterateT} at ordinal index <code>k</code>. If the <code>k</code> is less
 * than or equal to the size of the {@link IterateT}, the result is a {@link Monad#pure(Object) pure} effect of
 * {@link Maybe#nothing()}.
 *
 * @param <A> the {@link IterateT} element type
 * @param <M> the {@link IterateT} effect type
 */
public class NthM<M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>>
        implements Fn2<Integer, IterateT<M, A>, MMA> {

    private static final NthM<?, ?, ?> INSTANCE = new NthM<>();

    private NthM() {
    }

    @Override
    public MMA checkedApply(Integer k, IterateT<M, A> mas) throws Throwable {
        return lastM(dropM(k - 1, takeM(k, mas)));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> NthM<M, A, MMA> nthM() {
        return (NthM<M, A, MMA>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> Fn1<IterateT<M, A>, MMA> nthM(
            Integer k) {
        return NthM.<M, A, MMA>nthM().apply(k);
    }

    public static <M extends MonadRec<?, M>, A, MMA extends MonadRec<Maybe<A>, M>> MMA nthM(Integer k,
                                                                                            IterateT<M, A> mas) {
        return NthM.<M, A, MMA>nthM(k).apply(mas);
    }
}
