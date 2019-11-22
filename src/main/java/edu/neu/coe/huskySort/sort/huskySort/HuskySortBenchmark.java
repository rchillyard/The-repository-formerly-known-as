/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.simple.BucketSort;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.IntroSort;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.util.Benchmark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static edu.neu.coe.huskySort.sort.huskySort.AbstractHuskySort.UNICODE_CODER;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.*;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.generateRandomDoubleArray;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.generateRandomLocalDateTimeArray;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

@SuppressWarnings("Duplicates")
public class HuskySortBenchmark {

    public static void main(String[] args) throws IOException {
        benchmarkDouble();
//        benchmarkLocalDateTime();
//        benchmarkString();
    }

    private static void benchmarkString() throws IOException {
        final Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");
        benchmark(getWords("eng-uk_web_2002_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 10000, 1000);

        benchmark(getWords("eng-uk_web_2002_100K-sentences.txt", line -> getWords(regexLeipzig, line)), 100000, 200);

        benchmark(
                getWords("eng-uk_web_2002_1M-sentences.txt", line -> getWords(regexLeipzig, line)),
                500000,
                100
        );

        benchmark(getWords("3000-common-words.txt", line -> {
            List<String> words = new ArrayList<>();
            words.add(line);
            return words;
        }), 4000, 25000);

        benchmark(getWords("zho-simp-tw_web_2014_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 5000, 1000);
    }

    private static void benchmarkLocalDateTime() {
        Supplier<LocalDateTime[]> LocalDateTimeSupplier = () -> generateRandomLocalDateTimeArray(100000);
        // Test on date using pure tim sort.
        Benchmark<LocalDateTime[]> benchmark;
        benchmark = new Benchmark<>(
                (xs) -> { return Arrays.copyOf(xs, xs.length); },
                Arrays::sort
        );
        out.println("Sort LocalDateTimes using TimSort: \t" + benchmark.run(LocalDateTimeSupplier, 100) + "ms");

        // Test on date using husky sort.
        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySort = new QuickHuskySort<>();
        final Helper<ChronoLocalDateTime<?>> dateHelper = dateHuskySort.getHelper();
        benchmark = new Benchmark<>(
                (xs) -> Arrays.copyOf(xs, xs.length),
                (xs) -> dateHuskySort.sort(xs, HuskySortHelper.chronoLocalDateTimeCoder),
                (xs) -> { if (!dateHelper.sorted(xs)) System.err.println("not sorted"); }
        );
        out.println("Sort LocalDateTimes using huskySort with TimSort: \t" + benchmark.run(LocalDateTimeSupplier, 100) + "ms");

        // Test on date using husky sort with insertion sort.
        InsertionSort<ChronoLocalDateTime<?>> insertionSort = new InsertionSort<>();
        benchmark = new Benchmark<>(
                (xs) -> Arrays.copyOf(xs, xs.length),
                (xs) -> dateHuskySort.sort(xs, HuskySortHelper.chronoLocalDateTimeCoder, (xs2) -> insertionSort.sort(xs2, false)),
                (xs) -> { if (!dateHelper.sorted(xs)) System.err.println("not sorted"); }
        );
        out.println("Sort LocalDateTimes using huskySort with insertionSort: \t" + benchmark.run(LocalDateTimeSupplier, 100) + "ms");
        out.println();
    }

    private static void benchmarkDouble() {
        Supplier<Double[]> doubleSupplier = () -> generateRandomDoubleArray(1000000);

        // Test on double using pure tim sort.
        Benchmark<Double[]> benchmark;
        benchmark = new Benchmark<>(
                Arrays::sort
        );
        out.println("Sort Doubles using TimSort: \t" + benchmark.run(doubleSupplier, 100) + "ms");

        // Test on double using bucket sort.
        BucketSort<Double> bucketSort = new BucketSort<>(1000);
        benchmark = new Benchmark<>(
                (xs) -> {
                    bucketSort.init();
                    return xs;
                },
                (xs) -> { bucketSort.sort(xs, false); }
        );
        out.println("Sort Doubles using BucketSort: \t" + benchmark.run(doubleSupplier, 100) + "ms");

        // Test on double using husky sort.
        QuickHuskySort<Double> doubleHuskySort = new QuickHuskySort<>();
        final Helper<Double> dateHelper = doubleHuskySort.getHelper();
        benchmark = new Benchmark<>(
                (xs) -> xs,
                (xs) -> doubleHuskySort.sort(xs, HuskySortHelper.doubleCoder),
                (xs) -> { if (!dateHelper.sorted(xs)) System.err.println("not sorted"); }
        );
        out.println("Sort Doubles using huskySort with TimSort: \t" + benchmark.run(doubleSupplier, 100) + "ms");

        // Test on double using husky sort with insertion sort.
        InsertionSort<Double> insertionSort = new InsertionSort<>();
        benchmark = new Benchmark<>(
                (xs) -> xs,
                (xs) -> doubleHuskySort.sort(xs, HuskySortHelper.doubleCoder, (xs2) -> insertionSort.sort(xs2, false)),
                (xs) -> { if (!dateHelper.sorted(xs)) System.err.println("not sorted"); }
        );
        out.println("Sort Doubles using huskySort with insertionSort: \t" + benchmark.run(doubleSupplier, 100) + "ms");
        out.println();
    }

    private static void benchmark(String[] words, int nWords, int nRuns) {
        out.println("Testing with " + nRuns + " runs of sorting " + nWords + " words");
        Benchmark<String[]> benchmark;
        String normalizePrefix = "Normalized time per run: ";
        String identityPrefix = "Time per run in sec: ";
        Function<Double, Double> normalizeNormalizer = (time) -> time / nWords / Math.log(nWords) * 1e6;
        Function<Double, Double> identityNormalizer = Function.identity();

        out.println(LocalDateTime.now() + ": Starting Timsort test");
        benchmark = new Benchmark<>(Arrays::sort);
        showTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        System.out.println(LocalDateTime.now() + ": Starting Quicksort test");
        Sort<String> quickSort = new QuickSort_3way<>();
        benchmark = new Benchmark<>(
                (xs) -> quickSort.sort(xs, false)
        );
        showTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        System.out.println(LocalDateTime.now() + ": Starting IntroSort test");
        Sort<String> introSort = new IntroSort<>();
        benchmark = new Benchmark<>(
                (xs) -> {
                    introSort.sort(xs, false);
                },
                (xs) -> {
                    if (!introSort.getHelper().sorted(xs)) System.err.println("not sorted");
                }
        );
        showTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );


        QuickHuskySort<String> quickHuskySort = new QuickHuskySort<>();
        IntroHuskySort<String> introHuskySort = new IntroHuskySort<>();
        Helper<String> helper = new Helper<>("StringHelper", nWords, nanoTime());

        out.println(LocalDateTime.now() + ": Starting QuickHuskySort test");
        benchmark = new Benchmark<>(
                (Consumer<String[]>) xs1 -> quickHuskySort.sort(xs1, UNICODE_CODER),
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        out.println(LocalDateTime.now() + ": Starting IntroHuskySort test");
        benchmark = new Benchmark<>(
                (Consumer<String[]>) xs1 -> introHuskySort.sort(xs1, UNICODE_CODER),
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        out.println(LocalDateTime.now() + ": Starting QuickHuskySort test with insertion sort.");
        InsertionSort<String> insertionSort = new InsertionSort<>();
        benchmark = new Benchmark<>(
                (xs) -> {
                    quickHuskySort.sort(xs, UNICODE_CODER, (xs2) -> insertionSort.sort(xs2, false));
                },
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        out.println(LocalDateTime.now() + ": Starting IntroHuskySort test with insertion sort.");
        benchmark = new Benchmark<>(
                (xs) -> {
                    introHuskySort.sort(xs, UNICODE_CODER, (xs2) -> insertionSort.sort(xs2, false));
                },
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        out.println(LocalDateTime.now() + ": Starting Husky sort test with printout inversions");
        long inversions = 0;
        for (int i = 0; i < nRuns; i++) {
            String[] xs = generateRandomStringArray(words, nWords);
            quickHuskySort.sort(xs, UNICODE_CODER, (xs2) -> {
                // do nothing, so we can count inversions.
            });
            inversions += InversionCounter.getInversions(xs);
        }
        inversions = inversions / nRuns;
        out.println("Mean inversions after first part: " + inversions);

        out.println();
    }
}
