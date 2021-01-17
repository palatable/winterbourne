package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Eq.eq;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CycleM.cycleM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FindM.findM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ZipM.zipM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenEmissionsFolded;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.impureNaturals;
import static testsupport.matchers.IOMatcher.yieldsValue;
import static testsupport.matchers.IterableMatcher.iterates;

public class CycleMTest {

    @Test
    public void cyclesTheSameSequence() {
        assertThat(takeM(atLeastOne(9), cycleM(takeM(atLeastOne(3), naturalsM(pureIdentity())))),
                   streams(natural(0), natural(1), natural(2),
                           natural(0), natural(1), natural(2),
                           natural(0), natural(1), natural(2)));
    }

    @Test
    public void cyclesTheSameSequenceForever() {
        assertThat(takeM(atLeastOne(9), dropM(atLeastOne(STACK_EXPLODING_NUMBER - STACK_EXPLODING_NUMBER % 3),
                                              cycleM(takeM(atLeastOne(3), naturalsM(pureIdentity()))))),
                   whenEmissionsFolded(equalTo(new Identity<>(
                                               strictQueue(atLeastZero(0), atLeastZero(1), atLeastZero(2),
                                                           atLeastZero(0), atLeastZero(1), atLeastZero(2),
                                                           atLeastZero(0), atLeastZero(1), atLeastZero(2)))),
                                       pureIdentity()));
    }

    @Test
    public void infinityInfinities() {
        IO<Maybe<Tuple2<Natural, Natural>>> actual =
                findM(into((i, j) -> !eq(i, j)),
                      takeM(atLeastOne(STACK_EXPLODING_NUMBER),
                            zipM(naturalsM(pureIO()),
                                 cycleM(naturalsM(pureIO())))));
        assertThat(actual, yieldsValue(equalTo(nothing())));
    }

    @Test
    public void rerunsTheEffects() {
        assertThat(takeM(atLeastOne(10), cycleM(takeM(atLeastOne(2), impureNaturals())))
                           .foldAwait((ns, n) -> io(ns.snoc(n)), io(strictQueue())),
                   yieldsValue(iterates(abs(0), abs(1), abs(2), abs(3), abs(4),
                                        abs(5), abs(6), abs(7), abs(8), abs(9))));
    }
}