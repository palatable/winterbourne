package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.builtin.Join.join;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AwaitMTest {

    @Test
    public void forcesTheMinimumWorkNecessaryToDetermineNextElement() {
        assertEquals(new Identity<>(Maybe.<Tuple2<Object, StreamT<Identity<?>, Object>>>nothing()),
                     awaitM(empty(pureIdentity())));

        assertEquals(new Identity<>(Maybe.<Tuple2<Object, StreamT<Identity<?>, Object>>>nothing()),
                     awaitM(streamT(() -> new Identity<>(nothing()), pureIdentity())));

        Writer<String, Maybe<Tuple2<Integer, StreamT<Writer<String, ?>, Integer>>>> awaitWriter =
                awaitM(streamT(writer(tuple(nothing(), "a")),
                               listen(just(1)),
                               writer(tuple(nothing(), "b"))));
        assertThat(awaitWriter.fmap(m -> m.fmap(Tuple2::_1)),
                   whenRunWith(join(), equalTo(tuple(just(1), "a"))));
    }
}