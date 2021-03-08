package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Eq.eq;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Map.map;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.shoki.interop.Shoki.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CycleM.cycleM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EchoM.echoM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.MagnetizeByM.magnetizeByM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ZipM.zipM;

/**
 * Group the emissions of a {@link StreamT} by storing them in consecutive non-empty {@link StrictQueue StrictQueues}
 * containing at most <code>k</code> elements, where <code>k</code> is the group size. The resulting {@link StreamT}
 * will preserve the source {@link StreamT} elisions, as well as produce additional elisions during incremental group
 * construction, so as to avoid unnecessarily blocking while the internal groups are being assembled.
 *
 * @param <A> The {@link StreamT} element type
 * @param <M> The {@link StreamT} effect type
 */
public final class InGroupsOfM<M extends MonadRec<?, M>, A>
        implements Fn2<NonZero, StreamT<M, A>, StreamT<M, StrictQueue<A>>> {

    private static final InGroupsOfM<?, ?> INSTANCE = new InGroupsOfM<>();

    private InGroupsOfM() {
    }

    @Override
    public StreamT<M, StrictQueue<A>> checkedApply(NonZero n, StreamT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return magnetizeByM((t1, t2) -> eq(t1._1(), t2._1()),
                            zipM(echoM(n, cycleM(streamT(mUnit.pure(strictQueue(just(true), just(false)))))), as))
                .fmap(it -> strictQueue(map(Tuple2::_2, it)));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> InGroupsOfM<M, A> inGroupsOfM() {
        return (InGroupsOfM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, StrictQueue<A>>> inGroupsOfM(NonZero n) {
        return $(inGroupsOfM(), n);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, StrictQueue<A>> inGroupsOfM(NonZero n, StreamT<M, A> as) {
        return $(inGroupsOfM(n), as);
    }
}

