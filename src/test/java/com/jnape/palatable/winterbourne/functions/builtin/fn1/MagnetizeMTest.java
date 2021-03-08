package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.MagnetizeM.magnetizeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ReplicateM.replicateM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class MagnetizeMTest {

    @Test
    public void magnetizesElementsPairwise() {
        assertThat(magnetizeM(takeM(atLeastOne(4), naturalsM(pureIdentity()))
                                      .fmap(Natural::inc)
                                      .flatMap(n -> replicateM(n, new Identity<>(n)))),
                   streams(nothing(), just(strictQueue(abs(1))),
                           nothing(), just(strictQueue(abs(2), abs(2))),
                           nothing(), nothing(), just(strictQueue(abs(3), abs(3), abs(3))),
                           nothing(), nothing(), nothing(), just(strictQueue(abs(4), abs(4), abs(4), abs(4)))));

        assertThat(magnetizeM(takeM(atLeastOne(4), naturalsM(pureIdentity()))),
                   streams(nothing(),
                           just(strictQueue(abs(0))),
                           just(strictQueue(abs(1))),
                           just(strictQueue(abs(2))),
                           just(strictQueue(abs(3)))));

        assertThat(magnetizeM(empty(pureIdentity())),
                   streams());

        assertThat(magnetizeM(streamT(new Identity<>(just(0)))),
                   streams(nothing(), just(strictQueue(0))));

        assertThat(magnetizeM(streamT(new Identity<>(
                           strictQueue(nothing(), just(1), nothing(), just(1), nothing(), just(2), nothing())))),
                   streams(nothing(), nothing(), nothing(), nothing(), nothing(),
                           just(strictQueue(1, 1)),
                           nothing(),
                           just(strictQueue(2))));
    }
}