/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.functions;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Try<V> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <V> Try<V> toTry(final Optional<V> optionalV) {
        return optionalV.map(Try::success).orElseGet(() -> failure(new NoSuchElementException()));
    }

    public static <V> Try<V> failure(final Throwable t) {
        Objects.requireNonNull(t);
        return new Failure<>(t);
    }

    public static <V> Try<V> success(final V value) {
        Objects.requireNonNull(value);
        return new Success<>(value);
    }

    public static <T> Try<T> fallible(final Supplier<T> f) {
        Objects.requireNonNull(f);
        try {
            return Try.success(f.get());
        } catch (final Throwable t) {
            return Try.failure(t);
        }
    }

    public abstract Boolean isSuccess();

    public abstract Boolean isFailure();

    public abstract void throwException();

    public abstract Throwable getMessage();

    public abstract V get();

    public abstract <U> Try<U> map(Function<? super V, ? extends U> f);

    public abstract <U> Try<U> flatMap(Function<? super V, Try<U>> f);

    private static class Failure<V> extends Try<V> {
        private final RuntimeException exception;

        public Failure(final Throwable t) {
            super();
            this.exception = new RuntimeException(t);
        }

        public Boolean isSuccess() {
            return false;
        }

        public void throwException() {
            throw this.exception;
        }

        public V get() {
            throw exception;
        }

        public Boolean isFailure() {
            return true;
        }

        public <U> Try<U> map(final Function<? super V, ? extends U> f) {
            Objects.requireNonNull(f);
            return Try.failure(exception);
        }

        public <U> Try<U> flatMap(final Function<? super V, Try<U>> f) {
            Objects.requireNonNull(f);
            return Try.failure(exception);
        }

        public Throwable getMessage() {
            return exception;
        }
    }

    private static class Success<V> extends Try<V> {
        private final V value;

        public Success(final V value) {
            super();
            this.value = value;
        }

        public Boolean isSuccess() {
            return true;
        }

        public void throwException() {
        }

        public V get() {
            return value;
        }

        public Boolean isFailure() {
            return false;
        }

        public <U> Try<U> map(final Function<? super V, ? extends U> f) {
            Objects.requireNonNull(f);
            try {
                return Try.success(f.apply(value));
            } catch (final Throwable t) {
                return Try.failure(t);
            }
        }

        public <U> Try<U> flatMap(final Function<? super V, Try<U>> f) {
            Objects.requireNonNull(f);
            try {
                return f.apply(value);
            } catch (final Throwable t) {
                return Try.failure(t);
            }
        }

        public Throwable getMessage() {
            throw new IllegalStateException("no messages when success");
        }
    }

    /**
     * Singleton class.
     */
    private Try() {
    }
}