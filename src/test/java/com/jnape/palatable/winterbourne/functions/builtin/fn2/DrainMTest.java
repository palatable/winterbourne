package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.impl.StrictQueue;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DrainM.drainM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;

public class DrainMTest {

    @Test
    public void drainsIntoCollection() {
        assertEquals(new Identity<>(strictQueue(abs(0), abs(1), abs(2), abs(3), abs(4))),
                     drainM(StrictQueue::strictQueue,
                            StrictQueue::snoc,
                            takeM(atLeastOne(5), naturalsM(pureIdentity()))));

        assertEquals(new Identity<>(strictQueue()),
                     drainM(StrictQueue::strictQueue, StrictQueue::snoc, empty(pureIdentity())));

        assertEquals(new Identity<>(strictQueue()),
                     drainM(StrictQueue::strictQueue, StrictQueue::snoc, streamT(new Identity<>(nothing()))));
    }
}