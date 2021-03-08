package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FilterM.filterM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.ScanLeftM.scanLeftM;
import static com.jnape.palatable.winterbourne.testsupport.functions.DivisibleBy.divisibleBy;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class ScanLeftMTest {

    @Test
    public void buildsUpContinuationOfIncrementalAccumulations() {
        assertThat(scanLeftM((n, m) -> new Identity<>(n.plus(m)), new Identity<>(zero()),
                             takeM(atLeastOne(5), naturalsM(pureIdentity()).fmap(Natural::inc))),
                   streams(natural(0), natural(1), natural(3),
                           natural(6), natural(10), natural(15)));
    }

    @Test
    public void preservesSkipsWhileAccumulatingResults() {
        assertThat(scanLeftM((n, m) -> new Identity<>(n.plus(m)), new Identity<>(zero()),
                             takeM(atLeastOne(5),
                                   filterM(divisibleBy(atLeastOne(2)),
                                           naturalsM(pureIdentity()).fmap(Natural::inc)))),
                   streams(nothing(), natural(0),
                           nothing(), natural(2),
                           nothing(), natural(6),
                           nothing(), natural(12),
                           nothing(), natural(20),
                           natural(30)));
    }

    @Test
    public void initialAccumulationIsOnlyResultIfEmpty() {
        assertThat(scanLeftM((Natural n, Natural m) -> new Identity<>(n.plus(m)),
                             new Identity<>(zero()),
                             empty(pureIdentity())),
                   streams(natural(0)));
    }
}