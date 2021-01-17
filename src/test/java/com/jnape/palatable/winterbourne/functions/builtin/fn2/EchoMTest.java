package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EchoM.echoM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.impureNaturals;
import static testsupport.matchers.IOMatcher.yieldsValue;

@RunWith(Traits.class)
public class EchoMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>, StreamT<IO<?>, Object>> testSubject() {
        return echoM(atLeastOne(3));
    }

    @Test
    public void echoesEachElement() {
        assertThat(takeM(atLeastOne(9), echoM(atLeastOne(3), naturalsM(pureIdentity()))),
                   streams(natural(0), natural(0), natural(0),
                           natural(1), natural(1), natural(1),
                           natural(2), natural(2), natural(2)));
    }

    @Test
    public void echoesEachElementForever() {
        assertThat(takeM(atLeastOne(3), dropM(atLeastOne(3 * STACK_EXPLODING_NUMBER),
                                              echoM(atLeastOne(3), naturalsM(pureIdentity())))),
                   whenEmissionsFolded(equalTo(new Identity<StrictQueue<Natural>>(
                                               strictQueue(atLeastOne(STACK_EXPLODING_NUMBER),
                                                           atLeastOne(STACK_EXPLODING_NUMBER),
                                                           atLeastOne(STACK_EXPLODING_NUMBER)))),
                                       pureIdentity()));
    }

    @Test
    public void echoesOutputOfImpureEffect() {
        assertThat(takeM(atLeastOne(9), echoM(atLeastOne(3), impureNaturals())),
                   whenFolded(yieldsValue(equalTo(
                           strictQueue(
                                   just(abs(0)), just(abs(0)), just(abs(0)),
                                   just(abs(1)), just(abs(1)), just(abs(1)),
                                   just(abs(2)), just(abs(2)), just(abs(2))))),
                              pureIO()));
    }
}