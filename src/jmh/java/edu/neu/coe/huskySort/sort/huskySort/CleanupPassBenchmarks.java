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
 * far more on the coder than anyone had assumed. Measured on the english corpus, p is 1.10e-4 with
 * UNICODE_CODER, 7.21e-4 with englishCoder and 4.4e-7 with englishSaturatingCoder -- a factor of
 * 1,637 between the two english coders -- and p is essentially constant in n, with X = p n^2 / 4
 * holding across a 16-fold range of n. Since insertion sort costs N + X and Timsort costs roughly
 * N log(runs), which of them wins is a question about the coder as much as about the size. Multiply
 * by n for the pn of a cell: at n = 1,000,000 these are 110, 721 and 0.44 respectively.
 * <p>
 * NOTE: those figures are from the corpus as tokenized after the word-splitter repair of
 * 2026-09-22. Before it, {@link HuskySortBenchmarkHelper#REGEX_LEIPZIG} truncated every sentence at its
 * first digit or non-ASCII symbol, discarding 15.2% of the english corpus; the same five coders
 * then read 1.15e-4 / 3.43e-4 / 4.7e-7. The repair roughly doubled both masking coders' inversion
 * counts, by recovering the accented vocabulary that the truncation had been hiding, and left every
 * run count within 4%.
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
 *     unicode            4x16 mask    366,865        2.7       27,535,704   110.14
 *     asciiMasking       9x7  mask     30,520       32.8      185,161,666   740.65
 *     asciiSaturating    9x7  sat      29,327       34.1          216,488     0.87
 *     englishMasking    10x6  mask     17,305       57.8      180,352,275   721.41
 *     englishSaturating 10x6  sat      16,061       62.3          110,206     0.44
 * </pre>
 * Within each pair the run counts differ by 4% and 8% while the inversion counts differ by 855x and
 * 1,637x: a badly-coded word lands far from home, which costs thousands of inversions but only one
 * extra descent. So {@code N log(runs)} predicts the cleanups within a pair are indistinguishable,
 * while {@code N + X} predicts the masking forms are three orders worse.
 * <p>
 * Note for anyone tempted by the intuition that a 7-bit mask should be much the safer one because
 * every printable ASCII character survives it intact: it is not, on this corpus, and the reason is
 * sharper than it first looks. {@code asciiCoder} carries <i>more</i> inversions than
 * {@code englishCoder}, not fewer --- and punctuation has nothing to do with either, because
 * {@link HuskySortBenchmarkHelper#REGEX_STRING_SPLITTER} splits on every non-letter, so no token in
 * this vocabulary contains an apostrophe, a hyphen or a digit in the first place. The only
 * characters that reach outside a 6-bit or a 7-bit window are the letters at or above 128, which
 * neither width preserves, so both coders fail on exactly the same words and the difference between
 * them is character count against window width, nothing else. The "don't" / "dongt" collision that
 * motivates saturation elsewhere in this project is real arithmetic but is not exercised by the
 * english corpus.
 * <p>
 * Predicted before the run, so that a surprise reads as one. Hand timing on an eight-core Mac at
 * n = 1,000,000: encode / cleanup / sum in ms of 50.1 / 132.0 / 182.2 unicode, 66.3 / 46.6 / 112.9
 * asciiMasking, 95.0 / 46.3 / 141.3 asciiSaturating, 70.1 / 41.0 / 111.1 englishMasking and
 * 93.1 / 43.8 / 136.9 englishSaturating. That is: cleanup ordered by run count across all five and
 * blind to inversions (185.2M and 216,488 both give ~46 ms), each saturating coder costing some
 * 25 ms of encode to save nothing measurable in cleanup, and the masking forms therefore ahead
 * overall. Repeats put masking / saturating for {@code timsortCleanup} anywhere in 0.94x to 1.24x,
 * so that ratio is not claimed finer than "about one". If JMH agrees, TODO item 37's coder change
 * loses on speed and has to be argued on monotonicity instead.
 * <p>
 * All of it is conditional on Timsort. {@code AdaptiveInsertionSort} costs N + X and is fully
 * sensitive to the same inversions Timsort ignores: measured on these arrays, adaptive / Timsort is
 * 1.03x on englishSaturating against <b>19.1x</b> on englishMasking, and 1.03x against 16.4x on the
 * ascii pair, so masking / saturating for {@code adaptiveInsertionCleanup} should come out near
 * 20x. The two masking adaptive rows at n = 1,000,000, around 830--890 ms/op, are the slowest cells
 * in this class outside pinyin.
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
         * "chineseUnicode" is the same coder on the <i>chinese</i> corpus rather than the english
         * one, and is here for the parallel-cleanup question below: it is the only configuration in
         * the project where the cleanup is both cheap in absolute terms and long-run, which is
         * where {@code Arrays.parallelSort} loses to {@code Arrays.sort};
         * "englishSaturating" is what they use now and is the low-p one; "pinyin" is the highest-p
         * case in the project, and the one whose cleanup must run in a non-natural ordering.
         * <p>
         * "englishMasking" and "asciiMasking" are the masking twins of "englishSaturating" and
         * "asciiSaturating" -- the same characters at the same width, narrowed with {@code &} rather
         * than clamped. They are here to settle whether saturation earns its encode cost, and they
         * make two independent pairs differing by 731x and 373x in inversions while differing by 5%
         * and 3% in runs, which is what discriminates the two cost models. See the class comment.
         */
        @Param({"englishSaturating", "englishMasking", "asciiSaturating", "asciiMasking", "unicode", "chineseUnicode", "pinyin", "pinyinRank"})
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
                case "pinyinRank":
                    vocabulary = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
                    huskyCoder = HuskyCoderFactory.chineseEncoderPinyinRank;
                    ordering = HuskyCoderChinesePinyin.NAME_ORDER;
                    break;
                case "chineseUnicode":
                    vocabulary = HuskySortBenchmarkHelper.getWords("zho-simp-tw_web_2014_10K-sentences.txt",
                            line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, HuskySortBenchmarkHelper.REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
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
                    line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, HuskySortBenchmarkHelper.REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
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
     * The same Timsort on the common {@link java.util.concurrent.ForkJoinPool}. Paired with
     * {@link #timsortCleanup} this is the A/B that decides whether
     * {@link ParallelRadixHuskySort}'s opt-in parallel cleanup should ever become its default, and
     * --- more useful --- what the rule for choosing between them is.
     * <p>
     * It is a real question rather than a formality, because this one can lose. On a nearly ordered
     * array serial Timsort finds long runs and stops, while {@code parallelSort} still cuts into
     * about {@code 4p} blocks and pays roughly {@code log(4p)} merge levels: about four times the
     * work, divided by p. Hand timing on eight cores, one corpus per JVM, put it at 1.04x / 1.87x
     * on english at n = 200,000 / 1,000,000, <b>0.53x</b> / 1.50x on chinese, and 3.38x / 2.54x on
     * pinyin --- so it pays in proportion to the work available, and on chinese at n = 200,000 it
     * is nearly twice as slow as doing nothing.
     * <p>
     * The reason this class is the right place to settle it, rather than an end-to-end row, is that
     * n alone cannot express the rule: chinese and chinesenames at n = 200,000 are the same size
     * and want opposite answers. What separates them is how much disorder the coder left, which is
     * exactly what this class parameterises.
     * <p>
     * NOTE: {@code Arrays.parallelSort} sorts serially below {@code MIN_ARRAY_SORT_GRAN} (8,192),
     * so at small n this becomes {@link #timsortCleanup} plus a little overhead, by design.
     */
    @Benchmark
    public String[] parallelTimsortCleanup(final CleanupState state) {
        final String[] copy = state.copy();
        Arrays.parallelSort(copy, state.ordering);
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
