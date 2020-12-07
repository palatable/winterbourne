package com.jnape.palatable.winterbourne.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import org.junit.Test;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.winterbourne.functions.builtin.fn1.IndexM.indexM;
import static com.jnape.palatable.winterbourne.functions.builtin.fn2.ReplicateM.replicateM;
import static org.junit.Assert.assertThat;
import static testsupport.matchers.IterateTMatcher.iterates;

public class IndexMTest {

    @Test
    public void indexes() {
        assertThat(indexM(replicateM(5, new Identity<>('a'))),
                   iterates(tuple(1, 'a'), tuple(2, 'a'), tuple(3, 'a'), tuple(4, 'a'), tuple(5, 'a')));
    }
}