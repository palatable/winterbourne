package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.singleton;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.LastM.lastM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;

public class LastMTest {

    @Test
    public void lastEmpty() {
        assertEquals(new Identity<>(nothing()), lastM(empty(pureIdentity())));
    }

    @Test
    public void lastNonEmpty() {
        assertEquals(new Identity<>(just(0)), lastM(singleton(new Identity<>(0))));
        assertEquals(new Identity<>(just(STACK_EXPLODING_NUMBER)),
                     lastM(takeM(STACK_EXPLODING_NUMBER, naturalNumbersM(pureIdentity()))));
    }
}