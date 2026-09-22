package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Structural probe of what the cleanup pass is actually left to do, per corpus.
 * <p>
 * Not a test and not a benchmark: it reports exact counts, which is the point. Request 10 showed the
 * parallel sorter is ~94% serial on english and chinesenames, and the arithmetic leaves roughly 190
 * ms per million elements unaccounted for by the encode or by anything the thread count touches --
 * so the suspicion is the cleanup pass. The paper's model (see the discussion of $p_{crit}$) assumes
 * the inversions remaining after step 2 are few relative to n, which makes the cleanup linear and
 * therefore a minor term. Both cannot be true at once.
 * <p>
 * Counting settles it without a stopwatch, which matters because the machine this runs on cannot
 * time an 8-thread sort reliably. Run structure is what Timsort's cost actually follows -- a fully
 * ascending array is one run and costs n-1 comparisons with no moves -- so the run count is the
 * number to look at, with the inversion count (on a smaller sample, being O(n log n) comparisons) as
 * the quantity the paper's p refers to.
 * <p>
 * Run with: mvn -q test-compile, then java -cp target/test-classes:target/classes:... CleanupPassProbe
 */
public class CleanupPassProbe {

    private static final int RUN_STRUCTURE_N = 1_000_000;
    private static final int INVERSION_N = 200_000;

    public static void main(final String[] args) throws Exception {
        final Config config = Config.load();
        for (final String corpus : new String[]{"english", "chinese", "chinesenames"}) {
            final String[] corpusWords = wordsFor(corpus);
            reportCharacters(corpus, corpusWords);
            for (final String coderName : codersFor(corpus)) {
                report(corpus, coderName, coderNamed(coderName), corpusWords, config);
            }
        }
    }

    private static void report(final String corpus, final String coderName, final HuskyCoder<String> coder, final String[] corpusWords, final Config config) {
        final Comparator<String> order = corpus.equals("chinesenames") ? HuskyCoderChinesePinyin.NAME_ORDER : Comparator.naturalOrder();

        System.out.println("=== " + corpus + " / " + coderName + " (corpus holds " + corpusWords.length + " distinct words)");
        System.out.println("    coder.perfect() declares: " + coder.perfect());

        // The single statistic that explains the run count. Words sharing a husky code are left in
        // arbitrary relative order by the radix phase, so each such group is work the cleanup must
        // do; the mean group size is how much disorder the encoding hands on.
        final java.util.HashSet<Long> distinctCodes = new java.util.HashSet<>();
        for (final String word : corpusWords) distinctCodes.add(coder.huskyEncode(word));
        System.out.printf("    the %,d distinct words yield %,d distinct codes: mean %.2f words per code%n",
                corpusWords.length, distinctCodes.size(), (double) corpusWords.length / distinctCodes.size());

        final String[] big = sample(corpusWords, RUN_STRUCTURE_N);
        final String[] afterRadix = radixPhaseOnly(big, coder, config);
        final int runs = countRuns(afterRadix, order);
        final int descents = countDescents(afterRadix, order);
        System.out.printf("    n=%,d after the radix phase: %,d natural runs, %,d descents (%.2f%% of adjacent pairs)%n",
                RUN_STRUCTURE_N, runs, descents, 100.0 * descents / (RUN_STRUCTURE_N - 1));
        System.out.printf("    mean run length %.1f; a perfect encoding would give 1 run of %,d%n",
                (double) RUN_STRUCTURE_N / runs, RUN_STRUCTURE_N);

        // How much of a full sort does the cleanup actually cost? Run structure says Timsort has
        // 366k runs to merge on english, which suggests "most of one", but that is a derivation,
        // and galloping and cache locality both favour merging runs that are adjacent in key space.
        // Timed as a ratio of two sorts in one process, best-of-five: absolute times on this
        // machine are worthless, but the ratio of two measurements taken seconds apart survives.
        final long cleanup = bestOfFive(afterRadix, order);
        final long fromScratch = bestOfFive(big, order);
        System.out.printf("    cleanup pass costs %.3f of sorting the same array from scratch (%,d vs %,d us)%n",
                (double) cleanup / fromScratch, cleanup, fromScratch);

        // The serial floor, all on one machine so the ratio means something: the encode and the
        // cleanup are both serial and neither is touched by the chunk count, so their sum is the
        // least this design can cost however well the digit passes parallelise. Set against
        // Arrays.parallelSort on the same array in the same process, it says whether parallelising
        // the passes could win even in principle.
        final long encode = bestOfFiveEncode(big, coder);
        final long systemParallel = bestOfFiveParallel(big, order);
        final long floor = encode + cleanup;
        System.out.printf("    serial floor (encode %,d + cleanup %,d = %,d us) vs Arrays.parallelSort %,d us on this machine: %.2fx%n",
                encode, cleanup, floor, systemParallel, (double) floor / systemParallel);

        final String[] small = sample(corpusWords, INVERSION_N);
        final String[] smallAfterRadix = radixPhaseOnly(small, coder, config);
        final long inversions = countInversions(smallAfterRadix, order);
        final double maxInversions = (double) INVERSION_N * (INVERSION_N - 1) / 4.0;
        System.out.printf("    n=%,d after the radix phase: %,d inversions remain; p = %.4g of the %.3g a random array averages%n",
                INVERSION_N, inversions, inversions / maxInversions, maxInversions);
        System.out.println();
    }

