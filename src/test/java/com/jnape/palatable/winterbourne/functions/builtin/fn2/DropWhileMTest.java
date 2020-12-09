package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LT.lt;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LTE.lte;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropWhileM.dropWhileM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.isEmpty;
import static testsupport.matchers.IterateTMatcher.iterates;

public class DropWhileMTest {

    @Test
    public void dropEmpty() {
        assertThat(dropWhileM(constantly(true), empty(pureIdentity())),
                   isEmpty());
    }

    @Test
    public void dropAll() {
        assertThat(dropWhileM(lt(100), takeM(10, naturalNumbersM(pureIdentity()))), isEmpty());
    }

    @Test
    public void dropSome() {
        assertThat(dropWhileM(lte(5), takeM(10, naturalNumbersM(pureIdentity()))),
                   iterates(6, 7, 8, 9, 10));
    }

    @Test
    public void dropSomeIntermittent() {
        assertThat(dropWhileM(lte(5).diMapL(i -> i % 7), takeM(10, naturalNumbersM(pureIdentity()))),
                   iterates(6, 7, 8, 9, 10));
    }

    @Test
    public void dropStackSafe() {
        assertEquals(new Identity<>(just(STACK_EXPLODING_NUMBER + 1)),
                     headM(dropWhileM(lte(STACK_EXPLODING_NUMBER), naturalNumbersM(pureIdentity()))));
    }
}