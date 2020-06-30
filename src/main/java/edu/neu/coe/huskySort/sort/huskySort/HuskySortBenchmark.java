/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.simple.*;
import edu.neu.coe.huskySort.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static edu.neu.coe.huskySort.sort.huskySort.AbstractHuskySort.UNICODE_CODER;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.getWords;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.englishCoder;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.generateRandomLocalDateTimeArray;
import static edu.neu.coe.huskySort.util.Utilities.*;

public class HuskySortBenchmark {

    public HuskySortBenchmark(Config config) {
        this.config = config;
    }

    public static void main(String[] args) throws IOException {
        Config config = Config.load(HuskySortBenchmark.class);
        logger.info("HuskySortBenchmark.main: " + config.get("huskysort", "version") + " with word counts: " + Arrays.toString(args));
        if (args.length == 0) logger.warn("No word counts specified on the command line");
        HuskySortBenchmark benchmark = new HuskySortBenchmark(config);
        benchmark.sortNumerics(100000);
        benchmark.sortStrings(Arrays.stream(args).map(Integer::parseInt));
        benchmark.sortLocalDateTimes(100000);
    }

    private void sortStrings(Stream<Integer> wordCounts) throws IOException {
        logger.info("Beginning String sorts");

        // NOTE: common words benchmark
        benchmarkStringSorters(getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList), 4000, 5000);

        // NOTE: Leipzig English words benchmarks (according to command-line arguments)
        wordCounts.forEach(this::doLeipzigBenchmarkEnglish);

