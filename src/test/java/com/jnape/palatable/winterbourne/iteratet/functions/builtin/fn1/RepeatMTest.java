package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.RepeatM.repeatM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.NthM.nthM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterateTMatcher.iterates;

public class RepeatMTest {

    @Test
    public void repeatsElement() {
        assertThat(takeM(10, repeatM(new Identity<>(1))), iterates(1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
    }

    @Test
    public void runsEffectEachTime() {
        assertEquals(tuple(asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 10),
                     takeM(10, repeatM(writer(tuple(1, 1))))
                             .<List<Integer>, Writer<Integer, List<Integer>>>toCollection(ArrayList::new)
                             .runWriter(monoid(Integer::sum, 0)));
    }

    @Test
    public void repeatsALotOfThings() {
        assertEquals(new Identity<>(just(1)), nthM(STACK_EXPLODING_NUMBER, repeatM(new Identity<>(1))));
    }
}