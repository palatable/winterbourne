package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn2.curried;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.api.Natural.one;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CycleM.cycleM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.FoldCutM.foldCutM;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.writerNaturals;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenExecutedWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CycleMTest {

    @Test
    public void cyclesEmpty() {
        assertEquals(new Identity<>(strictQueue(nothing(), nothing(), nothing())),
                     foldCutM(curried(into((i, xs) -> x -> new Identity<>(i == 3
                                                                          ? terminate(tuple(i, xs))
                                                                          : recurse(tuple(i + 1, xs.snoc(x)))))),
                              new Identity<>(tuple(0, StrictQueue.<Maybe<Unit>>strictQueue())),
                              cycleM(StreamT.<Identity<?>, Unit>empty(pureIdentity())))
                             .fmap(Tuple2::_2));
    }

    @Test
    public void cyclesTheSameSequence() {
        assertThat(takeM(atLeastOne(9), cycleM(takeM(atLeastOne(3), naturalsM(pureIdentity())))),
                   streams(natural(0), natural(1), natural(2),
                           natural(0), natural(1), natural(2),
                           natural(0), natural(1), natural(2)));
    }

    @Test
    public void rerunsTheEffects() {
        assertThat(takeM(atLeastOne(4), cycleM(takeM(atLeastOne(2), writerNaturals())))
                           .<Writer<StrictQueue<Natural>, Unit>>awaitAll(),
                   whenExecutedWith(monoid(StrictQueue::snocAll, strictQueue()),
                                    equalTo(strictQueue(zero(), one(), zero(), one()))));
    }
}