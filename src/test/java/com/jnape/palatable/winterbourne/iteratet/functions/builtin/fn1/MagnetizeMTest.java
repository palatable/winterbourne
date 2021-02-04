package com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1;

import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.monad.transformer.builtin.IterateT;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.MagnetizeM.magnetizeM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn1.NaturalNumbersM.naturalNumbersM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.ReplicateM.replicateM;
import static com.jnape.palatable.winterbourne.iteratet.functions.builtin.fn2.TakeM.takeM;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static testsupport.matchers.IterateTMatcher.iterates;

public class MagnetizeMTest {

    @Test
    public void groupsLikeElements() {
        Identity<List<IterateT<Identity<?>, Integer>>> actual = magnetizeM(takeM(10, naturalNumbersM(pureIdentity())
                .flatMap(n -> replicateM(n, new Identity<>(n)))))
                .toCollection(ArrayList::new);
        assertThat(actual.runIdentity(),
                   contains(iterates(1),
                            iterates(2, 2),
                            iterates(3, 3, 3),
                            iterates(4, 4, 4, 4)));
    }
}