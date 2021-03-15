package com.jnape.palatable.winterbourne.functions.builtin.fn4;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.semigroup.Semigroup;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;
import com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monoid.builtin.Present.present;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.impl.StrictStack.strictStack;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldCutM.gFoldCutM;
import static org.junit.Assert.assertEquals;

public class GFoldCutMTest {

    @Test
    public void emptyStream() {
        StreamT<Identity<?>, Natural> empty = empty(pureIdentity());
        StreamT<Identity<?>, Natural> nats = streamT(new Identity<>(strictStack(natural(1),
                                                                                nothing(),
                                                                                natural(2),
                                                                                nothing(),
                                                                                natural(3))));

        Fn1<StreamT<Identity<?>, Natural>, Identity<Maybe<Natural>>> runTerminateAsap =
                gFoldCutM(runStreamT(),
                          terminateAsap(pureIdentity()),
                          new Identity<>(just(abs(0))));

        Fn1<StreamT<Identity<?>, Natural>, Identity<Maybe<Natural>>> runRecurse =
                gFoldCutM(runStreamT(),
                          recurseWith(present(Natural::plus), pureIdentity()),
                          new Identity<>(just(abs(0))));

        Fn1<StreamT<Identity<?>, Natural>, Identity<Natural>> awaitTerminateAsap =
                gFoldCutM(awaitM(),
                          terminateAsap(pureIdentity()),
                          new Identity<>(abs(0)));

        Fn1<StreamT<Identity<?>, Natural>, Identity<Natural>> awaitRecurse =
                gFoldCutM(awaitM(),
                          recurseWith(Natural::plus, pureIdentity()),
                          new Identity<>(abs(0)));

        assertEquals(new Identity<>(just(abs(0))), runRecurse.apply(empty));
        assertEquals(new Identity<>(just(abs(6))), runRecurse.apply(nats));

        assertEquals(new Identity<>(just(abs(0))), runTerminateAsap.apply(empty));
        assertEquals(new Identity<>(just(abs(1))), runTerminateAsap.apply(nats));

        assertEquals(new Identity<>(abs(0)), awaitRecurse.apply(empty));
        assertEquals(new Identity<>(abs(6)), awaitRecurse.apply(nats));

        assertEquals(new Identity<>(abs(0)), awaitTerminateAsap.apply(empty));
        assertEquals(new Identity<>(abs(1)), awaitTerminateAsap.apply(nats));
    }

    private static <M extends MonadRec<?, M>, A> Fn2<A, A, MonadRec<RecursiveResult<A, A>, M>> terminateAsap(
            Pure<M> pureM) {
        return (x, y) -> pureM.apply(RecursiveResult.<A, A>terminate(y));
    }

    private static <M extends MonadRec<?, M>, A> Fn2<A, A, MonadRec<RecursiveResult<A, A>, M>> recurseWith(
            Semigroup<A> sg, Pure<M> pureM) {
        return (x, y) -> pureM.apply(recurse(sg.apply(x, y)));
    }

    private static <M extends MonadRec<?, M>, A>
    Fn1<StreamT<M, A>, MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>> runStreamT() {
        return StreamT::<MonadRec<Maybe<Tuple2<Maybe<A>, StreamT<M, A>>>, M>>runStreamT;
    }

}