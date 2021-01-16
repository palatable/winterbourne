package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GTE.gte;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FilterM.filterM;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.impureNaturals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IOMatcher.yieldsValue;

public class HeadMTest {

    @Test
    public void headOfEmpty() {
        StreamT<Identity<?>, Natural> numbers = empty(pureIdentity());
        assertEquals(nothing(),
                     HeadM.<Identity<?>, Natural, Identity<Maybe<Natural>>>headM(numbers)
                             .runIdentity());
    }

    @Test
    public void headOfNothingButSkips() {
        StreamT<Identity<?>, Natural> numbers = streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing())));
        assertEquals(nothing(),
                     HeadM.<Identity<?>, Natural, Identity<Maybe<Natural>>>headM(numbers)
                             .runIdentity());
    }

    @Test
    public void headOfSomething() {
        StreamT<Identity<?>, Natural> numbers = naturalsM(pureIdentity());
        assertEquals(just(zero()),
                     HeadM.<Identity<?>, Natural, Identity<Maybe<Natural>>>headM(numbers)
                             .runIdentity());
    }

    @Test
    public void headWhenSkipsPrecedeFirstEmission() {
        StreamT<Identity<?>, Natural> numbers = filterM(gte(abs(5)), naturalsM(pureIdentity()));
        assertEquals(just(abs(5)),
                     HeadM.<Identity<?>, Natural, Identity<Maybe<Natural>>>headM(numbers)
                             .runIdentity());
    }

    @Test
    public void headWhenThereAreLotsOfSkips() {
        assertThat(headM(filterM(gte(abs(STACK_EXPLODING_NUMBER)), impureNaturals())),
                   yieldsValue(equalTo(just(abs(STACK_EXPLODING_NUMBER)))));
    }
}
