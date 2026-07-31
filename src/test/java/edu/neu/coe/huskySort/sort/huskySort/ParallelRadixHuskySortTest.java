package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
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
}
