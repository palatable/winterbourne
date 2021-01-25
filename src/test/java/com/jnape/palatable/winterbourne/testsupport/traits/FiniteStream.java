package com.jnape.palatable.winterbourne.testsupport.traits;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.traitor.traits.Trait;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.NthM.nthM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static testsupport.matchers.IOMatcher.yieldsValue;

public class FiniteStream implements Trait<Fn1<StreamT<IO<?>, ?>, StreamT<IO<?>, ?>>> {

    @Override
    public void test(Fn1<StreamT<IO<?>, ?>, StreamT<IO<?>, ?>> fn) {
        Natural.NonZero sufficientlyInfinite = atLeastOne(1000000L);
        StreamT<IO<?>, Integer> stream = streamT(io(strictQueue(just(1), just(2), just(3))));
        @SuppressWarnings("unchecked") StreamT<IO<?>, Object> result = (StreamT<IO<?>, Object>) fn.apply(stream);

        assertThat(nthM(sufficientlyInfinite, result),
                   yieldsValue(equalTo(nothing())));
    }
}
