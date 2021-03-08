package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static org.junit.Assert.assertEquals;

public class HeadMTest {

    @Test
    public void headOfEmpty() {
        assertEquals(new Identity<>(nothing()),
                     headM(empty(pureIdentity())));
    }

    @Test
    public void headOfNothingButElisions() {
        assertEquals(new Identity<>(nothing()),
                     headM(streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing())))));
    }

    @Test
    public void firstEmission() {
        assertEquals(new Identity<>(Maybe.<Natural>just(zero())),
                     headM(naturalsM(pureIdentity())));
    }

    @Test
    public void firstEmissionAfterElisions() {
        assertEquals(new Identity<>(just(one())),
                     headM(streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing(), just(one()))))));
    }
}
