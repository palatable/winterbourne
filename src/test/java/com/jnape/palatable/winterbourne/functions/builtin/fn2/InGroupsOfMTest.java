package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.api.Queue;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EnqueueM.enqueueM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.InGroupsOfM.inGroupsOfM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

@RunWith(Traits.class)
public class InGroupsOfMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>,StreamT<IO<?>, StreamT<IO<?>, Object>>> testSubject() {
        return inGroupsOfM(atLeastOne(1));
    }

    @Test
    public void groupingEmptyIsEmpty() {
        assertThat(inGroupsOfM(atLeastOne(2), empty(pureIdentity())), streams());
    }

    @Test
    public void evenlyDividedGroupsOfTwo() {
        assertThat(NaturalsM.<Identity<?>>naturalsM()
                           .fmap(takeM(atLeastOne(6)))
                           .fmap(inGroupsOfM(atLeastOne(2)))
                           .<Identity<Queue<Natural, StreamT<Identity<?>, Natural>>>>fmap(enqueueM(StrictQueue::strictQueue))
                           .apply(pureIdentity())
                           .runIdentity(),
                   contains(streams(natural(0), natural(1)),
                            streams(natural(2), natural(3)),
                            streams(natural(4), natural(5))));
    }

    @Test
    public void groupsOfTwoWithLastGroupShort() {
        assertThat(NaturalsM.<Identity<?>>naturalsM()
                           .fmap(takeM(atLeastOne(5)))
                           .fmap(inGroupsOfM(atLeastOne(2)))
                           .<Identity<Queue<Natural, StreamT<Identity<?>, Natural>>>>fmap(enqueueM(StrictQueue::strictQueue))
                           .apply(pureIdentity())
                           .runIdentity(),
                   contains(streams(natural(0), natural(1)),
                            streams(natural(2), natural(3)),
                            streams(natural(4))));
    }
}
