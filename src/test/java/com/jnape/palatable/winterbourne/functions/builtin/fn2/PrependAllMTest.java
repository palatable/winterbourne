package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.TailM.tailM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.PrependAllM.prependAllM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class PrependAllMTest {

    @Test
    public void prependsToAllEmissions() {
        assertThat(prependAllM(zero(),
                               takeM(atLeastOne(3),
                                     tailM(naturalsM(pureIdentity())))),
                   streams(nothing(),
                           natural(0), natural(1),
                           natural(0), natural(2),
                           natural(0), natural(3)));
    }

    @Test
    public void emptyRemainsEmpty() {
        assertThat(prependAllM(zero(), empty(pureIdentity())), streams());
        assertThat(prependAllM(zero(), streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing())))),
                   streams(nothing(), nothing(), nothing()));
    }
}