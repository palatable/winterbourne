package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LT.lt;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropWhileM.dropWhileM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class DropWhileMTest {

    @Test
    public void dropEmpty() {
        assertThat(dropWhileM(constantly(true), empty(pureIdentity())),
                   streams());
    }

    @Test
    public void dropsViaElision() {
        assertThat(dropWhileM(lt(abs(3)), takeM(atLeastOne(5), naturalsM(pureIdentity()))),
                   streams(nothing(), nothing(), nothing(), natural(3), natural(4)));
    }

    @Test
    public void dropAll() {
        assertThat(dropWhileM(constantly(true), takeM(atLeastOne(5), naturalsM(pureIdentity()))),
                   streams(nothing(), nothing(), nothing(), nothing(), nothing()));
    }
}
