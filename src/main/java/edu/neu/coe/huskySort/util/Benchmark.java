/*
 * Copyright (c) 2018-22. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import java.util.function.*;

import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * @param <T> The generic type T is that of the input to the function f which you will pass in to the constructor.
 */
public class Benchmark<T> {

    /**
     * Calculate the appropriate number of warmup runs.
     *
     * @param m the number of runs.
     * @return at least 2 and at most m/10.
     */
    static int getWarmupRuns(final int m) {
        return Integer.max(MIN_WARMUP_RUNS, Integer.min(10, m / 10));
    }

    /**
     * Run function f m times and return the average time in milliseconds.
     *
     * @param t the value that will in turn be passed to function f.
     * @param m the number of times the function f will be called.
     * @return the average number of milliseconds taken for each run of function f.
     */
    public double run(final T t, final int m) {
        return run(() -> t, m);
    }

    /**
     * Run function f m times and return the average time in milliseconds.
     *
     * @param supplier a Supplier of a T
     * @param m        the number of times the function f will be called.
     * @return the average number of milliseconds taken for each run of function f.
     */
    public double run(final Supplier<T> supplier, final int m) {
        System.out.println("============================================================");
        logger.info("Begin run: " + description + " with " + formatWhole(m) + " runs");
        // Warmup phase
        final Function<T, T> function = t -> {
            fRun.accept(t);
            return t;
        };
        new Timer().repeat(true, getWarmupRuns(m), supplier, function, fPre, null);

        // Timed phase
        return new Timer().repeat(false, m, supplier, function, fPre, fPost);
    }

    @Override
    public String toString() {
        return "Benchmark " + description;
    }

    /**
     * Constructor for a Benchmark with option of specifying all three functions.
     *
     * @param description the description of the benchmark.
     * @param fPre        a function of T => T.
     *                    Function fPre is run before each invocation of fRun (but with the clock stopped).
     *                    The result of fPre (if any) is passed to fRun.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *                    When you create a lambda defining fRun, you must return "null."
     * @param fPost       a predicate on a U and which succeeds the call of function, but which is not timed (may be null).
     *                    If defined and false is returned, an exception will be thrown.
     */
    public Benchmark(final String description, final UnaryOperator<T> fPre, final Consumer<T> fRun, final Predicate<T> fPost) {
        this.description = description;
        this.fPre = fPre;
        this.fRun = fRun;
        this.fPost = fPost;
    }

    /**
     * Constructor for a Benchmark with option of specifying all three functions.
     *
     * @param description the description of the benchmark.
     * @param fPre        a function of T => T.
     *                    Function fPre is run before each invocation of fRun (but with the clock stopped).
     *                    The result of fPre (if any) is passed to fRun.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     */
    public Benchmark(final String description, final UnaryOperator<T> fPre, final Consumer<T> fRun) {
        this(description, fPre, fRun, null);
    }

    /**
     * Constructor for a Benchmark with only fRun and fPost Consumer parameters.
     *
     * @param description the description of the benchmark.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *                    When you create a lambda defining fRun, you must return "null."
     * @param fPost       a predicate on a T and which succeeds the call of function, but which is not timed (may be null).
     *                    If defined and false is returned, an exception will be thrown.
     */
    public Benchmark(final String description, final Consumer<T> fRun, final Predicate<T> fPost) {
        this(description, null, fRun, fPost);
    }

    /**
     * Constructor for a Benchmark where only the (timed) run function is specified.
     *
     * @param description the description of the benchmark.
     * @param f           a Consumer function (i.e. a function of T => Void).
     *                    Function f is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     */
    public Benchmark(final String description, final Consumer<T> f) {
        this(description, null, f, null);
    }

    private static int MIN_WARMUP_RUNS = 2;

    /**
     * Method used by unit tests to allow for a small number of warmup runs.
     *
     * @param m the minimum number of warmup runs to use.
     */
    public static void setMinWarmupRuns(final int m) {
        MIN_WARMUP_RUNS = m;
    }

    private final String description;
    private final UnaryOperator<T> fPre;
    private final Consumer<T> fRun;
    private final Predicate<T> fPost;

    final static LazyLogger logger = new LazyLogger(Benchmark.class);
}
