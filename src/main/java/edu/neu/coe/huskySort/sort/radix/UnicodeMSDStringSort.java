package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

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
        final int n = a.length;
        final CharacterMap.UnicodeString[] xs = new CharacterMap.UnicodeString[n];
        for (int i = 0; i < n; i++) xs[i] = characterMap.new UnicodeString(a[i]);
        aux = new CharacterMap.UnicodeString[n];
        doRecursiveSort(xs, 0, n, 0);
//        System.out.println(Arrays.toString(xs));
        for (int i = 0; i < n; i++) a[i] = xs[i].word;
    }

    /**
     * Constructor of UnicodeMSDStringSort, which requires a CharacterMap.
     *
     * @param characterMap the appropriate character map for the type of unicode strings to be sorted.
     */
    public UnicodeMSDStringSort(final CharacterMap characterMap) {
        this.characterMap = characterMap;
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
//        System.out.println("doRecursiveSort: from="+from+", to="+to+", d="+d);
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
//            System.out.println("   keys="+"("+keys.length+")"+ Arrays.toString(keys));
            // XXX Copy back.
            if (to - from >= 0) System.arraycopy(aux, 0, xs, from, to - from);
            // XXX Recursively sort for each character value.
            int offset = 0;
            // XXX For each key, recursively sort the appropriate sub-array on the next character position.
            for (final UnicodeCharacter key : keys) {
                if (key == UnicodeCharacter.NullChar)
                    return;
                final int index = counts.get(key);
//                System.out.println("   key="+key+", offset="+offset+", index="+index);
                doRecursiveSort(xs, from + offset, from + index, d + 1);
                offset = index;
            }
        }
    }

    /**
     * Execute insertion sort on the given sub-array, but skipping the first d characters when determining the order.
     *
     * @param a    an array.
     * @param from the first element of the array to be considered.
     * @param to   the first element following the sub-array NOT to be considered.
     * @param d    the number of characters to be ignored.
     */
    private static void insertionSort(final CharacterMap.UnicodeString[] a, final int from, final int to, final int d) {
        for (int i = from; i < to; i++)
            for (int j = i; j > from && less(a[j], a[j - 1], d); j--)
                swap(a, j, j - 1);
    }

    private static boolean less(final CharacterMap.UnicodeString v, final CharacterMap.UnicodeString w, final int d) {
        return v.compare(w, d) < 0;
    }

    private static void swap(final Object[] a, final int j, final int i) {
        final Object temp = a[j];
        a[j] = a[i];
        a[i] = temp;
    }

    private static int cutoff = 15; // XXX default value for the insertion sort cutoff.
    
    private static CharacterMap.UnicodeString[] aux; // XXX auxiliary array for distribution.

    private final CharacterMap characterMap; // NOTE this is used, despite IDEA's analysis.
}