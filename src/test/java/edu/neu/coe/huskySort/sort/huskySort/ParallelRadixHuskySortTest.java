package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Correctness tests for ParallelRadixHuskySort.
 * <p>
 * Parallelizing the digit passes introduces a new class of bug that RadixHuskySortTest's
 * sequential tests cannot catch: an off-by-one in the per-chunk histogram-to-offset combine
 * step, which would show up only when the chunk boundaries actually split runs of equal or
 * adjacent keys in a way that a single-threaded implementation never exercises. So, on top of
 * mirroring RadixHuskySortTest's correctness sweep, every test here also sweeps a range of
 * chunk/thread counts -- including 1 (degenerate, single chunk), a count that does not evenly
 * divide n, and a count far larger than what MIN_CHUNK_SIZE would ever pick on its own -- against
 * the same trusted reference (Arrays.sort), not just "looks sorted".
 */
public class ParallelRadixHuskySortTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @BeforeClass
    public static void before() throws IOException {
        config = Config.load(ParallelRadixHuskySortTest.class);
    }

    private static Config config;

    private static <X extends Comparable<X>> ParallelRadixHuskySort<X> newSorter(final int digitBits, final int parallelism, final HuskyCoder<X> coder) {
        return new ParallelRadixHuskySort<>("test", 0, digitBits, coder, java.util.Arrays::sort, config, parallelism);
    }

    @Test
    public void testSortStringSmall() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final ParallelRadixHuskySort<String> sorter = newSorter(8, 4, HuskyCoderFactory.unicodeCoder);
        final String[] ys = sorter.sort(xs);
        assertTrue("sorted", helper.sorted(ys));
    }

    @Test
    public void testSortStringRandom() {
        final int N = 2000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        for (final int parallelism : new int[]{1, 2, 3, 8}) {
            final ParallelRadixHuskySort<String> sorter = newSorter(8, parallelism, HuskyCoderFactory.asciiCoder);
            final String[] ys = sorter.sort(Arrays.copyOf(xs, xs.length));
            assertArrayEquals("parallelism=" + parallelism, expected, ys);
        }
    }

    /**
     * Stress-test on many small random arrays, sweeping both digit width and chunk/thread count,
     * comparing against Arrays.sort as the trusted reference -- the parallel analogue of
     * RadixHuskySortTest's testStressSmallLongArrays, which is what caught a real bug (an
     * off-by-one in a from-scratch quicksort partition) in this project's own history.
     */
    @Test
    public void testStressSmallLongArrays() {
        final Random r = new Random(7);
        for (final int digitBits : new int[]{1, 3, 7, 8, 11, 16, 20}) {
            for (final int parallelism : new int[]{1, 2, 5, 16}) {
                for (int trial = 0; trial < 50; trial++) {
                    final int n = 1 + r.nextInt(200);
                    final Long[] xs = new Long[n];
                    for (int i = 0; i < n; i++) xs[i] = r.nextLong();
                    final Long[] expected = Arrays.copyOf(xs, n);
                    Arrays.sort(expected);

                    final ParallelRadixHuskySort<Long> sorter = newSorter(digitBits, parallelism, HuskyCoderFactory.longCoder);
                    final Long[] ys = sorter.sort(xs);
                    assertArrayEquals("digitBits=" + digitBits + ", parallelism=" + parallelism + ", n=" + n, expected, ys);
                }
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

        for (final int parallelism : new int[]{1, 4, 7}) {
            final ParallelRadixHuskySort<Long> sorter = newSorter(ParallelRadixHuskySort.DEFAULT_DIGIT_BITS, parallelism, HuskyCoderFactory.longCoder);
            final Long[] ys = sorter.sort(Arrays.copyOf(xs, N));
            assertArrayEquals("parallelism=" + parallelism, expected, ys);
        }
    }

    @Test
    public void testNegativeAndPositiveIntegers() {
        final Random r = new Random(43);
        final int N = 5000;
        final Integer[] xs = new Integer[N];
        for (int i = 0; i < N; i++) xs[i] = r.nextInt();
        final Integer[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final ParallelRadixHuskySort<Integer> sorter = newSorter(ParallelRadixHuskySort.DEFAULT_DIGIT_BITS, 4, HuskyCoderFactory.integerCoder);
        final Integer[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testAlreadySorted() {
        final int n = 3000;
        final Long[] xs = new Long[n];
        for (int i = 0; i < n; i++) xs[i] = (long) i;
        final Long[] expected = Arrays.copyOf(xs, n);

        final ParallelRadixHuskySort<Long> sorter = newSorter(11, 6, HuskyCoderFactory.longCoder);
        final Long[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testReverseSorted() {
        final int n = 3000;
        final Long[] xs = new Long[n];
        for (int i = 0; i < n; i++) xs[i] = (long) (n - i);
        final Long[] expected = Arrays.copyOf(xs, n);
        Arrays.sort(expected);

        final ParallelRadixHuskySort<Long> sorter = newSorter(11, 6, HuskyCoderFactory.longCoder);
        final Long[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testEmptyAndSingleton() {
        final ParallelRadixHuskySort<Long> sorter = newSorter(8, 4, HuskyCoderFactory.longCoder);
        assertArrayEquals(new Long[0], sorter.sort(new Long[0]));
        assertArrayEquals(new Long[]{5L}, sorter.sort(new Long[]{5L}));
    }

    // ---------- Stability (parallel LSD radix sort, via per-chunk offset combine, must remain
    // stable overall: within a bucket, chunk c's elements must all land after chunk c-1's, and
    // within a chunk, original relative order is preserved by the histogram/scatter design).
    // Same Tagged/TaggedKeyCoder pattern as RadixHuskySortTest, swept across chunk counts too. ----------

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
    public void testStabilityManyDuplicateKeysAcrossChunkCounts() {
        for (final int parallelism : new int[]{1, 2, 3, 7, 16}) {
            final int n = 5000;
            final int numDistinctKeys = 20;
            final Random random = new Random(123);
            final Tagged[] xs = new Tagged[n];
            for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(numDistinctKeys), i);

            final ParallelRadixHuskySort<Tagged> sorter = newSorter(8, parallelism, new TaggedKeyCoder());
            final Tagged[] sorted = sorter.sort(xs);

            assertStableAndSorted(sorted);
        }
    }

    @Test
    public void testStabilityAcrossDigitWidthsAndChunkCounts() {
        final Random random = new Random(321);
        for (final int digitBits : new int[]{8, 11, 16}) {
            for (final int parallelism : new int[]{1, 4, 9}) {
                final int n = 3000;
                final Tagged[] xs = new Tagged[n];
                for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(15), i);

                final ParallelRadixHuskySort<Tagged> sorter = newSorter(digitBits, parallelism, new TaggedKeyCoder());
                final Tagged[] sorted = sorter.sort(xs);
                assertStableAndSorted(sorted);
            }
        }
    }

    @Test
    public void testStabilityWithNegativeKeys() {
        final int n = 4000;
        final Random random = new Random(55);
        final Tagged[] xs = new Tagged[n];
        for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(21) - 10, i); // keys -10..10
        final ParallelRadixHuskySort<Tagged> sorter = newSorter(8, 5, new TaggedKeyCoder());
        final Tagged[] sorted = sorter.sort(xs);
        assertStableAndSorted(sorted);
    }

    /**
     * The strongest possible stability check: every element ties on key, so a stable sort must
     * leave the array in exactly its original order -- including when that single key's bucket
     * is split across many chunks, which is exactly the scenario a broken offset-combine step
     * would get wrong.
     */
    @Test
    public void testStabilityAllSameKeyAcrossChunkCounts() {
        for (final int parallelism : new int[]{1, 3, 8, 16}) {
            final int n = 500;
            final Tagged[] xs = new Tagged[n];
            for (int i = 0; i < n; i++) xs[i] = new Tagged(42L, i);

            final ParallelRadixHuskySort<Tagged> sorter = newSorter(8, parallelism, new TaggedKeyCoder());
            final Tagged[] sorted = sorter.sort(xs);

            for (int i = 0; i < n; i++)
                assertEquals("parallelism=" + parallelism + ": all-same-key input should come out in original order", i, sorted[i].tag);
        }
    }

    /**
     * Chunk count exceeding n: MIN_CHUNK_SIZE would never pick this on its own, but the full
     * constructor allows it directly, and the chunking arithmetic (n / chunks with a remainder
     * distributed across the first chunks) must still produce exactly n total elements with no
     * empty-chunk edge case going wrong.
     */
    @Test
    public void testChunkCountExceedsN() {
        final int n = 10;
        final Long[] xs = {5L, -3L, 100L, 0L, 42L, -1L, 7L, 8L, 9L, 2L};
        final Long[] expected = Arrays.copyOf(xs, n);
        Arrays.sort(expected);

        final ParallelRadixHuskySort<Long> sorter = newSorter(8, 64, HuskyCoderFactory.longCoder);
        final Long[] ys = sorter.sort(Arrays.copyOf(xs, n));
        assertArrayEquals(expected, ys);
    }

    /**
     * The automatic digit width budgets buckets x chunks at n/4 and takes the widest digit fitting
     * inside it, clamped to [8, 16]. These are the real configurations the benchmarks use, so a
     * regression here would silently change what the auto benchmarks measure.
     */
    @Test
    public void testChooseDigitBits() {
        // Permits at its full corpus size: 16 bits would put 524,288 bucket slots against 198,900
        // records, which is the case item 34 of TODO.md is about. The budget admits 12.
        assertEquals("permits 198,900 over 8 chunks", 12, RadixHuskySort.chooseDigitBits(198_900, 8));
        assertEquals("permits 100,000 over 8 chunks", 11, RadixHuskySort.chooseDigitBits(100_000, 8));
        // Long[] at ten million is the one configuration where a wide digit is already affordable.
        assertEquals("Long[] 10,000,000 over 8 chunks", 16, RadixHuskySort.chooseDigitBits(10_000_000, 8));
        // Fewer chunks means a bigger share of the budget each.
        assertEquals("permits 198,900 over 1 chunk", 15, RadixHuskySort.chooseDigitBits(198_900, 1));
        // Clamped at both ends, including the degenerate small-n case that must not wrap round.
        assertEquals("clamped below", RadixHuskySort.MIN_AUTO_DIGIT_BITS, RadixHuskySort.chooseDigitBits(10, 64));
        assertEquals("clamped below at n=0", RadixHuskySort.MIN_AUTO_DIGIT_BITS, RadixHuskySort.chooseDigitBits(0, 1));
        assertEquals("clamped above", RadixHuskySort.MAX_AUTO_DIGIT_BITS, RadixHuskySort.chooseDigitBits(Integer.MAX_VALUE, 1));
    }

    /**
     * The auto width must sort as correctly as any fixed one, across chunk counts -- including the
     * negative keys that exercise the sign bias, and duplicate keys that exercise stability.
     */
    @Test
    public void testAutoDigitBitsSortsCorrectly() {
        final Random r = new Random(42);
        final int n = 20_000;
        final Long[] xs = new Long[n];
        for (int i = 0; i < n; i++) xs[i] = r.nextLong();
        final Long[] expected = Arrays.copyOf(xs, n);
        Arrays.sort(expected);

        for (final int parallelism : new int[]{1, 3, 8}) {
            final ParallelRadixHuskySort<Long> sorter = newSorter(ParallelRadixHuskySort.AUTO_DIGIT_BITS, parallelism, HuskyCoderFactory.longCoder);
            final Long[] ys = sorter.sort(Arrays.copyOf(xs, n));
            assertArrayEquals("parallelism=" + parallelism, expected, ys);
        }
    }

    @Test
    public void testAutoDigitBitsIsStable() {
        final int n = 20_000;
        for (final int parallelism : new int[]{1, 3, 8}) {
            final Random random = new Random(42);
            final Tagged[] xs = new Tagged[n];
            for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(50), i);
            final ParallelRadixHuskySort<Tagged> sorter = newSorter(ParallelRadixHuskySort.AUTO_DIGIT_BITS, parallelism, new TaggedKeyCoder());
            assertStableAndSorted(sorter.sort(xs));
        }
    }

    /**
     * An explicitly-given width is honoured exactly, never silently replaced by the automatic one:
     * the benchmark labels and the paper's digit-width sweep depend on /16 meaning 16 bits even
     * where the automatic choice would have picked something narrower.
     */
    @Test
    public void testExplicitDigitBitsNotOverridden() {
        assertEquals("auto would pick 12 here", 12, RadixHuskySort.chooseDigitBits(198_900, 8));
        final Random r = new Random(42);
        final int n = 198_900;
        final Long[] xs = new Long[n];
        for (int i = 0; i < n; i++) xs[i] = r.nextLong();
        final Long[] expected = Arrays.copyOf(xs, n);
        Arrays.sort(expected);
        final ParallelRadixHuskySort<Long> sorter = newSorter(16, 8, HuskyCoderFactory.longCoder);
        assertEquals("ParallelRadixHuskySort/16", new ParallelRadixHuskySort<>(16, HuskyCoderFactory.longCoder, config).toString());
        assertArrayEquals(expected, sorter.sort(Arrays.copyOf(xs, n)));
    }

    /**
     * A minimum chunk size of 1 lets the chunk count reach "parallelism" even for a small array,
     * which is how the benchmarks exercise more chunks than the default 16,384-element floor would
     * ever allow. The sort must stay correct and stable with chunks that small.
     */
    @Test
    public void testSmallMinChunkSizeSortsCorrectlyAndStably() {
        final int n = 5_000;
        final Random random = new Random(42);
        final Tagged[] xs = new Tagged[n];
        for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(40), i);
        final ParallelRadixHuskySort<Tagged> sorter = new ParallelRadixHuskySort<>("tiny chunks", 0, 8, 1, new TaggedKeyCoder(), java.util.Arrays::sort, config, 8);
        assertStableAndSorted(sorter.sort(xs));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinChunkSizeMustBePositive() {
        new ParallelRadixHuskySort<Long>("bad", 0, 8, 0, HuskyCoderFactory.longCoder, java.util.Arrays::sort, config, 4);
    }

    /**
     * The seven-argument constructor must keep meaning what it did before minChunkSize existed,
     * since every pre-existing caller still uses it.
     */
    @Test
    public void testSevenArgConstructorDefaultsMinChunkSize() {
        final int n = 5_000;
        final Random random = new Random(42);
        final Long[] xs = new Long[n];
        for (int i = 0; i < n; i++) xs[i] = random.nextLong();
        final Long[] expected = Arrays.copyOf(xs, n);
        Arrays.sort(expected);
        assertArrayEquals(expected, newSorter(8, 8, HuskyCoderFactory.longCoder).sort(Arrays.copyOf(xs, n)));
    }

    /**
     * The same guard RadixHuskySortTest.testChineseNamesUseCollatorNotNaturalOrder provides for the
     * serial sorter, for the four-argument constructor added so that the string benchmarks (TODO.md
     * item 33) can name a thread count without also having to name a post-sorter. A caller that
     * reaches for the primary constructor and passes Arrays::sort gets natural (Unicode code point)
     * order for a coder whose intended order is pinyin -- silently, since the array does come back
     * sorted, just by the wrong ordering. HuskyCoderChinesePinyin never claims perfect(), so its
     * cleanup pass always runs and always decides the final answer.
     */
    @Test
    public void testChineseNamesUseCollatorNotNaturalOrder() {
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅", "杨腊香", "许凤山", "王广风", "黄锡鸿", "罗庆富", "顾芳芳", "宋雪光", "王诗卉"};
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected, HuskyCoderChinesePinyin.NAME_ORDER);

        for (final int parallelism : new int[]{1, 4, 8}) {
            for (final int digitBits : new int[]{ParallelRadixHuskySort.AUTO_DIGIT_BITS, 8, 16}) {
                final ParallelRadixHuskySort<String> sorter = new ParallelRadixHuskySort<>(digitBits, HuskyCoderFactory.chineseEncoderPinyin, config, parallelism);
                final String[] actual = sorter.sort(Arrays.copyOf(xs, xs.length));
                assertArrayEquals("digitBits=" + digitBits + ", parallelism=" + parallelism, expected, actual);
            }
        }
    }

    @Test
    public void testAutoDigitBitsRejectedNowhereAndNamed() {
        assertEquals("ParallelRadixHuskySort/auto", new ParallelRadixHuskySort<>(ParallelRadixHuskySort.AUTO_DIGIT_BITS, HuskyCoderFactory.longCoder, config).toString());
    }
}
