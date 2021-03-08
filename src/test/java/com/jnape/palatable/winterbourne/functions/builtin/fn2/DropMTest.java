package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class DropMTest {

    @Test
    public void dropsByIntroducingElisions() {
        assertThat(dropM(atLeastOne(3),
                         streamT(new Identity<>(strictQueue(just(1), just(2), just(3), just(4), just(5))))),
                   streams(nothing(), nothing(), nothing(), just(4), just(5)));
    }

    @Test
    public void onlyDropsEmissions() {
        assertThat(dropM(atLeastOne(3),
                         streamT(new Identity<>(strictQueue(just(1), nothing(), just(3), just(4), just(5))))),
                   streams(nothing(), nothing(), nothing(), nothing(), just(5)));
    }
}