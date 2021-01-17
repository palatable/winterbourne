package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Not.not;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Limit a <code>{@link StreamT}&lt;M, A&gt;</code> by skipping the first contiguous group of emitted elements that
 * satisfy the predicate, only emitting elements that occur after this group, beginning with the first element for
 * which the predicate evaluates to <code>false</code>.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> The {@link StreamT} element type
 */
public final class DropWhileM<M extends MonadRec<?, M>, A>
        implements Fn2<Fn1<? super A, Boolean>, StreamT<M, A>, StreamT<M, A>> {

    private static final DropWhileM<?, ?> INSTANCE = new DropWhileM<>();

    private DropWhileM() {
    }

    @Override
    public StreamT<M, A> checkedApply(Fn1<? super A, Boolean> predicate, StreamT<M, A> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return streamT(
                () -> as.runStreamT().fmap(m -> m.fmap(into(
                        (mHead, tail) -> mHead
                                .filter(not(predicate))
                                .match(constantly(tuple(nothing(), dropWhileM(predicate, tail))),
                                       constantly(tuple(mHead, tail)))))),
                Pure.of(mUnit));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> DropWhileM<M, A> dropWhileM() {
        return (DropWhileM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>>
    dropWhileM(Fn1<? super A, Boolean> predicate) {
        return $(dropWhileM(), predicate);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A>
    dropWhileM(Fn1<? super A, Boolean> predicate, StreamT<M, A> as) {
        return $(dropWhileM(predicate), as);
    }
}
