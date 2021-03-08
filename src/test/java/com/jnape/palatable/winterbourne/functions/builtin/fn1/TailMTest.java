package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.TailM.tailM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class TailMTest {

    @Test
    public void emptyBegetsEmpty() {
        assertThat(tailM(empty(pureIdentity())), streams());
        assertThat(tailM(streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing())))),
                   streams(nothing(), nothing(), nothing()));
    }

    @Test
    public void singletonBegetsEmpty() {
        assertThat(tailM(streamT(new Identity<>(just("manha manha")))),
                   streams(nothing()));
        assertThat(tailM(streamT(new Identity<>(strictQueue(nothing(), just("manha manha"), nothing())))),
                   streams(nothing(), nothing(), nothing()));
    }

    @Test
    public void pluralSkipsFirstEmission() {
        assertThat(tailM(streamT(new Identity<>(strictQueue(just(1), just(2), just(3))))),
                   streams(nothing(), just(2), just(3)));
        assertThat(tailM(streamT(new Identity<>(strictQueue(nothing(), just(1), nothing(),
                                                            just(2), just(3), nothing(),
                                                            just(4), nothing(), nothing())))),
                   streams(nothing(), nothing(), nothing(),
                           just(2), just(3), nothing(),
                           just(4), nothing(), nothing()));
    }
}