        // NOTE: Leipzig Chines words benchmarks (according to command-line arguments)
        doLeipzigBenchmark("zho-simp-tw_web_2014_10K-sentences.txt", 5000, 1000);
    }

    private void doLeipzigBenchmarkEnglish(int x) {
        String resource = "eng-uk_web_2002_" + (x < 50000 ? "10K" : x < 200000 ? "100K" : "1M") + "-sentences.txt";
        try {
            doLeipzigBenchmark(resource, x, Utilities.round(100000000 / minComparisons(x)));
        } catch (FileNotFoundException e) {
            logger.warn("Unable to find resource: " + resource, e);
        }
    }

    public void sortLocalDateTimes(final int n) {
        logger.info("Beginning LocalDateTime sorts");
        // TODO why do we have localDateTimeSupplier IN ADDITION TO localDateTimes?
        Supplier<LocalDateTime[]> localDateTimeSupplier = () -> generateRandomLocalDateTimeArray(n);
        BaseHelper<ChronoLocalDateTime<?>> helper = new BaseHelper<>("DateTimeHelper");
        final LocalDateTime[] localDateTimes = generateRandomLocalDateTimeArray(n);

        // CONSIDER finding the common ground amongst these sorts and get them all working together.

        // NOTE Test on date using pure tim sort.
        if (isConfigBenchmarkDateSorter("timsort"))
            logger.info(benchmarkFactory("Sort LocalDateTimes using Arrays::sort (TimSort)", Arrays::sort, null).run(localDateTimeSupplier, 100) + "ms");

        // NOTE this is supposed to match the previous benchmark run exactly. I don't understand why it takes rather less time.
        if (isConfigBenchmarkDateSorter("timsort")) {
            logger.info(benchmarkFactory("Repeat Sort LocalDateTimes using timSort::mutatingSort", new TimSort<>(helper)::mutatingSort, null).run(localDateTimeSupplier, 100) + "ms");
            // NOTE this is intended to replace the run two lines previous. It should take the exact same amount of time.
            runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, n, 100, 0);
        }

        // NOTE Test on date using husky sort.
        if (isConfigBenchmarkDateSorter("quickhuskysort"))
            dateSortBenchmark(localDateTimeSupplier, localDateTimes, new QuickHuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder, config), "Sort LocalDateTimes using huskySort with TimSort", 1);

        // NOTE Test on date using husky sort with insertion sort.
        if (isConfigBenchmarkDateSorter("quickhuskyinsertionsort"))
            dateSortBenchmark(localDateTimeSupplier, localDateTimes, new QuickHuskySort<>("QuickHuskySort/Insertion", HuskySortHelper.chronoLocalDateTimeCoder, new InsertionSort<>(helper)::mutatingSort, config), "Sort LocalDateTimes using huskySort with insertionSort", 2);
    }

    public void sortNumerics(final int n) {
        logger.info("Beginning Numeric sorts");
        String timsort = "timsort";
        String introhuskysort = "introhuskysort";
        String sInteger = "integer";
        String sDouble = "double";
        String sLong = "long";
        String sBigInteger = "biginteger";
        String sBigDecimal = "bigdecimal";

        if (isConfigBenchmarkNumberSorter(timsort, sInteger))
            sortNumeric(n, Integer.class, Random::nextInt, Arrays::sort, null);
        if (isConfigBenchmarkNumberSorter(introhuskysort, sInteger))
            sortNumeric(n, Integer.class, Random::nextInt, new PureHuskySort<>(HuskySortHelper.integerCoder)::sort, Utilities::checkSorted);

        if (isConfigBenchmarkNumberSorter(timsort, sDouble))
            sortNumeric(n, Double.class, Random::nextDouble, Arrays::sort, null);
        if (isConfigBenchmarkNumberSorter(introhuskysort, sDouble))
            sortNumeric(n, Double.class, Random::nextDouble, new PureHuskySort<>(HuskySortHelper.doubleCoder)::sort, Utilities::checkSorted);

        if (isConfigBenchmarkNumberSorter(timsort, sLong))
            sortNumeric(n, Long.class, Random::nextLong, Arrays::sort, null);
        if (isConfigBenchmarkNumberSorter(introhuskysort, sLong))
            sortNumeric(n, Long.class, Random::nextLong, new PureHuskySort<>(HuskySortHelper.longCoder)::sort, Utilities::checkSorted);

        if (isConfigBenchmarkNumberSorter(timsort, sBigInteger))
            sortNumeric(n, BigInteger.class, r -> BigInteger.valueOf(r.nextLong()), Arrays::sort, null);
        if (isConfigBenchmarkNumberSorter(introhuskysort, sBigInteger))
            sortNumeric(n, BigInteger.class, r -> BigInteger.valueOf(r.nextLong()), new PureHuskySort<>(HuskySortHelper.bigIntegerCoder)::sort, Utilities::checkSorted);

        if (isConfigBenchmarkNumberSorter(timsort, sBigDecimal))
            sortNumeric(n, BigDecimal.class, r -> BigDecimal.valueOf(r.nextDouble() * Long.MAX_VALUE), Arrays::sort, null);
        if (isConfigBenchmarkNumberSorter(introhuskysort, sBigDecimal))
            sortNumeric(n, BigDecimal.class, r -> BigDecimal.valueOf(r.nextDouble() * Long.MAX_VALUE), new PureHuskySort<>(HuskySortHelper.bigDecimalCoder)::sort, Utilities::checkSorted);
    }

    public static <X extends Number & Comparable<X>> void sortNumeric(int n, final Class<X> clazz, final Function<Random, X> randomNumberFunction, final Consumer<X[]> sortFunction, final Consumer<X[]> postProcessor) {
        final Benchmark<X[]> benchmark = new Benchmark<>(
                "System sort for " + clazz,
                (xs) -> Arrays.copyOf(xs, xs.length),
                sortFunction,
                postProcessor
        );
        logger.info(benchmark.run(getSupplier(n, clazz, randomNumberFunction), 100) + "ms");
    }

    /**
     * Method to run pure (non-instrumented) string sorter benchmarks.
     * <p>
     * NOTE: this is package-private because it is used by unit tests.
     *
     * @param words  the word source.
     * @param nWords the number of words to be sorted.
     * @param nRuns  the number of runs.
     */
    void benchmarkStringSorters(String[] words, int nWords, int nRuns) {
        logger.info("Testing pure sorts with " + formatWhole(nRuns) + " runs of sorting " + formatWhole(nWords) + " words");
        Random random = new Random();

        if (isConfigBenchmarkStringSorter("purehuskysort")) {
            PureHuskySort<String> pureHuskySort = new PureHuskySort<>(englishCoder);
            Benchmark<String[]> benchmark = new Benchmark<>("PureHuskySort", null, pureHuskySort::sort, null);
            doPureBenchmark(words, nWords, nRuns, random, benchmark);
        }

        if (isConfigBenchmarkStringSorter("puresystemsort")) {
            Benchmark<String[]> benchmark = new Benchmark<>("SystemSort", null, Arrays::sort, null);
            doPureBenchmark(words, nWords, nRuns, random, benchmark);
        }
    }

    /**
     * Method to run instrumented string sorter benchmarks.
     * <p>
     * NOTE: this is package-private because it is used by unit tests.
     *
     * @param words  the word source.
     * @param nWords the number of words to be sorted.
     * @param nRuns  the number of runs.
     */
    void benchmarkStringSortersInstrumented(String[] words, int nWords, int nRuns) {
        logger.info("Testing with " + formatWhole(nRuns) + " runs of sorting " + formatWhole(nWords) + " words" + (config.isInstrumented() ? " and instrumented" : ""));

        if (isConfigBenchmarkStringSorter("mergesort"))
            runStringSortBenchmark(words, nWords, nRuns, new MergeSortBasic<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("quicksort3way"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickSort_3way<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("quicksort"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickSort_DualPivot<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("introsort"))
            runStringSortBenchmark(words, nWords, nRuns, new IntroSort<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("introhuskysort")) {
            IntroHuskySort<String> sorter = IntroHuskySort.createIntroHuskySortWithInversionCount(UNICODE_CODER, nWords, config);
            runStringSortBenchmark(words, nWords, nRuns, sorter, timeLoggersLinearithmic);
            if (IntroHuskySort.isCountInterimInversions(config) && sorter.isClosed()) logInterimInversions(nWords, sorter);
        }

        if (isConfigBenchmarkStringSorter("quickhuskysort"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickHuskySort<>(UNICODE_CODER, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("quickuskyinsertionsort"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickHuskySort<>("QuickHuskySort/Insertion", UNICODE_CODER, new InsertionSort<String>()::mutatingSort, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("introhuskyinsertionsort"))
            runStringSortBenchmark(words, nWords, nRuns, new IntroHuskySort<>("IntroHuskySort/Insertion", UNICODE_CODER, new InsertionSort<String>()::mutatingSort, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("huskybucketsort")) {
            final SortWithHelper<String> sorter = new HuskyBucketSort<>(16, UNICODE_CODER, config);
            sorter.init(nWords);
            runStringSortBenchmark(words, nWords, nRuns, sorter, timeLoggersLinearithmic);
        }

        if (isConfigBenchmarkStringSorter("huskybucketintrosort"))
            runStringSortBenchmark(words, nWords, nRuns, new HuskyBucketSort<>(1000, UNICODE_CODER, config), timeLoggersLinearithmic);

        // NOTE: this is very slow of course, so recommendation is not to enable this option.
        if (isConfigBenchmarkStringSorter("insertionsort"))
            runStringSortBenchmark(words, nWords, nRuns / 10, new InsertionSort<>(nWords, config), timeLoggersQuadratic);
    }

    /**
     * Method to run a sorting benchmark, using an explicit preProcessor.
     *
     * @param words        an array of available words (to be chosen randomly).
     * @param nWords       the number of words to be sorted.
     * @param nRuns        the number of runs of the sort to be preformed.
     * @param sorter       the sorter to use--NOTE that this sorter will be closed at the end of this method.
     * @param preProcessor the pre-processor function, if any.
     * @param timeLoggers  a set of timeLoggers to be used.
     */
    static void runStringSortBenchmark(String[] words, int nWords, int nRuns, SortWithHelper<String> sorter, UnaryOperator<String[]> preProcessor, TimeLogger[] timeLoggers) {
        new SorterBenchmark<>(String.class, preProcessor, sorter, words, nRuns, timeLoggers).run(nWords);
        sorter.close();
    }

    /**
     * Method to run a sorting benchmark using the standard preProcess method of the sorter.
     *
     * @param words       an array of available words (to be chosen randomly).
     * @param nWords      the number of words to be sorted.
     * @param nRuns       the number of runs of the sort to be preformed.
     * @param sorter      the sorter to use--NOTE that this sorter will be closed at the end of this method.
     * @param timeLoggers a set of timeLoggers to be used.
     *
     * NOTE: this method is public because it is referenced in a unit test of a different package
     */
    public static void runStringSortBenchmark(String[] words, int nWords, int nRuns, SortWithHelper<String> sorter, TimeLogger[] timeLoggers) {
        runStringSortBenchmark(words, nWords, nRuns, sorter, sorter::preProcess, timeLoggers);
    }

    /**
     * For mergesort, the number of array accesses is actually 6 times the number of comparisons.
     * That's because, in addition to each comparison, there will be approximately two copy operations.
     * Thus, in the case where comparisons are based on primitives,
     * the normalized time per run should approximate the time for one array access.
     */
    public final static TimeLogger[] timeLoggersLinearithmic = {
            new TimeLogger("Raw time per run (mSec): ", (time, n) -> time),
            new TimeLogger("Normalized time per run (n log n): ", (time, n) -> time / minComparisons(n) / 6 * 1e6)
    };

    final static LazyLogger logger = new LazyLogger(HuskySortBenchmark.class);

    final static Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");

    /**
     * This is based on log2(n!)
     *
     * @param n the number of elements.
     * @return the minimum number of comparisons possible to sort n randomly ordered elements.
     */
    static double minComparisons(int n) {
        double lgN = lg(n);
        return n * (lgN - LgE) + lgN / 2 + 1.33;
    }

    /**
     * This is the mean number of inversions in a randomly ordered set of n elements.
     * For insertion sort, each (low-level) swap fixes one inversion, so on average there are this number of swaps.
     * The minimum number of comparisons is slightly higher.
     *
     * @param n the number of elements
     * @return one quarter n-squared more or less.
     */
    static double meanInversions(int n) {
        return 0.25 * n * (n - 1);
    }

    static List<String> lineAsList(String line) {
        List<String> words = new ArrayList<>();
        words.add(line);
        return words;
    }

    private static <T extends Number & Comparable<T>> Supplier<T[]> getSupplier(int n, final Class<T> clazz, Function<Random, T> randomNumberFunction) {
        return () -> Utilities.fillRandomArray(clazz, new Random(), n, randomNumberFunction);
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

    private static void doPureBenchmark(String[] words, int nWords, int nRuns, Random random, Benchmark<String[]> benchmark) {
        final double time = benchmark.run(() -> Utilities.fillRandomArray(String.class, random, nWords, r -> words[r.nextInt(words.length)]), nRuns);
        for (TimeLogger timeLogger : timeLoggersLinearithmic) timeLogger.log(time, nWords);
    }

    private void dateSortBenchmark(Supplier<LocalDateTime[]> localDateTimeSupplier, LocalDateTime[] localDateTimes, QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortSystemSort, String s, int i) {
        logger.info(benchmarkFactory(s, dateHuskySortSystemSort::sort, dateHuskySortSystemSort::postProcess).run(localDateTimeSupplier, 100) + "ms");
        // NOTE: this is intended to replace the run in the previous line. It should take the exact same amount of time.
        runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, 100000, 100, i);
    }

    private void doLeipzigBenchmark(String resource, int nWords, int nRuns) throws FileNotFoundException {
        benchmarkStringSorters(getWords(resource, HuskySortBenchmark::getLeipzigWords), nWords, nRuns);
        if (isConfigBoolean(Config.HELPER, BaseHelper.INSTRUMENT))
            benchmarkStringSortersInstrumented(getWords(resource, HuskySortBenchmark::getLeipzigWords), nWords, nRuns);
    }

    @SuppressWarnings("SameParameterValue")
    private void runDateTimeSortBenchmark(Class<?> tClass, ChronoLocalDateTime<?>[] dateTimes, int N, int m, int whichSort) {
        final SortWithHelper<ChronoLocalDateTime<?>> sorter = whichSort == 0 ? new TimSort<>() : whichSort == 1 ? new QuickHuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder, config) : new QuickHuskySort<>("QuickHuskySort/Insertion", HuskySortHelper.chronoLocalDateTimeCoder, new InsertionSort<ChronoLocalDateTime<?>>()::mutatingSort, config);
        @SuppressWarnings("unchecked") final SorterBenchmark<ChronoLocalDateTime<?>> sorterBenchmark = new SorterBenchmark<>((Class<ChronoLocalDateTime<?>>) tClass, (xs) -> Arrays.copyOf(xs, xs.length), sorter, dateTimes, m, timeLoggersLinearithmic);
        sorterBenchmark.run(N);
    }

    private static void logInterimInversions(int nWords, IntroHuskySort<String> sorter) {
        double mean = sorter.getMeanInterimInversions();
        String percentage = formatDecimal3Places((1.0 - mean / IntroHuskySort.expectedInversions(nWords)) * 100);
        logger.debug("HuskySort interim inversions: " + asInt(mean));
        logger.info("HuskySort first pass success rate: " + percentage + "%");
    }

    private static double lg(double n) {
        return Math.log(n) / Math.log(2);
    }

    /**
     * For (basic) insertionsort, the number of array accesses is actually 6 times the number of comparisons.
     * That's because, for each inversions, there will typically be one swap (four array accesses) and (at least) one comparision (two array accesses).
     * Thus, in the case where comparisons are based on primitives,
     * the normalized time per run should approximate the time for one array access.
     */
    private final static TimeLogger[] timeLoggersQuadratic = {
            new TimeLogger("Raw time per run (mSec): ", (time, n) -> time),
            new TimeLogger("Normalized time per run (n^2): ", (time, n) -> time / meanInversions(n) / 6 * 1e6)
    };

    private static final Consumer<String[]> DO_NOTHING = (xs2) -> {
        // XXX do nothing.
    };

    private static final double LgE = lg(Math.E);

    private boolean isConfigBenchmarkStringSorter(String option) {
        return isConfigBoolean("benchmarkstringsorters", option);
    }

    private boolean isConfigBenchmarkDateSorter(String option) {
        return isConfigBoolean("benchmarkdatesorters", option);
    }

    private boolean isConfigBenchmarkNumberSorter(String sortOption, String typeOption) {
        return isConfigBoolean("benchmarknumbersorters", sortOption) && isConfigBoolean("benchmarknumbersorters", typeOption);
    }

    private boolean isConfigBoolean(String section, String option) {
        return config.getBoolean(section, option);
    }

    private final Config config;
}
