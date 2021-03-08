package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.IndexM.indexM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;

public class IndexMTest {

    @Test
    public void emptyStream() {
        assertThat(indexM(empty(pureIdentity())),
                streams());
    }

    @Test
    public void indexes() {
        assertThat(indexM(streamT(new Identity<>(strictQueue(just("a"), nothing(), just("b"))))),
                   streams(just(tuple(zero(), "a")),
                           nothing(),
                           just(tuple(one(), "b"))));
    }
}
