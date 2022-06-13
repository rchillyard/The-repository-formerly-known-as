package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Random;

/**
 * Class to implement Most significant digit string sort (a radix sort).
 */
public final class UnicodeMSDStringSort {
    /**
     * Sort an array of Strings using UnicodeMSDStringSort.
     *
     * @param a the array to be sorted.
     */
    public void sort(final String[] a) {
//        logger.info("UnicodeMSDStringSort.sort: sorting " + a.length + " strings");
        final int n = a.length;
        final CharacterMap.UnicodeString[] xs = new CharacterMap.UnicodeString[n];
        for (int i = 0; i < n; i++) xs[i] = characterMap.new UnicodeString(a[i]);
        aux = new CharacterMap.UnicodeString[n];
        doRecursiveSort(xs, 0, n, 0);
        for (int i = 0; i < n; i++) a[i] = xs[i].word;
    }

    /**
     * Constructor of UnicodeMSDStringSort, which requires a CharacterMap.
     *
     * @param characterMap the appropriate character map for the type of unicode strings to be sorted.
     */
    public UnicodeMSDStringSort(final CharacterMap characterMap, final CountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper) {
        this.characterMap = characterMap;
        this.helper = helper;
    }

    /**
     * Constructor of UnicodeMSDStringSort, which requires a CharacterMap.
     *
     * @param characterMap the appropriate character map for the type of unicode strings to be sorted.
     */
    public UnicodeMSDStringSort(final CharacterMap characterMap) {
        this(characterMap, new BasicCountingSortHelper<>("UnicodeMSDStringSort", 0, new Random()));
    }

    /**
     * Used by unit tests to vary the cutoff value.
     *
     * @param cutoff an appropriate cutoff value for switching to insertion sort.
     */
    public static void setCutoff(final int cutoff) {
        UnicodeMSDStringSort.cutoff = cutoff;
    }

    /**
     * Sort from xs[from] to xs[to] (exclusive), ignoring the first d characters of each String.
     * This method is recursive.
     *
     * @param xs   the array to be sorted.
     * @param from the low index.
     * @param to   the high index (one above the highest actually processed).
     * @param d    the number of characters in each UnicodeString to be skipped.
     */
    private void doRecursiveSort(final CharacterMap.UnicodeString[] xs, final int from, final int to, final int d) {
        assert from >= 0 : "from " + from + " is negative";
        assert to <= xs.length : "to " + to + " is out of bounds: " + xs.length;
//        logger.debug("UnicodeMSDStringSort.doRecursiveSort: on " +(d > 0 ? xs[from].charAt(d-1) : "root")+ " from="+from+", to="+to+", d="+d);
        // XXX if there are fewer than two elements, we return immediately.
        if (from >= to - 1) return;
        // XXX if there is a small number of elements, we switch to insertion sort.
        if (to < from + cutoff) insertionSort(xs, from, to, d);
        else {
            final Counts counts = new Counts();
            counts.countCharacters(xs, from, to, d);
            final UnicodeCharacter[] keys = counts.accumulateCounts();
            for (int i = from; i < to; i++) {
                final CharacterMap.UnicodeString xsi = xs[i];
                counts.copyAndIncrementCount(xsi, aux, d);
            }
            // XXX Copy back.
            if (to - from >= 0) System.arraycopy(aux, 0, xs, from, to - from);
            // XXX Recursively sort for each character value.
            int offset = 0;
            // XXX For each key, recursively sort the appropriate sub-array on the next character position.
            for (final UnicodeCharacter key : keys) {
                if (key == UnicodeCharacter.NullChar)
                    continue;
                final int index = counts.get(key);
                doRecursiveSort(xs, from + offset, from + index, d + 1);
                offset = index;
            }
        }
    }

    /**
     * Execute insertion sort on the given sub-array, but skipping the first d characters when determining the order.
     *
     * @param xs   an array.
     * @param from the first element of the array to be considered.
     * @param to   the first element following the sub-array NOT to be considered.
     * @param d    the number of characters to be ignored.
     */
    private static void insertionSort(final CharacterMap.UnicodeString[] xs, final int from, final int to, final int d) {
//        logger.debug("UnicodeMSDStringSort.insertionSort: on " +(d > 0 ? xs[from].charAt(d-1) : "root")+ " from="+from+", to="+to+", d="+d);
        for (int i = from; i < to; i++)
            for (int j = i; j > from && less(xs[j], xs[j - 1], d); j--)
                swap(xs, j, j - 1);
    }

    private static boolean less(final CharacterMap.UnicodeString v, final CharacterMap.UnicodeString w, final int d) {
        return v.compareFromD(w, d) < 0;
    }

    private static void swap(final Object[] a, final int j, final int i) {
        final Object temp = a[j];
        a[j] = a[i];
        a[i] = temp;
    }

    final static LazyLogger logger = new LazyLogger(UnicodeMSDStringSort.class);

    private static int cutoff = 15; // XXX default value for the insertion sort cutoff.

    private static CharacterMap.UnicodeString[] aux; // XXX auxiliary array for distribution.

    private final CharacterMap characterMap; // NOTE this is used, despite IDEA's analysis.
    private final CountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper;
}