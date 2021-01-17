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

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LT.lt;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropWhileM.dropWhileM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenEmissionsFolded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.impureNaturals;
import static testsupport.matchers.IOMatcher.yieldsValue;

@RunWith(Traits.class)
public class DropWhileMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>,StreamT<IO<?>, Object>> testSubject() {
        return dropWhileM(constantly(false));
    }

    @Test
    public void dropsBySkipping() {
        StreamT<Identity<?>, Natural> numbers = takeM(atLeastOne(5), naturalsM(pureIdentity()));
        assertThat(dropWhileM(lt(abs(3)), numbers),
                   streams(nothing(), nothing(), nothing(), natural(3), natural(4)));
    }

    @Test
    public void dropsLotsOfThings() {
        assertThat(dropWhileM(lt(abs(STACK_EXPLODING_NUMBER + 5)),
                              takeM(atLeastOne(STACK_EXPLODING_NUMBER + 10), impureNaturals())),
                   whenEmissionsFolded(yieldsValue(equalTo(strictQueue(abs(STACK_EXPLODING_NUMBER + 5),
                                                                       abs(STACK_EXPLODING_NUMBER + 6),
                                                                       abs(STACK_EXPLODING_NUMBER + 7),
                                                                       abs(STACK_EXPLODING_NUMBER + 8),
                                                                       abs(STACK_EXPLODING_NUMBER + 9)))),
                                       pureIO()));
    }
}
