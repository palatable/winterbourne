package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.FoldM.foldM;
import static org.junit.Assert.assertEquals;

public class FoldMTest {

    @Test
    public void foldOperatesOnSkippedAndEmittedValues() {
        Fn1<StreamT<Identity<?>, Integer>, Identity<StrictQueue<Maybe<Integer>>>> foldM = foldM(
                (as, maybeA) -> new Identity<>(as.snoc(maybeA)),
                new Identity<>(strictQueue()));

        assertEquals(new Identity<>(strictQueue()),
                     foldM.apply(empty(pureIdentity())));

        assertEquals(new Identity<>(strictQueue(nothing())),
                     foldM.apply(streamT(new Identity<>(nothing()))));

        assertEquals(new Identity<>(strictQueue(just(1), just(2), just(3))),
                     foldM.apply(streamT(new Identity<>(strictQueue(just(1), just(2), just(3))))));

        assertEquals(new Identity<>(strictQueue(just(1), nothing(), just(3))),
                     foldM.apply(streamT(new Identity<>(strictQueue(just(1), nothing(), just(3))))));
    }
}