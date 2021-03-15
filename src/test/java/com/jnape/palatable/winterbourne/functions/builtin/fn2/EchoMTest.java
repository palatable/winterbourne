package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.shoki.impl.StrictQueue;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.functor.builtin.Writer.pureWriter;
import static com.jnape.palatable.lambda.monoid.Monoid.monoid;
import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.shoki.api.Natural.natural;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.EchoM.echoM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.Naturals.writerNaturals;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.streams;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static com.jnape.palatable.winterbourne.testsupport.matchers.WriterMatcher.whenRunWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class EchoMTest {

    @Test
    public void echoesEachElement() {
        assertThat(takeM(atLeastOne(9), echoM(atLeastOne(3), naturalsM(pureIdentity()))),
                   streams(natural(0), natural(0), natural(0),
                           natural(1), natural(1), natural(1),
                           natural(2), natural(2), natural(2)));
    }

    @Test
    public void effectIsNotRepeated() {
        assertThat(echoM(atLeastOne(3), takeM(atLeastOne(3), writerNaturals())),
                   whenFolded(whenRunWith(monoid(StrictQueue::snocAll, strictQueue()),
                                          equalTo(tuple(strictQueue(natural(0), natural(0), natural(0),
                                                                    natural(1), natural(1), natural(1),
                                                                    natural(2), natural(2), natural(2)),
                                                        strictQueue(abs(0), abs(1), abs(2))))),
                              pureWriter()));
    }
}