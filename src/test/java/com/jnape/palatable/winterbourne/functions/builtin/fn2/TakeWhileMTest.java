package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GTE.gte;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LTE.lte;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeWhileM.takeWhileM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class TakeWhileMTest {

    @Test
    public void takesNothingWhenEmpty() {
        assertThat(takeWhileM(constantly(true), empty(pureIdentity())),
                   streams());
    }

    @Test
    public void preservesElisions() {
        assertThat(takeWhileM(constantly(true),
                              streamT(new Identity<>(strictQueue(nothing(), nothing(), just(zero()))))),
                   streams(nothing(), nothing(), just(zero())));

        assertThat(takeWhileM(constantly(false),
                              streamT(new Identity<>(strictQueue(nothing(), nothing(), just(zero()))))),
                   streams(nothing(), nothing()));
    }

    @Test
    public void takesNothingWhenFirstEmissionDoesNotMatch() {
        assertThat(takeWhileM(gte(one()), naturalsM(pureIdentity())),
                   streams());
    }

    @Test
    public void takesPreservingElisionsUntilEmissionDoesNotMatch() {
        assertThat(takeWhileM(lte(abs(5)), dropM(atLeastOne(3), naturalsM(pureIdentity()))),
                   streams(nothing(), nothing(), nothing(), natural(3), natural(4), natural(5)));
    }
}
