/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.SortException;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortable;
import edu.neu.coe.huskySort.sort.radix.Alphabet;
import edu.neu.coe.huskySort.sort.radix.MSDStringSort;
import edu.neu.coe.huskySort.sort.simple.TimSort;
import edu.neu.coe.huskySort.sort.simple.*;
import edu.neu.coe.huskySort.util.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static edu.neu.coe.huskySort.sort.huskySort.AbstractHuskySort.UNICODE_CODER;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.chineseEncoder;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.englishCoder;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.generateRandomLocalDateTimeArray;
import static edu.neu.coe.huskySort.util.Utilities.*;

public final class HuskySortBenchmark {

    public HuskySortBenchmark(final Config config) {
        this.config = config;
    }

    /**
     * Run the main four benchmarks: string sorts, tuple sorts, date sorts and number sorts.
     */
    public void runBenchmarks() {
        // CONSIDER refactoring the following to conform to the others
        sortStrings(config.getIntegerStream("benchmarkstringsorters", "sizes"), 500000000);
        config.getIntegerStream("benchmarktuplesorters", "sizes").forEach(x -> sortTuples(x, 250000000));
        config.getIntegerStream("benchmarkdatesorters", "sizes").forEach(x -> sortLocalDateTimes(x, 50000000));
        config.getIntegerStream("benchmarknumbersorters", "sizes").forEach(x -> sortNumerics(x, 250000000));
    }

    /**
     * Benchmark the string sorts.
     *
     * @param wordCounts the numbers of words in the corpus files.
     * @param totalOps   the total number of comparison operations for these sorts.
     */
    @SuppressWarnings("SameParameterValue")
    void sortStrings(final Stream<Integer> wordCounts, final int totalOps) {
        logger.info("sortStrings: beginning String sorts");
        wordCounts.forEach(x -> doSortStrings(x, getRepetitions(x, totalOps)));
    }

