package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.simple.AdaptiveInsertionSort;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Which sort should the cleanup pass use? Timsort, binary insertion sort, or adaptive insertion
 * sort -- measured on the array the radix phase actually hands over, rather than on a whole sort
 * from which the cleanup has to be inferred by subtraction.
 * <p>
 * This re-opens the question §A.5 of the paper settled, for two reasons found on 2026-09-17.
 * <p>
 * First, the paper's cleanup term {@code k(N + pX)} describes <i>adaptive</i> insertion sort, whose
 * cost really is N plus the inversion count, but {@link InsertionSort} is not that algorithm: it
 * locates each element by binary search over the sorted prefix, so it performs {@code n log n}
 * comparisons regardless of how nearly ordered the input is. A measurement of "insertion sort" as a
 * cleanup pass means something quite different depending on which is meant, and the two differ by
 * about 4.5x at n = 200,000 on the english corpus -- which is close to the factor §A.5 reports.
 * <p>
 * Second, the choice depends on p, the residual inversion probability, and p turned out to depend
 * far more on the coder than anyone had assumed. Measured on the english corpus, p is 1.15e-4 with
 * UNICODE_CODER and 4.7e-7 with englishSaturatingCoder, a factor of 245 -- and p is essentially
 * constant in n, with X = p n^2 / 4 holding across a 16-fold range of n. Since insertion sort costs
 * N + X and Timsort costs roughly N log(runs), which of them wins is a question about the coder as
 * much as about the size.
 * <p>
 * Hand timing on a loaded machine put adaptive insertion sort ahead of Timsort by 1.12x at
 * n = 200,000 and 1.24x at n = 1,000,000 under the saturating coder, and behind it by 1.72x at
 * n = 1,000,000 under UNICODE_CODER. Those margins are small enough to need JMH, which is what this
 * class is for. Run it as:
 * <pre>
 * java -jar target/benchmarks.jar "CleanupPassBenchmarks" -f 5 -wi 5 -i 10
 * </pre>
 * and narrow with {@code -p coder=englishSaturating -p n=1000000} as needed.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class CleanupPassBenchmarks {

    @State(Scope.Thread)
    public static class CleanupState {
        @Param({"200000", "1000000"})
        public int n;

        /**
         * The coder decides p, and p decides the answer, so it is a parameter rather than a
         * constant. "unicode" is what the benchmarks used before 2026-09-17 and is the high-p case;
         * "englishSaturating" is what they use now and is the low-p one; "pinyin" is the highest-p
         * case in the project, and the one whose cleanup must run in a non-natural ordering.
         */
        @Param({"englishSaturating", "unicode", "pinyin"})
        public String coder;

        /**
         * The array as the radix phase leaves it: the input to the cleanup pass.
         */
        String[] afterRadixPhase;
        Comparator<String> ordering;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            final Config config = Config.load();
            final HuskyCoder<String> huskyCoder;
            final String[] vocabulary;
            switch (coder) {
                case "englishSaturating":
                    vocabulary = englishVocabulary();
                    huskyCoder = HuskyCoderFactory.englishSaturatingCoder;
                    ordering = Comparator.naturalOrder();
                    break;
                case "unicode":
                    vocabulary = englishVocabulary();
                    huskyCoder = AbstractHuskySort.UNICODE_CODER;
                    ordering = Comparator.naturalOrder();
                    break;
                case "pinyin":
                    vocabulary = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
                    huskyCoder = HuskyCoderFactory.chineseEncoderPinyin;
                    ordering = HuskyCoderChinesePinyin.NAME_ORDER;
                    break;
                default:
                    throw new IllegalStateException("unknown coder: " + coder);
            }
            final Random random = new Random(42);
            final String[] xs = Utilities.fillRandomArray(String.class, random, n, r -> vocabulary[r.nextInt(vocabulary.length)]);
            // preSort computes the coding and sort permutes the payload; postSort, which is the
            // cleanup, is deliberately not called, so this is exactly what the cleanup is handed.
            try (RadixHuskySort<String> sorter = new RadixHuskySort<>(RadixHuskySort.AUTO_DIGIT_BITS, huskyCoder, config)) {
                afterRadixPhase = sorter.preSort(xs, true);
                sorter.sort(afterRadixPhase, 0, afterRadixPhase.length);
            }
        }

        private static String[] englishVocabulary() {
            return HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_1M-sentences.txt",
                    line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, HuskySortBenchmark.REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
        }

        String[] copy() {
            return Arrays.copyOf(afterRadixPhase, afterRadixPhase.length);
        }
    }

    /**
     * Timsort, which is what step 3 uses today.
     */
    @Benchmark
    public String[] timsortCleanup(final CleanupState state) {
        final String[] copy = state.copy();
        Arrays.sort(copy, state.ordering);
        return copy;
    }

    /**
     * Adaptive insertion sort: (n-1) + X comparisons and X moves, the algorithm the paper's cleanup
     * term describes.
     */
    @Benchmark
    public String[] adaptiveInsertionCleanup(final CleanupState state) {
        final String[] copy = state.copy();
        AdaptiveInsertionSort.sort(copy, state.ordering);
        return copy;
    }

    /**
     * The repo's existing InsertionSort, which is binary insertion sort -- n log n comparisons
     * whatever the input's order. Included because it is very likely what §A.5 measured, and the
     * gap between it and the adaptive form is the point.
     * <p>
     * NOTE: natural ordering only, since it sorts through the Comparable interface, so it is
     * meaningless for the pinyin case and is skipped there rather than quietly sorting by the wrong
     * ordering.
     */
    @Benchmark
    public String[] binaryInsertionCleanup(final CleanupState state) {
        if (state.coder.equals("pinyin"))
            throw new IllegalStateException("binaryInsertionCleanup sorts by natural ordering, which is not the pinyin ordering."
                    + " Exclude it with a benchmark regex, or use -p coder=englishSaturating,unicode.");
        final String[] copy = state.copy();
        InsertionSort.mutatingInsertionSort(copy);
        return copy;
    }
}
