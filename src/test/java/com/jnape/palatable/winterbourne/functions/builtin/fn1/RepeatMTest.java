package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Writer.pureWriter;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RepeatMTest {

    @Test
    public void repeatsElement() {
        assertThat(takeM(atLeastOne(3), repeatM(new Identity<>(1))),
                   streams(just(1), just(1), just(1)));
    }

    @Test
    public void runsEffectEachTime() {
        assertThat(takeM(atLeastOne(3), repeatM(writer(tuple("aye", abs(1))))),
                   whenFolded(whenRunWith(monoid(Natural::plus, zero()),
                                          equalTo(tuple(
                                                  strictQueue(just("aye"), just("aye"), just("aye")),
                                                  abs(3)))),
                              pureWriter()));

    }
}
