package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Differential test of the hoist taken in request 13 (PR #73).
 * <p>
 * The shipped {@code BaseHuskySequenceCoder.huskyEncode(X[])} now tests {@code exactlyEncodable}
 * in a loop of its own, stopping at the first failure, and then encodes in a loop with no call in
 * it. This asserts that it agrees with the loop it replaced --- the single interleaved pass ---
 * on both the longs and the {@code perfect} flag, for every sequence coder and for inputs chosen
 * to straddle each coder's exactness boundary.
 * <p>
 * The point is not that the two loops look equivalent; it is that this project's last four defects
 * all produced correct output while the mechanism underneath was broken, so the mechanism is
 * asserted directly.
 */
public class HoistEquivalenceTest {

    /**
     * The loop as it stood before the hoist, reproduced verbatim.
     */
    private static Coding reference(final BaseHuskySequenceCoder<String> coder, final String[] xs) {
        boolean isPerfect = true;
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            final String x = xs[i];
            if (isPerfect) isPerfect = coder.exactlyEncodable(x);
            result[i] = coder.huskyEncode(x);
        }
        return new Coding(result, isPerfect);
    }

    @SuppressWarnings("unchecked")
    private static final BaseHuskySequenceCoder<String>[] CODERS = new BaseHuskySequenceCoder[]{
            (BaseHuskySequenceCoder<String>) HuskyCoderFactory.asciiCoder
            , (BaseHuskySequenceCoder<String>) HuskyCoderFactory.englishCoder
            , (BaseHuskySequenceCoder<String>) HuskyCoderFactory.asciiSaturatingCoder
            , (BaseHuskySequenceCoder<String>) HuskyCoderFactory.englishSaturatingCoder
            , (BaseHuskySequenceCoder<String>) HuskyCoderFactory.unicodeCoder
    };

    private static void assertAgrees(final String label, final BaseHuskySequenceCoder<String> coder, final String[] xs) {
        final Coding expected = reference(coder, xs);
        final Coding actual = coder.huskyEncode(xs);
        assertArrayEquals(label + ": longs", expected.longs, actual.longs);
        assertEquals(label + ": perfect", expected.perfect, actual.perfect);
    }

    /**
     * Arrays built to put the first inexact element at every position, including none at all,
     * since the hoisted loop differs from the old one precisely in where it stops.
     */
    @Test
    public void agreesWhereverTheFirstInexactElementFalls() {
        for (final BaseHuskySequenceCoder<String> coder : CODERS) {
            final String exact = "abc";
            final String[] inexact = {"abcdefghijklmnopqrstuvwxyz", "中文", "~!@#$%^&*()", ""};
            for (final String bad : inexact)
                for (int n = 0; n <= 6; n++)
                    for (int pos = 0; pos <= n; pos++) {
                        final String[] xs = new String[n];
                        Arrays.fill(xs, exact);
                        if (pos < n) xs[pos] = bad;
                        assertAgrees(coder + " n=" + n + " pos=" + pos + " bad=" + bad, coder, xs);
                    }
        }
    }

    /**
     * The all-exact case, which is the one the hoist scans twice and no benchmark corpus reaches.
     */
    @Test
    public void agreesOnPerfectArrays() {
        for (final BaseHuskySequenceCoder<String> coder : CODERS) {
            final List<String> exact = new ArrayList<>();
            for (char c = 'a'; c <= 'z'; c++) exact.add("" + c + c);
            final String[] xs = exact.toArray(new String[0]);
            assertAgrees(coder + " perfect", coder, xs);
        }
    }

    @Test
    public void agreesOnRandomAndEmptyInput() {
        final Random random = new Random(20261018);
        for (final BaseHuskySequenceCoder<String> coder : CODERS) {
            assertAgrees(coder + " empty", coder, new String[0]);
            for (int trial = 0; trial < 200; trial++) {
                final String[] xs = new String[random.nextInt(40)];
                for (int i = 0; i < xs.length; i++) {
                    final StringBuilder sb = new StringBuilder();
                    final int len = random.nextInt(14);
                    for (int j = 0; j < len; j++) sb.append((char) (random.nextInt(0x100) + (random.nextInt(10) == 0 ? 0x4e00 : 0)));
                    xs[i] = sb.toString();
                }
                assertAgrees(coder + " random trial=" + trial, coder, xs);
            }
        }
    }
}