    /**
     * The radix phase alone: preSort computes the coding, sort permutes the payload, and postSort --
     * which is the cleanup -- is deliberately not called. So the returned array is in exactly the
     * state the cleanup pass would be handed.
     */
    private static String[] radixPhaseOnly(final String[] xs, final HuskyCoder<String> coder, final Config config) {
        try (RadixHuskySort<String> sorter = new RadixHuskySort<>(RadixHuskySort.AUTO_DIGIT_BITS, coder, config)) {
            final String[] result = sorter.preSort(xs, true);
            sorter.sort(result, 0, result.length);
            return result;
        }
    }

    /**
     * Best of five in microseconds. The minimum rather than the mean: interference can only make a
     * run slower, so on a contended machine the fastest observation is the least corrupted one.
     */
    private static long bestOfFive(final String[] xs, final Comparator<String> order) {
        long best = Long.MAX_VALUE;
        for (int trial = 0; trial < 5; trial++) {
            final String[] copy = Arrays.copyOf(xs, xs.length);
            final long t0 = System.nanoTime();
            Arrays.sort(copy, order);
            final long elapsed = (System.nanoTime() - t0) / 1_000;
            if (elapsed < best) best = elapsed;
        }
        return best;
    }

    private static long bestOfFiveParallel(final String[] xs, final Comparator<String> order) {
        long best = Long.MAX_VALUE;
        for (int trial = 0; trial < 5; trial++) {
            final String[] copy = Arrays.copyOf(xs, xs.length);
            final long t0 = System.nanoTime();
            Arrays.parallelSort(copy, order);
            final long elapsed = (System.nanoTime() - t0) / 1_000;
            if (elapsed < best) best = elapsed;
        }
        return best;
    }

    private static long bestOfFiveEncode(final String[] xs, final HuskyCoder<String> coder) {
        long best = Long.MAX_VALUE;
        for (int trial = 0; trial < 5; trial++) {
            final long t0 = System.nanoTime();
            final long[] codes = coder.huskyEncode(xs).longs;
            final long elapsed = (System.nanoTime() - t0) / 1_000;
            if (codes.length != xs.length) throw new IllegalStateException("coder returned the wrong length");
            if (elapsed < best) best = elapsed;
        }
        return best;
    }


    /**
     * What the narrower coders can and cannot see. asciiCoder masks to 7 bits, so it is
     * order-preserving across the whole of ASCII and folds anything above it; englishCoder masks to
     * 6 bits, so it is order-preserving only within a 64-character window (64..127, the letters plus
     * @ [ \\ ] ^ _ ` { | } ~) and mis-orders digits, most punctuation and the space. Both truncate
     * beyond their character capacity. This reports how much of each corpus falls outside those
     * assumptions, which is what stands between the measured effect and a recommendation.
     */
    private static void reportCharacters(final String corpus, final String[] words) {
        int nonAscii = 0, outsideEnglishWindow = 0, longerThan9 = 0, longerThan10 = 0, longerThan4 = 0;
        long totalLength = 0;
        int maxLength = 0;
        final java.util.Map<Character, Integer> offenders = new java.util.HashMap<>();
        for (final String w : words) {
            totalLength += w.length();
            if (w.length() > maxLength) maxLength = w.length();
            if (w.length() > 4) longerThan4++;
            if (w.length() > 9) longerThan9++;
            if (w.length() > 10) longerThan10++;
            boolean hasNonAscii = false, hasOutsideWindow = false;
            for (int i = 0; i < w.length(); i++) {
                final char c = w.charAt(i);
                if (c > 127) {
                    hasNonAscii = true;
                    offenders.merge(c, 1, Integer::sum);
                }
                if (c < 64 || c > 127) hasOutsideWindow = true;
            }
            if (hasNonAscii) nonAscii++;
            if (hasOutsideWindow) outsideEnglishWindow++;
        }
        final int n = words.length;
        System.out.println("--- " + corpus + ": character distribution over its " + String.format("%,d", n) + " distinct words");
        System.out.printf("    mean length %.2f, max %d; longer than 4 chars: %.1f%%, than 9: %.1f%%, than 10: %.1f%%%n",
                (double) totalLength / n, maxLength, 100.0 * longerThan4 / n, 100.0 * longerThan9 / n, 100.0 * longerThan10 / n);
        System.out.printf("    contain a non-ASCII character (breaks asciiCoder's ordering): %,d words = %.3f%%%n",
                nonAscii, 100.0 * nonAscii / n);
        System.out.printf("    contain a character outside 64..127 (breaks englishCoder's ordering): %,d words = %.3f%%%n",
                outsideEnglishWindow, 100.0 * outsideEnglishWindow / n);
        offenders.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(8)
                .forEach(e -> System.out.printf("      non-ASCII U+%04X '%c' x%,d%n", (int) e.getKey(), e.getKey(), e.getValue()));
        System.out.println();
    }