    private void doSortStrings(final int n, final int m) {
        // NOTE: Leipzig English words benchmarks (according to command-line arguments)
        if (isConfigBenchmarkStringSorter("leipzigenglish"))
            doLeipzigBenchmarkEnglish(n, m);

        // NOTE: Leipzig Chinese words benchmarks (according to command-line arguments)
        if (isConfigBenchmarkStringSorter("leipzigchinese"))
            doLeipzigBenchmark("zho-simp-tw_web_2014_10K-sentences.txt", n, m, UNICODE_CODER);

        // NOTE: common words benchmark
        if (isConfigBenchmarkStringSorter("english"))
            benchmarkStringSorters(COMMON_WORDS_CORPUS, HuskySortBenchmarkHelper.getWords(COMMON_WORDS_CORPUS, HuskySortBenchmark::lineAsList), n, m, englishCoder);

        // NOTE: Chinese Name corpus benchmarks (according to command-line arguments)
        if (isConfigBenchmarkStringSorter("chinesenames"))
            benchmarkStringSorters(CHINESE_NAMES_CORPUS, HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList), n, m, chineseEncoder);
    }

    /**
     * Run benchmarks on sorting LocalDateTime.
     * CONSIDER: having this method always compare the system sort with pure husky sort.
     *
     * @param n        the number of elements to sort.
     * @param totalOps the total number of comparison operations for these sorts.
     */
    void sortLocalDateTimes(final int n, final int totalOps) {
        logger.info("sortLocalDateTimes: beginning LocalDateTime sorts");
        final int m = getRepetitions(n, totalOps);

        // TODO why do we have localDateTimeSupplier IN ADDITION TO localDateTimes?
        final Supplier<LocalDateTime[]> localDateTimeSupplier = () -> generateRandomLocalDateTimeArray(n);
        final BaseHelper<ChronoLocalDateTime<?>> helper = new BaseHelper<>("DateTimeHelper");
        final LocalDateTime[] localDateTimes = generateRandomLocalDateTimeArray(n);

        // CONSIDER finding the common ground amongst these sorts and get them all working together.

        // NOTE Test on date using pure tim sort.
        if (isConfigBenchmarkDateSorter("timsort"))
            logBenchmarkRun(HuskySortBenchmark.<LocalDateTime>benchmarkFactory(getDescription(n, "Sort ", " LocalDateTimes using Arrays::sort (TimSort)"), Arrays::sort, null).run(localDateTimeSupplier, m));

        // NOTE this is supposed to match the previous benchmark run exactly. I don't understand why it takes rather less time.
        if (isConfigBenchmarkDateSorter("timsort")) {
            logBenchmarkRun(HuskySortBenchmark.<LocalDateTime>benchmarkFactory(getDescription(n, "Repeat Sort ", " LocalDateTimes using timSort::mutatingSort"), new TimSort<>(helper)::mutatingSort, null).run(localDateTimeSupplier, m));
            // NOTE this is intended to replace the run two lines previous. It should take the exact same amount of time.
            runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, n, m, 0);
        }

        // NOTE Test on date using husky sort.
        if (isConfigBenchmarkDateSorter("quickhuskysort"))
            dateSortBenchmark(localDateTimeSupplier, localDateTimes, new QuickHuskySort<>(HuskyCoderFactory.chronoLocalDateTimeCoder, config), "Sort LocalDateTimes using huskySort with TimSort", 1, n, m);

        // NOTE Test on date using husky sort with insertion sort.
        if (isConfigBenchmarkDateSorter("quickhuskyinsertionsort"))
            dateSortBenchmark(localDateTimeSupplier, localDateTimes, new QuickHuskySort<>("QuickHuskySort/Insertion", HuskyCoderFactory.chronoLocalDateTimeCoder, new InsertionSort<>(helper)::mutatingSort, config), "Sort LocalDateTimes using huskySort with insertionSort", 2, n, m);
    }

    /**
     * Method to benchmark sorting of Tuples.
     * NOTE: this method always compares the system sort with pure husky sort.
     *
     * @param n        the number of elements to sort.
     * @param totalOps the total number of comparison operations for these sorts.
     */
    void sortTuples(final int n, final int totalOps) {
        logger.info("sortTuples: beginning Tuple sorts");
        final int m = getRepetitions(n, totalOps);
        final Tuple[] tuples = new Tuple[n];
        for (int i = 0; i < n; i++) tuples[i] = Tuple.create();
        compareSystemAndPureHuskySorts(n + " Tuples", getSupplier(n, Tuple.class, r -> tuples[r.nextInt(n)]), HuskyCoderFactory.createGenericCoder(), null, this::isConfigBenchmarkTupleSorter, m);
    }

    private static int getRepetitions(final int n, final int totalOps) {
        return MIN_REPS + round(totalOps / minComparisons(n));
    }

    /**
     * Method to benchmark sorting of various number types: Integer, Double, Long, BigInteger, Decimal.
     * Keep in mind that an actual Java program would be unlikely to want to sort an array of Integer, Double or Long because
     * they could be unboxed into an array of primitives.
     * However, they are here to add artistic verisimilitude to an otherwise bald and unconvincing narrative.
     * <p>
     * NOTE: this method always compares the system sort with pure husky sort.
     *
     * @param n        the number of elements to sort.
     * @param totalOps the total number of comparison operations for these sorts.
     */
    void sortNumerics(final int n, final int totalOps) {
        logger.info("sortNumerics: beginning numeric sorts");
        final int m = getRepetitions(n, totalOps);

        compareSystemAndPureHuskySortsNumeric(n + " Integers", getSupplier(n, Integer.class, Random::nextInt), HuskyCoderFactory.integerCoder, null, s1 -> isConfigBenchmarkNumberSorter(s1, "integer"), m, Integer.class, true);

        compareSystemAndPureHuskySortsNumeric(n + " Doubles", getSupplier(n, Double.class, Random::nextDouble), HuskyCoderFactory.doubleCoder, null, s1 -> isConfigBenchmarkNumberSorter(s1, "double"), m, Double.class, false);

        compareSystemAndPureHuskySortsNumeric(n + " Longs", getSupplier(n, Long.class, Random::nextLong), HuskyCoderFactory.longCoder, null, s1 -> isConfigBenchmarkNumberSorter(s1, "long"), m, Long.class, true);

        compareSystemAndPureHuskySortsNumeric(n + " BigIntegers", getSupplier(n, BigInteger.class, r1 -> BigInteger.valueOf(r1.nextLong())), HuskyCoderFactory.bigIntegerCoder, null, s1 -> isConfigBenchmarkNumberSorter(s1, "biginteger"), m, BigInteger.class, true);

        compareSystemAndPureHuskySortsNumeric(n + " BigDecimals", getSupplier(n, BigDecimal.class, r -> BigDecimal.valueOf(r.nextDouble() * Long.MAX_VALUE)), HuskyCoderFactory.bigDecimalCoder, null, s -> isConfigBenchmarkNumberSorter(s, "bigdecimal"), m, BigDecimal.class, false);

        compareSystemAndPureHuskySortsNumeric(n + " Bytes", getSupplier(n, Byte.class, byteFunction), HuskyCoderFactory.createProbabilisticCoder(config.getDouble("benchmarknumbersorters", "pcrit", 0.15)), null, s -> isConfigBenchmarkNumberSorter(s, "probabilistic"), m, Byte.class, true);
        compareSystemAndPureHuskySortsNumeric(n + " Integers", getSupplier(n, Integer.class, Random::nextInt), HuskyCoderFactory.createProbabilisticCoder(config.getDouble("benchmarknumbersorters", "pcrit", 0.15)), null, s -> isConfigBenchmarkNumberSorter(s, "probabilistic"), m, Integer.class, true);
    }

    /**
     * Method to run pure (non-instrumented) string sorter benchmarks.
     * <p>
     * NOTE: this is package-private because it is used by unit tests.
     * <p>
     * CONSIDER merging this with compareSystemAndPureHuskySorts
     *
     * @param corpus     the name of the corpus file to be used as a source of Strings.
     * @param words      the word source.
     * @param nWords     the number of words to be sorted.
     * @param nRuns      the number of runs.
     * @param huskyCoder the Husky coder to use in the test of PureHuskySort.
     */
    void benchmarkStringSorters(final String corpus, final String[] words, final int nWords, final int nRuns, final HuskyCoder<String> huskyCoder) {
        logger.info("benchmarkStringSorters: testing pure sorts with " + formatWhole(nRuns) + " runs of sorting " + formatWhole(nWords) + " words using coder: " + huskyCoder.name());
        final Random random = new Random();
        final boolean preSorted = isConfigBenchmarkStringSorter("presorted");
        final String s2 = ") words from " + corpus;

        if (isConfigBenchmarkStringSorter("puresystemsort")) {
            final Benchmark<String[]> benchmark = new Benchmark<>(getDescription(nWords, "SystemSort", s2), null, Arrays::sort, null);
            doPureBenchmark(words, nWords, nRuns, random, benchmark, preSorted);
        }

        if (isConfigBenchmarkStringSorter("purehuskysort")) {
            final boolean purehuskysortwithinsertionsort = isConfigBenchmarkStringSorter("purehuskysortwithinsertionsort");
            final PureHuskySort<String> pureHuskySort = new PureHuskySort<>(huskyCoder, preSorted, purehuskysortwithinsertionsort);
            final String s1 = "PureHuskySort" + (purehuskysortwithinsertionsort ? " with insertion sort" : "");
            final Benchmark<String[]> benchmark = new Benchmark<>(getDescription(nWords, s1, s2), null, pureHuskySort::sort, null);
            doPureBenchmark(words, nWords, nRuns, random, benchmark, preSorted);
        }

        if (isConfigBenchmarkStringSorter("mergehuskysort")) {
            final MergeHuskySort<String> mergeHuskySort = new MergeHuskySort<>(huskyCoder);
            final Benchmark<String[]> benchmark = new Benchmark<>(getDescription(nWords, "MergeHuskySort", s2), null, mergeHuskySort::sort, null);
            doPureBenchmark(words, nWords, nRuns, random, benchmark, preSorted);
        }

        if (isConfigBenchmarkStringSorter("puremergesort")) {
            final PrivateMethodInvoker invoker = new PrivateMethodInvoker(Arrays.class);
            final Class<?>[] classes = new Class[]{Object[].class};
            final Consumer<String[]> sort = strings -> invoker.invokePrivateExplicit("legacyMergeSort", classes, new Object[]{strings});
            final Benchmark<String[]> benchmark = new Benchmark<>(getDescription(nWords, "Legacy MergeSort", s2), null, sort, null);
            doPureBenchmark(words, nWords, nRuns, random, benchmark, preSorted);
        }

        if (isConfigBenchmarkStringSorter("purequicksort")) {
            final Benchmark<String[]> benchmark = new Benchmark<>(getDescription(nWords, "DualPivotQuicksort", s2), null, PureDualPivotQuicksort::sort, null);
            doPureBenchmark(words, nWords, nRuns, random, benchmark, preSorted);
        }

        if (isConfigBenchmarkStringSorter("msdstringsort")) {
            final MSDStringSort sorter = new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE));
            final Benchmark<String[]> benchmark = new Benchmark<>(getDescription(nWords, "MSDStringSort", s2), (x) -> {
                sorter.reset();
                return x;
//            }, sorter::sort);
            }, sorter::sort, HuskySortBenchmark::checkSorted);
            try {
                doPureBenchmark(words, nWords, nRuns, random, benchmark, preSorted);
            } catch (final SortException e) {
                final Alphabet alphabet = sorter.getAlphabet();
                System.out.println(alphabet);
                throw new RuntimeException("sort exception", e);
            }
        }
    }

    /**
     * NOTE: this may be duplicated elsewhere.
     *
     * @param xs an array of Comparables.
     */
    private static void checkSorted(final String[] xs) {
        if (xs.length < 2) return;
        for (int i = 1; i < xs.length; i++)
            if (xs[i].compareTo(xs[i - 1]) < 0) {
                System.out.println(Arrays.toString(xs));
                // TODO what are these two variables for?
                final char[] charsXsi_1 = xs[i - 1].toCharArray();
                final char[] charsXsi = xs[i].toCharArray();
                System.out.println(xs[i - 1]);
                System.out.println(xs[i]);
                throw new SortException("not in order at index " + i);
            }
    }

    private static String getDescription(final int nWords, final String s1, final String s2) {
        return s1 + " (" + nWords + s2;
    }

    /**
     * Method to run instrumented string sorter benchmarks.
     * <p>
     * NOTE: this is package-private because it is used by unit tests.
     *
     * @param words      the word source.
     * @param nWords     the number of words to be sorted.
     * @param nRuns      the number of runs.
     * @param huskyCoder the Husky coder to be used for the runs of HuskySort.
     */
    void benchmarkStringSortersInstrumented(final String[] words, final int nWords, final int nRuns, final HuskyCoder<String> huskyCoder) {
        logger.info("benchmarkStringSortersInstrumented: testing with " + formatWhole(nRuns) + " runs of sorting " + formatWhole(nWords) + " words" + (config.isInstrumented() ? " and instrumented" : ""));

        if (isConfigBenchmarkStringSorter("mergesort"))
            runStringSortBenchmark(words, nWords, nRuns, new MergeSortBasic<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("quicksort3way"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickSort_3way<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("quicksort"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickSort_DualPivot<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("introsort"))
            runStringSortBenchmark(words, nWords, nRuns, new IntroSort<>(nWords, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("introhuskysort")) {
            final IntroHuskySort<String> sorter = IntroHuskySort.createIntroHuskySortWithInversionCount(huskyCoder, nWords, config);
            runStringSortBenchmark(words, nWords, nRuns, sorter, timeLoggersLinearithmic);
            if (IntroHuskySort.isCountInterimInversions(config) && sorter.isClosed())
                logInterimInversions(nWords, sorter);
        }

        if (isConfigBenchmarkStringSorter("quickhuskysort"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickHuskySort<>(huskyCoder, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("quickuskyinsertionsort"))
            runStringSortBenchmark(words, nWords, nRuns, new QuickHuskySort<>("QuickHuskySort/Insertion", huskyCoder, new InsertionSort<String>()::mutatingSort, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("introhuskyinsertionsort"))
            runStringSortBenchmark(words, nWords, nRuns, new IntroHuskySort<>("IntroHuskySort/Insertion", huskyCoder, new InsertionSort<String>()::mutatingSort, config), timeLoggersLinearithmic);

        if (isConfigBenchmarkStringSorter("huskybucketsort")) {
            final SortWithHelper<String> sorter = new HuskyBucketSort<>(16, huskyCoder, config);
            sorter.init(nWords);
            runStringSortBenchmark(words, nWords, nRuns, sorter, timeLoggersLinearithmic);
        }

        if (isConfigBenchmarkStringSorter("huskybucketintrosort"))
            runStringSortBenchmark(words, nWords, nRuns, new HuskyBucketSort<>(1000, huskyCoder, config), timeLoggersLinearithmic);

        // NOTE: this is very slow of course, so recommendation is not to enable this option.
        if (isConfigBenchmarkStringSorter("insertionsort"))
            runStringSortBenchmark(words, nWords, nRuns / 10, new InsertionSort<>(nWords, config), timeLoggersQuadratic);

        // NOTE: we do not invoke MSDStringSort here at all.
    }

    /**
     * Main program to run all the standard benchmarks.
     *
     * @param args a list of corpus file sizes, such as 10000, 100000, 1M.
     * @throws IOException problem opening a resource.
     */
    public static void main(final String[] args) throws IOException {
        final Config config = Config.load(HuskySortBenchmark.class);
        logger.info("***********************************************************************************************\n" +
                "HuskySortBenchmark.main: " + config.get("huskysort", "version"));
        final HuskySortBenchmark benchmark = new HuskySortBenchmark(config);
        benchmark.runBenchmarks();
        logger.info("***********************************************************************************************\n");
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
    static void runStringSortBenchmark(final String[] words, final int nWords, final int nRuns, final SortWithHelper<String> sorter, final UnaryOperator<String[]> preProcessor, final TimeLogger[] timeLoggers) {
        new SorterBenchmark<>(String.class, preProcessor, sorter, words, nRuns, timeLoggers).run(nWords);
        sorter.close();
    }

    /**
     * Method to run a sorting benchmark using the standard preProcess method of the sorter.
     * NOTE: this method is public because it is referenced in a unit test of a different package
     *
     * @param words       an array of available words (to be chosen randomly).
     * @param nWords      the number of words to be sorted.
     * @param nRuns       the number of runs of the sort to be preformed.
     * @param sorter      the sorter to use--NOTE that this sorter will be closed at the end of this method.
     * @param timeLoggers a set of timeLoggers to be used.
     */
    public static void runStringSortBenchmark(final String[] words, final int nWords, final int nRuns, final SortWithHelper<String> sorter, final TimeLogger[] timeLoggers) {
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

    final static Pattern REGEX_LEIPZIG = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");

    public static final Function<Random, Byte> byteFunction = r -> {
        byte[] bytes = new byte[1];
        r.nextBytes(bytes);
        byte aByte = bytes[0];
        return aByte >= 0 ? aByte : (byte) (aByte ^ 0xFF);
    };

    /**
     * This is based on log2(n!)
     *
     * @param n the number of elements.
     * @return the minimum number of comparisons possible to sort n randomly ordered elements.
     */
    static double minComparisons(final int n) {
        final double lgN = lg(n);
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
    static double meanInversions(final int n) {
        return 0.25 * n * (n - 1);
    }

    /**
     * Method to return a String as a (singleton) List of Strings.
     *
     * @param line a String
     * @return a list with just the one String element in it.
     */
    public static List<String> lineAsList(final String line) {
        final List<String> words = new ArrayList<>();
        words.add(line);
        return words;
    }

    static <T extends Comparable<T>> Supplier<T[]> getSupplier(final int n, final Class<T> clazz, final Function<Random, T> randomNumberFunction) {
        return () -> Utilities.fillRandomArray(clazz, new Random(), n, randomNumberFunction);
    }

    private static List<String> getLeipzigWords(final String line) {
        return HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER);
    }

    private static <Y> Benchmark<Y[]> benchmarkFactory(final String description, final Consumer<Y[]> sorter, final Consumer<Y[]> checker) {
        return new Benchmark<>(
                description,
                (xs) -> Arrays.copyOf(xs, xs.length),
                sorter,
                checker
        );
    }

    static class Tuple implements HuskySortable<Tuple> {
        /**
         * Constructor.
         *
         * @param birthYear 4-digit year.
         * @param zip       5-digit zip code.
         * @param name      last name, first name.
         */
        public Tuple(final int birthYear, final int zip, final String name) {
            this.birthYear = birthYear;
            this.zip = zip;
            this.name = name;
        }

        /**
         * implementation of compareTo based on the huskyCode of each comparand.
         * Note that the huskyCode must be re-evaluated each time it is used in a compare,
         * unless the object itself caches the huskyCode.
         *
         * @param tuple the object to be compared
         * @return an int according to the ordering of this and x
         */
        @Override
        public int compareTo(final Tuple tuple) {
            final int cf1 = Integer.compare(birthYear, tuple.birthYear);
            if (cf1 != 0) return cf1;
            final int cf2 = Integer.compare(zip, tuple.zip);
            if (cf2 != 0) return cf2;
            return name.compareTo(tuple.name);
        }

        /**
         * This method returns a quasi-monotonically increasing long value corresponding to this.
         * The fields must be coded in the same order of priority as the comparison.
         *
         * @return a long such that when comparison is done by longs, it is approximately 90% accurate.
         */
        @Override
        public long huskyCode() {
            long result = birthYear - 1850;  // 8 bits
            result = result << 17 | zip; // 17 bits
            result = result << 38 | englishCoder.huskyEncode(name) & 0x3FFFFFFFFFL; // 38 bits
            return result;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof Tuple)) return false;
            final Tuple tuple = (Tuple) o;
            return zip == tuple.zip &&
                    birthYear == tuple.birthYear &&
                    name.equals(tuple.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(zip, name, birthYear);
        }

        @Override
        public String toString() {
            return "Tuple{" +
                    "zip=" + zip +
                    ", name='" + name + '\'' +
                    ", birthYear=" + birthYear +
                    '}';
        }

        public static Tuple create() {
            final int zip = random.nextInt(99999) + 1;
            final String word = tupleWords[random.nextInt(tupleWords.length)];
            final int age = random.nextInt(171) + 1850;
            return new Tuple(age, zip, word);
        }

        static void setRandom(final Random random) {
            Tuple.random = random;
        }

        static Random random = new Random();


        private final static String[] commonWords = getCommonWords();

        private final static String[] tupleWords = getWordSupplier(commonWords, 1000, new Random(0L), false).get();

        private static String[] getCommonWords() {
            return HuskySortBenchmarkHelper.getWords(COMMON_WORDS_CORPUS, s -> {
                final List<String> list = new ArrayList<>();
                list.add(s);
                return list;
            });
        }

        private final int zip;
        private final String name;
        private final int birthYear;
    }

    /**
     * Method to compare system sort with pure Husky sort
     *
     * @param <Y>        the underlying type of the array to be sorted.
     * @param subject    a String representing the number of instances and the class name being sorted.
     * @param supplier   a supplier of Y[] values.
     * @param huskyCoder the coder for the given Y class.
     * @param checker    a checker (may be null) which is applied only to the HuskySort results (not counted in the timings).
     * @param isConfig   a predicate which returns a boolean for both "timsort" or "huskysort".
     * @param m          the number of repetitions to be run.
     * @param clazz      the class of Y.
     * @param isInt      true if Y is an integer-style type.
     */
    static <Y extends Number & Comparable<Y>> void compareSystemAndPureHuskySortsNumeric(final String subject, final Supplier<Y[]> supplier, final HuskyCoder<Y> huskyCoder, @SuppressWarnings("SameParameterValue") final Consumer<Y[]> checker, final Predicate<String> isConfig, final int m, final Class<? extends Number> clazz, final boolean isInt) {
        compareSystemAndPureHuskySorts(subject, supplier, huskyCoder, checker, isConfig, m);

        if (isConfig.test("quicksort")) {
            doNumericQuicksort(subject, supplier, m, clazz, isInt);
        }
    }

    /**
     * Method to compare system sort with pure Husky sort
     *
     * @param <Y>        the underlying type of the array to be sorted.
     * @param subject    a String representing the number of instances and the class name being sorted.
     * @param supplier   a supplier of Y[] values.
     * @param huskyCoder the coder for the given Y class.
     * @param checker    a checker (may be null) which is applied only to the HuskySort results (not counted in the timings).
     * @param isConfig   a predicate which returns a boolean for both "timsort" or "huskysort".
     * @param m          the number of repetitions to be run.
     */
    static <Y extends Comparable<Y>> void compareSystemAndPureHuskySorts(final String subject, final Supplier<Y[]> supplier, final HuskyCoder<Y> huskyCoder, @SuppressWarnings("SameParameterValue") final Consumer<Y[]> checker, final Predicate<String> isConfig, final int m) {
        if (isConfig.test("timsort"))
            logBenchmarkRun(HuskySortBenchmark.<Y>benchmarkFactory("Sort " + subject + " using System sort", Arrays::sort, null).run(supplier, m));

        if (isConfig.test("huskysort"))
            logBenchmarkRun(benchmarkFactory("Sort " + subject + " using PureHuskySort", new PureHuskySort<>(huskyCoder, false, false)::sort, checker).run(supplier, m));

        if (isConfig.test("quicksort"))
            logBenchmarkRun(benchmarkFactory("Sort " + subject + " using DualPivotQuicksort", PureDualPivotQuicksort::sort, checker).run(supplier, m));

        if (isConfig.test("mergehuskysort"))
            logBenchmarkRun(benchmarkFactory("Sort " + subject + " using MergeHuskySort", new MergeHuskySort<>(huskyCoder)::sort, checker).run(supplier, m));
    }

    // CONSIDER why don't we just go with "10K", etc. for x??
    private void doLeipzigBenchmarkEnglish(final int n, final int m) {
        final String resource = "eng-uk_web_2002_" + (n < 50000 ? "10K" : n < 200000 ? "100K" : "1M") + "-sentences.txt";
        final HuskyCoder<String> huskyCoder = HuskySortHelper.getSequenceCoderByName(getConfigHuskyCoder());
        doLeipzigBenchmark(resource, n, m, huskyCoder);
    }

    // CONSIDER making this an instance method of Benchmark
    private static void doPureBenchmark(final String[] words, final int nWords, final int nRuns, final Random random, final Benchmark<String[]> benchmark, final boolean preSorted) {
        final double time = benchmark.run(getWordSupplier(words, nWords, random, preSorted), nRuns);
        logger.info("CSV, " + benchmark + ", " + nWords + ", " + time); // XXX What does CSV mean in this context?
        for (final TimeLogger timeLogger : timeLoggersLinearithmic) timeLogger.log(time, nWords);
    }

    private static Supplier<String[]> getWordSupplier(final String[] words, final int nWords, final Random random, final boolean preSorted) {
        // NOTE that the preSorted branch does not seem to work correctly with Chinese text.
        if (preSorted) {
            final String[] strings = Arrays.copyOf(words, Math.min(nWords, words.length));
            return () -> strings;
        } else return () -> fillRandomArray(String.class, random, nWords, r -> words[r.nextInt(words.length)]);
    }

    private void dateSortBenchmark(final Supplier<LocalDateTime[]> localDateTimeSupplier, final LocalDateTime[] localDateTimes, final QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySortSystemSort, final String s, final int i, final int n, final int m) {
        logBenchmarkRun(HuskySortBenchmark.<LocalDateTime>benchmarkFactory(s, dateHuskySortSystemSort::sort, dateHuskySortSystemSort::postProcess).run(localDateTimeSupplier, m));
        // NOTE: this is intended to replace the run in the previous line. It should take the exact same amount of time.
        runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, n, m, i);
    }

    private void doLeipzigBenchmark(final String resource, final int nWords, final int nRuns, final HuskyCoder<String> huskyCoder) {
        final String[] words = getLeipzigWordsFromResource(resource);
        // NOTE that the words retrieved from the resource have variable number of Chinese characters in each string.
        // I have noted strings with lengths from 2 up to 34 characters, with possibly more.
        if (isConfigBoolean(Config.HELPER, BaseHelper.INSTRUMENT))
            benchmarkStringSortersInstrumented(words, nWords, nRuns, huskyCoder);
        else
            benchmarkStringSorters(resource, words, nWords, nRuns, huskyCoder);
    }

    private static String[] getLeipzigWordsFromResource(final String resource) {
        return HuskySortBenchmarkHelper.getWords(resource, HuskySortBenchmark::getLeipzigWords);
    }

    private static <Y extends Number & Comparable<Y>> void doNumericQuicksort(final String subject, final Supplier<Y[]> supplier, final int m, final Class<? extends Number> clazz, final boolean isInt) {
        if (clazz == Byte.class) {
            logger.info("not attempting quicksort for: " + clazz);
            return;
        }
        if (isInt)
            try {
                final Method method = clazz.getMethod("valueOf", long.class);
                logBenchmarkRun(HuskySortBenchmark.<Y>benchmarkFactory("Sort " + subject + " using quicksort", a -> doQuicksort(a, true, method, true), null).run(supplier, m));
            } catch (final NoSuchMethodException e) {
                try {
                    final Method methodAlt = clazz.getMethod("valueOf", int.class);
                    logBenchmarkRun(HuskySortBenchmark.<Y>benchmarkFactory("Sort " + subject + " using quicksort", a -> doQuicksort(a, true, methodAlt, false), null).run(supplier, m));
                } catch (final Exception e1) {
                    logger.warn("cannot get valueOf method for int or long in " + clazz, e);
                }
            }
        else {
            try {
                final Method method = clazz.getMethod("valueOf", double.class);
                logBenchmarkRun(HuskySortBenchmark.<Y>benchmarkFactory("Sort " + subject + " using quicksort", a -> doQuicksort(a, false, method, false), null).run(supplier, m));
            } catch (final NoSuchMethodException e) {
                logger.warn("cannot get valueOf method for double in " + clazz, e);
            }
        }
    }

    private static void doQuicksort(final Number[] a, final boolean isInt, final Method method, final boolean isLong) {
        try {
            if (isInt)
                doSortInt(a, method, isLong);
            else
                doSortDouble(a, method);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            logger.warn("Exception performing quicksort on array of numeric objects");
        }
    }

    private static void doSortInt(final Number[] a, final Method method, final boolean isLong) throws IllegalAccessException, InvocationTargetException {
        final int length = a.length;
        final long[] xs = new long[length];
        for (int i = 0; i < length; i++) xs[i] = a[i].longValue();
        Arrays.sort(xs);
        for (int i = 0; i < length; i++)
            a[i] = isLong ? (Number) method.invoke(null, xs[i]) : (Number) method.invoke(null, (int) xs[i]);
    }

    private static void doSortDouble(final Number[] a, final Method method) throws IllegalAccessException, InvocationTargetException {
        final int length = a.length;
        final double[] xs = new double[length];
        for (int i = 0; i < length; i++) xs[i] = a[i].doubleValue();
        Arrays.sort(xs);
        for (int i = 0; i < length; i++) a[i] = (Number) method.invoke(null, xs[i]);
    }

    @SuppressWarnings("SameParameterValue")
    private void runDateTimeSortBenchmark(final Class<?> tClass, final ChronoLocalDateTime<?>[] dateTimes, final int N, final int m, final int whichSort) {
        final SortWithHelper<ChronoLocalDateTime<?>> sorter = whichSort == 0 ? new TimSort<>() : whichSort == 1 ? new QuickHuskySort<>(HuskyCoderFactory.chronoLocalDateTimeCoder, config) : new QuickHuskySort<>("QuickHuskySort/Insertion", HuskyCoderFactory.chronoLocalDateTimeCoder, new InsertionSort<ChronoLocalDateTime<?>>()::mutatingSort, config);
        // CONSIDER do we actually need to copy here?
        @SuppressWarnings("unchecked") final SorterBenchmark<ChronoLocalDateTime<?>> sorterBenchmark = new SorterBenchmark<>((Class<ChronoLocalDateTime<?>>) tClass, (xs) -> Arrays.copyOf(xs, xs.length), sorter, dateTimes, m, timeLoggersLinearithmic);
        sorterBenchmark.run(N);
    }

    private static void logInterimInversions(final int nWords, final IntroHuskySort<String> sorter) {
        final double mean = sorter.getMeanInterimInversions();
        final String percentage = formatDecimal3Places((1.0 - mean / IntroHuskySort.expectedInversions(nWords)) * 100);
        logger.debug("logInterimInversions: HuskySort interim inversions: " + asInt(mean));
        logger.info("logInterimInversions: HuskySort first pass success rate: " + percentage + "%");
    }

    /**
     * For (basic) insertionsort, the number of array accesses is actually 6 times the number of comparisons.
     * That's because, for each inversions, there will typically be one swap (four array accesses) and (at least) one comparison (two array accesses).
     * Thus, in the case where comparisons are based on primitives,
     * the normalized time per run should approximate the time for one array access.
     */
    private final static TimeLogger[] timeLoggersQuadratic = {
            new TimeLogger("Raw time per run (mSec): ", (time, n) -> time),
            new TimeLogger("Normalized time per run (n^2): ", (time, n) -> time / meanInversions(n) / 6 * 1e6)
    };

    static final String COMMON_WORDS_CORPUS = "3000-common-words.txt";
    static final String CHINESE_NAMES_CORPUS = "Chinese_Names_Corpus.txt";

    static final int MIN_REPS = 20;

    static private void logBenchmarkRun(final double time) {
        logger.info(TimeLogger.formatTime(time) + " ms");
    }

    private static final Consumer<String[]> DO_NOTHING = (xs2) -> {
        // XXX do nothing.
    };

    private static final double LgE = lg(Math.E);

    private boolean isConfigBenchmarkStringSorter(final String option) {
        return isConfigBoolean("benchmarkstringsorters", option);
    }

    private boolean isConfigBenchmarkDateSorter(final String option) {
        return isConfigBoolean("benchmarkdatesorters", option);
    }

    private boolean isConfigBenchmarkTupleSorter(final String option) {
        return isConfigBoolean("benchmarktuplesorters", option);
    }

    private boolean isConfigBenchmarkNumberSorter(final String sortOption, final String typeOption) {
        return isConfigBoolean("benchmarknumbersorters", sortOption) && isConfigBoolean("benchmarknumbersorters", typeOption);
    }

    private boolean isConfigBoolean(final String section, final String option) {
        return config.getBoolean(section, option);
    }

    private String getConfigHuskyCoder() {
        return config.get("huskysort", "huskycoder", "Unicode");
    }

    private final Config config;
}
