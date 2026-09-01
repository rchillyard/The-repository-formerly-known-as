package edu.neu.coe.huskySort.sort.radix;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * A radix sort emits its buckets in index order, so the character-to-index mapping has to be
 * monotonic: if v &lt; w then v's bucket must precede w's. Characters below 256 are their own index
 * and satisfy this for free; the rest are assigned positions in a spare region, and those positions
 * used to be handed out in order of first encounter. Two strings differing first at a character
 * beyond ASCII therefore came out in whichever order the input happened to present those characters,
 * which is not an order at all.
 * <p>
 * {@code Alphabet.prepare} now assigns the spare region in code-point order, over the whole input,
 * before any of it is bucketed.
 */
public class AlphabetOrderingTest {

    /**
     * The minimal shape: two characters beyond ASCII, the greater one encountered first, and enough
     * strings that the sort cannot fall through to insertion sort below its cutoff.
     */
    @Test
    public void sortsByCodePointNotByFirstEncounter() {
        final String greater = "Ž";  // 381
        final String lesser = "œ";   // 339
        final String[] xs = new String[20];
        for (int i = 0; i < xs.length; i++) xs[i] = (i % 2 == 0 ? greater : lesser) + "x" + i;
        assertSortsLikeTheJdk(xs);
    }

    /**
     * The mapping must be monotonic across the whole spare region, not merely for one pair.
     */
    @Test
    public void theSpareRegionIsMonotonicThroughout() {
        final char[] chars = {'Ž', 'œ', 'Š', 'š', 'Ÿ', 'ˆ', 'ÿ', 'Ā'};
        final String[] xs = new String[10 * chars.length];
        for (int i = 0; i < xs.length; i++) xs[i] = String.valueOf(chars[i % chars.length]) + "tail" + i;
        assertSortsLikeTheJdk(xs);

        final Alphabet alphabet = new Alphabet(Alphabet.RADIX_UNICODE);
        alphabet.prepare(xs);
        final char[] ascending = chars.clone();
        Arrays.sort(ascending);
        int previous = -1;
        for (final char c : ascending) {
            final int index = alphabet.getCountIndex(c);
            assertTrue("index for " + (int) c + " must exceed the index of every smaller character",
                    index > previous);
            previous = index;
        }
    }

    /**
     * Mixed ASCII and beyond, at a size that exercises the recursion rather than the cutoff.
     */
    @Test
    public void agreesWithArraysSortOnMixedInput() {
        final Random random = new Random(11);
        final char[] pool = {'a', 'b', 'z', 'é', 'Ž', 'œ', 'Š', 'ÿ'};
        for (int trial = 0; trial < 500; trial++) {
            final String[] xs = new String[1 + random.nextInt(200)];
            for (int i = 0; i < xs.length; i++) {
                final StringBuilder b = new StringBuilder();
                for (int j = 0; j < 1 + random.nextInt(6); j++) b.append(pool[random.nextInt(pool.length)]);
                xs[i] = b.toString();
            }
            assertSortsLikeTheJdk(xs);
        }
    }

    /**
     * An Alphabet is reusable: a second sort must not inherit the first one's assignment.
     */
    @Test
    public void aReusedAlphabetReassignsFromScratch() {
        final Alphabet alphabet = new Alphabet(Alphabet.RADIX_UNICODE);
        final MSDStringSort sorter = new MSDStringSort(alphabet);

        final String[] first = new String[20];
        for (int i = 0; i < first.length; i++) first[i] = "Ž" + i;
        sorter.sort(first);

        // a second input whose non-ASCII character sorts below the first input's
        final String[] second = new String[20];
        for (int i = 0; i < second.length; i++) second[i] = (i % 2 == 0 ? "œ" : "Ž") + "y" + i;
        final String[] expected = second.clone();
        Arrays.sort(expected);
        sorter.sort(second);
        assertArrayEquals("the second sort must not be biased by the first", expected, second);
    }

    private static void assertSortsLikeTheJdk(final String[] xs) {
        final String[] expected = xs.clone();
        Arrays.sort(expected);
        final String[] actual = xs.clone();
        new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE)).sort(actual);
        assertArrayEquals(Arrays.toString(xs), expected, actual);
    }
}
