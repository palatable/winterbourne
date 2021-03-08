package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Writer.pureWriter;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.NonZero;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ReplicateM.replicateM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ReplicateMTest {

    @Test
    public void producesNCopies() {
        assertThat(replicateM(atLeastOne(3), new Identity<>('1')),
                   streams(just('1'), just('1'), just('1')));
    }

    @Test
    public void runsEffectNTimes() {
        NonZero n = atLeastOne(3);
        assertThat(replicateM(n, writer(tuple("a", one()))),
                   whenFolded(whenRunWith(monoid(Natural::plus, zero()),
                                          equalTo(tuple(strictQueue(just("a"), just("a"), just("a")), n))),
                              pureWriter()));
    }
}
