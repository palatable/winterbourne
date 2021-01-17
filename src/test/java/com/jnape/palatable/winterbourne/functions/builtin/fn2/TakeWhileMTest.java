package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.traitor.annotations.TestTraits;
import com.jnape.palatable.traitor.runners.Traits;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jnape.palatable.winterbourne.testsupport.traits.FiniteStream;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.GTE.gte;
import static com.jnape.palatable.lambda.functions.builtin.fn2.LT.lt;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.api.Natural.*;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeWhileM.takeWhileM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(Traits.class)
public class TakeWhileMTest {

    @TestTraits({FiniteStream.class})
    public Fn1<StreamT<IO<?>, Object>, StreamT<IO<?>, Object>> testSubject() {
        return takeWhileM(constantly(true));
    }

    @Test
    public void takesNothingWhenEmpty() {
        StreamT<Identity<?>, Natural> numbers = empty(pureIdentity());
        assertEquals(nothing(),
                     takeWhileM(constantly(true), numbers)
                             .<Identity<Maybe<Tuple2<Maybe<Natural>, StreamT<Identity<?>, Natural>>>>>runStreamT()
                             .runIdentity());
    }

    @Test
    public void takesLeadingSkipsBeforeMatch() {
        StreamT<Identity<?>, Natural> numbers = streamT(new Identity<>(strictQueue(nothing(), nothing(), just(zero()))));
        assertThat(takeWhileM(constantly(true), numbers),
                   streams(nothing(), nothing(), just(zero())));
    }

    @Test
    public void takesLeadingSkipsBeforeNonMatch() {
        StreamT<Identity<?>, Natural> numbers = streamT(new Identity<>(strictQueue(nothing(), nothing(), just(zero()))));
        assertThat(takeWhileM(constantly(false), numbers),
                   streams(nothing(), nothing()));
    }

    @Test
    public void takesNothingWhenFirstEmissionDoesntMatch() {
        StreamT<Identity<?>, Natural> numbers = naturalsM(pureIdentity());
        assertEquals(nothing(),
                     takeWhileM(gte(one()), numbers)
                             .<Identity<Maybe<Tuple2<Maybe<Natural>, StreamT<Identity<?>, Natural>>>>>runStreamT()
                             .runIdentity());
    }

    @Test
    public void takesStuffPreservingSkips() {
        StreamT<Identity<?>, Natural> numbers = dropM(atLeastOne(3), naturalsM(pureIdentity()));
        assertThat(takeWhileM(lt(abs(5)), numbers),
                   streams(nothing(), nothing(), nothing(), natural(3), natural(4)));
    }
}
