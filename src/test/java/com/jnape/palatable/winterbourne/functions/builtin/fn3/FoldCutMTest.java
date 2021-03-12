package com.jnape.palatable.winterbourne.functions.builtin.fn3;

import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.monoid.builtin.Join.join;
import static com.jnape.palatable.shoki.impl.StrictStack.strictStack;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.FoldCutM.foldCutM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FoldCutMTest {

    @Test
    public void operatesOnSkippedAndEmittedValues() {
        assertThat(
                foldCutM(
                        (x, maybeY) -> maybeY.match(
                                constantly(writer(tuple(recurse(x), "_"))),
                                y -> writer(tuple(y == 2 ? terminate(x + y) : recurse(x + y), y.toString()))),
                        writer(tuple(0, "0")),
                        streamT(listen(strictStack(just(1), nothing(), just(2), just(3))))),
                whenRunWith(join(), equalTo(tuple(3, "01_2"))));
    }
}