package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.api.Natural.NonZero;
import com.jnape.palatable.shoki.api.Queue;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.MagnetizeM.magnetizeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EnqueueM.enqueueM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ReplicateM.replicateM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

@RunWith(Traits.class)
public class MagnetizeMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>, StreamT<IO<?>, StreamT<IO<?>, Object>>> testSubject() {
        return magnetizeM();
    }

    @Test
    public void groupsLikeElements() {
        assertThat(NaturalsM.<Identity<?>>naturalsM()
                           .fmap(ns -> ns.fmap(Natural::inc)
                                         .flatMap(n -> replicateM(n, new Identity<>(n))))
                           .fmap(takeM(atLeastOne(10)))
                           .fmap(magnetizeM())
                           .<Identity<Queue<Natural, StreamT<Identity<?>, NonZero>>>>fmap(enqueueM(StrictQueue::strictQueue))
                           .apply(pureIdentity())
                           .runIdentity(),
                   contains(streams(just(atLeastOne(1))),
                            streams(just(atLeastOne(2)), just(atLeastOne(2))),
                            streams(just(atLeastOne(3)), just(atLeastOne(3)), just(atLeastOne(3))),
                            streams(just(atLeastOne(4)), just(atLeastOne(4)), just(atLeastOne(4)), just(atLeastOne(4)))));
    }
}