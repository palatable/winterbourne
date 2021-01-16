package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Traits.class)
public class TakeMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>,Object>, StreamT<IO<?>,Object>> testSubject() {
        return takeM(atLeastOne(3));
    }

    @Test
    public void takesAFew() {
        StreamT<Identity<?>, String> numbers =
                streamT(new Identity<>(strictQueue(just("one"), just("two"), just("three"), just("four"), just("five"))));
        assertThat(takeM(atLeastOne(3), numbers), streams(just("one"), just("two"), just("three")));
    }

    @Test
    public void preservesSkipsWhileTakingEmissions() {
        StreamT<Identity<?>, String> numbers =
                streamT(new Identity<>(strictQueue(just("one"), nothing(), just("three"), just("four"), just("five"))));
        assertThat(takeM(atLeastOne(3), numbers), streams(just("one"), nothing(), just("three"), just("four")));
    }

    @Test
    public void exhaustsShortStreams() {
        StreamT<Identity<?>, String> numbers =
                streamT(new Identity<>(strictQueue(just("one"), nothing(), just("three"))));
        assertThat(takeM(atLeastOne(3), numbers), streams(just("one"), nothing(), just("three")));
    }

    @Test
    public void exhaustsTrailingSkipsInShortStreams() {
        StreamT<Identity<?>, String> numbers =
                streamT(new Identity<>(strictQueue(just("one"), nothing(), just("three"), nothing(), nothing())));
        assertThat(takeM(atLeastOne(3), numbers), streams(just("one"), nothing(), just("three"), nothing(), nothing()));
    }
}