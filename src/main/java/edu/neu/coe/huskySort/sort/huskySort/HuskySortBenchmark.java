/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.InversionCounter;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.IntroSort;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.util.Benchmark;
import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.SortBenchmark;
import org.ini4j.Configurable;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import static edu.neu.coe.huskySort.util.Benchmark.formatLocalDateTime;
import static java.lang.System.nanoTime;

public class HuskySortBenchmark {

    public HuskySortBenchmark(Configurable config) {
        this.config = config;
//        boolean helperCount = ini.get("helper", "count", boolean.class);
    }

    public static void main(String[] args) throws IOException {
        logger.info("HuskySortBenchmark.main");
        Configurable config = new Ini(new FileReader(new File("config.ini")));
        HuskySortBenchmark benchmark = new HuskySortBenchmark(config);
        benchmark.sortLocalDateTimes();
        benchmark.sortStrings();
    }

    private void sortStrings() throws IOException {
        logger.info("Beginning String sorts");

        doLeipzigBenchmark("eng-uk_web_2002_10K-sentences.txt", 10000, 1000);

        doLeipzigBenchmark("eng-uk_web_2002_100K-sentences.txt", 100000, 200);

        doLeipzigBenchmark("eng-uk_web_2002_1M-sentences.txt", 500000, 100);

        doBenchmark(getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList), 4000, 25000);

