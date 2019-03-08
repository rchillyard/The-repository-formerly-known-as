/*
 * Copyright (c) 2018. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @param <T> The generic type T is that of the input to the function f (or fRun) which you will pass in to the constructor.
 */
public class Aggregator<T> {

    /**
     * Constructor for a Aggregator with option of specifying all three functions.
     *
     * @param counter a counter object which will be incremented by the result of calling fPost.
     * @param fPre  a function of T => T.
     *              Function fPre is run before each invocation of fRun (but with the clock stopped).
     *              The result of fPre (if any) is passed to fRun.
     * @param fRun  a Consumer function (i.e. a function of T => Void).
     *              Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *              When you create a lambda defining fRun, you must return "null."
     * @param fPost a Function which takes a T and generates a Long which is then used to increment the Counter.
     */
    public Aggregator(Counter counter, UnaryOperator<T> fPre, Consumer<T> fRun, Function<T, Long> fPost) {
        this.counter = counter;
        this.fPre = fPre;
        this.fRun = fRun;
        this.fPost = fPost;
    }


    /**
     * Constructor for a Aggregator with only fRun and fPost Consumer parameters.
     * @param counter a counter object which will be incremented by the result of calling fPost.
     * @param fRun  a Consumer function (i.e. a function of T => Void).
     *              Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *              When you create a lambda defining fRun, you must return "null."
     * @param fPost a Consumer function (i.e. a function of T => Void).
     */
    public Aggregator(Counter counter, Consumer<T> fRun, Function<T, Long> fPost) {
        this(counter, null, fRun, fPost);
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
        counter.increment(fPost.apply(t1));
    }

    private final Counter counter;
    private final Function<T, T> fPre;
    private final Consumer<T> fRun;
    private final Function<T, Long> fPost;

}
