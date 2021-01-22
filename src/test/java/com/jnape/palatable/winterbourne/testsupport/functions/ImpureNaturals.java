package com.jnape.palatable.winterbourne.testsupport.functions;

import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;

import java.util.concurrent.atomic.AtomicReference;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.Fn1.withSelf;
import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.zero;
import static com.jnape.palatable.winterbourne.StreamT.streamT;

public final class ImpureNaturals {

    public static StreamT<IO<?>, Natural> impureNaturals() {
        return $(withSelf((self, ref) -> streamT(
                () -> io(() -> just(tuple(just(ref.getAndUpdate(Natural::inc)), self.apply(ref)))), pureIO())),
                 new AtomicReference<Natural>(zero()));
    }
}
