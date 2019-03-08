/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.IntroSort;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.util.Benchmark;
import edu.neu.coe.huskySort.util.SortBenchmark;

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
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.generateRandomLocalDateTimeArray;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

public class HuskySortBenchmark {

    public static void main(String[] args) throws IOException {
        benchmarkLocalDateTime();
        benchmarkString();
    }

    private static void benchmarkString() throws IOException {
        out.println("Beginning String sorts");
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
        out.println("Beginning LocalDateTime sorts");
        Supplier<LocalDateTime[]> LocalDateTimeSupplier = () -> generateRandomLocalDateTimeArray(100000);
        // Test on date using pure tim sort.
        Benchmark<LocalDateTime[]> benchmark;
        benchmark = new Benchmark<>(
                (xs) -> {
                    return Arrays.copyOf(xs, xs.length);
                },
                Arrays::sort
        );
        out.println("Sort LocalDateTimes using TimSort: \t" + benchmark.run(LocalDateTimeSupplier, 100) + "ms");

        // Test on date using husky sort.
        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortSystemSort = new QuickHuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder);
        final HuskyHelper<ChronoLocalDateTime<?>> dateHelper = dateHuskySortSystemSort.getHelper();
        benchmark = new Benchmark<>(
                (xs) -> Arrays.copyOf(xs, xs.length),
                dateHuskySortSystemSort::sort,
                dateHelper::checkSorted
        );
        out.println("Sort LocalDateTimes using huskySort with TimSort: \t" + benchmark.run(LocalDateTimeSupplier, 100) + "ms");

        // Test on date using husky sort with insertion sort.
        InsertionSort<ChronoLocalDateTime<?>> insertionSort = new InsertionSort<>();
        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortInsertionSort = new QuickHuskySort<>("QuickHuskySort/Insertion", HuskySortHelper.chronoLocalDateTimeCoder, insertionSort::mutatingSort);
        benchmark = new Benchmark<>(
                (xs) -> Arrays.copyOf(xs, xs.length),
                dateHuskySortInsertionSort::sort,
                dateHelper::checkSorted
        );
        out.println("Sort LocalDateTimes using huskySort with insertionSort: \t" + benchmark.run(LocalDateTimeSupplier, 100) + "ms");
        out.println();
    }

    static void benchmark(String[] words, int nWords, int nRuns) {
        out.println("Testing with " + nRuns + " runs of sorting " + nWords + " words");
        String normalizePrefix = "Normalized time per run: ";
        Function<Double, Double> normalizeNormalizer = (time) -> time / nWords / Math.log(nWords) * 1e6;

        out.println(LocalDateTime.now() + ": Starting Timsort test");
        showTime(
                new Benchmark<String[]>(Arrays::sort).run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        final String sQuicksort = "Quicksort";
        System.out.println(LocalDateTime.now() + ": Starting " + sQuicksort + " test");
        Sort<String> quickSort = new QuickSort_3way<>();
        showTime(
                new Benchmark<>(quickSort::mutatingSort).run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        final String sIntroSort = "IntroSort";
        System.out.println(LocalDateTime.now() + ": Starting " + sIntroSort + " test");
        Sort<String> introSort = new IntroSort<>();
        showTime(
                new Benchmark<>(
                        introSort::mutatingSort,
                        introSort.getHelper()::checkSorted
                ).run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );


        Helper<String> helper = new Helper<>("StringHelper", nWords, nanoTime());

        final SortBenchmark<String> sortBenchmark = new SortBenchmark<>(words, nRuns, normalizePrefix, normalizeNormalizer);

        sortBenchmark.run(nWords, new QuickHuskySort<>(UNICODE_CODER));

        // CONSIDER reinstating this when HuskyBucketSort is faster.
//        sortBenchmark.run(nWords, new HuskyBucketSort<>(nWords / 10, UNICODE_CODER));

        sortBenchmark.run(nWords, new IntroHuskySort<>(UNICODE_CODER));

        out.println(LocalDateTime.now() + ": Starting " + "QuickHuskySort" + " test with insertion sort.");
        Sort<String> quickHuskySortInsertion = new QuickHuskySort<>("QuickHuskySort/Insertion", UNICODE_CODER, new InsertionSort<String>()::mutatingSort);
        showTime(
                new Benchmark<>(
                        (Consumer<String[]>) quickHuskySortInsertion::sort,
                        helper::checkSorted
                ).run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        out.println(LocalDateTime.now() + ": Starting " + "IntroHuskySort" + " test with insertion sort.");
        Sort<String> introHuskySortInsertion = new IntroHuskySort<>("IntroHuskySort/Insertion", UNICODE_CODER, new InsertionSort<String>()::mutatingSort);
        showTime(
                new Benchmark<>(
                        (Consumer<String[]>) introHuskySortInsertion::sort,
                        helper::checkSorted
                ).run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );

        out.println(LocalDateTime.now() + ": Starting " + "QuickHuskySort" + " test with printout inversions");
        Sort<String> quickHuskySortNone = new QuickHuskySort<>("QuickHuskySort/print inversions", UNICODE_CODER, (xs2) -> {
            // do nothing, so we can count inversions.
        });
        long inversions = 0;
        for (int i = 0; i < nRuns; i++) {
            String[] xs = generateRandomStringArray(words, nWords);
            quickHuskySortNone.sort(xs);
            inversions += InversionCounter.getInversions(xs);
        }
        inversions = inversions / nRuns;
        out.println("Mean inversions after first part: " + inversions);
        out.println("Normalized mean inversions: " + inversions / Math.log(nWords));

        out.println();
    }

    private static void doStdSortBenchmark(String[] words, int nWords, int nRuns, String normalizePrefix, Function<Double, Double> normalizeNormalizer, Sort<String> sorter) {
        out.println(LocalDateTime.now() + ": Starting " + sorter + " test");
        showTime(
                new Benchmark<>(
                        (Consumer<String[]>) sorter::sort,
                        sorter.getHelper()::checkSorted
                ).run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizePrefix,
                normalizeNormalizer
        );
    }
}
