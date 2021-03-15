package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.tell;
import static com.jnape.palatable.lambda.monoid.builtin.Join.join;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ForEachM.forEachM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenExecutedWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ForEachMTest {

    @Test
    public void operatesOnAllElisionsAndEmissions() {
        assertThat(forEachM(maybeX -> tell(maybeX.fmap(Object::toString).orElse("_")),
                            streamT(listen(just(1)), listen(nothing()), listen(just(2)))),
                   whenExecutedWith(join(), equalTo("1_2")));
    }
}