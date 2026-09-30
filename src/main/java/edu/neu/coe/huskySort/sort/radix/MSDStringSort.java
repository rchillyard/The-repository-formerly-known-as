package edu.neu.coe.huskySort.sort.radix;

import static edu.neu.coe.huskySort.util.Config.shouldRecurse;


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

    /**
     * Retrieves the {@link Alphabet} instance associated with this MSDStringSort.
     * TESTME unused.
     *
     * @return the alphabet used for character-to-bucket mapping in the sorting process.
     */
    public Alphabet getAlphabet() {
        return alphabet;
    }

    /**
     * Sets the cutoff value for invoking insertion sort during the MSD String Sort process.
     * This value determines the threshold below which the MSD sort switches to a simpler sorting algorithm,
     * typically for improved performance on small subarrays.
     * TESTME unused.
     *
     * @param cutoff the size threshold below which insertion sort will be used.
     */
    public static void setCutoff(final int cutoff) {
        // NOTE 0 or less means "unset", matching every helper-based cutoff in this project
        // (ComparableSortHelper, InstrumentedComparisonSortHelper, BasicCountingSortHelper and
        // InstrumentedCountingSortHelper all read (cutoff >= 1) ? cutoff : default). Those are
        // guarded because this field is the one cutoff a caller can set directly, and it was not:
        // measured 2026-09-30, setCutoff(0) made the test below never true, so even an empty
        // range recursed and every sort ended in StackOverflowError.
        MSDStringSort.cutoff = cutoff >= 1 ? cutoff : DEFAULT_CUTOFF;
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
        // NOTE Config.shouldRecurse is the one place the cutoff comparison is written. This
        // read "hi < lo + cutoff" until 2026-09-30, so the effective cutoff was one less than
        // the value set -- 14 rather than the 15 declared below.
        if (!shouldRecurse(hi - lo, cutoff)) insertionSort(a, lo, hi, d);
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

    /**
     * The size threshold below which insertion sort is used. See {@link #setCutoff}: values of 0
     * or less mean "unset" and select this default, because the recursion does not terminate
     * without a positive cutoff.
     */
    public static final int DEFAULT_CUTOFF = 15;

    private static int cutoff = DEFAULT_CUTOFF;
    private static String[] aux;       // auxiliary array for distribution

    private final Alphabet alphabet;
}