package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.CatMaybesM.catMaybesM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.RepeatM.repeatM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.hamcrest.MatcherAssert.assertThat;
import static testsupport.matchers.IterateTMatcher.isEmpty;
import static testsupport.matchers.IterateTMatcher.iterates;

public class CatMaybesMTest {

    @Test
    public void emptyStaysEmpty() {
        assertThat(catMaybesM(empty(pureIdentity())), isEmpty());
    }

    @Test
    public void nothingButNothing() {
        assertThat(catMaybesM(takeM(4, repeatM(new Identity<>(nothing())))), isEmpty());
    }

    @Test
    public void justAFew() {
        assertThat(takeM(3, catMaybesM(naturalNumbersM(pureIdentity()).fmap(x -> x % 2 == 0 ? just(x) : nothing()))),
                   iterates(2, 4, 6));
    }
}
