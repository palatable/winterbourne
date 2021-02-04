package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.functions.builtin.fn2.GT.gt;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LT.lt;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LTE.lte;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.lambda.semigroup.builtin.Max.max;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeWhileM.takeWhileM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.isEmpty;
import static testsupport.matchers.IterateTMatcher.iterates;

public class TakeWhileMTest {

    @Test
    public void takeEmpty() {
        assertThat(takeWhileM(lt(5), empty(pureIdentity())), isEmpty());
    }

    @Test
    public void takeNothing() {
        assertThat(takeWhileM(gt(5), naturalNumbersM(pureIdentity())), isEmpty());
    }

    @Test
    public void takeSomeInfinite() {
        assertThat(takeWhileM(lte(5), naturalNumbersM(pureIdentity())), iterates(1, 2, 3, 4, 5));
    }

    @Test
    public void takeNothingIntermittent() {
        assertThat(takeWhileM(gt(5).diMapL(i -> i % 10), naturalNumbersM(pureIdentity())), isEmpty());
    }

    @Test
    public void takeSomeIntermittent() {
        assertThat(takeWhileM(lte(5).diMapL(i -> i % 10), naturalNumbersM(pureIdentity())), iterates(1, 2, 3, 4, 5));
    }

    @Test
    public void takeStackSafe() {
        assertEquals(new Identity<>(STACK_EXPLODING_NUMBER),
                     takeWhileM(lte(STACK_EXPLODING_NUMBER), naturalNumbersM(pureIdentity()))
                             .fold((a, i) -> new Identity<>(max(a, i)), new Identity<>(0)));
    }
}