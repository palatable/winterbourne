package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.Functor;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Maybe.pureMaybe;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.builtin.Join.join;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.StreamT.unfold;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.isEmpty;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenEmissionsFolded;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class StreamTTest {

    @Test
    public void runStreamTForcesTheMinimumWorkNecessaryToEmitOrYieldNextElement() {
        assertEquals(new Identity<>(Maybe.<Tuple2<Maybe<Integer>, StreamT<Identity<?>, Integer>>>nothing()),
                     StreamT.<Identity<?>, Integer>streamT(() -> new Identity<>(nothing()), pureIdentity())
                             .runStreamT());

        assertThat(streamT(writer(tuple(nothing(), "a")),
                           listen(just(1)),
                           writer(tuple(nothing(), "b")))
                           .<Writer<String, Maybe<Tuple2<Maybe<Integer>, StreamT<Writer<String, ?>, Integer>>>>>runStreamT()
                           .fmap(m -> m.fmap(Tuple2::_1)),
                   whenRunWith(join(), equalTo(tuple(just(nothing()), "a"))));
    }

    @Test
    public void awaitStreamTForcesTheMinimumWorkNecessaryToDetermineNextElement() {
        assertEquals(new Identity<>(Maybe.<Tuple2<Integer, StreamT<Identity<?>, Integer>>>nothing()),
                     StreamT.<Identity<?>, Integer>streamT(() -> new Identity<>(nothing()), pureIdentity())
                             .awaitStreamT());

        assertThat(streamT(writer(tuple(nothing(), "a")),
                           listen(just(1)),
                           writer(tuple(nothing(), "b")))
                           .<Writer<String, Maybe<Tuple2<Integer, StreamT<Writer<String, ?>, Integer>>>>>awaitStreamT()
                           .fmap(m -> m.fmap(Tuple2::_1)),
                   whenRunWith(join(), equalTo(tuple(just(1), "a"))));
    }

    @Test
    public void emptyStreamT() {
        assertThat(empty(pureIdentity()), isEmpty());
    }

    @Test
    public void streamTFromVarargs() {
        assertThat(streamT(new Identity<>(just(1)), new Identity<>(just(2)), new Identity<>(just(3))),
                   streams(just(1), just(2), just(3)));
        assertThat(streamT(new Identity<>(just(1)), new Identity<>(nothing()), new Identity<>(just(3))),
                   streams(just(1), nothing(), just(3)));
        assertThat(streamT(new Identity<>(nothing()), new Identity<>(nothing()), new Identity<>(nothing())),
                   streams(nothing(), nothing(), nothing()));
        assertThat(streamT(new Identity<>(nothing())), streams(nothing()));
    }

    @Test
    public void streamTFromCollectionOfEffects() {
        assertThat(streamT(strictQueue(new Identity<>(just(1)), new Identity<>(just(2)), new Identity<>(just(3))),
                           pureIdentity()),
                   streams(just(1), just(2), just(3)));
        assertThat(streamT(strictQueue(new Identity<>(just(1)), new Identity<>(nothing()), new Identity<>(just(3))),
                           pureIdentity()),
                   streams(just(1), nothing(), just(3)));
        assertThat(streamT(strictQueue(new Identity<>(nothing()), new Identity<>(nothing()), new Identity<>(nothing())),
                           pureIdentity()),
                   streams(nothing(), nothing(), nothing()));
        assertThat(streamT(strictQueue(), pureIdentity()), isEmpty());
    }

    @Test
    public void streamTFromCollectionInsideEffect() {
        assertThat(streamT(new Identity<>(strictQueue(just(1), just(2), just(3)))),
                   streams(just(1), just(2), just(3)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(3)))),
                   streams(just(1), nothing(), just(3)));
        assertThat(streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing()))),
                   streams(nothing(), nothing(), nothing()));
        assertThat(streamT(new Identity<>(strictQueue())), isEmpty());
    }

    @Test
    public void unfoldEmitsOrYieldsUntilFinished() {
        assertThat(unfold(x -> new Identity<>(
                           x > 5 ? nothing()
                                 : x % 2 == 0
                                   ? just(tuple(nothing(), x + 1))
                                   : just(tuple(just(x), x + 1))), new Identity<>(1)),
                   streams(just(1), nothing(), just(3), nothing(), just(5)));
    }

    @Test
    public void mapStreamTTransformsTheEffectButPreservesYieldsAndEmits() {
        NaturalTransformation<Identity<?>, Maybe<?>> identityToMaybe = new NaturalTransformation<>() {
            @Override
            public <A, GA extends Functor<A, Maybe<?>>> GA apply(Functor<A, Identity<?>> fa) {
                return just(fa.<Identity<A>>coerce().runIdentity()).coerce();
            }
        };
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(3))))
                           .mapStreamT(identityToMaybe),
                   whenEmissionsFolded(equalTo(just(strictQueue(1, 3))), pureMaybe()));
    }

    @Test
    public void consAddsEffectToTheFrontOfTheStream() {
        assertThat(streamT(new Identity<>(just(1))).cons(new Identity<>(just(0))), streams(just(0), just(1)));
        assertThat(streamT(new Identity<>(just(1))).cons(new Identity<>(nothing())), streams(nothing(), just(1)));
        assertThat(empty(pureIdentity()).cons(new Identity<>(just(0))), streams(just(0)));
        assertThat(empty(pureIdentity()).cons(new Identity<>(nothing())), streams(nothing()));
    }

    @Test
    public void snocAddsEffectToTheBackOfTheStream() {
        assertThat(streamT(() -> new Identity<>(just(tuple(just(1), StreamT.<Identity<?>, Integer>empty(pureIdentity())))), pureIdentity()).snoc(new Identity<>(just(2))), streams(just(1), just(2)));
        assertThat(streamT(new Identity<>(just(1))).snoc(new Identity<>(just(2))), streams(just(1), just(2)));
        assertThat(streamT(new Identity<>(just(1))).snoc(new Identity<>(nothing())), streams(just(1), nothing()));
        assertThat(empty(pureIdentity()).snoc(new Identity<>(just(2))), streams(just(2)));
        assertThat(empty(pureIdentity()).snoc(new Identity<>(nothing())), streams(nothing()));
    }
}