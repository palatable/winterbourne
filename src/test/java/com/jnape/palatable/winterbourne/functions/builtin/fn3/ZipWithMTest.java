package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.functor.builtin.Writer.pureWriter;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.ZipWithM.zipWithM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZipWithMTest {

    @Test
    public void bothEmpty() {
        assertThat(zipWithM(tupler(),
                empty(pureIdentity()),
                empty(pureIdentity())),
                streams());
    }

    @Test
    public void oneEmpty() {
        assertThat(zipWithM(tupler(),
                naturalsM(pureIdentity()),
                empty(pureIdentity())),
                streams());
    }

    @Test
    public void equalLength() {
        StreamT<Identity<?>, Natural> nats = takeM(atLeastOne(3), naturalsM(pureIdentity()));

        assertThat(zipWithM(tupler(),
                nats,
                nats.fmap(Natural::inc)),
                streams(just(tuple(zero(), one())), just(tuple(abs(1), abs(2))), just(tuple(abs(2), abs(3)))));
    }

    @Test
    public void unequalLength() {
        StreamT<Identity<?>, Natural> nats = takeM(atLeastOne(3), naturalsM(pureIdentity()));
        StreamT<Identity<?>, NonZero> natsPlusOne = takeM(atLeastOne(4), naturalsM(pureIdentity())).fmap(Natural::inc);

        assertThat(zipWithM(tupler(),
                nats,
                natsPlusOne),
                streams(just(tuple(zero(), one())), just(tuple(abs(1), abs(2))), just(tuple(abs(2), abs(3)))));

        assertThat(zipWithM(Tupler2.<Natural, NonZero>tupler().flip(),
                natsPlusOne,
                nats),
                streams(just(tuple(zero(), one())), just(tuple(abs(1), abs(2))), just(tuple(abs(2), abs(3)))));
    }

    @Test
    public void onlyElisions() {
        StreamT<Identity<?>, Object> twoElisions = streamT(new Identity<>(strictQueue(nothing(), nothing())));
        StreamT<Identity<?>, Integer> oneElision = streamT(new Identity<>(strictQueue(nothing())));

        assertThat(zipWithM(tupler(),
                twoElisions,
                oneElision),
                streams(nothing(), nothing()));

        assertThat(zipWithM(tupler(),
                oneElision,
                twoElisions),
                streams(nothing()));
    }

    @Test
    public void elisionPrecedence() {
        assertThat(zipWithM(tupler(),
                streamT(new Identity<>(strictQueue(nothing(), nothing(), just(1), nothing(), nothing(), just(2)))),
                streamT(new Identity<>(strictQueue(just(3), nothing(), nothing(), just(4), nothing(), nothing())))),
                streams(nothing(), nothing(), just(tuple(1,3)), nothing(), nothing(), nothing(), nothing(), just(tuple(2, 4))));
    }

    @Test
    public void streamCompletionStopsRemainingEffects() {
        StreamT<Writer<StrictQueue<String>, ?>, Integer> longer = streamT(
                writer(tuple(just(1), strictQueue("a"))),
                writer(tuple(just(3), strictQueue("c"))),
                writer(tuple(just(4), strictQueue("d"))));

        StreamT<Writer<StrictQueue<String>, ?>, Integer> shorter = streamT(writer(tuple(just(2), strictQueue("b"))));

        assertThat(zipWithM(tupler(),
                longer,
                shorter),
                whenFolded(whenRunWith(monoid(StrictQueue::snocAll, strictQueue()),
                        equalTo(tuple(strictQueue(just(tuple(1, 2))), strictQueue("a", "b", "c")))),
                pureWriter()));

        assertThat(zipWithM(tupler(),
                shorter,
                longer),
                whenFolded(whenRunWith(monoid(StrictQueue::snocAll, strictQueue()),
                        equalTo(tuple(strictQueue(just(tuple(2, 1))), strictQueue("b", "a", "c")))),
                        pureWriter()));
    }
}
