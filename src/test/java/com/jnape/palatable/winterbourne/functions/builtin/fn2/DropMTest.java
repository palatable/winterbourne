package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenEmissionsFolded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IOMatcher.yieldsValue;

@RunWith(Traits.class)
public class DropMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>, StreamT<IO<?>, Object>> testSubject() {
        return dropM(one());
    }

    @Test
    public void dropsByIntroducingSkips() {
        StreamT<Identity<?>, String> numbers =
                streamT(new Identity<>(strictQueue(just("one"), just("two"), just("three"), just("four"), just("five"))));
        assertThat(dropM(atLeastOne(3), numbers),
                   streams(nothing(), nothing(), nothing(), just("four"), just("five")));
    }

    @Test
    public void onlyDropsEmissions() {
        StreamT<Identity<?>, String> numbers =
                streamT(new Identity<>(strictQueue(just("one"), nothing(), just("three"), just("four"), just("five"))));
        assertThat(dropM(atLeastOne(3), numbers),
                   streams(nothing(), nothing(), nothing(), nothing(), just("five")));
    }

    @Test
    public void dropsEmissionsForever() {
        StreamT<IO<?>, Integer> numbers = takeM(atLeastOne(STACK_EXPLODING_NUMBER + 5),
                                                naturalsM(pureIO()).fmap(Natural::intValue));
        assertThat(dropM(atLeastOne(STACK_EXPLODING_NUMBER), numbers),
                   whenEmissionsFolded(yieldsValue(equalTo(strictQueue(STACK_EXPLODING_NUMBER,
                                                                       STACK_EXPLODING_NUMBER + 1,
                                                                       STACK_EXPLODING_NUMBER + 2,
                                                                       STACK_EXPLODING_NUMBER + 3,
                                                                       STACK_EXPLODING_NUMBER + 4))),
                                       pureIO()));
    }
}