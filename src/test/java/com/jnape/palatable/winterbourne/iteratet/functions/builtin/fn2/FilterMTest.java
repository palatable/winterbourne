package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GT.gt;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LT.lt;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.lambda.semigroup.builtin.Max.max;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.FilterM.filterM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.isEmpty;
import static testsupport.matchers.IterateTMatcher.iterates;

public class FilterMTest {

    @Test
    public void filterEmpty() {
        assertThat(filterM(constantly(true), empty(pureIdentity())), isEmpty());
    }

    @Test
    public void filterNothing() {
        assertThat(filterM(constantly(true), takeM(5, naturalNumbersM(pureIdentity()))),
                   iterates(1, 2, 3, 4, 5));
    }

    @Test
    public void filterAll() {
        assertThat(filterM(constantly(false), takeM(5, naturalNumbersM(pureIdentity()))), isEmpty());
    }

    @Test
    public void filterSome() {
        assertThat(filterM(gt(3), takeM(5, naturalNumbersM(pureIdentity()))), iterates(4, 5));
        assertThat(filterM(lt(3), takeM(5, naturalNumbersM(pureIdentity()))), iterates(1, 2));
    }

    @Test
    public void filterSomeIntermittent() {
        assertThat(filterM(i -> i % 2 == 0, takeM(10, naturalNumbersM(pureIdentity()))),
                   iterates(2, 4, 6, 8, 10));
        assertThat(filterM(i -> i % 2 == 1, takeM(10, naturalNumbersM(pureIdentity()))),
                   iterates(1, 3, 5, 7, 9));
    }

    @Test
    public void filterStackSafe() {
        assertEquals(new Identity<>(STACK_EXPLODING_NUMBER),
                     filterM(constantly(true), takeM(STACK_EXPLODING_NUMBER, naturalNumbersM(pureIdentity())))
                             .fold((a, i) -> new Identity<>(max(a, i)), new Identity<>(0)));
    }
}