package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import org.junit.Test;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.iterates;

public class NaturalNumbersMTest {

    @Test
    public void producesTheNats() {
        assertThat(takeM(10, naturalNumbersM(pureIdentity())), iterates(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void producesNatsForever() {
        assertThat(dropM(STACK_EXPLODING_NUMBER, takeM(STACK_EXPLODING_NUMBER + 10, naturalNumbersM(pureIdentity()))),
                   iterates(STACK_EXPLODING_NUMBER + 1,
                            STACK_EXPLODING_NUMBER + 2,
                            STACK_EXPLODING_NUMBER + 3,
                            STACK_EXPLODING_NUMBER + 4,
                            STACK_EXPLODING_NUMBER + 5,
                            STACK_EXPLODING_NUMBER + 6,
                            STACK_EXPLODING_NUMBER + 7,
                            STACK_EXPLODING_NUMBER + 8,
                            STACK_EXPLODING_NUMBER + 9,
                            STACK_EXPLODING_NUMBER + 10));
    }
}