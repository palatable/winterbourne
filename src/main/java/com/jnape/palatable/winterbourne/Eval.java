package com.jnape.palatable.winterbourne;

import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.monad.Monad;

import java.util.concurrent.atomic.AtomicInteger;

import static com.jnape.palatable.lambda.functions.builtin.fn3.Times.times;

abstract class Eval<A> implements Monad<A, Eval<?>> {
    private Eval() {
    }

    public abstract A value();

    @Override
    public final <B> Eval<B> fmap(Fn1<? super A, ? extends B> fn) {
        return (Eval<B>) Monad.super.<B>fmap(fn);
    }

    @Override
    public final <B> Eval<B> zip(Applicative<Fn1<? super A, ? extends B>, Eval<?>> appFn) {
        return (Eval<B>) Monad.super.zip(appFn);
    }

    @Override
    public <B> Eval<B> flatMap(Fn1<? super A, ? extends Monad<B, Eval<?>>> f) {
        @SuppressWarnings("unchecked") Fn1<? super A, ? extends Eval<B>> fn = (Fn1<? super A, ? extends Eval<B>>) f;
        return new Composed<>(this, fn);
    }

    @Override
    public final <B> Eval<B> pure(B b) {
        return Eval.value(b);
    }

    public static <A> Eval<A> value(A a) {
        return new Value<>(a);
    }

    public static <A> Eval<A> now(Fn0<? extends A> thunk) {
        return value(thunk.apply());
    }

    public static <A> Eval<A> once(Fn0<? extends A> thunk) {
        return new Once<>(thunk);
    }

    public static <A> Eval<A> always(Fn0<? extends A> thunk) {
        return new Always<>(thunk);
    }

    @Override
    public String toString() {
        return "Eval[" + value() + "]";
    }

    private static final class Value<A> extends Eval<A> {
        private final A a;

        private Value(A a) {
            this.a = a;
        }

        @Override
        public <B> Eval<B> flatMap(Fn1<? super A, ? extends Monad<B, Eval<?>>> f) {
            return f.apply(a).coerce();
        }

        @Override
        public A value() {
            return a;
        }
    }

    private static final class Once<A> extends Eval<A> {

        private volatile A                value;
        private volatile Fn0<? extends A> thunk;

        private Once(Fn0<? extends A> thunk) {
            this.thunk = thunk;
        }

        @Override
        public <B> Eval<B> flatMap(Fn1<? super A, ? extends Monad<B, Eval<?>>> f) {
            return thunk == null ? f.apply(value).coerce() : super.flatMap(f);
        }

        @Override
        public A value() {
            if (thunk != null) {
                synchronized (this) {
                    Fn0<? extends A> thunk = this.thunk;
                    if (thunk != null) {
                        value      = thunk.apply();
                        this.thunk = null;
                    }
                }
            }
            return value;
        }
    }

    private static final class Composed<A, B> extends Eval<B> {

        private final Eval<A>                           a;
        private final Fn1<? super A, ? extends Eval<B>> b;

        private Composed(Eval<A> a, Fn1<? super A, ? extends Eval<B>> b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public B value() {
            Eval<B> eval = this;
            while (eval instanceof Eval.Composed<?, ?>) {
                eval = ((Composed<?, B>) eval).eliminateExistential(new Eliminator<>() {
                    @Override
                    public <Z> Eval<B> apply(Eval<Z> source, Fn1<? super Z, ? extends Eval<B>> fn) {
                        return source instanceof Eval.Composed<?, ?>
                               ? associateRight((Composed<?, Z>) source, fn)
                               : fn.apply(source.value());
                    }
                });
            }
            return eval.value();
        }

        private static <Z, A> Eval<A> associateRight(Composed<?, Z> source, Fn1<? super Z, ? extends Eval<A>> f) {
            return source.eliminateExistential(new Eliminator<>() {
                @Override
                public <Y> Eval<A> apply(Eval<Y> source, Fn1<? super Y, ? extends Eval<Z>> g) {
                    return new Composed<>(source, y -> new Composed<>(g.apply(y), f));
                }
            });
        }

        public <R> R eliminateExistential(Eliminator<B, R> eliminator) {
            return eliminator.apply(a, b);
        }

        private interface Eliminator<B, R> {
            <A> R apply(Eval<A> source, Fn1<? super A, ? extends Eval<B>> fn);
        }
    }

    private static final class Always<A> extends Eval<A> {
        private final Fn0<? extends A> thunk;

        private Always(Fn0<? extends A> thunk) {
            this.thunk = thunk;
        }

        @Override
        public A value() {
            return thunk.apply();
        }
    }

    public static void main(String[] args) {
        time("Eval.value", Eval.value(0));
        time("Eval.now", Eval.now(() -> 0));
        time("Eval.once", Eval.once(() -> 0));
        time("Eval.always", Eval.always(() -> 0));
    }

    private static void time(String label, Eval<Integer> eval) {
        System.out.print(label);
        int           nested     = 1000;
        int           iterations = 1000;
        int           steps      = iterations / 10;
        AtomicInteger count      = new AtomicInteger(0);
        long          start      = System.nanoTime();
        Object result = times(iterations - 1, e -> {
            if (count.incrementAndGet() % steps == 0)
                System.out.print(".");
            e.value();
            return e;
        }, times(nested, e -> e.fmap(x -> x + 1), eval)).value();
        long   end          = System.nanoTime();
        long   total        = end - start;
        String displayTotal = displayNanos(total);
        String displayAvg   = displayNanos(total / ((long) nested * iterations));
        System.out.format(".(total: %s, avg: %s): %s",
                          displayTotal,
                          displayAvg,
                          result)
                .println();
    }

    private static String displayNanos(long total) {
        int _1us = 1_000;
        int _1ms = _1us * 1000;
        int _1s  = _1ms * 1000;

        return total > _1s
               ? ((total / _1s) + ".") + (((total / _1ms) % 1000) / 100 + "s")
               : total > _1ms
                 ? (total / _1ms) + "ms"
                 : total > _1us
                   ? (total / _1us) + "us"
                   : total + "ns";
    }
}
