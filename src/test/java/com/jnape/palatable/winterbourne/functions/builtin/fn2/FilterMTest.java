package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FilterM.filterM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class FilterMTest {

    @Test
    public void filterEmpty() {
        assertThat(filterM(constantly(true), empty(pureIdentity())), streams());
        assertThat(filterM(constantly(false), empty(pureIdentity())), streams());
    }

    @Test
    public void elisionsArePreserved() {
        assertThat(filterM(constantly(true), streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing())))),
                   streams(nothing(), nothing(), nothing()));

        assertThat(filterM(constantly(false), streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing())))),
                   streams(nothing(), nothing(), nothing()));
    }

    @Test
    public void filterReplacesMatchesWithElisions() {
        StreamT<Identity<?>, Integer> ints = streamT(new Identity<>(strictQueue(
                nothing(),
                just(1),
                nothing(),
                just(2),
                nothing(),
                just(3))));

        assertThat(filterM(x -> x % 2 == 0, ints),
                   streams(nothing(), nothing(), nothing(), just(2), nothing(), nothing()));

        assertThat(filterM(constantly(true), ints),
                   streams(nothing(), just(1), nothing(), just(2), nothing(), just(3)));

        assertThat(filterM(constantly(false), ints),
                   streams(nothing(), nothing(), nothing(), nothing(), nothing(), nothing()));
    }
}
