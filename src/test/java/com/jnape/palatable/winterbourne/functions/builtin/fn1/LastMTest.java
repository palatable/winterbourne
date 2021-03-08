package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.LastM.lastM;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LastMTest {

    @Test
    public void lastEmpty() {
        assertEquals(new Identity<>(nothing()), lastM(empty(pureIdentity())));
    }

    @Test
    public void lastNonEmpty() {
        assertEquals(new Identity<>(just(0)), lastM(streamT(new Identity<>(just(0)))));

        assertThat(lastM(streamT(writer(tuple(nothing(), strictQueue("a"))),
                                 writer(tuple(nothing(), strictQueue("b"))),
                                 writer(tuple(just(0), strictQueue("c"))),
                                 writer(tuple(nothing(), strictQueue("d"))),
                                 writer(tuple(nothing(), strictQueue("e"))))),
                   WriterMatcher.<StrictQueue<String>, Maybe<Integer>>whenRunWith(
                           monoid(StrictQueue::snocAll, strictQueue()),
                           equalTo(tuple(just(0), strictQueue("a", "b", "c", "d", "e")))));
    }
}
