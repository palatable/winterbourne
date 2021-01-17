package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FilterM.filterM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ZipM.zipM;
import static com.jnape.palatable.winterbourne.testsupport.functions.DivisibleBy.divisibleBy;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZipMTest {
    @Test
    public void zips() {
        StreamT<Identity<?>, Natural> numbers = naturalsM(pureIdentity());
        assertThat(takeM(atLeastOne(3), zipM(numbers, numbers)),
                   streams(just(tuple(zero(), zero())),
                           just(tuple(one(), one())),
                           just(tuple(abs(2), abs(2)))));
    }

    @Test
    public void zipsWithSkips() {
        StreamT<Identity<?>, Natural> numbers = naturalsM(pureIdentity());
        assertThat(takeM(atLeastOne(3), zipM(numbers, filterM(divisibleBy(atLeastOne(2)), numbers))),
                   streams(just(tuple(zero(), zero())),
                           nothing(),
                           just(tuple(one(), abs(2))),
                           nothing(),
                           just(tuple(abs(2), abs(4)))));
    }
}