package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

/**
 * Given a <code>{@link StreamT}&lt;M, A&gt;</code>, a starting value <code>B</code> wrapped in an effect, and a
 * <code>{@link Fn2}&lt;B, A, MonadRec&lt;B, M&gt;&gt;</code>, iteratively accumulate over the {@link StreamT},
 * returning a {@link StreamT} of all of the intermediate accumulator values <code>B</code>. Note that, as the
 * name implies, this function accumulates from left to right, such that with a {@link StreamT} <code>s</code>
 * emitting [1,2,3,4,5], <code>scanLeftM(f, M 0, s)</code> produces a <code>StreamT</code> which will emit <code>0,
 * f(0, 1), f(f(0, 1), 2), f(f(f(0, 1), 2), 3), f(f(f(f(0, 1), 2), 3), 4), f(f(f(f(f(0, 1), 2), 3), 4), 5)</code>.
 *
 * @param <M> The <code>StreamT</code>> effect type
 * @param <A> The <code>StreamT</code>> element type
 * @param <B> The accumulation type
 */
public final class ScanLeftM<M extends MonadRec<?, M>, A, B>
        implements Fn3<Fn2<B, A, MonadRec<B, M>>, MonadRec<B, M>, StreamT<M, A>, StreamT<M, B>> {

    private static final ScanLeftM<?, ?, ?> INSTANCE = new ScanLeftM<>();

    private ScanLeftM() {
    }

    @Override
    public StreamT<M, B> checkedApply(Fn2<B, A, MonadRec<B, M>> fn, MonadRec<B, M> mb, StreamT<M, A> as) {
        return streamT(() -> as.runStreamT().flatMap(m -> m.match(
                __ -> mb.fmap(b -> just(tuple(just(b), empty(Pure.of(mb))))),
                into((mHead, tail) -> mHead.match(
                        __ -> mb.pure(just(tuple(nothing(), scanLeftM(fn, mb, tail)))),
                        a -> mb.fmap(b -> just(tuple(just(b), scanLeftM(fn, fn.apply(b, a), tail)))))))),
                       Pure.of(mb));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, B> ScanLeftM<M, A, B> scanLeftM() {
        return (ScanLeftM<M, A, B>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, B> Fn2<MonadRec<B, M>, StreamT<M, A>, StreamT<M, B>> scanLeftM(
            Fn2<B, A, MonadRec<B, M>> fn) {
        return ScanLeftM.<M, A, B>scanLeftM().apply(fn);
    }

    public static <M extends MonadRec<?, M>, A, B> Fn1<StreamT<M, A>, StreamT<M, B>> scanLeftM(
            Fn2<B, A, MonadRec<B, M>> fn, MonadRec<B, M> mb) {
        return $(scanLeftM(fn), mb);
    }

    public static <M extends MonadRec<?, M>, A, B> StreamT<M, B> scanLeftM(
            Fn2<B, A, MonadRec<B, M>> fn, MonadRec<B, M> mb, StreamT<M, A> as) {
        return $(scanLeftM(fn, mb), as);
    }
}
