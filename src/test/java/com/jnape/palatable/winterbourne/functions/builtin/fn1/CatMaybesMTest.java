package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CatMaybesM.catMaybesM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class CatMaybesMTest {

    @Test
    public void skipsAbsentElements() {
        assertThat(catMaybesM(streamT(new Identity<>(
                           strictQueue(just(natural(0)), just(nothing()), just(natural(2)),
                                       just(nothing()), just(natural(4)))))),
                   streams(natural(0), nothing(), natural(2), nothing(), natural(4)));
    }

    @Test
    public void preservesExistingSkips() {
        assertThat(catMaybesM(streamT(new Identity<>(
                           strictQueue(just(natural(0)), just(nothing()), nothing(),
                                       just(nothing()), just(natural(4)))))),
                   streams(natural(0), nothing(), nothing(), nothing(), natural(4)));
    }
}
