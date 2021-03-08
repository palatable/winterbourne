package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.impl.StrictQueue;
import org.junit.Assert;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Eq.eq;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GTE.gte;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.FindM.findM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.writerNaturals;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class FindMTest {

    @Test
    public void findsNothingWhenEmpty() {
        assertEquals(new Identity<>(nothing()), findM(constantly(false), empty(pureIdentity())));
        assertEquals(new Identity<>(nothing()), findM(constantly(true), empty(pureIdentity())));
    }

    @Test
    public void findsNothingWhenNothingMatches() {
        assertEquals(new Identity<>(Maybe.<Natural>nothing()),
                     findM(gte(abs(5)), takeM(atLeastOne(5), naturalsM(pureIdentity()))));
    }

    @Test
    public void findsSomething() {
        assertEquals(new Identity<>(natural(5)),
                     findM(gte(abs(5)), naturalsM(pureIdentity())));
    }

    @Test
    public void findRequiresNoAdditionalEffortAfterPositiveMatch() {
        Assert.<Writer<StrictQueue<Natural>, Maybe<Natural>>>assertThat(
                findM(eq(zero()), writerNaturals()),
                whenRunWith(monoid(StrictQueue::snocAll, strictQueue()),
                            equalTo(tuple(natural(0), strictQueue(zero())))));
    }
}
