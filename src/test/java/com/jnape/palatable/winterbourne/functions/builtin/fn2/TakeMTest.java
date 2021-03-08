package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class TakeMTest {

    @Test
    public void takesAFew() {
        assertThat(takeM(atLeastOne(3), naturalsM(pureIdentity())),
                   streams(natural(0), natural(1), natural(2)));
    }

    @Test
    public void preservesElisionsWhileTakingEmissions() {
        assertThat(takeM(atLeastOne(3),
                         streamT(new Identity<>(strictQueue(just(1), nothing(), just(3), just(4), just(5))))),
                   streams(just(1), nothing(), just(3), just(4)));
    }

    @Test
    public void exhaustsShortStreams() {
        assertThat(takeM(atLeastOne(3), streamT(new Identity<>(strictQueue(just(1), nothing(), just(3), nothing())))),
                   streams(just(1), nothing(), just(3), nothing()));
    }
}