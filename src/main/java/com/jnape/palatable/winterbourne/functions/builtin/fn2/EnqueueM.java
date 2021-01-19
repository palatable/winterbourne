package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.api.Queue;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;

/**
 * Given an {@link Fn0} of some <code>{@link Queue}&lt;{@link Natural}, A&gt;</code>, create an instance of this Queue
 * and snoc the elements emitted by the provided <code>{@link StreamT}</code> to the instance. Note that instances of
 *
 * @param <A>   the iterable element type
 * @param <M>   the effect type
 * @param <MQA> the narrowed enqueued return type
 */
public final class EnqueueM<M extends MonadRec<?, M>, A, MQA extends MonadRec<Queue<Natural, A>, M>>
        implements Fn2<Fn0<? extends Queue<Natural, A>>, StreamT<M, A>, MQA> {

    private static final EnqueueM<?, ?, ?> INSTANCE = new EnqueueM<>();

    private EnqueueM() {
    }

    @Override
    public MQA checkedApply(Fn0<? extends Queue<Natural, A>> qFn0, StreamT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return as.foldAwait((c, a) -> mUnit.pure(c.snoc(a)),
                            mUnit.pure(qFn0.apply()))
                 .coerce();
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, MQA extends MonadRec<Queue<Natural, A>, M>> EnqueueM<M, A, MQA>
    enqueueM() {
        return (EnqueueM<M, A, MQA>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, MQA extends MonadRec<Queue<Natural, A>, M>> Fn1<StreamT<M, A>, MQA>
    enqueueM(Fn0<? extends Queue<Natural, A>> qFn0) {
        return $(enqueueM(), qFn0);
    }

    public static <M extends MonadRec<?, M>, A, MQA extends MonadRec<Queue<Natural, A>, M>> MQA enqueueM(
            Fn0<? extends Queue<Natural, A>> qFn0, StreamT<M, A> as) {
        return $(enqueueM(qFn0), as);
    }
}
