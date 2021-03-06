package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import org.junit.Test;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class NaturalsMTest {

    @Test
    public void producesTheNats() {
        assertThat(takeM(atLeastOne(3), naturalsM(pureIdentity())),
                   streams(natural(0), natural(1), natural(2)));
    }
}