    private static int countRuns(final String[] xs, final Comparator<String> order) {
        int runs = 1;
        for (int i = 1; i < xs.length; i++)
            if (order.compare(xs[i - 1], xs[i]) > 0) runs++;
        return runs;
    }

    private static int countDescents(final String[] xs, final Comparator<String> order) {
        int descents = 0;
        for (int i = 1; i < xs.length; i++)
            if (order.compare(xs[i - 1], xs[i]) > 0) descents++;
        return descents;
    }

    /**
     * Exact inversion count by merge sort, so it is O(n log n) comparisons rather than O(n^2).
     */
    private static long countInversions(final String[] xs, final Comparator<String> order) {
        final String[] work = Arrays.copyOf(xs, xs.length);
        return sortCounting(work, new String[xs.length], 0, xs.length, order);
    }

    private static long sortCounting(final String[] a, final String[] buf, final int from, final int to, final Comparator<String> order) {
        if (to - from < 2) return 0;
        final int mid = (from + to) >>> 1;
        long count = sortCounting(a, buf, from, mid, order) + sortCounting(a, buf, mid, to, order);
        int i = from, j = mid, k = from;
        while (i < mid && j < to) {
            if (order.compare(a[i], a[j]) <= 0) buf[k++] = a[i++];
            else {
                // a[j] jumps ahead of every element remaining in the left half.
                count += mid - i;
                buf[k++] = a[j++];
            }
        }
        while (i < mid) buf[k++] = a[i++];
        while (j < to) buf[k++] = a[j++];
        System.arraycopy(buf, from, a, from, to - from);
        return count;
    }

    private static String[] sample(final String[] corpusWords, final int n) {
        // The same construction StringSortBenchmarks.StringState uses, seed included, so these
        // counts describe the very arrays request 10 timed.
        final Random random = new Random(42);
        return Utilities.fillRandomArray(String.class, random, n, r -> corpusWords[r.nextInt(corpusWords.length)]);
    }

    private static String[] wordsFor(final String corpus) {
        switch (corpus) {
            case "english":
                return HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_1M-sentences.txt", CleanupPassProbe::leipzig);
            case "chinese":
                return HuskySortBenchmarkHelper.getWords("zho-simp-tw_web_2014_10K-sentences.txt", CleanupPassProbe::leipzig);
            case "chinesenames":
                return HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
            default:
                throw new IllegalArgumentException(corpus);
        }
    }

    /**
     * For english, the three coders that could plausibly be used on it. StringSortBenchmarks gives it
     * UNICODE_CODER, which packs four 16-bit characters; asciiCoder packs nine at 7 bits and
     * englishCoder ten at 6, and the same class already uses englishCoder for the commonwords
     * corpus. Whether that choice costs anything is the question this answers. The other two corpora
     * have only one sensible coder each.
     */
    private static String[] codersFor(final String corpus) {
        switch (corpus) {
            case "english": return new String[]{"unicode(4x16)", "ascii(9x7)", "english(10x6)"};
            case "chinese": return new String[]{"unicode(4x16)"};
            default: return new String[]{"pinyin"};
        }
    }

    private static HuskyCoder<String> coderNamed(final String name) {
        switch (name) {
            case "unicode(4x16)": return AbstractHuskySort.UNICODE_CODER;
            case "ascii(9x7)": return HuskyCoderFactory.asciiCoder;
            case "english(10x6)": return HuskyCoderFactory.englishCoder;
            case "pinyin": return HuskyCoderFactory.chineseEncoderPinyin;
            default: throw new IllegalArgumentException(name);
        }
    }

    private static java.util.List<String> leipzig(final String line) {
        return HuskySortBenchmarkHelper.splitLineIntoStrings(line, HuskySortBenchmark.REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER);
    }
}
