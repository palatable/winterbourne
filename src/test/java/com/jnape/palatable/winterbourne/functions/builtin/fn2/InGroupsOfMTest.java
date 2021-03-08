package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.InGroupsOfM.inGroupsOfM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class InGroupsOfMTest {

    @Test
    public void groupingEmptyIsEmpty() {
        assertThat(inGroupsOfM(atLeastOne(2), empty(pureIdentity())), streams());
    }

    @Test
    public void evenlyDividedGroupsOfTwo() {
        assertThat(NaturalsM.<Identity<?>>naturalsM()
                           .fmap(takeM(atLeastOne(6)))
                           .fmap(inGroupsOfM(atLeastOne(2)))
                           .apply(pureIdentity()),
                   streams(nothing(), nothing(),
                           just(strictQueue(abs(0), abs(1))),
                           nothing(),
                           just(strictQueue(abs(2), abs(3))),
                           nothing(),
                           just(strictQueue(abs(4), abs(5)))));
    }

    @Test
    public void groupsOfTwoWithLastGroupShort() {
        assertThat(NaturalsM.<Identity<?>>naturalsM()
                           .fmap(takeM(atLeastOne(5)))
                           .fmap(inGroupsOfM(atLeastOne(2)))
                           .apply(pureIdentity()),
                   streams(nothing(), nothing(),
                           just(strictQueue(abs(0), abs(1))),
                           nothing(),
                           just(strictQueue(abs(2), abs(3))),
                           just(strictQueue(abs(4)))));
    }
}
