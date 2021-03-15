package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.Fn3;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.api.Collection;
import com.jnape.palatable.winterbourne.StreamT;
import com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldCutM.gFoldCutM;

/**
 * Given an {@link Fn0} of some <code>{@link Collection}&lt;?, A&gt;</code> and a way to add to it, create an instance
 * of the {@link Collection} and add the elements emitted by the provided <code>{@link StreamT}</code> to the instance.
 *
 * @param <A>  the {@link StreamT} element type and resulting {@link Collection} element type
 * @param <M>  the effect type
 * @param <C>  the {@link Collection} type
 * @param <MC> the narrowed return type
 */
public final class DrainM<M extends MonadRec<?, M>, A, C extends Collection<?, A>, MC extends MonadRec<C, M>>
        implements Fn3<Fn0<? extends C>, Fn2<? super C, ? super A, ? extends C>, StreamT<M, A>, MC> {

    private static final DrainM<?, ?, ?, ?> INSTANCE = new DrainM<>();

    private DrainM() {
    }

    @Override
    public MC checkedApply(Fn0<? extends C> newC, Fn2<? super C, ? super A, ? extends C> add, StreamT<M, A> as) {
        Pure<M> pureM = Pure.of(as.pure(UNIT).<MonadRec<Maybe<Tuple2<Maybe<Unit>, StreamT<M, Unit>>>, M>>runStreamT());
        return gFoldCutM(awaitM(), (c, a) -> pureM.apply(recurse(add.apply(c, a))), pureM.apply(newC.apply()), as);
    }

    @SuppressWarnings("unchecked")
    public static <M extends MonadRec<?, M>, A, C extends Collection<?, A>, MC extends MonadRec<C, M>>
    DrainM<M, A, C, MC> drainM() {
        return (DrainM<M, A, C, MC>) INSTANCE;
    }

    public static <M extends MonadRec<?, M>, A, C extends Collection<?, A>, MC extends MonadRec<C, M>>
    Fn2<Fn2<? super C, ? super A, ? extends C>, StreamT<M, A>, MC> drainM(Fn0<? extends C> newC) {
        return DrainM.<M, A, C, MC>drainM().apply(newC);
    }

    public static <M extends MonadRec<?, M>, A, C extends Collection<?, A>, MC extends MonadRec<C, M>>
    Fn1<StreamT<M, A>, MC> drainM(Fn0<? extends C> newC, Fn2<? super C, ? super A, ? extends C> add) {
        return $(drainM(newC), add);
    }

    public static <M extends MonadRec<?, M>, A, C extends Collection<?, A>, MC extends MonadRec<C, M>> MC drainM(
            Fn0<? extends C> newC, Fn2<? super C, ? super A, ? extends C> add, StreamT<M, A> as) {
        return $(drainM(newC, add), as);
    }
}
