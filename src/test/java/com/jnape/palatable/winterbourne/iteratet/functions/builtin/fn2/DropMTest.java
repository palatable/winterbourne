package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.isEmpty;
import static testsupport.matchers.IterateTMatcher.iterates;

public class DropMTest {

    @Test
    public void dropEmpty() {
        assertThat(dropM(5, empty(pureIdentity())), isEmpty());
    }

    @Test
    public void dropNonEmpty() {
        assertThat(dropM(2, takeM(1, naturalNumbersM(pureIdentity()))), isEmpty());
        assertThat(dropM(2, takeM(2, naturalNumbersM(pureIdentity()))), isEmpty());
        assertThat(dropM(2, takeM(3, naturalNumbersM(pureIdentity()))), iterates(3));
    }

    @Test
    public void dropStackSafe() {
        assertEquals(new Identity<>(just(STACK_EXPLODING_NUMBER + 1)),
                     headM(dropM(STACK_EXPLODING_NUMBER, naturalNumbersM(pureIdentity()))));
    }
}