        doLeipzigBenchmark("zho-simp-tw_web_2014_10K-sentences.txt", 5000, 1000);
    }

    private void doLeipzigBenchmark(String resource, int nWords, int nRuns) throws FileNotFoundException {
        doBenchmark(getWords(resource, HuskySortBenchmark::getLeipzigWords), nWords, nRuns);
    }

    private void doBenchmark(String[] words, int nWords, int nRuns) {
        benchmarkStringSorters(words, nWords, nRuns, config);
    }

    private void sortLocalDateTimes() {
        logger.info("Beginning LocalDateTime sorts");
        Supplier<LocalDateTime[]> localDateTimeSupplier = () -> generateRandomLocalDateTimeArray(100000);
        // Test on date using pure tim sort.
        logger.info(getBenchmark("Sort LocalDateTimes using TimSort", Arrays::sort, null).run(localDateTimeSupplier, 100) + "ms");

        // Test on date using husky sort.
        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortSystemSort = new QuickHuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder);
        final HuskyHelper<ChronoLocalDateTime<?>> dateHelper = dateHuskySortSystemSort.getHelper();
        logger.info(getBenchmark("Sort LocalDateTimes using huskySort with TimSort", dateHuskySortSystemSort::sort, dateHelper::checkSorted).run(localDateTimeSupplier, 100) + "ms");

        // Test on date using husky sort with insertion sort.
        InsertionSort<ChronoLocalDateTime<?>> insertionSort = new InsertionSort<>();
        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortInsertionSort = new QuickHuskySort<>("QuickHuskySort/Insertion", HuskySortHelper.chronoLocalDateTimeCoder, insertionSort::mutatingSort);
        logger.info(getBenchmark("Sort LocalDateTimes using huskySort with insertionSort", dateHuskySortInsertionSort::sort, dateHelper::checkSorted).run(localDateTimeSupplier, 100) + "ms");
    }

    private Benchmark<LocalDateTime[]> getBenchmark(String description, Consumer<LocalDateTime[]> sorter, Consumer<LocalDateTime[]> checker) {
        return new Benchmark<>(
                description, (xs) -> Arrays.copyOf(xs, xs.length),
                sorter,
                checker
        );
    }

    void benchmarkStringSorters(String[] words, int nWords, int nRuns, Configurable config) {
        logger.info("Testing with " + nRuns + " runs of sorting " + nWords + " words");
        String normalizePrefix = "Normalized time per run: ";
        Function<Double, Double> normalizeNormalizer = (time) -> time / nWords / Math.log(nWords) * 1e6;

        // TODO incorporate these raw figures
        String rawPrefix = "Raw time per run: ";
        Function<Double, Double> rawNormalizer = (time) -> time;

        String sTimsort = "Timsort";
        logger.info(formatLocalDateTime() + ": Starting " + sTimsort + " test");
        performSortAndLogNormalizedTime(sTimsort, words, nWords, nRuns, normalizePrefix, normalizeNormalizer, Arrays::sort, null);

        final String sQuicksort = "Quicksort";
        logger.info(formatLocalDateTime() + ": Starting " + sQuicksort + " test");
        performSortAndLogNormalizedTime(sQuicksort, words, nWords, nRuns, normalizePrefix, normalizeNormalizer, new QuickSort_3way<String>()::mutatingSort, null);

        final String sIntroSort = "IntroSort";
        logger.info(formatLocalDateTime() + ": Starting " + sIntroSort + " test");
        performSortAndLogNormalizedTime(sIntroSort, words, nWords, nRuns, normalizePrefix, normalizeNormalizer, new IntroSort<String>()::mutatingSort, ((Sort<String>) new IntroSort<String>()).getHelper()::checkSorted);

        Helper<String> helper = new Helper<>("StringHelper", nWords, nanoTime());

        final SortBenchmark<String> sortBenchmark = new SortBenchmark<>(words, nRuns, normalizePrefix, normalizeNormalizer);

        sortBenchmark.run(nWords, new QuickHuskySort<>(UNICODE_CODER));

        // CONSIDER reinstating this when HuskyBucketSort is faster.
//        sortBenchmark.run(nWords, new HuskyBucketSort<>(nWords / 10, UNICODE_CODER));

        sortBenchmark.run(nWords, new IntroHuskySort<>(UNICODE_CODER));

        logger.info(formatLocalDateTime() + ": Starting " + "QuickHuskySort" + " test with insertion sort.");
        String sQuickHuskySortInsertion = "QuickHuskySort/Insertion";
        Sort<String> quickHuskySortInsertion = new QuickHuskySort<>(sQuickHuskySortInsertion, UNICODE_CODER, new InsertionSort<String>()::mutatingSort);
        performSortAndLogNormalizedTime(sQuickHuskySortInsertion, words, nWords, nRuns, normalizePrefix, normalizeNormalizer, quickHuskySortInsertion::sort, helper::checkSorted);

        logger.info(formatLocalDateTime() + ": Starting " + "IntroHuskySort" + " test with insertion sort.");
        String sIntroHuskySortInsertion = "IntroHuskySort/Insertion";
        Sort<String> introHuskySortInsertion = new IntroHuskySort<>(sIntroHuskySortInsertion, UNICODE_CODER, new InsertionSort<String>()::mutatingSort);
        performSortAndLogNormalizedTime(sIntroHuskySortInsertion, words, nWords, nRuns, normalizePrefix, normalizeNormalizer, introHuskySortInsertion::sort, helper::checkSorted);

        logger.info(formatLocalDateTime() + ": Starting " + "QuickHuskySort" + " test with printout inversions");
        Sort<String> quickHuskySortNone = new QuickHuskySort<>("QuickHuskySort/print inversions", UNICODE_CODER, (xs2) -> {
            // do nothing, so we can count inversions.
        });
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

    private static void performSortAndLogNormalizedTime(String description, String[] words, int nWords, int nRuns, String prefix, Function<Double, Double> normalizer, Consumer<String[]> sortFunction, Consumer<String[]> checkFunction) {
        logNormalizedTime(
                new Benchmark<>(description, sortFunction, checkFunction).run(generateRandomStringArray(words, nWords), nRuns),
                prefix,
                normalizer
        );
    }

    private static List<String> lineAsList(String line) {
        List<String> words = new ArrayList<>();
        words.add(line);
        return words;
    }

    private static List<String> getLeipzigWords(String line) {
        return getWords(regexLeipzig, line);
    }


    private final Configurable config;

    // NOTE: not currently used
    private static void doStdSortBenchmark(String[] words, int nWords, int nRuns, String normalizePrefix, Function<Double, Double> normalizeNormalizer, Sort<String> sorter) {
        logger.info(formatLocalDateTime() + ": Starting " + sorter + " test");
        logger.info(formatLocalDateTime() + ": Starting " + sorter + " test");
        performSortAndLogNormalizedTime(sorter.toString(), words, nWords, nRuns, normalizePrefix, normalizeNormalizer, sorter::sort, sorter.getHelper()::checkSorted);
    }

    final static LazyLogger logger = new LazyLogger(HuskySortBenchmark.class);

    final static Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");

}
