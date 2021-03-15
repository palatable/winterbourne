package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import org.junit.Assert;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.builtin.Join.join;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitAllM.awaitAllM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenExecutedWith;
import static org.hamcrest.core.IsEqual.equalTo;

public class AwaitAllMTest {

    @Test
    public void awaitAllRunsEntireStream() {
        Assert.<Writer<String, Unit>>assertThat(
                awaitAllM(streamT(writer(tuple(just(1), "1")),
                                  writer(tuple(nothing(), "_")),
                                  writer(tuple(nothing(), "2")),
                                  writer(tuple(nothing(), "_")))),
                whenExecutedWith(join(), equalTo("1_2_")));
    }
}