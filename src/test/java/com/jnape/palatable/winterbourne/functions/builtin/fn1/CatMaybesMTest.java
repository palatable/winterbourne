package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn4.IfThenElse.ifThenElse;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CatMaybesM.catMaybesM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static com.jnape.palatable.winterbourne.testsupport.functions.DivisibleBy.divisibleBy;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.impureNaturals;
import static testsupport.matchers.IOMatcher.yieldsValue;

public class CatMaybesMTest {

    @Test
    public void skipsAbsentElements() {
        StreamT<Identity<?>, Maybe<Natural>> numbers = streamT(new Identity<>(
                strictQueue(just(just(zero())), just(nothing()), just(natural(2)),
                            just(nothing()), just(natural(4)))));
        assertThat(catMaybesM(numbers),
                   streams(just(zero()), nothing(), natural(2), nothing(), natural(4)));
    }

    @Test
    public void preservesExistingSkips() {
        StreamT<Identity<?>, Maybe<Natural>> numbers = streamT(new Identity<>(
                strictQueue(just(just(zero())), just(nothing()), nothing(),
                            just(nothing()), just(natural(4)))));
        assertThat(catMaybesM(numbers),
                   streams(just(zero()), nothing(), nothing(), nothing(), natural(4)));
    }

    @Test
    public void worksWithImpureEffects() {
        StreamT<IO<?>, Maybe<Natural>> numbers =
                impureNaturals().fmap(ifThenElse(divisibleBy(atLeastOne(2)),
                                                 Maybe::just, constantly(nothing())));

        assertThat(takeM(atLeastOne(3), catMaybesM(numbers)),
                   whenFolded(yieldsValue(equalTo(
                           strictQueue(just(zero()), nothing(),
                                       just(abs(2)), nothing(),
                                       just(abs(4))))),
                              pureIO()));
    }
}
