package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.adt.hlist.Tuple3;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.MagnetizeBy;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into3.into3;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

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
        implements Fn2<Fn2<? super A, ? super A, Boolean>, StreamT<M, A>, StreamT<M, StreamT<M, A>>> {

    private static final MagnetizeByM<?, ?> INSTANCE = new MagnetizeByM<>();

    private MagnetizeByM() {
    }

    @Override
    public StreamT<M, StreamT<M, A>> checkedApply(Fn2<? super A, ? super A, Boolean> predicate, StreamT<M, A> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return streamT(
                () -> as.awaitStreamT()
                        .fmap(m -> m.fmap(into((pivot, ys) -> tuple(pivot, as.pure(pivot), ys))))
                        .trampolineM(m -> m.match(
                                __ -> mUnit.pure(terminate(nothing())),
                                into3((pivot, group, ys) -> ys.awaitStreamT().<RecursiveResult<Maybe<Tuple3<A, StreamT<M, A>, StreamT<M, A>>>, Maybe<Tuple2<Maybe<StreamT<M, A>>, StreamT<M, A>>>>>fmap(m2 -> m2.match(
                                        __ -> terminate(just(tuple(just(group), empty(Pure.of(mUnit))))),
                                        into((y, tail) ->
                                                     predicate.apply(pivot, y)
                                                     ? recurse(just(tuple(y, group.concat(as.pure(y)), tail)))
                                                     : terminate(just(tuple(just(group),
                                                                            as.pure(y).concat(tail))))))))))
                        .fmap(m -> m.fmap(t -> t.fmap(magnetizeByM(predicate)))),
                Pure.of(mUnit));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> MagnetizeByM<M, A> magnetizeByM() {
        return (MagnetizeByM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, StreamT<M, A>>> magnetizeByM(
            Fn2<? super A, ? super A, Boolean> predicate) {
        return $(magnetizeByM(), predicate);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, StreamT<M, A>> magnetizeByM(
            Fn2<? super A, ? super A, Boolean> predicate, StreamT<M, A> as) {
        return $(magnetizeByM(predicate), as);
    }
}
