package com.jnape.palatable.winterbourne.testsupport.matchers;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.builtin.fn1.CatMaybes;
import com.jnape.palatable.lambda.functions.specialized.Pure;
import com.jnape.palatable.lambda.functor.builtin.Identity;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.shoki.impl.StrictQueue;
import com.jnape.palatable.shoki.interop.Shoki;
import com.jnape.palatable.winterbourne.StreamT;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.jnape.palatable.lambda.functor.builtin.Identity.pureIdentity;
import static com.jnape.palatable.shoki.impl.StrictQueue.strictQueue;
import static com.jnape.palatable.winterbourne.functions.builtin.fn3.FoldM.foldM;
import static org.hamcrest.CoreMatchers.equalTo;

public final class StreamTMatcher<A, M extends MonadRec<?, M>> extends TypeSafeMatcher<StreamT<M, A>> {
    private final Pure<M>                                             pureM;
    private final Matcher<? super MonadRec<StrictQueue<Maybe<A>>, M>> matcher;

    private StreamTMatcher(Pure<M> pureM,
                           Matcher<? super MonadRec<StrictQueue<Maybe<A>>, M>> matcher) {
        this.pureM   = pureM;
        this.matcher = matcher;
    }

    protected boolean matchesSafely(StreamT<M, A> streamT) {
        return matcher.matches(foldM((as, maybeA) -> pureM.apply(as.snoc(maybeA)),
                                     pureM.apply(strictQueue()),
                                     streamT));
    }

    public void describeTo(Description description) {
        description.appendText("a StreamT when folded matching: ");
        description.appendDescriptionOf(matcher);
    }

    @Override
    protected void describeMismatchSafely(StreamT<M, A> streamT, Description mismatchDescription) {
        mismatchDescription.appendText("folded StreamT matched: ");
        matcher.describeMismatch(foldM((as, maybeA) -> pureM.apply(as.snoc(maybeA)),
                                       pureM.apply(strictQueue()),
                                       streamT),
                                 mismatchDescription);
    }

    public static <A, M extends MonadRec<?, M>, MAS extends MonadRec<StrictQueue<A>, M>> StreamTMatcher<A, M> whenEmissionsFolded(
            Matcher<? super MAS> matcher, Pure<M> pureM) {
        return new StreamTMatcher<>(pureM, new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(MonadRec<StrictQueue<Maybe<A>>, M> item) {
                return matcher.matches(catMaybes(item));
            }

            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }

            @Override
            protected void describeMismatchSafely(MonadRec<StrictQueue<Maybe<A>>, M> item,
                                                  Description mismatchDescription) {
                matcher.describeMismatch(catMaybes(item), mismatchDescription);
            }

            private MonadRec<StrictQueue<A>, M> catMaybes(MonadRec<StrictQueue<Maybe<A>>, M> item) {
                return item.fmap(CatMaybes::catMaybes).fmap(Shoki::strictQueue);
            }
        });
    }

    public static <A, M extends MonadRec<?, M>, MAS extends MonadRec<StrictQueue<Maybe<A>>, M>> StreamTMatcher<A, M> whenFolded(
            Matcher<? super MAS> matcher, Pure<M> pureM) {
        return new StreamTMatcher<>(pureM, new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(MonadRec<StrictQueue<Maybe<A>>, M> item) {
                return matcher.matches(item);
            }

            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }

            @Override
            protected void describeMismatchSafely(MonadRec<StrictQueue<Maybe<A>>, M> item,
                                                  Description mismatchDescription) {
                matcher.describeMismatch(item, mismatchDescription);
            }
        });
    }

    @SafeVarargs
    public static <A> StreamTMatcher<A, Identity<?>> streams(Maybe<A>... as) {
        return whenFolded(equalTo(new Identity<>(strictQueue(as))), pureIdentity());
    }

    @SafeVarargs
    public static <A> StreamTMatcher<A, Identity<?>> emits(A... as) {
        return whenEmissionsFolded(equalTo(new Identity<>(strictQueue(as))), pureIdentity());
    }
}