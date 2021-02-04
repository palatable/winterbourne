package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GTE.gte;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.FindM.findM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;

public class FindMTest {

    @Test
    public void findsSomething() {
        assertEquals(new Identity<>(just(4)), findM(gte(4), takeM(10, naturalNumbersM(pureIdentity()))));
    }

    @Test
    public void findsNothingWhenEmpty() {
        assertEquals(new Identity<>(nothing()), findM(constantly(true), empty(pureIdentity())));
    }

    @Test
    public void findsNothingWhenNothingMatches() {
        assertEquals(new Identity<>(Maybe.<Integer>nothing()),
                     findM(gte(14), takeM(10, naturalNumbersM(pureIdentity()))));
    }

    @Test
    public void findsANeedleInAHaystack() {
        assertEquals(new Identity<>(just(STACK_EXPLODING_NUMBER)),
                     findM(gte(STACK_EXPLODING_NUMBER), naturalNumbersM(pureIdentity())));
    }
}