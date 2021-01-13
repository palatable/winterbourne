package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functor.Functor;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.framework.Subjects;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import testsupport.traits.ApplicativeLaws;
import testsupport.traits.Equivalence;
import testsupport.traits.FunctorLaws;
import testsupport.traits.MonadLaws;
import testsupport.traits.MonadRecLaws;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Maybe.pureMaybe;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Id.id;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.pureWriter;
import static com.jnape.palatable.lambda.functor.builtin.Writer.tell;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pin;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.lambda.monoid.builtin.Join.join;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.traitor.framework.Subjects.subjects;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.StreamT.unfold;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenEmissionsFolded;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenExecutedWith;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IOMatcher.yieldsValue;
import static testsupport.traits.Equivalence.equivalence;

@RunWith(Traits.class)
public class StreamTTest {

    private static final StreamT<Identity<?>, Integer> EXPLICITLY_EMPTY = empty(pureIdentity());
    private static final StreamT<Identity<?>, Integer> IMPLICITLY_EMPTY = streamT(() -> new Identity<>(nothing()), pureIdentity());

    @TestTraits({FunctorLaws.class, ApplicativeLaws.class, MonadLaws.class, MonadRecLaws.class})
    public Subjects<Equivalence<StreamT<Identity<?>, ?>>> testSubject() {
        Fn1<? super StreamT<Identity<?>, ?>, Object> inTermsOfSkipsAndEmissions = streamT -> streamT
                .<StrictQueue<Maybe<?>>, Identity<StrictQueue<Maybe<?>>>>fold(
                        (as, maybeA) -> new Identity<>(as.snoc(maybeA)),
                        new Identity<>(strictQueue()))
                .runIdentity();
        return subjects(equivalence(EXPLICITLY_EMPTY, inTermsOfSkipsAndEmissions),
                        equivalence(IMPLICITLY_EMPTY, inTermsOfSkipsAndEmissions),
                        equivalence(streamT(new Identity<>(strictQueue(just(1)))), inTermsOfSkipsAndEmissions),
                        equivalence(streamT(new Identity<>(strictQueue(just(1), just(2), just(3)))), inTermsOfSkipsAndEmissions),
                        equivalence(streamT(new Identity<>(strictQueue(just(1), nothing(), just(3)))), inTermsOfSkipsAndEmissions),
                        equivalence(streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing()))), inTermsOfSkipsAndEmissions),
                        equivalence(streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing()))), inTermsOfSkipsAndEmissions));
    }

    @Test
    public void runStreamTForcesTheMinimumWorkNecessaryToEmitOrSkipNextElement() {
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
    public void liftingResultsInSingleton() {
        assertThat(empty(pureWriter()).lift(new Identity<>(0)),
                   streams(just(0)));
    }

    @Test
    public void skipsArePreserved() {
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .fmap(id()),
                   streams(just(1), nothing(), just(2)));

        assertThat(streamT(new Identity<>(strictQueue(just(1))))
                           .zip(streamT(new Identity<>(just(id())), new Identity<>(nothing()), new Identity<>(just(id())))),
                   streams(just(1), nothing(), just(1)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .zip(streamT(new Identity<>(just(id())))),
                   streams(just(1), nothing(), just(2)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .zip(streamT(new Identity<>(just(id())), new Identity<>(nothing()), new Identity<>(just(id())))),
                   streams(just(1), nothing(), just(1), nothing(), just(2), nothing(), just(2)));

        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .flatMap(x -> streamT(new Identity<>(just(x)))),
                   streams(just(1), nothing(), just(2)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .flatMap(x -> streamT(new Identity<>(just(x)), new Identity<>(nothing()), new Identity<>(just(x)))),
                   streams(just(1), nothing(), just(1), nothing(), just(2), nothing(), just(2)));

        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .trampolineM(x -> streamT(new Identity<>(just(terminate(x))))),
                   streams(just(1), nothing(), just(2)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .trampolineM(x -> streamT(new Identity<>(just(terminate(x))), new Identity<>(nothing()), new Identity<>(just(terminate(x))))),
                   streams(just(1), nothing(), just(1), nothing(), just(2), nothing(), just(2)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .trampolineM(x -> x < 3
                                             ? streamT(new Identity<>(just(terminate(x))), new Identity<>(nothing()), new Identity<>(just(recurse(x + 1))))
                                             : streamT(new Identity<>(just(terminate(x))))),
                   streams(just(1), nothing(), just(2), nothing(), just(3), nothing(), just(2), nothing(), just(3)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .trampolineM(x -> empty(pureIdentity())),
                   streams(nothing()));
        assertThat(empty(pureIdentity()).trampolineM(x -> streamT(new Identity<>(just(terminate(x))))),
                   streams());
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(2))))
                           .trampolineM(x -> streamT(new Identity<>(nothing()))),
                   streams(nothing(), nothing(), nothing()));
    }

    @Test
    public void trampolineWithMultipleRecursionsAndTermination() {
        StreamT<Identity<?>, Integer> firstFour = streamT(new Identity<>(strictQueue(just(1), just(2), just(3), just(4))));
        StreamT<Identity<?>, Integer> trampolined = firstFour
                .trampolineM(x -> x % 3 == 0 && x < 30
                                  ? streamT(new Identity<>(strictQueue(just(terminate(x + 10)),
                                                                       just(recurse(x + 11)),
                                                                       just(recurse(x + 12)),
                                                                       just(recurse(x + 13)))))
                                  : streamT(new Identity<>(just(terminate(x)))));

        assertThat(trampolined, streams(just(1), just(2), just(13), just(14), just(25), just(26), just(37), just(38), just(39), just(40), just(28), just(16), just(4)));
    }

    @Test
    public void emptyStreamT() {
        assertThat(empty(pureIdentity()), streams());
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
        assertThat(streamT(strictQueue(), pureIdentity()), streams());
    }

    @Test
    public void streamTFromCollectionInsideEffect() {
        assertThat(streamT(new Identity<>(strictQueue(just(1), just(2), just(3)))),
                   streams(just(1), just(2), just(3)));
        assertThat(streamT(new Identity<>(strictQueue(just(1), nothing(), just(3)))),
                   streams(just(1), nothing(), just(3)));
        assertThat(streamT(new Identity<>(strictQueue(nothing(), nothing(), nothing()))),
                   streams(nothing(), nothing(), nothing()));
        assertThat(streamT(new Identity<>(strictQueue())), streams());
    }

    @Test
    public void unfoldEmitsOrSkipsUntilFinished() {
        assertThat(unfold(x -> new Identity<>(
                           x > 5 ? nothing()
                                 : x % 2 == 0
                                   ? just(tuple(nothing(), x + 1))
                                   : just(tuple(just(x), x + 1))), new Identity<>(1)),
                   streams(just(1), nothing(), just(3), nothing(), just(5)));
    }

    @Test
    public void mapStreamTTransformsTheEffectButPreservesSkipsAndEmits() {
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
        assertThat(streamT(new Identity<>(just(1))).snoc(new Identity<>(just(2))), streams(just(1), just(2)));
        assertThat(streamT(new Identity<>(just(1))).snoc(new Identity<>(nothing())), streams(just(1), nothing()));
        assertThat(EXPLICITLY_EMPTY.snoc(new Identity<>(just(0))), streams(just(0)));
        assertThat(EXPLICITLY_EMPTY.snoc(new Identity<>(nothing())), streams(nothing()));
    }

    @Test
    public void concat() {
        assertThat(EXPLICITLY_EMPTY.concat(EXPLICITLY_EMPTY), streams());
        assertThat(EXPLICITLY_EMPTY.concat(IMPLICITLY_EMPTY), streams());
        assertThat(IMPLICITLY_EMPTY.concat(EXPLICITLY_EMPTY), streams());
        assertThat(IMPLICITLY_EMPTY.concat(IMPLICITLY_EMPTY), streams());

        StreamT<Identity<?>, Integer> oneTwoThree = streamT(new Identity<>(strictQueue(just(1), just(2), just(3))));
        StreamT<Identity<?>, Integer> fourFiveSix = streamT(new Identity<>(strictQueue(just(4), just(5), just(6))));

        StreamTMatcher<Integer, Identity<?>> streamsOneThroughSix = streams(just(1), just(2), just(3), just(4), just(5), just(6));
        assertThat(oneTwoThree.concat(fourFiveSix),
                   streamsOneThroughSix);
        assertThat(oneTwoThree.concat(EXPLICITLY_EMPTY).concat(fourFiveSix).concat(IMPLICITLY_EMPTY),
                   streamsOneThroughSix);
        assertThat(EXPLICITLY_EMPTY.concat(oneTwoThree).concat(IMPLICITLY_EMPTY).concat(fourFiveSix),
                   streamsOneThroughSix);
    }

    @Test
    public void foldCutOperatesOnSkippedAndEmittedValues() {
        assertThat(StreamT.<Writer<String, ?>, Integer>streamT(listen(just(1)),
                                                               listen(nothing()),
                                                               listen(just(2)),
                                                               listen(just(3)))
                           .<Integer, Writer<String, Integer>>foldCut(
                                   (x, maybeY) -> maybeY.match(
                                           constantly(writer(tuple(recurse(x), "_"))),
                                           y -> writer(tuple(y == 2 ? RecursiveResult.<Integer, Integer>terminate(x + y)
                                                                    : RecursiveResult.<Integer, Integer>recurse(x + y),
                                                             y.toString()))),
                                   writer(tuple(0, "0"))),
                   whenRunWith(join(), equalTo(tuple(3, "01_2"))));
    }

    @Test
    public void foldCutAwaitOperatesOnlyOnEmittedValues() {
        assertThat(StreamT.<Writer<String, ?>, Integer>streamT(listen(just(1)),
                                                               listen(nothing()),
                                                               listen(just(2)),
                                                               listen(just(3)))
                           .<Integer, Writer<String, Integer>>foldCutAwait(
                                   (x, y) -> writer(tuple(y == 2 ? terminate(x + y) : recurse(x + y), y.toString())),
                                   writer(tuple(0, "0"))),
                   whenRunWith(join(), equalTo(tuple(3, "012"))));
    }

    @Test
    public void foldOperatesOnSkippedAndEmittedValues() {
        Fn2<StrictQueue<Maybe<Integer>>, Maybe<Integer>, MonadRec<StrictQueue<Maybe<Integer>>, Identity<?>>> toCollection = (as, maybeA) -> new Identity<>(as.snoc(maybeA));
        assertEquals(new Identity<>(StrictQueue.<Maybe<Integer>>strictQueue()),
                     EXPLICITLY_EMPTY.fold(toCollection, new Identity<>(strictQueue())));
        assertEquals(new Identity<>(StrictQueue.<Maybe<Integer>>strictQueue()), IMPLICITLY_EMPTY.fold(toCollection, new Identity<>(strictQueue())));
        assertEquals(new Identity<>(strictQueue(just(1), just(2), just(3))),
                     streamT(new Identity<>(strictQueue(just(1), just(2), just(3)))).fold(toCollection, new Identity<>(strictQueue())));
        assertEquals(new Identity<>(StrictQueue.<Maybe<Integer>>strictQueue(just(1), nothing(), just(3))),
                     streamT(new Identity<>(strictQueue(just(1), nothing(), just(3)))).fold(toCollection, new Identity<>(strictQueue())));
    }

    @Test
    public void foldAwaitOperatesOnlyOnEmittedValues() {
        assertEquals(new Identity<>(0), EXPLICITLY_EMPTY.foldAwait((x, y) -> new Identity<>(x + y), new Identity<>(0)));
        assertEquals(new Identity<>(0), IMPLICITLY_EMPTY.foldAwait((x, y) -> new Identity<>(x + y), new Identity<>(0)));
        assertEquals(new Identity<>(6),
                     streamT(new Identity<>(strictQueue(just(1), just(2), just(3))))
                             .foldAwait((x, y) -> new Identity<>(x + y),
                                        new Identity<>(0)));
        assertEquals(new Identity<>(4),
                     streamT(new Identity<>(strictQueue(just(1), nothing(), just(3))))
                             .foldAwait((x, y) -> new Identity<>(x + y),
                                        new Identity<>(0)));
    }

    @Test
    public void forEachOperatesOnSkippedAndEmittedValues() {
        assertThat(StreamT.<Writer<String, ?>, Integer>streamT(listen(just(1)), listen(nothing()), listen(just(2)))
                           .<Writer<String, Unit>>forEach(maybeX -> tell(maybeX.fmap(Object::toString).orElse("_"))),
                   whenExecutedWith(join(), equalTo("1_2")));
    }

    @Test
    public void forEachAwaitOperatesOnlyOnEmittedValues() {
        assertThat(StreamT.<Writer<String, ?>, Integer>streamT(listen(just(1)), listen(nothing()), listen(just(2)))
                           .<Writer<String, Unit>>forEachAwait(x -> tell(x.toString())),
                   whenExecutedWith(join(), equalTo("12")));
    }

    @Test
    public void awaitAllRunsEntireStream() {
        assertThat(streamT(writer(tuple(just(1), "1")),
                           writer(tuple(nothing(), "_")),
                           writer(tuple(nothing(), "2")),
                           writer(tuple(nothing(), "_")))
                           .<Writer<String, Unit>>awaitAll(),
                   whenExecutedWith(join(), equalTo("1_2_")));
    }

    @Test
    public void zipCartesianProductWithObservableEffectsAndSkips() {
        StreamT<Writer<String, ?>, Integer> as = streamT(writer(tuple(just(1), "1")),
                                                         writer(tuple(nothing(), "a")),
                                                         writer(tuple(just(2), "2")),
                                                         writer(tuple(just(3), "3")));
        StreamT<Writer<String, ?>, Integer> bs = streamT(writer(tuple(just(4), "4")),
                                                         writer(tuple(just(5), "5")),
                                                         writer(tuple(nothing(), "b")),
                                                         writer(tuple(just(6), "6")));

        assertThat(as.zip(bs.fmap(b -> a -> tuple(a, b))),
                   whenFolded(whenRunWith(join(), equalTo(tuple(
                           strictQueue(just(tuple(1, 4)), just(tuple(1, 5)), nothing(), just(tuple(1, 6)),
                                       nothing(),
                                       just(tuple(2, 4)), just(tuple(2, 5)), nothing(), just(tuple(2, 6)),
                                       just(tuple(3, 4)), just(tuple(3, 5)), nothing(), just(tuple(3, 6))),
                           "145b6a425b635b6"
                   ))), pureWriter()));
    }

    @Test(timeout = 500)
    public void zipComposesInParallel() {
        CountDownLatch a = new CountDownLatch(1);
        CountDownLatch b = new CountDownLatch(1);
        CountDownLatch c = new CountDownLatch(1);

        StreamT<IO<?>, Integer> foo = streamT(io(() -> {
            a.countDown();
            b.await();
            c.countDown();
            return just(1);
        }));

        StreamT<IO<?>, Integer> bar = streamT(io(() -> {
            a.await();
            b.countDown();
            c.await();
            return just(2);
        }));

        assertThat(pin(foo.zip(bar.fmap(y -> x -> tuple(x, y))).fold((as, maybeA) -> io(as.snoc(maybeA)), io(strictQueue())),
                       Executors.newFixedThreadPool(2)),
                   yieldsValue(equalTo(strictQueue(just(tuple(1, 2))))));
    }

    @Test
    public void flatMapToEmptyStackSafety() {
        assertEquals(new Identity<>(UNIT), unfold(x -> new Identity<>(x <= STACK_EXPLODING_NUMBER ? just(tuple(just(x), x + 1)) : nothing()), new Identity<>(1))
                .flatMap(constantly(empty(pureIdentity())))
                .forEach(constantly(new Identity<>(UNIT))));

        assertThat(unfold(x -> listen(x <= STACK_EXPLODING_NUMBER ? just(tuple(just(x), x + 1)) : nothing()),
                          Writer.<Integer, Integer>listen(1))
                           .flatMap(x -> streamT(() -> writer(tuple(nothing(), x)), pureWriter())),
                   whenFolded(whenRunWith(monoid(Integer::sum, 0), equalTo(tuple(
                           strictQueue(),
                           1250025000
                   ))), pureWriter()));
    }

    @Test
    public void flatMapCostsNoMoreEffortThanRequiredToMakeEmitOrSkipDetermination() {
        AtomicInteger flatMapCost = new AtomicInteger(0);
        AtomicInteger unfoldCost  = new AtomicInteger(0);

        StreamT<Identity<?>, Integer> costRecordingImpureStreamT = unfold(x -> {
            unfoldCost.incrementAndGet();
            return new Identity<>(x <= 10 ? just(tuple(just(x), x + 1)) : nothing());
        }, new Identity<>(1))
                .flatMap(x -> {
                    flatMapCost.incrementAndGet();
                    return streamT(new Identity<>(just(x)));
                });

        assertEquals(just(1),
                     costRecordingImpureStreamT
                             .<Identity<Maybe<Tuple2<Maybe<Integer>, StreamT<Identity<?>, Integer>>>>>runStreamT()
                             .runIdentity()
                             .flatMap(Tuple2::_1));

        assertEquals(1, flatMapCost.get());
        assertEquals(1, unfoldCost.get());
    }
}