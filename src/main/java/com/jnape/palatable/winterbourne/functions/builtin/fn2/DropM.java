package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.iterateT;
import static com.jnape.palatable.lambda.monad.transformer.builtin.MaybeT.maybeT;

/**
 * Skip the first <code>n</code> elements of an {@link IterateT} by returning an {@link IterateT} that begins iteration
 * after the <code>nth</code> element. If <code>n</code> is greater than or equal to the length of the {@link IterateT},
 * an empty {@link IterateT} is returned.
 *
 * @param <A> The {@link IterateT} element type
 * @param <M> the {@link IterateT} effect type
 * @see DropWhileM
 * @see TakeM
 */
public final class DropM<A, M extends MonadRec<?, M>> implements Fn2<Integer, IterateT<M, A>, IterateT<M, A>> {

    private static final DropM<?, ?> INSTANCE = new DropM<>();

    @Override
    public IterateT<M, A> checkedApply(Integer n, IterateT<M, A> mas) throws Throwable {
        MonadRec<Maybe<Tuple2<A, IterateT<M, A>>>, M> headM = mas.runIterateT();
        return iterateT(maybeT(headM.pure(just(tuple(n, iterateT(headM)))))
                                .trampolineM(into((k, as) -> k == 0
                                                             ? maybeT(headM.pure(just(terminate(as))))
                                                             : maybeT(as.runIterateT())
                                                                     .fmap(Tuple2::_2)
                                                                     .fmap(tupler(k - 1))
                                                                     .fmap(RecursiveResult::recurse)))
                                .flatMap(it -> maybeT(it.runIterateT()))
                                .runMaybeT());
    }

    @SuppressWarnings("unchecked")
    public static <A, M extends MonadRec<?, M>> DropM<A, M> dropM() {
        return (DropM<A, M>) INSTANCE;
    }

    public static <A, M extends MonadRec<?, M>> Fn1<IterateT<M, A>, IterateT<M, A>> dropM(Integer n) {
        return DropM.<A, M>dropM().apply(n);
    }

    public static <A, M extends MonadRec<?, M>> IterateT<M, A> dropM(Integer n, IterateT<M, A> as) {
        return DropM.<A, M>dropM(n).apply(as);
    }
}
