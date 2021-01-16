package com.jnape.palatable.winterbourne.functions.builtin.fn2;

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
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;

public class FindMTest {

    @Test
    public void findsNothingWhenEmpty() {
        StreamT<Identity<?>, Natural> numbers = empty(pureIdentity());
        assertEquals(nothing(),
                     FindM.<Identity<?>, Natural, Identity<Maybe<Natural>>>findM(gte(abs(5)), numbers)
                             .runIdentity());
    }

    @Test
    public void findsNothingWhenNothingMatches() {
        StreamT<Identity<?>, Natural> numbers = takeM(atLeastOne(5), naturalsM(pureIdentity()));
        assertEquals(nothing(),
                     FindM.<Identity<?>, Natural, Identity<Maybe<Natural>>>findM(gte(abs(5)), numbers)
                             .runIdentity());
    }

    @Test
    public void findsSomething() {
        StreamT<Identity<?>, Natural> numbers = naturalsM(pureIdentity());
        assertEquals(just(abs(5)),
                     FindM.<Identity<?>, Natural, Identity<Maybe<Natural>>>findM(gte(abs(5)), numbers)
                             .runIdentity());
    }
}
