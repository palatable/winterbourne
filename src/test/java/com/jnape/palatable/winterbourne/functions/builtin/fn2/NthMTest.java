package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.NthM.nthM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;

public class NthMTest {

    @Test
    public void nthFromEmpty() {
        assertEquals(new Identity<>(nothing()),
                     nthM(one(), empty(pureIdentity())));
    }

    @Test
    public void nthAfterEnd() {
        assertEquals(new Identity<>(Maybe.<Natural>nothing()),
                     nthM(atLeastOne(5), takeM(atLeastOne(3), naturalsM(pureIdentity()))));
    }

    @Test
    public void nthAtEnd() {
        assertEquals(new Identity<>(just("b")),
                     nthM(atLeastOne(1), streamT(new Identity<>(
                             strictQueue(just("a"), nothing(), nothing(), just("b"), nothing())))));
    }

    @Test
    public void nthWithinRange() {
        assertEquals(new Identity<>(natural(5)),
                     nthM(atLeastOne(5), takeM(atLeastOne(10), naturalsM(pureIdentity()))));
    }

    @Test
    public void largeNthFromInfinite() {
        assertEquals(new Identity<>(natural(STACK_EXPLODING_NUMBER)),
                     nthM(atLeastOne(STACK_EXPLODING_NUMBER), naturalsM(pureIdentity())));
    }
}
