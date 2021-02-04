package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.lambda.monad.transformer.builtin.IterateT.empty;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.InGroupsOfM.inGroupsOfM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static testsupport.matchers.IterateTMatcher.isEmpty;
import static testsupport.matchers.IterateTMatcher.iterates;

public class InGroupsOfMTest {

    @Test
    public void groupingEmptyIsEmpty() {
        assertThat(inGroupsOfM(2, empty(pureIdentity())), isEmpty());
    }

    @Test
    public void evenlyDividedGroupsOfTwo() {
        Identity<List<IterateT<Identity<?>, Integer>>> actual =
                inGroupsOfM(2, takeM(6, naturalNumbersM(pureIdentity())))
                        .toCollection(ArrayList::new);
        assertThat(actual.runIdentity(),
                   contains(iterates(1, 2), iterates(3, 4), iterates(5, 6)));
    }

    @Test
    public void groupsOfTwoWithLastGroupShort() {
        Identity<List<IterateT<Identity<?>, Integer>>> actual =
                inGroupsOfM(2, takeM(5, naturalNumbersM(pureIdentity())))
                        .toCollection(ArrayList::new);
        assertThat(actual.runIdentity(),
                   contains(iterates(1, 2), iterates(3, 4), iterates(5)));
    }
}