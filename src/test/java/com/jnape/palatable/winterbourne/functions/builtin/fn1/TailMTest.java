package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import org.junit.Test;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.StreamT.empty;
import static com.jnape.palatable.winterbourne.StreamT.streamT;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.TailM.tailM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static org.junit.Assert.assertThat;
import static testsupport.matchers.IOMatcher.yieldsValue;
import static testsupport.matchers.IterableMatcher.isEmpty;
import static testsupport.matchers.IterableMatcher.iterates;

public class TailMTest {

    @Test
    public void emptyBegetsEmpty() {
        assertThat(tailM(empty(pureIO())), whenFolded(yieldsValue(isEmpty()), pureIO()));
        assertThat(tailM(streamT(io(strictQueue(nothing(), nothing(), nothing())))),
                   whenFolded(yieldsValue(iterates(nothing(), nothing(), nothing())), pureIO()));
    }

    @Test
    public void singletonBegetsEmpty() {
        assertThat(tailM(streamT(io(just("manha manha")))),
                   whenFolded(yieldsValue(iterates(nothing())), pureIO()));
        assertThat(tailM(streamT(io(strictQueue(nothing(), just("manha manha"), nothing())))),
                   whenFolded(yieldsValue(iterates(nothing(), nothing(), nothing())), pureIO()));
    }

    @Test
    public void pluralSkipsFirstEmission() {
        assertThat(tailM(streamT(io(strictQueue(just("manha manha"), just("doot dooo"), just("do do do"))))),
                   whenFolded(yieldsValue(iterates(nothing(), just("doot dooo"), just("do do do"))), pureIO()));
        assertThat(tailM(streamT(io(strictQueue(nothing(), just("manha manha"), nothing(),
                                                just("manha manha"), just("doot dooo"), nothing(),
                                                just("do do do"), nothing(), nothing())))),
                   whenFolded(yieldsValue(iterates(nothing(), nothing(), nothing(),
                                                   just("manha manha"), just("doot dooo"), nothing(),
                                                   just("do do do"), nothing(), nothing())), pureIO()));
    }
}