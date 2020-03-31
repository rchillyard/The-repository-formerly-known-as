/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.InversionCounter;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.IntroSort;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.sort.simple.TimSort;
import edu.neu.coe.huskySort.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static edu.neu.coe.huskySort.sort.huskySort.AbstractHuskySort.UNICODE_CODER;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.generateRandomStringArray;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.getWords;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.generateRandomLocalDateTimeArray;

public class HuskySortBenchmark {

    public HuskySortBenchmark(Config config) {
        this.config = config;
    }

    public static void main(String[] args) throws IOException {
        logger.info("HuskySortBenchmark.main");
        HuskySortBenchmark benchmark = new HuskySortBenchmark(new Config("config.ini"));
        benchmark.sortLocalDateTimes();
        benchmark.sortStrings();
    }

    private void sortStrings() throws IOException {
        logger.info("Beginning String sorts");

        doLeipzigBenchmark("eng-uk_web_2002_10K-sentences.txt", 10000, 1000);

        doLeipzigBenchmark("eng-uk_web_2002_100K-sentences.txt", 100000, 200);

        doLeipzigBenchmark("eng-uk_web_2002_1M-sentences.txt", 500000, 100);

        benchmarkStringSorters(getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList), 4000, 25000, HuskySortBenchmark.timeLoggersLinearithmic);

