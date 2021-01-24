package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.PrependAllM.prependAllM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

@RunWith(Traits.class)
public class PrependAllMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>, StreamT<IO<?>, Object>> testSubject() {
        return prependAllM(new Object());
    }

    @Test
    public void prependsToAllEmissions() {
        assertThat(prependAllM(zero(),
                               takeM(atLeastOne(3),
                                     dropM(atLeastOne(3),
                                           naturalsM(pureIdentity())))),
                   streams(nothing(), nothing(), nothing(),
                           natural(0), natural(3),
                           natural(0), natural(4),
                           natural(0), natural(5)));
    }

    @Test
    public void emptyRemainsEmpty() {
        assertThat(prependAllM(zero(),
                               dropM(atLeastOne(3),
                                     takeM(atLeastOne(3),
                                           naturalsM(pureIdentity())))),
                   streams(nothing(), nothing(), nothing()));
    }
}