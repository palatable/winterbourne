package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FilterM.filterM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.jnape.palatable.winterbourne.testsupport.functions.DivisibleBy.divisibleBy;

@RunWith(Traits.class)
public class FilterMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>,Object>, StreamT<IO<?>,Object>> testSubject() {
        return filterM(constantly(true));
    }

    @Test
    public void filterIntroducesSkips() {
        assertThat(filterM(divisibleBy(atLeastOne(2)),
                           takeM(atLeastOne(5), naturalsM(pureIdentity()))),
                   streams(just(zero()), nothing(), natural(2), nothing(), natural(4)));
    }
}
