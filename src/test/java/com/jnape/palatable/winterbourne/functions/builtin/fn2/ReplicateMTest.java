package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ReplicateM.replicateM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ReplicateMTest {

    @Test
    public void producesNCopies() {
        assertThat(replicateM(atLeastOne(3), new Identity<>('1')),
                   streams(just('1'),
                           just('1'),
                           just('1')));
    }

    @Test
    public void runsEffectNTimes() {
        NonZero n = atLeastOne(3);
        Natural runs = replicateM(n, Writer.<Natural, String>writer(tuple("aye", one())))
                .<Writer<Natural, Unit>>awaitAll()
                .runWriter(monoid(Natural::plus, zero()))
                ._2();

        assertEquals(n, runs);
    }
}
