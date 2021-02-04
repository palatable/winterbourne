package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.IntersperseM.intersperseM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class IntersperseMTest {

    @Test
    public void interspersesAmongSeveral() {
        assertThat(intersperseM("+", streamT(new Identity<>(strictQueue(nothing(), just("a"), nothing(),
                                                                        just("b"), just("c"), nothing())))),
                   streams(nothing(), nothing(), just("a"), nothing(),
                           just("+"), just("b"), just("+"), just("c"), nothing()));
    }

    @Test
    public void leavesSingletonSingle() {
        assertThat(intersperseM("+", streamT(new Identity<>(just("a")))),
                   streams(nothing(), just("a")));
    }

    @Test
    public void leavesEmptyEmpty() {
        assertThat(intersperseM("+", empty(pureIdentity())),
                   streams());
    }
}