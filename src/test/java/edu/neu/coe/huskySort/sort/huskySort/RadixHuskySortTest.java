package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Correctness tests for RadixHuskySort.
 * <p>
 * NOTE: the task brief that motivated this class specifically calls out a bug found in an early
 * prototype quicksort (an off-by-one in the partition recursion bounds) that silently dropped
 * elements near the pivot, and which only showed up under stress-testing many small random
 * arrays. These tests therefore check exact ordering against a trusted reference sort (not just
 * that the result "looks sorted"), across a range of small sizes, and across a sweep of digit
 * widths, including one (11) that does not evenly divide 64.
 */
public class RadixHuskySortTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @BeforeClass
    public static void before() throws IOException {
        config = Config.load(RadixHuskySortTest.class);
    }

    private static Config config;

    @Test
    public void testSortStringSmall() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.unicodeCoder, config);
        final String[] ys = sorter.sort(xs);
        assertTrue("sorted", helper.sorted(ys));
    }

    @Test
    public void testSortStringRandom() {
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        final int N = 2000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        final String[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testSortStringPaddedBigInteger() {
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        final int N = 2000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> {
            final int x = r.nextInt(1000000000);
            final BigInteger b = BigInteger.valueOf(x).multiply(BigInteger.valueOf(1000000));
            return b.toString();
        });
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        final String[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    /**
     * Stress-test on many small random arrays, across a sweep of digit widths (including one,
     * 11, that does not evenly divide 64), comparing against Arrays.sort as the trusted reference.
     * This is the JUnit formalization of the ad hoc stress-test loop in the original prototype
     * (RadixVsQuickBenchmark.java), which is what caught the original quicksort bug.
     */
    @Test
    public void testStressSmallLongArrays() {
        final Random r = new Random(7);
        for (final int digitBits : new int[]{1, 3, 7, 8, 11, 16, 20}) {
            for (int trial = 0; trial < 500; trial++) {
                final int n = 1 + r.nextInt(200);
                final Long[] xs = new Long[n];
                for (int i = 0; i < n; i++) xs[i] = r.nextLong();
                final Long[] expected = Arrays.copyOf(xs, n);
                Arrays.sort(expected);

                final RadixHuskySort<Long> sorter = new RadixHuskySort<>(digitBits, HuskyCoderFactory.longCoder, config);
                final Long[] ys = sorter.sort(xs);
                assertArrayEquals("digitBits=" + digitBits + ", n=" + n, expected, ys);
            }
        }
    }

    @Test
    public void testNegativeAndPositiveLongs() {
        final Random r = new Random(42);
        final int N = 5000;
        final Long[] xs = new Long[N];
        for (int i = 0; i < N; i++) xs[i] = r.nextLong();
        final Long[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Long> sorter = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        final Long[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testNegativeAndPositiveIntegers() {
        final Random r = new Random(43);
        final int N = 5000;
        final Integer[] xs = new Integer[N];
        for (int i = 0; i < N; i++) xs[i] = r.nextInt();
        final Integer[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Integer> sorter = new RadixHuskySort<>(11, HuskyCoderFactory.integerCoder, config);
        final Integer[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testNegativeAndPositiveDoubles() {
        final Random r = new Random(44);
        final int N = 5000;
        final Double[] xs = new Double[N];
        for (int i = 0; i < N; i++) xs[i] = (r.nextDouble() - 0.5) * 2 * Long.MAX_VALUE;
        final Double[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Double> sorter = new RadixHuskySort<>(16, HuskyCoderFactory.doubleCoder, config);
        final Double[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testAdversarialCollapsedHighBits() {
        // NOTE: all keys share the same top 56 bits (differ only in the low 8 bits), so if the
        // radix sort had a bug specific to skewed/collapsed high-order digits (per Reviewer 4's
        // concern about adversarial inputs), it would show up here.
        final Random r = new Random(99);
        final int N = 3000;
        final Long[] xs = new Long[N];
        for (int i = 0; i < N; i++) xs[i] = 0x1234_5678_9ABC_DE00L | (r.nextInt(256) & 0xFFL);
        final Long[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Long> sorter = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        final Long[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    /**
     * Broader sweep than testAdversarialCollapsedHighBits above (TODO.md item 7 / Reviewer 4's
     * critique that the paper never showed what happens when the encoding has poor entropy):
     * correctness must hold regardless of how severely the high-order bits collapse, not just
     * for one arbitrary severity. Sweeps from no collapse (0 fixed bits) to near-total collapse
     * (63 of 64 bits fixed, only the sign bit varies).
     */
    @Test
    public void testAdversarialCollapsedHighBitsSweep() {
        final Random r = new Random(101);
        for (final int fixedHighBits : new int[]{0, 8, 16, 32, 48, 56, 60, 63}) {
            final long mask = fixedHighBits == 0 ? 0L : (-1L << (64 - fixedHighBits));
            final long fixedBits = 0x5A5A_5A5A_5A5A_5A5AL & mask;
            final int N = 2000;
            final Long[] xs = new Long[N];
            for (int i = 0; i < N; i++) xs[i] = (r.nextLong() & ~mask) | fixedBits;
            final Long[] expected = Arrays.copyOf(xs, N);
            Arrays.sort(expected);

            final RadixHuskySort<Long> sorter = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
            final Long[] ys = sorter.sort(xs);
            assertArrayEquals("fixedHighBits=" + fixedHighBits, expected, ys);
        }
    }

    /**
     * Strings sharing a long common prefix: a realistic version of Reviewer 4's "poor entropy
     * in high-order bits" concern, since string husky coders pack characters left-to-right into
     * the long (most significant first) -- a shared prefix means the leading many bits of every
     * element's husky code are identical, exactly the adversarial condition the brief asks
     * about, but arising naturally rather than being artificially constructed.
     */
    @Test
    public void testAdversarialSharedPrefixStrings() {
        final Random r = new Random(102);
        for (final int prefixLength : new int[]{0, 5, 10, 20, 40}) {
            final String prefix = "a".repeat(prefixLength);
            final int N = 2000;
            final String[] xs = new String[N];
            for (int i = 0; i < N; i++) xs[i] = prefix + (r.nextLong() & Long.MAX_VALUE);
            final String[] expected = Arrays.copyOf(xs, N);
            Arrays.sort(expected);

            final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.englishCoder, config);
            final String[] ys = sorter.sort(xs);
            assertArrayEquals("prefixLength=" + prefixLength, expected, ys);
        }
    }

    @Test
    public void testAlreadySortedAndReverseSorted() {
        final int N = 1000;
        final Long[] ascending = new Long[N];
        for (int i = 0; i < N; i++) ascending[i] = (long) i;
        final Long[] descending = new Long[N];
        for (int i = 0; i < N; i++) descending[i] = (long) (N - i);

        final RadixHuskySort<Long> sorter1 = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        assertArrayEquals(ascending, sorter1.sort(Arrays.copyOf(ascending, N)));

        final RadixHuskySort<Long> sorter2 = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        final Long[] expectedFromDescending = Arrays.copyOf(descending, N);
        Arrays.sort(expectedFromDescending);
        assertArrayEquals(expectedFromDescending, sorter2.sort(descending));
    }

    @Test
    public void testEmptyAndSingleton() {
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        assertArrayEquals(new String[0], sorter.sort(new String[0]));
        assertArrayEquals(new String[]{"one"}, sorter.sort(new String[]{"one"}));
    }

    /**
     * Regression test for a real bug found 2026-07-24: RadixHuskySort's convenience
     * constructor hardcoded Arrays::sort as the cleanup-pass post-sorter, never consulting
     * HuskyCoder.getCollator() -- so for a Collator-supplying coder (HuskyCoderChinesePinyin,
     * which always needs the cleanup pass, since it never claims perfect()), the result was
     * silently sorted by natural (Unicode code point) order instead of the coder's actual
     * intended order. QuickHuskySort already handled this correctly; RadixHuskySort did not.
     * No existing test caught this: RadixHuskySortTest never used a Collator-supplying coder,
     * and HuskyCoderChinesePinyinTest never exercised RadixHuskySort.
     */
    @Test
    public void testChineseNamesUseCollatorNotNaturalOrder() {
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅", "杨腊香", "许凤山", "王广风", "黄锡鸿", "罗庆富", "顾芳芳", "宋雪光", "王诗卉"};
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected, HuskyCoderChinesePinyin.NAME_ORDER);

        for (final int digitBits : new int[]{8, 11, 16}) {
            final RadixHuskySort<String> sorter = new RadixHuskySort<>(digitBits, HuskyCoderFactory.chineseEncoderPinyin, config);
            final String[] actual = sorter.sort(Arrays.copyOf(xs, xs.length));
            assertArrayEquals("digitBits=" + digitBits, expected, actual);
        }
    }

    // ---------- Stability tests (TODO.md item 6) ----------
    //
    // LSD radix sort via counting sort is inherently stable: each digit pass is itself a
    // stable counting sort, and composing stable sorts pass by pass preserves overall
    // stability. These tests verify that property actually holds for RadixHuskySort, using a
    // payload type (Tagged) whose comparison/encoding key is deliberately coarse (few distinct
    // values, heavy duplication) so that stability -- not just "is it sorted" -- is what's
    // actually being exercised, tracked via a `tag` field (the element's original index) that
    // has no effect on ordering.

    /**
     * A key coder that is genuinely "perfect" for Tagged (the encoding matches compareTo
     * exactly), so no cleanup pass runs -- stability is then purely a property of
     * RadixHuskySort's own first (and only) pass, not diluted by java.util.Arrays.sort's
     * already-known stability in a fallback cleanup pass.
     */
    private static final class TaggedKeyCoder implements HuskyCoder<Tagged> {
        @Override
        public long huskyEncode(final Tagged x) {
            return x.key;
        }

        @Override
        public boolean perfect() {
            return true;
        }
    }

    private static final class Tagged implements Comparable<Tagged> {
        final long key;
        final int tag;

        Tagged(final long key, final int tag) {
            this.key = key;
            this.tag = tag;
        }

        @Override
        public int compareTo(final Tagged other) {
            return Long.compare(key, other.key);
        }

        @Override
        public String toString() {
            return "Tagged(key=" + key + ", tag=" + tag + ")";
        }
    }

    /**
     * Asserts both that keys are non-decreasing (it actually sorted) and that, within any run
     * of equal keys, tags appear in ascending order -- since tags are exactly the elements'
     * original indices, ascending tags within a tied run is precisely "original relative order
     * preserved", i.e. stability.
     */
    private static void assertStableAndSorted(final Tagged[] sorted) {
        for (int i = 1; i < sorted.length; i++) {
            assertTrue("not sorted: " + sorted[i - 1] + " should not come after " + sorted[i],
                    sorted[i - 1].key <= sorted[i].key);
            if (sorted[i - 1].key == sorted[i].key)
                assertTrue("stability violated: " + sorted[i - 1] + " should precede " + sorted[i],
                        sorted[i - 1].tag < sorted[i].tag);
        }
    }

    @Test
    public void testStabilityManyDuplicateKeys() {
        final int n = 5000;
        final int numDistinctKeys = 20;
        final Random random = new Random(123);
        final Tagged[] xs = new Tagged[n];
        for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(numDistinctKeys), i);

        final RadixHuskySort<Tagged> sorter = new RadixHuskySort<>(new TaggedKeyCoder(), config);
        final Tagged[] sorted = sorter.sort(xs);

        assertStableAndSorted(sorted);
    }

    @Test
    public void testStabilityAcrossDigitWidths() {
        final Random random = new Random(321);
        for (final int digitBits : new int[]{8, 11, 16}) {
            final int n = 3000;
            final Tagged[] xs = new Tagged[n];
            for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(15), i);

            final RadixHuskySort<Tagged> sorter = new RadixHuskySort<>(digitBits, new TaggedKeyCoder(), config);
            final Tagged[] sorted = sorter.sort(xs);
            assertStableAndSorted(sorted);
        }
    }

    @Test
    public void testStabilityWithNegativeKeys() {
        final int n = 4000;
        final Random random = new Random(55);
        final Tagged[] xs = new Tagged[n];
        for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(21) - 10, i); // keys -10..10
        final RadixHuskySort<Tagged> sorter = new RadixHuskySort<>(new TaggedKeyCoder(), config);
        final Tagged[] sorted = sorter.sort(xs);
        assertStableAndSorted(sorted);
    }

    /**
     * The strongest possible stability check: every element ties on key, so a stable sort must
     * leave the array in exactly its original order.
     */
    @Test
    public void testStabilityAllSameKey() {
        final int n = 500;
        final Tagged[] xs = new Tagged[n];
        for (int i = 0; i < n; i++) xs[i] = new Tagged(42L, i);

        final RadixHuskySort<Tagged> sorter = new RadixHuskySort<>(new TaggedKeyCoder(), config);
        final Tagged[] sorted = sorter.sort(xs);

        for (int i = 0; i < n; i++)
            assertEquals("all-same-key input should come out in original order", i, sorted[i].tag);
    }
}
