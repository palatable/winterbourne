package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import org.junit.Test;
import testsupport.matchers.IterableMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CycleM.cycleM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.NthM.nthM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IOMatcher.yieldsValue;
import static testsupport.matchers.IterateTMatcher.iterates;

public class CycleMTest {

    @Test
    public void cyclesTheSameSequence() {
        assertThat(takeM(9, cycleM(takeM(3, naturalNumbersM(pureIdentity())))),
                   iterates(1, 2, 3, 1, 2, 3, 1, 2, 3));
    }

    @Test
    public void cyclesTheSameSequenceForever() {
        assertThat(takeM(9, dropM(STACK_EXPLODING_NUMBER - STACK_EXPLODING_NUMBER % 3,
                                  cycleM(takeM(3, naturalNumbersM(pureIdentity()))))),
                   iterates(1, 2, 3, 1, 2, 3, 1, 2, 3));
    }

    @Test
    public void infinityInfinities() {
        assertEquals(new Identity<>(just(STACK_EXPLODING_NUMBER)),
                     nthM(STACK_EXPLODING_NUMBER, cycleM(naturalNumbersM(pureIdentity()))));
    }

    @Test
    public void cyclesArePredictablyWeirdWithNonRepeatableSequences() {
        assertThat(takeM(10, cycleM(takeM(2, repeatM(io(new AtomicInteger(0)::incrementAndGet)))))
                           .<List<Integer>, IO<List<Integer>>>toCollection(ArrayList::new),
                   yieldsValue(IterableMatcher.iterates(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
    }
}