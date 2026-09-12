package edu.neu.coe.huskySort.sort.radix;


/**
 * Class to implement Most significant digit string sort (a radix sort).
 */
public final class MSDStringSort {

    public MSDStringSort(final Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * Sort an array of Strings using MSDStringSort.
     *
     * @param a the array to be sorted.
     */
    public void sort(final String[] a) {
        final int n = a.length;
        aux = new String[n];
        // NOTE the alphabet must see the whole input before any of it is bucketed, so that characters
        // beyond ASCII are given positions in code-point order rather than in order of first encounter.
        alphabet.prepare(a);
        sort(a, 0, n, 0);
    }

    public void reset() {
        alphabet.reset();
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public static void setCutoff(final int cutoff) {
        MSDStringSort.cutoff = cutoff;
    }

    /**
     * Sort from a[lo] to a[hi] (exclusive), ignoring the first d characters of each String.
     * This method is recursive.
     *
     * @param a  the array to be sorted.
     * @param lo the low index.
     * @param hi the high index (one above the highest actually processed).
     * @param d  the number of characters in each String to be skipped.
     */
    private void sort(final String[] a, final int lo, final int hi, final int d) {
        assert lo >= 0 : "lo " + lo + " is negative";
        assert hi <= a.length : "hi " + hi + " is out of bounds: " + a.length;
        if (hi < lo + cutoff) insertionSort(a, lo, hi, d);
        else {
            final int countLength = alphabet.getCountLength();
            final int[] count = new int[countLength];
            for (int i = lo; i < hi; i++) {
                final int x = alphabet.getCountIndex(charAt(a[i], d));
                count[x + 2]++;
            }
            for (int r = 0; r < alphabet.counts() + 1; r++)      // Transform counts to indices.
                count[r + 1] += count[r];
            for (int i = lo; i < hi; i++)
                aux[count[alphabet.getCountIndex(charAt(a[i], d)) + 1]++] = a[i];
            // Copy back.
            if (hi - lo >= 0) System.arraycopy(aux, 0, a, lo, hi - lo);
            // Recursively sort for each character value.
            // NOTE r = 0 is the bucket of strings which have no character at depth d, because
            // charAt returns 0 once a string is exhausted. Those strings are all equal and there is
            // nothing at d + 1 to separate them by, so recursing into that bucket makes no progress:
            // it would recurse until the stack ran out for any 15 or more equal strings, 15 being
            // the cutoff below which insertion sort would otherwise have taken over.
            // UnicodeMSDStringSort carries the same guard, as `key != UnicodeCharacter.NullChar`.
            for (int r = 1; r < alphabet.counts(); r++)
                sort(a, lo + count[r], lo + count[r + 1], d + 1);
        }
    }

    private static char charAt(final String s, final int d) {
        if (d < s.length()) return s.charAt(d);
        else return (char) 0;
    }

    private static void insertionSort(final String[] a, final int lo, final int hi, final int d) {
        for (int i = lo; i < hi; i++)
            for (int j = i; j > lo && less(a[j], a[j - 1], d); j--)
                swap(a, j, j - 1);
    }

    /**
     * Compare v and w from character d onwards, allocating nothing.
     * <p>
     * The result agrees with {@code v.substring(d).compareTo(w.substring(d)) < 0} for every d at which
     * that expression is legal, and is additionally defined for d beyond a string's length, where
     * substring would throw. Comparing in place matters here because this is a timed baseline against
     * the HuskySort variants: two String allocations per comparison, over the whole below-cutoff phase,
     * measures the allocator as much as the algorithm.
     *
     * @param v the first String.
     * @param w the second String.
     * @param d the number of leading characters known to be equal, and therefore skipped.
     * @return true if v is less than w.
     */
    private static boolean less(final String v, final String w, final int d) {
        final int vLength = v.length(), wLength = w.length();
        final int limit = Math.min(vLength, wLength);
        for (int i = d; i < limit; i++) {
            final char cv = v.charAt(i), cw = w.charAt(i);
            if (cv != cw) return cv < cw;
        }
        return vLength < wLength;
    }

    private static void swap(final Object[] a, final int j, final int i) {
        final Object temp = a[j];
        a[j] = a[i];
        a[i] = temp;
    }

    private static int cutoff = 15;
    private static String[] aux;       // auxiliary array for distribution

    private final Alphabet alphabet;
}