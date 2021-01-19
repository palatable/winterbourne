package com.jnape.palatable.winterbourne.functions.builtin.fn2;

import com.jnape.palatable.lambda.io.IO;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.api.Queue;
import com.jnape.palatable.shoki.impl.StrictQueue;
import org.junit.Test;

import static com.jnape.palatable.shoki.api.Natural.abs;
import static com.jnape.palatable.shoki.api.Natural.atLeastOne;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.TakeM.takeM;
import static com.jnape.palatable.winterbourne.testsupport.functions.ImpureNaturals.impureNaturals;
import static org.junit.Assert.assertThat;
import static testsupport.matchers.IOMatcher.yieldsValue;
import static testsupport.matchers.IterableMatcher.iterates;

public class EnqueueMTest {

    @Test
    public void collectsIntoAQueue() {
        assertThat(EnqueueM.<IO<?>, Natural, IO<Queue<Natural, Natural>>>enqueueM(
                StrictQueue::strictQueue, takeM(atLeastOne(5), impureNaturals())),
                   yieldsValue(iterates(abs(0), abs(1), abs(2), abs(3), abs(4))));
    }
}