/*
 * Copyright (c) 2018. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.SelectionSort;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @param <T> The generic type T is that of the input to the function f which you will pass in to the constructor.
 */
public class Benchmark<T> {

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
    public Benchmark(UnaryOperator<T> fPre, Consumer<T> fRun, Consumer<T> fPost) {
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
    public Benchmark(UnaryOperator<T> fPre, Consumer<T> fRun) {
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
    public Benchmark(Consumer<T> fRun, Consumer<T> fPost) {
        this(null, fRun, fPost);
    }

    /**
     * Constructor for a Benchmark where only the (timed) run function is specified.
     *
     * @param f a Consumer function (i.e. a function of T => Void).
     *          Function f is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *          When you create a lambda defining f, you must return "null."
     */
    public Benchmark(Consumer<T> f) {
        this(null, f, null);
    }

    /**
     * Run function f m times and return the average time in milliseconds.
     *
     * @param t the value that will in turn be passed to function f.
     * @param m the number of times the function f will be called.
     * @return the average number of milliseconds taken for each run of function f.
     */
    public double run(T t, int m) {
        return run(() -> t, m);
    }

    /**
     * Run function f m times and return the average time in milliseconds.
     *
     * @param supplier a Supplier of a T
     * @param m        the number of times the function f will be called.
     * @return the average number of milliseconds taken for each run of function f.
     */
    public double run(Supplier<T> supplier, int m) {
        // Warmup phase
        int warmupRuns = Integer.min(2, Integer.max(10, m / 10));
        for (int i = 0; i < warmupRuns; i++) doRun(supplier.get(), true);
        // Timed phase
        long totalTime = 0;
        for (int i = 0; i < m; i++) totalTime += doRun(supplier.get(), false);
        return (double) totalTime / m / 1000000;
    }

    private long doRun(T t, boolean warmup) {
        T t1 = fPre != null ? fPre.apply(t) : t;
        if (warmup) {
            fRun.accept(t1);
            return 0;
        }
        long start = System.nanoTime();
        fRun.accept(t1);
        long nanos = System.nanoTime() - start;
        if (fPost != null) fPost.accept(t1);
        return nanos;
    }

    private final Function<T, T> fPre;
    private final Consumer<T> fRun;
    private final Consumer<T> fPost;

    /**
     * Everything below this point has to do with a particular example of running a Benchmark.
     * In this case, we time three types of simple sort on a random integer array of length 1000.
     * Each test is run 200 times.
     *
     * @param args the command-line arguments, of which none are significant.
     */
    public static void main(String[] args) {
        Random random = new Random();
        int m = 100; // This is the number of repetitions: sufficient to give a good mean value of timing
        int n = 100000; // This is the size of the array
        for (int k = 0; k < 5; k++) {
            Integer[] array = new Integer[n];
            for (int i = 0; i < n; i++) array[i] = random.nextInt();
            benchmarkSort(array, "InsertionSort: " + n, new InsertionSort<>(), m);
            benchmarkSort(array, "SelectionSort: " + n, new SelectionSort<>(), m);
//        benchmarkSort(array, "ShellSort    ", new ShellSort<>(3), m);
            n = n * 2;
        }
    }

    // TODO this needes to be unit-tested
    private static void benchmarkSort(Integer[] array, String name, Sort<Integer> sorter, int m) {
        UnaryOperator<Integer[]> preFunction = (xs) -> Arrays.copyOf(array, array.length);
        Consumer<Integer[]> sortFunction = sorter::mutatingSort;
        final Helper<Integer> helper = sorter.getHelper();
        Consumer<Integer[]> cleanupFunction = (xs) -> {
            if (!helper.sorted(xs)) throw new RuntimeException("not sorted");
        };
        Benchmark<Integer[]> bm = new Benchmark<>(preFunction, sortFunction, cleanupFunction);
        double x = bm.run(array, m);
        System.out.println(name + ": " + x + " millisecs");
    }
}
