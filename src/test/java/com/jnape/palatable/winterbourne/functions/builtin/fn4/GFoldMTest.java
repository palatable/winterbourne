package com.jnape.palatable.winterbourne.functions.builtin.fn4;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.AwaitM.awaitM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.GFoldM.gFoldM;
import static org.junit.Assert.assertEquals;

public class GFoldMTest {

    @Test
    public void operatesOnValuesFromAdvancementFunction() {
        StreamT<Identity<?>, Integer> explicitlyEmpty = empty(pureIdentity());
        StreamT<Identity<?>, Integer> implicitlyEmpty = streamT(new Identity<>(nothing()));
        StreamT<Identity<?>, Integer> emissionsOnly =
                streamT(new Identity<>(strictQueue(just(1), just(2), just(3))));
        StreamT<Identity<?>, Integer> elisions =
                streamT(new Identity<>(strictQueue(just(1), nothing(), just(3))));

        Fn1<StreamT<Identity<?>, Integer>, Identity<StrictQueue<Maybe<Integer>>>> fold =
                gFoldM(StreamT::<Identity<Maybe<Tuple2<Maybe<Integer>, StreamT<Identity<?>, Integer>>>>>runStreamT,
                       (StrictQueue<Maybe<Integer>> as, Maybe<Integer> maybeA) -> new Identity<>(as.snoc(maybeA)),
                       new Identity<>(strictQueue()));

        assertEquals(new Identity<>(strictQueue()), fold.apply(explicitlyEmpty));
        assertEquals(new Identity<>(strictQueue(nothing())), fold.apply(implicitlyEmpty));
        assertEquals(new Identity<>(strictQueue(just(1), just(2), just(3))), fold.apply(emissionsOnly));
        assertEquals(new Identity<>(strictQueue(just(1), nothing(), just(3))), fold.apply(elisions));

        Fn1<StreamT<Identity<?>, Integer>, Identity<StrictQueue<Integer>>> foldAwait =
                gFoldM(awaitM(),
                       (StrictQueue<Integer> as, Integer maybeA) -> new Identity<>(as.snoc(maybeA)),
                       new Identity<>(strictQueue()));

        assertEquals(new Identity<>(strictQueue()), foldAwait.apply(explicitlyEmpty));
        assertEquals(new Identity<>(strictQueue()), foldAwait.apply(implicitlyEmpty));
        assertEquals(new Identity<>(strictQueue(1, 2, 3)), foldAwait.apply(emissionsOnly));
        assertEquals(new Identity<>(strictQueue(1, 3)), foldAwait.apply(elisions));
    }
}