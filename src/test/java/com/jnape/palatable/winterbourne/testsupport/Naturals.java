package com.jnape.palatable.winterbourne.testsupport;

import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.UnfoldM.unfoldM;

public final class Naturals {

    private Naturals() {
    }

    public static StreamT<Writer<StrictQueue<Natural>, ?>, Natural> writerNaturals() {
        return unfoldM(n -> writer(tuple(just(tuple(just(n), n.inc())), strictQueue(n))),
                       listen((Natural) zero()));
    }
}
