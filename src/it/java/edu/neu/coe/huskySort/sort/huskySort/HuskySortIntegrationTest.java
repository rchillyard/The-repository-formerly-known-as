/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.util.Benchmark;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.ProcessorDependentTimeout;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static edu.neu.coe.huskySort.sort.huskySort.AbstractHuskySort.UNICODE_CODER;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.generateRandomStringArray;
import static org.junit.Assert.assertTrue;

/**
 * NOTE: JUnit does not allow variables to be used for the timeout. That means that we cannot adjust these timeout for the speed of a particular machine.
 * The values given are for a MacBook Pro 2.8 GHz Intel Core i7 (4 cores) with 16 GB 2133 MHz LPDDRP.
 * Java version is 1.8.0_152.
 * <p>
 * The expected time for a pure quicksort of N items and M runs is 2 k M N log N (where log represents naturaL log, I.E. to the base e).
 * Bear in mind that the Benchmark code does M/10 warmup runs also (but these are counted only in the overall elapsed time--not the benchmark time).
 */

@SuppressWarnings("ALL")
public class HuskySortIntegrationTest {

    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(HuskySortIntegrationTest.class);
    }

    @Rule
    public Timeout timeoutBuilder = new ProcessorDependentTimeout(10, TimeUnit.SECONDS, config);

    final static Pattern REGEX_LEIPZIG = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");
    final MyBenchmark benchmarkHuskySort = new MyBenchmark(new QuickHuskySort<String>(UNICODE_CODER, config), 19.1);
    final MyBenchmark benchmarkQuick3sort = new MyBenchmark(new QuickSort_3way<String>(), 20);
    private static Config config;

    @Test
    public void testHusky10K() throws Exception {
        final String[] words = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_10K-sentences.txt", line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
        final int m = 1900;
        final int n = 10000;
        checkTime(n, benchmarkHuskySort.run(words, n, m));
    }

    @Test
    public void testHusky31K() throws Exception {
        final String[] words = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_100K-sentences.txt", line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
        final int m = 200;
        final int n = 31623;
        checkTime(n, benchmarkHuskySort.run(words, n, m));
    }

    @Ignore // (timeout = 30000)
    public void testHusky100K() throws Exception {
        final String[] words = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_1M-sentences.txt", line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
        final int m = 100;
        final int n = 100000;
        checkTime(n, benchmarkHuskySort.run(words, n, m));
    }

    @Ignore //(timeout = 5000)
    public void testControl10K() throws Exception {
        final String[] words = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_10K-sentences.txt", line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
        final int m = 1000;
        final int n = 10000;
        checkTime(n, benchmarkQuick3sort.run(words, n, m));
    }

    @Test(timeout = 10000)
    public void testControl31K() throws Exception {
        final String[] words = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_100K-sentences.txt", line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
        final int m = 200;
        final int n = 31623;
        checkTime(n, benchmarkQuick3sort.run(words, n, m));
    }

    @Ignore //(timeout = 30000)
    public void testControl100K() throws Exception {
        final String[] words = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_1M-sentences.txt", line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
        final int m = 100;
        final int n = 100000;
        checkTime(n, benchmarkQuick3sort.run(words, n, m));
    }

    private void checkTime(int n, double run) {
        final double estimateMeanRuntime = 2 * n * Math.log(n);
        double normalizedRuntime = run / estimateMeanRuntime;
        System.out.println(String.format("Mean normalized run time: %6.2f", normalizedRuntime));
        assertTrue(normalizedRuntime >= 0.5 && normalizedRuntime <= 2.5);
    }

    class MyBenchmark {
        private final Sort<String> sorter;
        private final double k;
        private final Benchmark<String[]> benchmark;

        /**
         * Class to wrap a Benchmark for testing purposes.
         *
         * @param k the expected time for the method to run for an input array of size 1 (in nanoseconds).
         */
        public MyBenchmark(Sort<String> sorter, double k) {
            this.sorter = sorter;
            this.k = k;
            this.benchmark = new Benchmark<>("HuskySortIntegrationTest", sorter::sort);
        }

        /**
         * Run and return the normalized time (t/k).
         *
         * @param words the words from which to generate a random pattern.
         * @param n     the number of words to sort.
         * @param m     the number of runs.
         * @return the normalized per-run time
         */
        public double run(String[] words, int n, int m) {
            System.out.println("run benchmark: " + this + " (k = " + k +
                    " nanosec) with " + n + " words");
            final double milliseconds = benchmark.run(() -> {
                return generateRandomStringArray(words, n);
            }, m);
            return milliseconds / k * 1E6;
        }

        @Override
        public String toString() {
            return "MyBenchmark on " + sorter;
        }
    }
}