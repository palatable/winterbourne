package com.jnape.palatable.winterbourne.functions.builtin.fn4;

import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.winterbourne.StreamT;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.io.IO.io;
import static com.jnape.palatable.lambda.io.IO.pureIO;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.NaturalsM.naturalsM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.DropM.dropM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn4.FoldMapM.foldMapM;
import static com.jnape.palatable.winterbourne.testsupport.matchers.StreamTMatcher.whenFolded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static testsupport.matchers.IOMatcher.yieldsValue;
import static testsupport.matchers.IterableMatcher.iterates;

public class FoldMapMTest {

    @Test
    public void mapsFoldsAndPreservesSkips() {
        StreamT<IO<?>, Natural> numbers = takeM(atLeastOne(5), dropM(atLeastOne(10), naturalsM(pureIO())));

        int initialCount = 0;
        AtomicInteger finalCount = new AtomicInteger(-1);

        assertThat(foldMapM(count -> io(() -> finalCount.set(count)),
                            (n, count) -> io(tuple(Integer.toString(n.intValue()), count + 1)),
                            io(initialCount),
                            numbers),
                   whenFolded(yieldsValue(iterates(
                           nothing(), nothing(), nothing(), nothing(), nothing(),
                           nothing(), nothing(), nothing(), nothing(), nothing(),
                           just("10"), just("11"), just("12"), just("13"), just("14"))), pureIO()));
        assertThat(finalCount.get(), equalTo(5));
    }
}