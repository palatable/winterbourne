package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.HList;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.MagnetizeBy;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into3.into3;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.UnfoldM.unfoldM;

/**
 * Given a binary predicate and an <code>{@link StreamT}&lt;M, A&gt;</code>, return an <code>{@link StreamT}&lt;M,
 * {@link  StreamT}&lt;M, A&gt;&gt;</code> of the contiguous groups of elements that match the predicate pairwise.
 * <p>
 * See <code>{@link MagnetizeBy}</code> for an example using <code>Iterable</code>
 *
 * @param <A> the {@link StreamT} element type
 * @param <M> the {@link StreamT} effect type
 */
public final class MagnetizeByM<M extends MonadRec<?, M>, A>
        implements Fn2<Fn2<? super A, ? super A, Boolean>, StreamT<M, A>, StreamT<M, StrictQueue<A>>> {

    private static final MagnetizeByM<?, ?> INSTANCE = new MagnetizeByM<>();

    private MagnetizeByM() {
    }

    @Override
    public StreamT<M, StrictQueue<A>> checkedApply(Fn2<? super A, ? super A, Boolean> predicate, StreamT<M, A> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return unfoldM(into3((pred, queued, rest) -> rest.runStreamT().fmap(maybeMore -> maybeMore.match(
                __ -> queued.isEmpty()
                      ? nothing()
                      : just(tuple(just(queued), tuple(constantly(false), strictQueue(), empty(mUnit::pure)))),
                into((maybeA, tail) -> just(maybeA.match(
                        __ -> tuple(nothing(), tuple(pred, queued, tail)),
                        a -> (pred.apply(a)
                              ? tuple(Maybe.<StrictQueue<A>>nothing(), queued.snoc(a))
                              : tuple(just(queued), strictQueue(a)))
                                .fmap(queued_ -> tuple(predicate.apply(a), queued_, tail)))))
        ))), mUnit.pure(HList.<Fn1<? super A, Boolean>, StrictQueue<A>, StreamT<M, A>>tuple(
                constantly(true), strictQueue(), as)));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> MagnetizeByM<M, A> magnetizeByM() {
        return (MagnetizeByM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, StrictQueue<A>>> magnetizeByM(
            Fn2<? super A, ? super A, Boolean> predicate) {
        return $(magnetizeByM(), predicate);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, StrictQueue<A>> magnetizeByM(
            Fn2<? super A, ? super A, Boolean> predicate, StreamT<M, A> as) {
        return $(magnetizeByM(predicate), as);
    }
}
