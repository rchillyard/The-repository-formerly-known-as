/*
 * Copyright (c) 2018. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
     * @param description the description of the benchmark.
     * @param fPre        a function of T => T.
     *                    Function fPre is run before each invocation of fRun (but with the clock stopped).
     *                    The result of fPre (if any) is passed to fRun.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *                    When you create a lambda defining fRun, you must return "null."
     * @param fPost       a Consumer function (i.e. a function of T => Void).
     */
    public Benchmark(String description, UnaryOperator<T> fPre, Consumer<T> fRun, Consumer<T> fPost) {
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
    public Benchmark(String description, UnaryOperator<T> fPre, Consumer<T> fRun) {
        this(description, fPre, fRun, null);
    }

    /**
     * Constructor for a Benchmark with only fRun and fPost Consumer parameters.
     *
     * @param description the description of the benchmark.
     * @param fRun        a Consumer function (i.e. a function of T => Void).
     *                    Function fRun is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     *                    When you create a lambda defining fRun, you must return "null."
     * @param fPost       a Consumer function (i.e. a function of T => Void).
     */
    public Benchmark(String description, Consumer<T> fRun, Consumer<T> fPost) {
        this(description, null, fRun, fPost);
    }

    /**
     * Constructor for a Benchmark where only the (timed) run function is specified.
     *
     * @param description the description of the benchmark.
     * @param f a Consumer function (i.e. a function of T => Void).
     *          Function f is the function whose timing you want to measure. For example, you might create a function which sorts an array.
     */
    public Benchmark(String description, Consumer<T> f) {
        this(description, null, f, null);
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
        logger.info("Begin run: " + description + " with " + m + " runs");
        // Warmup phase
        int warmupRuns = Integer.min(2, Integer.max(10, m / 10));
        final Function<T, T> function = t -> {
            fRun.accept(t);
            return t;
        };
        new Timer().repeat(warmupRuns, supplier, function, fPre, null);

        // Timed phase
        return new Timer().repeat(m, supplier, function, fPre, fPost);
    }

    private final String description;
    private final UnaryOperator<T> fPre;
    private final Consumer<T> fRun;
    private final Consumer<T> fPost;

//    /**
//     * Everything below this point has to do with a particular example of running a Benchmark.
//     * In this case, we time three types of simple sort on a random integer array of length 1000.
//     * Each test is run 200 times.
//     *
//     * @param args the command-line arguments, of which none are significant.
//     */
//    public static void main(String[] args) {
//        Random random = new Random();
//        int m = 100; // This is the number of repetitions: sufficient to give a good mean value of timing
//        int n = 100000; // This is the size of the array
//        for (int k = 0; k < 5; k++) {
//            Integer[] array = new Integer[n];
//            for (int i = 0; i < n; i++) array[i] = random.nextInt();
//            benchmarkSort("InsertionSort", array, "InsertionSort: " + n, new InsertionSort<>(), m);
//            benchmarkSort("SelectionSort", array, "SelectionSort: " + n, new SelectionSort<>(), m);
////        benchmarkSort("ShellSort", array, "ShellSort    ", new ShellSort<>(3), m);
//            n = n * 2;
//        }
//    }
//
//    // TODO this needs to be unit-tested
//    private static void benchmarkSort(String description, Integer[] array, String name, Sort<Integer> sorter, int m) {
//        UnaryOperator<Integer[]> preFunction = (xs) -> Arrays.copyOf(array, array.length);
//        Consumer<Integer[]> sortFunction = sorter::mutatingSort;
//        final Helper<Integer> helper = sorter.getHelper();
//        Consumer<Integer[]> cleanupFunction = (xs) -> {
//            if (!helper.sorted(xs)) throw new RuntimeException("not sorted");
//        };
//        Benchmark<Integer[]> bm = new Benchmark<>(description, preFunction, sortFunction, cleanupFunction);
//        double x = bm.run(array, m);
//        logger.info(name + ": " + x + " millisecs");
//    }

    public static String formatLocalDateTime() {
        return dateTimeFormatter.format(LocalDateTime.now());
    }

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ss.SSSSSS");

    final static LazyLogger logger = new LazyLogger(Benchmark.class);
}
