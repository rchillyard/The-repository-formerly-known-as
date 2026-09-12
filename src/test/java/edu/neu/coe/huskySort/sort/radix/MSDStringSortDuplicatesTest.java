package edu.neu.coe.huskySort.sort.radix;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

/**
 * MSDStringSort could not sort any array containing as many equal strings as its cutoff.
 * <p>
 * Once a string is exhausted, {@code charAt} returns 0, so every such string falls into count
 * bucket 0 at every greater depth. Recursing into that bucket made no progress: the range never
 * shrank, {@code d} climbed without bound, and the recursion ran until the stack was exhausted.
 * Below the cutoff of 15 insertion sort took over before this could happen, which is why the
 * boundary is exactly there — 14 equal strings sorted, 15 threw StackOverflowError.
 * <p>
 * It reached daylight because every test in {@link MSDStringSortTest} is commented out. These are
 * not.
 * <p>
 * NOTE the fix must not simply stop recursing when a bucket spans the whole range: strings sharing
 * a common prefix legitimately do that, and must recurse at d + 1 to be separated. Only the bucket
 * of ended strings is the non-progressing one, and it is already sorted, being all equal.
 * {@link UnicodeMSDStringSort} guards it as {@code key != UnicodeCharacter.NullChar}.
 */
public class MSDStringSortDuplicatesTest {

    @Test
    public void sortsMoreEqualStringsThanTheCutoff() {
        for (final int copies : new int[]{14, 15, 16, 100}) {
            final String[] xs = new String[copies];
            Arrays.fill(xs, "hello");
            final String[] expected = xs.clone();
            sort(xs);
            assertArrayEquals(copies + " equal strings", expected, xs);
        }
    }

    @Test
    public void sortsDuplicatesMixedWithDistinctStrings() {
        final String[] xs = new String[60];
        Arrays.fill(xs, 0, 40, "hello");
        for (int i = 40; i < 60; i++) xs[i] = "world" + i;
        final String[] expected = xs.clone();
        Arrays.sort(expected);
        sort(xs);
        assertArrayEquals(expected, xs);
    }

    /**
     * A string which is a prefix of another must still sort before it, which is the case the ended-
     * string bucket exists to handle — so skipping its recursion must not disturb the order.
     */
    @Test
    public void aPrefixSortsBeforeWhatExtendsIt() {
        final String[] xs = new String[60];
        for (int i = 0; i < 60; i++) xs[i] = "ab".repeat(1 + i % 3);
        final String[] expected = xs.clone();
        Arrays.sort(expected);
        sort(xs);
        assertArrayEquals(expected, xs);
    }

    /**
     * The general check, against the JDK. Both duplicate-heavy and long-shared-prefix shapes, since
     * the fix touches the boundary between them.
     */
    @Test
    public void agreesWithArraysSortOnAwkwardShapes() {
        final Random random = new Random(7);
        final String[] pool = {"", "a", "ab", "abc", "abcd", "b", "ba", "hello", "hell", "he", "zzz", "z"};
        for (int trial = 0; trial < 2000; trial++) {
            final String[] xs = new String[1 + random.nextInt(120)];
            for (int i = 0; i < xs.length; i++) xs[i] = pool[random.nextInt(pool.length)];
            assertSortsLikeTheJdk(xs);
        }
        for (int trial = 0; trial < 500; trial++) {
            final String[] xs = new String[1 + random.nextInt(80)];
            for (int i = 0; i < xs.length; i++)
                xs[i] = "commonprefix".repeat(1 + random.nextInt(3))
                        + (random.nextBoolean() ? "" : String.valueOf((char) ('a' + random.nextInt(4))));
            assertSortsLikeTheJdk(xs);
        }
    }

    private static void assertSortsLikeTheJdk(final String[] xs) {
        final String[] expected = xs.clone();
        Arrays.sort(expected);
        final String[] actual = xs.clone();
        sort(actual);
        assertArrayEquals(Arrays.toString(xs), expected, actual);
    }

    private static void sort(final String[] xs) {
        final MSDStringSort sorter = new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE));
        sorter.reset();
        sorter.sort(xs);
    }
}
