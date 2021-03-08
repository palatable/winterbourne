package com.jnape.palatable.winterbourne.testsupport.functions;

import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.winterbourne.StreamT;

import java.util.concurrent.atomic.AtomicReference;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn1.withSelf;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functor.builtin.Writer.listen;
import static com.jnape.palatable.lambda.functor.builtin.Writer.writer;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.StreamT.unfold;

public final class ImpureNaturals {

    public static StreamT<IO<?>, Natural> impureNaturals() {
        return $(withSelf((self, ref) -> streamT(
                () -> io(() -> just(tuple(just(ref.getAndUpdate(Natural::inc)), self.apply(ref)))), pureIO())),
                 new AtomicReference<Natural>(zero()));
    }

    public static StreamT<Writer<StrictQueue<Natural>, ?>, Natural> writerNaturals() {
        return unfold(n -> writer(tuple(just(tuple(just(n), n.inc())), strictQueue(n))),
                      listen((Natural) zero()));
    }
}