        doLeipzigBenchmark("zho-simp-tw_web_2014_10K-sentences.txt", 5000, 1000);
    }

    private void sortLocalDateTimes() {
        logger.info("Beginning LocalDateTime sorts");
        BaseHelper<ChronoLocalDateTime<?>> helper = new BaseHelper<>(null);
        Supplier<LocalDateTime[]> localDateTimeSupplier = () -> generateRandomLocalDateTimeArray(100000);
        // Test on date using pure tim sort.
        logger.info(benchmarkFactory("Sort LocalDateTimes using Arrays::sort (TimSort)", Arrays::sort, null).run(localDateTimeSupplier, 100) + "ms");

        // NOTE: this is supposed to match the previous benchmark run exactly. I don't understand why it takes rather less time.
        Sort<ChronoLocalDateTime<?>> timSort = new TimSort<>(helper);
        logger.info(benchmarkFactory("Repeat Sort LocalDateTimes using timSort::mutatingSort", timSort::mutatingSort, null).run(localDateTimeSupplier, 100) + "ms");
        final LocalDateTime[] localDateTimes = generateRandomLocalDateTimeArray(100000);
        // NOTE: this is intended to replace the run two lines previous. It should take the exact same amount of time.
        runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, 100000, 100, 0);

        // Test on date using husky sort.
        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortSystemSort = new QuickHuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder);
        final HuskyHelper<ChronoLocalDateTime<?>> dateHelper = dateHuskySortSystemSort.getHelper();
        logger.info(benchmarkFactory("Sort LocalDateTimes using huskySort with TimSort", dateHuskySortSystemSort::sort, dateHelper::postProcess).run(localDateTimeSupplier, 100) + "ms");
        // NOTE: this is intended to replace the run in the previous line. It should take the exact same amount of time.
        runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, 100000, 100, 1);

        // Test on date using husky sort with insertion sort.
        InsertionSort<ChronoLocalDateTime<?>> insertionSort = new InsertionSort<>(helper);
        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortInsertionSort = new QuickHuskySort<>("QuickHuskySort/Insertion", HuskySortHelper.chronoLocalDateTimeCoder, insertionSort::mutatingSort, false);
        logger.info(benchmarkFactory("Sort LocalDateTimes using huskySort with insertionSort", dateHuskySortInsertionSort::sort, dateHelper::postProcess).run(localDateTimeSupplier, 100) + "ms");
        // NOTE: this is intended to replace the run in the previous line. It should take the exact same amount of time.
        runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, 100000, 100, 2);
    }

    private void doLeipzigBenchmark(String resource, int nWords, int nRuns) throws FileNotFoundException {
        benchmarkStringSorters(getWords(resource, HuskySortBenchmark::getLeipzigWords), nWords, nRuns, timeLoggersLinearithmic);
    }

    void benchmarkStringSorters(String[] words, int nWords, int nRuns, TimeLogger[] timeLoggers) {
        logger.info("Testing with " + nRuns + " runs of sorting " + nWords + " words");

        boolean instrumented = this.config.get("helper", "instrument", boolean.class);

        runStringSortBenchmark(words, nWords, nRuns, new TimSort<>(nWords, instrumented), null, timeLoggers);

        runStringSortBenchmark(words, nWords, nRuns, new QuickSort_3way<>(nWords, instrumented), null, timeLoggers);

        runStringSortBenchmark(words, nWords, nRuns, new IntroSort<>(nWords, instrumented), null, timeLoggers);

        runStringSortBenchmark(words, nWords, nRuns / 10, new InsertionSort<>(nWords, instrumented), null, timeLoggersQuadratic);

        runStringSortBenchmark(words, nWords, nRuns, new QuickHuskySort<>(UNICODE_CODER, instrumented), null, timeLoggers);

        final Sort<String> stringHuskyBucketSort = new HuskyBucketSort<>(16, UNICODE_CODER, instrumented);
        final UnaryOperator<String[]> stringHuskyBucketSortPreProcess = stringHuskyBucketSort::preProcess;
        runStringSortBenchmark(words, nWords, nRuns, stringHuskyBucketSort, stringHuskyBucketSortPreProcess, timeLoggers);

        runStringSortBenchmark(words, nWords, nRuns, new IntroHuskySort<>(UNICODE_CODER, instrumented), null, timeLoggers);

        final Sort<String> quickHuskySortInsertion = new QuickHuskySort<>("QuickHuskySort/Insertion", UNICODE_CODER, new InsertionSort<String>()::mutatingSort, instrumented);
        runStringSortBenchmark(words, nWords, nRuns, quickHuskySortInsertion, quickHuskySortInsertion::preProcess, timeLoggers);

        final Sort<String> introHuskySortInsertion = new IntroHuskySort<>("IntroHuskySort/Insertion", UNICODE_CODER, new InsertionSort<String>()::mutatingSort, instrumented);
        runStringSortBenchmark(words, nWords, nRuns, introHuskySortInsertion, introHuskySortInsertion::preProcess, timeLoggers);

        final Sort<String> introHuskyBucketSort = new HuskyBucketSort<>(1000, UNICODE_CODER, instrumented);
        runStringSortBenchmark(words, nWords, nRuns, introHuskyBucketSort, introHuskyBucketSort::preProcess, timeLoggers);

        final Sort<String> quickHuskySortNone = new QuickHuskySort<>("QuickHuskySort/print inversions", UNICODE_CODER, (xs2) -> {
            // do nothing, so we can count inversions.
        }, instrumented);
        long inversions = 0;
        for (int i = 0; i < nRuns; i++) {
            String[] xs = generateRandomStringArray(words, nWords);
            quickHuskySortNone.sort(xs);
            inversions += new InversionCounter(xs).getInversions();
        }
        inversions = inversions / nRuns;
        logger.info("Mean inversions after first part: " + inversions);
        logger.info("Normalized mean inversions: " + inversions / Math.log(nWords));
    }

    /**
     * Method to run a sorting benchmark.
     *
     * @param words        an array of available words (to be chosen randomly).
     * @param nWords       the number of words to be sorted.
     * @param nRuns        the number of runs of the sort to be preformed.
     * @param sorter       the sorter to use--NOTE that this sorter will be closed at the end of this method.
     * @param preProcessor the pre-processor function, if any.
     * @param timeLoggers  a set of timeLoggers to be used.
     */
    void runStringSortBenchmark(String[] words, int nWords, int nRuns, Sort<String> sorter, UnaryOperator<String[]> preProcessor, TimeLogger[] timeLoggers) {
        new SorterBenchmark<>(String.class, preProcessor, sorter, words, nRuns, timeLoggers).run(nWords);
        sorter.close();
    }

    @SuppressWarnings("SameParameterValue")
    private void runDateTimeSortBenchmark(Class<?> tClass, ChronoLocalDateTime<?>[] dateTimes, int N, int m, int whichSort) {
        final InsertionSort<ChronoLocalDateTime<?>> insertionSort = new InsertionSort<>();
        final Sort<ChronoLocalDateTime<?>> sorter = whichSort == 0 ? new TimSort<>() : whichSort == 1 ? new QuickHuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder) : new QuickHuskySort<>("QuickHuskySort/Insertion", HuskySortHelper.chronoLocalDateTimeCoder, insertionSort::mutatingSort, false);
        @SuppressWarnings("unchecked") final SorterBenchmark<ChronoLocalDateTime<?>> sorterBenchmark = new SorterBenchmark<>((Class<ChronoLocalDateTime<?>>) tClass, (xs) -> Arrays.copyOf(xs, xs.length), sorter, dateTimes, m, HuskySortBenchmark.timeLoggersLinearithmic);
        sorterBenchmark.run(N);
    }

    private static List<String> lineAsList(String line) {
        List<String> words = new ArrayList<>();
        words.add(line);
        return words;
    }

    private static List<String> getLeipzigWords(String line) {
        return getWords(regexLeipzig, line);
    }

    // TODO: to be eliminated soon.
    private static Benchmark<LocalDateTime[]> benchmarkFactory(String description, Consumer<LocalDateTime[]> sorter, Consumer<LocalDateTime[]> checker) {
        return new Benchmark<>(
                description,
                (xs) -> Arrays.copyOf(xs, xs.length),
                sorter,
                checker
        );
    }

    private final Config config;

    final static LazyLogger logger = new LazyLogger(HuskySortBenchmark.class);

    final static Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");

    final static TimeLogger[] timeLoggersLinearithmic = {
            new TimeLogger("Raw time per run (mSec): ", (time, n) -> time),
            new TimeLogger("Normalized time per run (n log n): ", (time, n) -> time / Math.log(n.doubleValue()) / n * 1e5)
    };

    final static TimeLogger[] timeLoggersQuadratic = {
            new TimeLogger("Raw time per run (mSec): ", (time, n) -> time),
            new TimeLogger("Normalized time per run (n^2): ", (time, n) -> 2 * time / n.doubleValue() / n * 1e5)
    };


}
