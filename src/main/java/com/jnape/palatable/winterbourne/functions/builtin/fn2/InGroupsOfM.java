package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Eq.eq;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CycleM.cycleM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EchoM.echoM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.MagnetizeByM.magnetizeByM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ZipM.zipM;

/**
 * Emit the elements of a <code>{@link StreamT}&lt;M, A&gt;</code> in a {@link StreamT} of smaller {@link StreamT}s,
 * each of which will emit <code>n</code> elements, until the input {@link StreamT} has less than <code>n</code>
 * elements remaining and they are all emitted by the final {@link StreamT}.
 *
 * @param <A> The {@link StreamT} element type
 * @param <M> The {@link StreamT} effect type
 */
public final class InGroupsOfM<M extends MonadRec<?, M>, A>
        implements Fn2<NonZero, StreamT<M, A>, StreamT<M, StreamT<M, A>>> {

    private static final InGroupsOfM<?, ?> INSTANCE = new InGroupsOfM<>();

    private InGroupsOfM() {
    }

    @Override
    public StreamT<M, StreamT<M, A>> checkedApply(NonZero n, StreamT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return magnetizeByM((t1, t2) -> eq(t1._1(), t2._1()),
                            zipM(echoM(n, cycleM(streamT(mUnit.pure(strictQueue(just(true), just(false)))))), as))
                .fmap(it -> it.fmap(Tuple2::_2));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> InGroupsOfM<M, A> inGroupsOfM() {
        return (InGroupsOfM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, StreamT<M, A>>> inGroupsOfM(NonZero n) {
        return $(inGroupsOfM(), n);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, StreamT<M, A>> inGroupsOfM(NonZero n, StreamT<M, A> as) {
        return $(inGroupsOfM(n), as);
    }
}

