package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ZipM.zipM;

/**
 * Given a <code>{@link StreamT}&lt;M, A&gt;</code>, pair each emitted element with its offset index.
 *
 * @param <M> the {@link StreamT} effect type
 * @param <A> the {@link StreamT} element type
 */
public final class IndexM<M extends MonadRec<?, M>, A> implements Fn1<StreamT<M, A>, StreamT<M, Tuple2<Natural, A>>> {

    private static final IndexM<?, ?> INSTANCE = new IndexM<>();

    private IndexM() {
    }

    @Override
    public StreamT<M, Tuple2<Natural, A>> checkedApply(StreamT<M, A> as) {
        MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M> mUnit = as.pure(UNIT).runStreamT();
        return zipM(naturalsM(Pure.of(mUnit)), as);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A> IndexM<M, A> indexM() {
        return (IndexM<M, A>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A> StreamT<M, Tuple2<Natural, A>> indexM(StreamT<M, A> as) {
        return $(indexM(), as);
    }
}
