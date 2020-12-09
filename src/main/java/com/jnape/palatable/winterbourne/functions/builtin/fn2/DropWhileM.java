package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.suspended;

/**
 * Limit the {@link IterateT} by skipping the first contiguous group of elements that satisfy the predicate,
 * beginning iteration at the first element for which the predicate evaluates to <code>false</code>.
 *
 * @param <M> the {@link IterateT} effect type
 * @param <A> The {@link IterateT} element type
 */
public final class DropWhileM<M extends MonadRec<?, M>, A>
        implements Fn2<Fn1<? super A, ? extends Boolean>, IterateT<M, A>, IterateT<M, A>> {

    private static final DropWhileM<?, ?> INSTANCE = new DropWhileM<>();

    @Override
    public IterateT<M, A> checkedApply(Fn1<? super A, ? extends Boolean> predicate,
                                       IterateT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<A, IterateT<M, A>>>, M> unwrapped = as.runIterateT();
        return suspended(
                () -> unwrapped.trampolineM(mta -> mta
                        .match(constantly(unwrapped.pure(terminate(nothing()))),
                               into((A h, IterateT<M, A> t) ->
                                            predicate.apply(h) ? t.runIterateT().fmap(RecursiveResult::recurse)
                                                               : unwrapped.pure(terminate(mta))))),
                Pure.of(unwrapped));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> DropWhileM<M, A> dropWhileM() {
        return (DropWhileM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<IterateT<M, A>, IterateT<M, A>> dropWhileM(
            Fn1<? super A, ? extends Boolean> predicate) {
        return DropWhileM.<M, A>dropWhileM().apply(predicate);
    }

    public static <M extends MonadRec<?, M>, A> IterateT<M, A> dropWhileM(Fn1<? super A, ? extends Boolean> predicate,
                                                                          IterateT<M, A> as) {
        return DropWhileM.<M, A>dropWhileM(predicate).apply(as);
    }
}
