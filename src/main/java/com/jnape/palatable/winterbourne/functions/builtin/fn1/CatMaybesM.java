package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Given a <code>{@link StreamT}&lt;M, {@link Maybe}&lt;A&gt;&gt;</code>, return a
 * <code>{@link StreamT}&lt;M, A&gt;</code> which emits the present values and skips the absent ones.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> the {@link Maybe} element type, as well as the resulting {@link StreamT} element type
 */
public final class CatMaybesM<M extends MonadRec<?, M>, A> implements Fn1<StreamT<M, Maybe<A>>, StreamT<M, A>> {

    private static final CatMaybesM<?, ?> INSTANCE = new CatMaybesM<>();

    private CatMaybesM() {
    }

    @Override
    public StreamT<M, A> checkedApply(StreamT<M, Maybe<A>> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return streamT(() -> as.runStreamT().fmap(m -> m.fmap(t -> t.biMap(Monad::join, catMaybesM()))),
                       Pure.of(mUnit));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> CatMaybesM<M, A> catMaybesM() {
        return (CatMaybesM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, A> catMaybesM(StreamT<M, Maybe<A>> as) {
        return $(catMaybesM(), as);
    }
}
