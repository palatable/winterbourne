package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Prepend each emitted element of a <code>{@link StreamT}&lt;M, A&gt;</code> with the supplied value. An empty
 * <code>StreamT</code> is left untouched.
 *
 * @param <M> the <code>StreamT</code> effect type
 * @param <A> the <code>StreamT</code> element type
 */
public final class PrependAllM<M extends MonadRec<?, M>, A> implements Fn2<A, StreamT<M, A>, StreamT<M, A>> {

    private static final PrependAllM<?, ?> INSTANCE = new PrependAllM<>();

    private PrependAllM() {
    }

    @Override
    public StreamT<M, A> checkedApply(A prefix, StreamT<M, A> as) throws Throwable {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return streamT(() -> as.runStreamT().fmap(m -> m.fmap(into((mHead, tail) -> mHead
                               .match(__ -> tuple(mHead, prependAllM(prefix, tail)),
                                      a -> tuple(just(prefix), tail.pure(a).concat(prependAllM(prefix, tail))))))),
                       Pure.of(mUnit));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> PrependAllM<M, A> prependAllM() {
        return (PrependAllM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> Fn1<StreamT<M, A>, StreamT<M, A>> prependAllM(A prefix) {
        return $(prependAllM(), prefix);
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> prependAllM(A prefix, StreamT<M, A> as) {
        return $(prependAllM(prefix), as);
    }
}
