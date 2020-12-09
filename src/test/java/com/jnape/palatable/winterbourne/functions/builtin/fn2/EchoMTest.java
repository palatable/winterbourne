package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import org.junit.Test;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EchoM.echoM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.iterates;

public class EchoMTest {

    @Test
    public void echoesEachElement() {
        assertThat(takeM(9, echoM(3, naturalNumbersM(pureIdentity()))),
                   iterates(1, 1, 1, 2, 2, 2, 3, 3, 3));
    }

    @Test
    public void echoesEachElementForever() {
        assertThat(takeM(3, dropM(3 * (STACK_EXPLODING_NUMBER - 1), echoM(3, naturalNumbersM(pureIdentity())))),
                   iterates(STACK_EXPLODING_NUMBER, STACK_EXPLODING_NUMBER, STACK_EXPLODING_NUMBER));
    }
}