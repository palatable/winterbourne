package com.jnape.palatable.winterbourne.testsupport.functions;

import com.jnape.palatable.lambda.functions.specialized.BiPredicate;
import com.jnape.palatable.lambda.functions.specialized.Predicate;
import com.jnape.palatable.shoki.api.Natural;
import com.jnape.palatable.shoki.api.Natural.NonZero;

import static com.jnape.palatable.lambda.functions.builtin.fn2.$.$;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Eq.eq;
import static com.jnape.palatable.lambda.functions.specialized.Predicate.predicate;
import static com.jnape.palatable.shoki.api.Natural.zero;

public final class DivisibleBy implements BiPredicate<NonZero, Natural> {

    private static final DivisibleBy INSTANCE = new DivisibleBy();

    private DivisibleBy() {
    }

    @Override
    public Boolean checkedApply(NonZero divisor, Natural n) {
        return eq(zero(), n.modulo(divisor));
    }

    public static BiPredicate<NonZero, Natural> divisibleBy() {
        return INSTANCE;
    }

    public static Predicate<Natural> divisibleBy(NonZero divisor) {
        return predicate($(divisibleBy(), divisor));
    }

    public static Boolean divisibleBy(NonZero divisor, Natural n) {
        return $(divisibleBy(divisor), n);
    }
}
