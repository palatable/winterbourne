package com.jnape.palatable.winterbourne.functions.builtin.fn4;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.tell;
import static com.jnape.palatable.lambda.monoid.builtin.Join.join;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitT.awaitT;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenExecutedWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class GForEachMTest {

    @Test
    public void operatesOnAllValuesBasedOnAdvanceFunction() {
        assertThat(
                GForEachM.<Writer<String, ?>, Integer, Maybe<Integer>, Writer<String, Unit>>gForEachM(
                        StreamT::<Writer<String, Maybe<Tuple2<Maybe<Integer>, StreamT<Writer<String, ?>, Integer>>>>>
                                runStreamT,
                        maybeX -> tell(maybeX.fmap(Object::toString).orElse("_")),
                        streamT(listen(just(1)), listen(nothing()), listen(just(2)))),
                whenExecutedWith(join(), equalTo("1_2")));

        assertThat(
                GForEachM.<Writer<String, ?>, Integer, Integer, Writer<String, Unit>>gForEachM(
                        awaitT(),
                        x -> tell(x.toString()),
                        streamT(listen(just(1)), listen(nothing()), listen(just(2)))),
                whenExecutedWith(join(), equalTo("12")));
    }
}