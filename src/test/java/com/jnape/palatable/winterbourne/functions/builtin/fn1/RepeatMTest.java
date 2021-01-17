package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.shoki.api.Natural;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Snoc.snoc;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.HeadM.headM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.RepeatM.repeatM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static java.util.Collections.emptyList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;
import static testsupport.matchers.IOMatcher.yieldsValue;
import static testsupport.matchers.IterableMatcher.iterates;

public class RepeatMTest {

    @Test
    public void repeatsElement() {
        assertThat(takeM(atLeastOne(10), repeatM(new Identity<>(1))),
                   streams(just(1), just(1), just(1), just(1), just(1), just(1), just(1), just(1), just(1), just(1)));
    }

    @Test
    public void runsEffectEachTime() {
        Tuple2<Iterable<String>, Natural> actual =
                takeM(atLeastOne(10), repeatM(writer(tuple("aye", abs(1)))))
                        .foldAwait((acc, v) -> listen(snoc(v, acc)),
                                   Writer.<Natural, Iterable<String>>listen(emptyList()))
                        .runWriter(monoid(Natural::plus, zero()));
        assertThat(actual._1(), iterates("aye", "aye", "aye", "aye", "aye", "aye", "aye", "aye", "aye", "aye"));
        assertEquals(abs(10), actual._2());
    }

    @Test
    public void repeatsALotOfThings() {
        assertEquals(new Identity<>(just(1)), headM(dropM(atLeastOne(STACK_EXPLODING_NUMBER - 1), repeatM(new Identity<>(1)))));
    }

    @Test
    public void rerunsTheEffect() {
        AtomicReference<Natural> ref = new AtomicReference<>(zero());
        assertThat(takeM(atLeastOne(5), repeatM(io(() -> ref.getAndUpdate(Natural::inc)))),
                   whenFolded(yieldsValue(equalTo(
                           strictQueue(just(abs(0)), just(abs(1)), just(abs(2)), just(abs(3)), just(abs(4))))),
                              pureIO()));
    }
}
