package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.NthM.nthM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;

public class NthMTest {

    @Test
    public void negativeNthIsNothing() {
        assertEquals(new Identity<>(Maybe.<Integer>nothing()), nthM(-5, naturalNumbersM(pureIdentity())));
    }

    @Test
    public void zerothIsNothing() {
        assertEquals(new Identity<>(Maybe.<Integer>nothing()), nthM(0, naturalNumbersM(pureIdentity())));
    }

    @Test
    public void nthFromEmpty() {
        assertEquals(new Identity<>(nothing()), nthM(1, empty(pureIdentity())));
    }

    @Test
    public void nthAfterEnd() {
        assertEquals(new Identity<>(Maybe.<Integer>nothing()), nthM(5, takeM(3, naturalNumbersM(pureIdentity()))));
    }

    @Test
    public void nthWithinRange() {
        assertEquals(new Identity<>(just(5)), nthM(5, takeM(10, naturalNumbersM(pureIdentity()))));
    }

    @Test
    public void largeNthFromInfinite() {
        assertEquals(new Identity<>(just(STACK_EXPLODING_NUMBER)),
                     nthM(STACK_EXPLODING_NUMBER, naturalNumbersM(pureIdentity())));
    }
}