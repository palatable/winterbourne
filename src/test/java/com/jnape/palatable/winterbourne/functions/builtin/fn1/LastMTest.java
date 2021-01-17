package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.LastM.lastM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;

public class LastMTest {

    @Test
    public void lastEmpty() {
        assertEquals(new Identity<>(nothing()), lastM(empty(pureIdentity())));
    }

    @Test
    public void lastNonEmpty() {
        assertEquals(new Identity<>(just(0)), lastM(streamT(new Identity<>(just(0)))));
        assertEquals(new Identity<>(natural(STACK_EXPLODING_NUMBER - 1)),
                     lastM(takeM(atLeastOne(STACK_EXPLODING_NUMBER), naturalsM(pureIdentity()))));
    }
}
