/*
 * Copyright (c) 2018. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @param <T> The generic type T is that of the input to the function f which you will pass in to the constructor.
 */
public class Aggregator<T> {

    /**
     * Constructor for a Benchmark with option of specifying all three functions.
     *
     * @param fPre  a function of T => T.
     *              Function fPre is run before each invocation of fRun (but with the clock stopped).
     *              The result of fPre (if any) is passed to fRun.
     * @param fRun  a Consumer function (i.e. a function of T => Void).
     *              Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *              When you create a lambda defining fRun, you must return "null."
     * @param fPost a Consumer function (i.e. a function of T => Void).
     *              Function fPost is run after each invocation of fRun (but with the clock stopped).
     */
    public Aggregator(UnaryOperator<T> fPre, Consumer<T> fRun, Consumer<T> fPost) {
        this.fPre = fPre;
        this.fRun = fRun;
        this.fPost = fPost;
    }

    /**
     * Constructor for a Benchmark with option of specifying all three functions.
     *
     * @param fPre a function of T => T.
     *             Function fPre is run before each invocation of fRun (but with the clock stopped).
     *             The result of fPre (if any) is passed to fRun.
     * @param fRun a Consumer function (i.e. a function of T => Void).
     *             Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *             When you create a lambda defining fRun, you must return "null."
     */
    public Aggregator(UnaryOperator<T> fPre, Consumer<T> fRun) {
        this(fPre, fRun, null);
    }

    /**
     * Constructor for a Benchmark with only fRun and fPost Consumer parameters.
     *
     * @param fRun  a Consumer function (i.e. a function of T => Void).
     *              Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *              When you create a lambda defining fRun, you must return "null."
     * @param fPost a Consumer function (i.e. a function of T => Void).
     *              Function fPost is run after each invocation of fRun (but with the clock stopped).
     */
    public Aggregator(Consumer<T> fRun, Consumer<T> fPost) {
        this(null, fRun, fPost);
    }

    /**
     * Constructor for a Benchmark where only the (timed) run function is specified.
     *
     * @param f a Consumer function (i.e. a function of T => Void).
     *          Function f is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *          When you create a lambda defining f, you must return "null."
     */
    public Aggregator(Consumer<T> f) {
        this(null, f, null);
    }

    /**
     * Run function f m times and return the average time in milliseconds.
     *
     * @param t the value that will in turn be passed to function f.
     * @param m the number of times the function f will be called.
     */
    public void run(T t, int m) {
        run(() -> t, m);
    }

    /**
     * Run function f m times and return the average time in milliseconds.
     *
     * @param supplier a Supplier of a T
     * @param m        the number of times the function f will be called.
     */
    public void run(Supplier<T> supplier, int m) {
        for (int i = 0; i < m; i++) doRun(supplier.get());
    }

    private void doRun(T t) {
        T t1 = fPre != null ? fPre.apply(t) : t;
        fRun.accept(t1);
        if (fPost != null) fPost.accept(t1);
    }

    private final Function<T, T> fPre;
    private final Consumer<T> fRun;
    private final Consumer<T> fPost;

}
