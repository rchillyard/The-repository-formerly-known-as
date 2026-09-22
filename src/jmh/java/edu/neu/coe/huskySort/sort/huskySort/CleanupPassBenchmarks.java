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
 * UNICODE_CODER, 3.43e-4 with englishCoder and 4.7e-7 with englishSaturatingCoder -- a factor of
 * 731 between the two english coders -- and p is essentially constant in n, with X = p n^2 / 4
 * holding across a 16-fold range of n. Since insertion sort costs N + X and Timsort costs roughly
 * N log(runs), which of them wins is a question about the coder as much as about the size. Multiply
 * by n for the pn of a cell: at n = 1,000,000 these are 115, 343 and 0.47 respectively.
 * <p>
 * Hand timing on a loaded machine put adaptive insertion sort ahead of Timsort by 1.12x at
 * n = 200,000 and 1.24x at n = 1,000,000 under the saturating coder, and behind it by 1.72x at
 * n = 1,000,000 under UNICODE_CODER. Those margins are small enough to need JMH, which is what this
 * class is for.
 * <p>
 * <b>Third, added 2026-09-22: does saturation earn its encode cost, and does Timsort's cleanup
 * follow runs or inversions?</b> Yunlu's request-11 run (PR #67) measured the saturating english
 * coder at 1.7x to 3.0x the masking one to encode --- +73 to +115 ms per million words --- and
 * correctly observed that the other side of that trade had never been measured, because this class
 * held no masking cell. The {@code englishMasking} and {@code asciiMasking} parameters are those
 * cells. Each masking coder takes the same characters at the same width as its saturating twin and
 * agrees with it exactly inside the window, so the pairs differ only on words reaching outside it;
 * the masking form wraps those back into the letter range ({@code englishCoder} ties "don't" with
 * "dongt", since an apostrophe and 'g' both mask to 39; {@code asciiCoder} sorts an e-acute as 'i',
 * since {@code 233 & 0x7F} is 105) while the saturating form pushes them to the window's edges and
 * stays monotonic.
 * <p>
 * These are clean experiments because within a pair the two disorder statistics move in opposite
 * directions, so the result discriminates between the two cost models instead of merely confirming
 * one. Structural counts on the english corpus at n = 1,000,000, from CleanupPassProbe:
 * <pre>
 *                                  runs   mean run       inversions       pn
 *     unicode            4x16 mask    365,958        2.7       28,459,608   113.84
 *     asciiMasking       9x7  mask     30,921       32.3       87,797,452   351.19
 *     asciiSaturating    9x7  sat      30,079       33.2          235,407     0.94
 *     englishMasking    10x6  mask     17,506       57.1       85,818,889   343.28
 *     englishSaturating 10x6  sat      16,641       60.1          117,376     0.47
 * </pre>
 * Within each pair the run counts differ by 3% and 5% while the inversion counts differ by 373x and
 * 731x: a badly-coded word lands far from home, which costs thousands of inversions but only one
 * extra descent. So {@code N log(runs)} predicts the cleanups within a pair are indistinguishable,
 * while {@code N + X} predicts the masking forms are one to two orders worse.
 * <p>
 * Note for anyone tempted by the intuition that a 7-bit mask should be much the safer one because
 * every printable ASCII character survives it intact: it is not, on this corpus. {@code asciiCoder}
 * carries <i>more</i> inversions than {@code englishCoder}, not fewer, because what dominates the
 * count is not punctuation --- which 7 bits do preserve --- but the characters at or above 128,
 * which neither width preserves, and the Leipzig corpus's upstream mojibake supplies those in
 * quantity (TODO item 36). Punctuation is the visible hazard; the invisible one is the corpus.
 * <p>
 * Predicted before the run, so that a surprise reads as one. Hand timing on an eight-core Mac, 15
 * interleaved reps at n = 1,000,000, gave encode / cleanup / sum in ms of 40.9 / 116.8 / 157.8
 * unicode, 61.2 / 41.0 / 102.2 asciiMasking, 79.5 / 39.4 / 118.9 asciiSaturating, 60.3 / 35.3 /
 * 95.7 englishMasking and 83.1 / 34.3 / 117.4 englishSaturating. That is: cleanup ordered exactly
 * by run count across all five and blind to inversions (87.8M and 235,407 both give ~40 ms), each
 * saturating coder costing some 20 ms of encode to save about 1 ms of cleanup, and the masking
 * forms therefore ahead overall. If JMH agrees, TODO item 37's coder change loses on speed and has
 * to be argued on monotonicity instead. Note that all of this is conditional on Timsort: adaptive
 * insertion sort costs N + X, so on either masking cell it should be of the order of a second, and
 * those {@code adaptiveInsertionCleanup} rows are expected to be the worst english figures here.
 * <p>
 * Run it as:
 * <pre>
 * java -jar target/benchmarks.jar "CleanupPassBenchmarks" -f 5 -wi 5 -i 10
 * </pre>
 * and narrow with {@code -p coder=englishSaturating -p n=1000000} as needed. Note that
 * {@code binaryInsertionCleanup} throws on the pinyin cell, so an unfiltered sweep of this class
 * needs {@code -p coder=englishSaturating,englishMasking,asciiSaturating,asciiMasking,unicode}
 * for that method.
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
         * <p>
         * "englishMasking" and "asciiMasking" are the masking twins of "englishSaturating" and
         * "asciiSaturating" -- the same characters at the same width, narrowed with {@code &} rather
         * than clamped. They are here to settle whether saturation earns its encode cost, and they
         * make two independent pairs differing by 731x and 373x in inversions while differing by 5%
         * and 3% in runs, which is what discriminates the two cost models. See the class comment.
         */
        @Param({"englishSaturating", "englishMasking", "asciiSaturating", "asciiMasking", "unicode", "pinyin"})
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
                case "englishMasking":
                    vocabulary = englishVocabulary();
                    huskyCoder = HuskyCoderFactory.englishCoder;
                    ordering = Comparator.naturalOrder();
                    break;
                case "asciiSaturating":
                    vocabulary = englishVocabulary();
                    huskyCoder = HuskyCoderFactory.asciiSaturatingCoder;
                    ordering = Comparator.naturalOrder();
                    break;
                case "asciiMasking":
                    vocabulary = englishVocabulary();
                    huskyCoder = HuskyCoderFactory.asciiCoder;
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
                    + " Exclude it with a benchmark regex, or use -p coder=englishSaturating,englishMasking,asciiSaturating,asciiMasking,unicode.");
        final String[] copy = state.copy();
        InsertionSort.mutatingInsertionSort(copy);
        return copy;
    }
}
