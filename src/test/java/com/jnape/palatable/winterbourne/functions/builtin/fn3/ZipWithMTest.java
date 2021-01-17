package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.functions.DivisibleBy;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FilterM.filterM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.ZipWithM.zipWithM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.impureNaturals;
import static testsupport.matchers.IOMatcher.yieldsValue;

@RunWith(Traits.class)
public class ZipWithMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>, StreamT<IO<?>, Object>> testSubject() {
        return zipWithM((o1, o2) -> new Object(), streamT(io(strictQueue(nothing(), just(new Object()),
                                                                         nothing(), just(new Object()),
                                                                         just(new Object()), nothing()))));
    }

    @Test
    public void zipsSquares() {
        assertThat(zipWithM(Natural::times,
                            takeM(atLeastOne(4), naturalsM(pureIdentity())),
                            takeM(atLeastOne(4), naturalsM(pureIdentity())).fmap(n -> n.plus(abs(10)))),
                   streams(natural(0), natural(11), natural(24), natural(39)));
    }

    @Test
    public void zipsAsymmetricStreams() {
        assertThat(zipWithM(Natural::times,
                            takeM(atLeastOne(3), naturalsM(pureIdentity())),
                            takeM(atLeastOne(4), naturalsM(pureIdentity())).fmap(n -> n.plus(abs(10)))),
                   streams(natural(0), natural(11), natural(24)));
    }

    @Test
    public void zipsSymmetricallyIntermittentStreams() {
        assertThat(zipWithM(Natural::times,
                            takeM(atLeastOne(3), filterM(DivisibleBy.divisibleBy(atLeastOne(2)), naturalsM(pureIdentity()))),
                            takeM(atLeastOne(3), filterM(DivisibleBy.divisibleBy(atLeastOne(2)), naturalsM(pureIdentity())))
                                    .fmap(n -> n.plus(abs(10)))),
                   streams(natural(0), nothing(), nothing(), natural(24), nothing(), nothing(), natural(56)));
    }

    @Test
    public void zipsImpureAsymmetricallyIntermittentStreams() {
        assertThat(zipWithM(Natural::times,
                            takeM(atLeastOne(3), filterM(DivisibleBy.divisibleBy(atLeastOne(2)), impureNaturals())),
                            takeM(atLeastOne(3), filterM(DivisibleBy.divisibleBy(atLeastOne(3)), impureNaturals()))),
                   whenFolded(yieldsValue(equalTo(strictQueue(natural(0), nothing(), nothing(), nothing(),
                                                              natural(6), nothing(), nothing(), nothing(),
                                                              natural(24)))),
                              pureIO()));
    }
}
