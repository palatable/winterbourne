package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static org.junit.Assert.assertEquals;

public class HeadMTest {

    @Test
    public void headEmpty() {
        assertEquals(new Identity<>(nothing()), headM(empty(pureIdentity())));
    }

    @Test
    public void headNonEmpty() {
        assertEquals(new Identity<>(just(1)), headM(naturalNumbersM(pureIdentity())));
    }
}