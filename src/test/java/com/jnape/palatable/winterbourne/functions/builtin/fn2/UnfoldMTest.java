package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.UnfoldM.unfoldM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class UnfoldMTest {

    @Test
    public void emitsOrSkipsUntilFinished() {
        assertThat(unfoldM(x -> new Identity<>(
                           x > 5 ? nothing()
                                 : x % 2 == 0
                                   ? just(tuple(nothing(), x + 1))
                                   : just(tuple(just(x), x + 1))), new Identity<>(1)),
                   streams(just(1), nothing(), just(3), nothing(), just(5)));
    }

}