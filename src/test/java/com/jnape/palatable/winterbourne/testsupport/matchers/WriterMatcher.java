package com.jnape.palatable.winterbourne.testsupport.matchers;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functor.builtin.Writer;
import com.jnape.palatable.lambda.monoid.Monoid;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public final class WriterMatcher<W, A> extends TypeSafeMatcher<Writer<W, A>> {

    private final Matcher<? super Tuple2<A, W>> expected;
    private final Monoid<W>                     wMonoid;

    private WriterMatcher(Matcher<? super Tuple2<A, W>> expected, Monoid<W> wMonoid) {
        this.expected = expected;
        this.wMonoid  = wMonoid;
    }

    @Override
    protected boolean matchesSafely(Writer<W, A> item) {
        return expected.matches(item.runWriter(wMonoid));
    }

    @Override
    public void describeTo(Description description) {
        expected.describeTo(description);
    }

    @Override
    protected void describeMismatchSafely(Writer<W, A> item, Description mismatchDescription) {
        expected.describeMismatch(item.runWriter(wMonoid), mismatchDescription);
    }

    public static <W, A> WriterMatcher<W, A> whenRunWith(Monoid<W> wMonoid, Matcher<? super Tuple2<A, W>> matcher) {
        return new WriterMatcher<>(matcher, wMonoid);
    }

    public static <W, A> WriterMatcher<W, A> whenExecutedWith(Monoid<W> wMonoid, Matcher<? super W> matcher) {
        return whenRunWith(wMonoid, new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Tuple2<A, W> item) {
                return matcher.matches(item._2());
            }

            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }

            @Override
            protected void describeMismatchSafely(Tuple2<A, W> item, Description mismatchDescription) {
                matcher.describeMismatch(item._2(), mismatchDescription);
            }
        });
    }

    public static <W, A> WriterMatcher<W, A> whenEvaluatedWith(Monoid<W> wMonoid, Matcher<? super A> matcher) {
        return whenRunWith(wMonoid, new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Tuple2<A, W> item) {
                return matcher.matches(item._1());
            }

            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }

            @Override
            protected void describeMismatchSafely(Tuple2<A, W> item, Description mismatchDescription) {
                matcher.describeMismatch(item._1(), mismatchDescription);
            }
        });
    }
}