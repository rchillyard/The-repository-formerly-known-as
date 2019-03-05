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
    private Try() {
    }

    public abstract Boolean isSuccess();

    public abstract Boolean isFailure();

    public abstract void throwException();

    public abstract Throwable getMessage();

    public abstract V get();

    public abstract <U> Try<U> map(Function<? super V, ? extends U> f);

    public abstract <U> Try<U> flatMap(Function<? super V, Try<U>> f);

    public static <V> Try<V> toTry(Optional<V> optionalV) {
        if (optionalV.isPresent()) return success(optionalV.get());
        else return failure(new NoSuchElementException());
    }

    public static <V> Try<V> failure(Throwable t) {
        Objects.requireNonNull(t);
        return new Failure<>(t);
    }

    public static <V> Try<V> success(V value) {
        Objects.requireNonNull(value);
        return new Success<>(value);
    }

    public static <T> Try<T> fallible(Supplier<T> f) {
        Objects.requireNonNull(f);
        try {
            return Try.success(f.get());
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

    private static class Failure<V> extends Try<V> {
        public Failure(Throwable t) {
            super();
            this.exception = new RuntimeException(t);
        }

        @Override
        public Boolean isSuccess() {
            return false;
        }

        @Override
        public void throwException() {
            throw this.exception;
        }

        @Override
        public V get() {
            throw exception;
        }

        @Override
        public Boolean isFailure() {
            return true;
        }

        @Override
        public <U> Try<U> map(Function<? super V, ? extends U> f) {
            Objects.requireNonNull(f);
            return Try.failure(exception);
        }

        @Override
        public <U> Try<U> flatMap(Function<? super V, Try<U>> f) {
            Objects.requireNonNull(f);
            return Try.failure(exception);
        }

        @Override
        public Throwable getMessage() {
            return exception;
        }

        private final RuntimeException exception;
    }

    private static class Success<V> extends Try<V> {
        private final V value;

        public Success(V value) {
            super();
            this.value = value;
        }

        @Override
        public Boolean isSuccess() {
            return true;
        }

        @Override
        public void throwException() {
        }

        @Override
        public V get() {
            return value;
        }

        @Override
        public Boolean isFailure() {
            return false;
        }

        @Override
        public <U> Try<U> map(Function<? super V, ? extends U> f) {
            Objects.requireNonNull(f);
            try {
                return Try.success(f.apply(value));
            } catch (Throwable t) {
                return Try.failure(t);
            }
        }

        @Override
        public <U> Try<U> flatMap(Function<? super V, Try<U>> f) {
            Objects.requireNonNull(f);
            try {
                return f.apply(value);
            } catch (Throwable t) {
                return Try.failure(t);
            }
        }

        @Override
        public Throwable getMessage() {
            throw new IllegalStateException("no messages when success");
        }
    }
}