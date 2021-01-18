package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.builtin.fn2.GTE;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.api.Queue;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM;
import com.jnape.palatable.winterbourne.functions.builtin.fn2.NthM;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import java.util.Objects;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Drop.drop;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GTE.gte;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LT.lt;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Snoc.snoc;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Take.take;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.CycleM.cycleM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EnqueueM.enqueueM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.MagnetizeByM.magnetizeByM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.NthM.nthM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static java.util.Collections.emptyList;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IterableMatcher.iterates;

@RunWith(Traits.class)
public class MagnetizeByMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>, StreamT<IO<?>, StreamT<IO<?>, Object>>> testSubject() {
        return magnetizeByM(Objects::equals);
    }

    @Test
    public void magnetizesEmpty() {
        assertThat(magnetizeByM(GTE.<Integer>gte(), empty(pureIdentity())),
                   streams());
    }

    @Test
    public void magnetizesSingleton() {
        assertThat(magnetizeByM(gte(), streamT(new Identity<>(just(1))))
                           .foldAwait((streams, stream) -> new Identity<>(snoc(stream, streams)),
                                      new Identity<Iterable<StreamT<Identity<?>, Integer>>>(emptyList()))
                           .runIdentity(),
                   contains(streams(just(1))));
    }

    @Test
    public void magnetizesElementsInSeveralGroups() {
        assertThat(magnetizeByM(gte(),
                                streamT(new Identity<>(strictQueue(
                                        just(1), just(2), nothing(), just(3),
                                        just(2), just(2), just(3), nothing(),
                                        nothing(), just(2), just(1)))))
                           .foldAwait((streams, stream) -> new Identity<>(snoc(stream, streams)),
                                      new Identity<Iterable<StreamT<Identity<?>, Integer>>>(emptyList()))
                           .runIdentity(),
                   contains(streams(just(1), just(2), just(3)),
                            streams(just(2), just(2), just(3)),
                            streams(just(2)),
                            streams(just(1))));
    }

    @Test
    public void magnetizesLargeGroups() {
        Queue<Natural, Natural> thirdGroup =
                NaturalsM.<Identity<?>>naturalsM()
                        .fmap(takeM(atLeastOne(10_000)))
                        .fmap(cycleM())
                        .fmap(magnetizeByM(gte()))
                        .<Identity<Maybe<StreamT<Identity<?>, Natural>>>>fmap(nthM(atLeastOne(3)))
                        .apply(pureIdentity())
                        .fmap(m -> m.orElse(empty(pureIdentity())))
                        .flatMap(enqueueM(StrictQueue::strictQueue))
                        .runIdentity();

        assertThat(take(3, thirdGroup), iterates(zero(), one(), abs(2)));
        assertThat(drop(9_997, thirdGroup), iterates(abs(9_997), abs(9_998), abs(9_999)));
    }

    @Test
    public void magnetizesLotsOfSmallGroups() {
        assertThat(NthM.<Identity<?>, StreamT<Identity<?>, Natural>, Identity<Maybe<StreamT<Identity<?>, Natural>>>>nthM(
                atLeastOne(STACK_EXPLODING_NUMBER),
                magnetizeByM(lt(), naturalsM(pureIdentity())))
                           .runIdentity()
                           .orElseThrow(AssertionError::new),
                   streams(natural(STACK_EXPLODING_NUMBER)));
    }
}
