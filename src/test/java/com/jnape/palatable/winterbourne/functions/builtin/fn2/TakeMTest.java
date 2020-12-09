package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.of;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.isEmpty;
import static testsupport.matchers.IterateTMatcher.iteratesAll;

public class TakeMTest {

    @Test
    public void takeEmpty() {
        assertThat(takeM(5, empty(pureIdentity())), isEmpty());
    }

    @Test
    public void takeAllShort() {
        assertThat(takeM(5, of(new Identity<>(1), new Identity<>(2), new Identity<>(3))),
                   iteratesAll(asList(1, 2, 3)));
    }

    @Test
    public void takeAll() {
        assertThat(takeM(5, of(new Identity<>(1), new Identity<>(2), new Identity<>(3),
                               new Identity<>(4), new Identity<>(5))),
                   iteratesAll(asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void takeSome() {
        assertThat(takeM(5, of(new Identity<>(1), new Identity<>(2), new Identity<>(3),
                               new Identity<>(4), new Identity<>(5), new Identity<>(6))),
                   iteratesAll(asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void takeSomeInfinite() {
        assertThat(takeM(5, naturalNumbersM(pureIdentity())), iteratesAll(asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void takeStackSafe() {
        assertEquals(new Identity<>((STACK_EXPLODING_NUMBER + 1) * (STACK_EXPLODING_NUMBER / 2)),
                     takeM(STACK_EXPLODING_NUMBER, naturalNumbersM(pureIdentity()))
                             .fold((a, i) -> new Identity<>(a + i), new Identity<>(0)));
    }
}