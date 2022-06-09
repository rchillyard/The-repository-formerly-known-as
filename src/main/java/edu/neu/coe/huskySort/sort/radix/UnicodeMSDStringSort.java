package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

/**
 * Class to implement Most significant digit string sort (a radix sort).
 */
public final class UnicodeMSDStringSort {

    public UnicodeMSDStringSort(final CharacterMap characterMap) {
        this.characterMap = characterMap;
    }

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
        sort(xs, 0, n, 0);
//        System.out.println(Arrays.toString(xs));
        for (int i = 0; i < n; i++) a[i] = xs[i].word;
    }

    public void reset() {
        characterMap.clear();
    }

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
     * @param d    the number of characters in each String to be skipped.
     */
    private void sort(final CharacterMap.UnicodeString[] xs, final int from, final int to, final int d) {
        assert from >= 0 : "from " + from + " is negative";
        assert to <= xs.length : "to " + to + " is out of bounds: " + xs.length;
        if (to < from + cutoff) insertionSort(xs, from, to, d);
        else doRecursiveSort(xs, from, to, d);
    }

    private void doRecursiveSort(final CharacterMap.UnicodeString[] xs, final int from, final int to, final int d) {
        if (from >= to - 1) return;
        final Counts counts = new Counts();
        counts.countCharacters(xs, from, to, d);
        final UnicodeCharacter[] keys = counts.accumulateCounts();
        for (int i = from; i < to; i++) {
            final CharacterMap.UnicodeString xsi = xs[i];
            counts.copyAndIncrementCount(xsi, aux, d);
        }
        // Copy back.
        if (to - from >= 0) System.arraycopy(aux, 0, xs, from, to - from);
        // Recursively sort for each character value.
        int offset = 0;
        for (final UnicodeCharacter key : keys) {
            final int index = counts.get(key);
            sort(xs, from + offset, from + index, d + 1);
            offset = index;
        }
    }


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

    private static int cutoff = 15;
    private static CharacterMap.UnicodeString[] aux;       // auxiliary array for distribution

    private final CharacterMap characterMap;
}