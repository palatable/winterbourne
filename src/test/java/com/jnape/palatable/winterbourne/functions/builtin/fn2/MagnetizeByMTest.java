package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.functions.builtin.fn2.GTE;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GTE.gte;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.MagnetizeByM.magnetizeByM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.junit.Assert.assertThat;

public class MagnetizeByMTest {

    @Test
    public void magnetizesEmpty() {
        assertThat(magnetizeByM(GTE.<Integer>gte(), empty(pureIdentity())),
                   streams());
    }

    @Test
    public void magnetizesSingleton() {
        assertThat(magnetizeByM(gte(), streamT(new Identity<>(just(1)))),
                   streams(nothing(), just(strictQueue(1))));
    }

    @Test
    public void magnetizesElementsInSeveralGroups() {
        assertThat(magnetizeByM(gte(),
                                streamT(new Identity<>(strictQueue(
                                        just(1), just(2), nothing(), just(3),
                                        just(2), just(2), just(3), nothing(),
                                        nothing(), just(2), just(1))))),
                   streams(nothing(), nothing(), nothing(), nothing(),
                           just(strictQueue(1, 2, 3)),
                           nothing(), nothing(), nothing(), nothing(),
                           just(strictQueue(2, 2, 3)),
                           just(strictQueue(2)),
                           just(strictQueue(1))));
    }

    /*
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
     */
}
