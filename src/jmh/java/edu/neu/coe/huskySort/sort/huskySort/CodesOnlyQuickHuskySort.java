package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;

/**
 * NOT A SORT. This is a measurement scaffold, and it deliberately leaves its payload unsorted.
 * <p>
 * It exists to isolate one cost. Appendix A.1 of the paper derives QuickHuskySort's array-access
 * count as {@code A = 4 + 0.6 * 4 = 6.4}, where the 0.6 stands for the proportion of the
 * object-reference swap's four accesses whose targets are not already in cache. That factor was
 * estimated around 2020 and has never been measured; it is the only quantity in the paper's
 * analysis that is a guess rather than a count, and the appendix says so.
 * <p>
 * This subclass overrides {@link QuickHuskySort#swap} to exchange only the {@code long[]} codes,
 * leaving the {@code Object[]} untouched. Everything else -- the comparisons, the branch decisions,
 * the recursion, the {@code long[]} memory traffic -- is inherited unchanged, because the
 * partitioning has exactly one implementation and both variants run it. So the difference in cache
 * refills between {@code QuickHuskySort} and this class is attributable to the two object reads and
 * two object writes per swap, and dividing that difference by four times the swap count gives the
 * factor 0.6 was standing in for.
 * <p>
 * Because it never permutes the payload, the array it returns is sorted only in its codes. Any
 * timing taken from it is the cost of a sort that did not happen, and must never be quoted as a
 * sorting result. It lives in the jmh source set so that nothing in main or test can reach it.
 * <p>
 * One caveat, recorded so that whoever reads the numbers knows it: {@code downHeap} moves object
 * references directly rather than through {@code swap}, so this override does not suppress those.
 * That path runs only when introSort exhausts its depth threshold, which the random inputs used
 * here do not provoke, so its contribution to the differential is nil in practice rather than by
 * construction.
 *
 * @param <X> the type of the elements whose codes are sorted.
 * @see <a href="file:../../../../../../../../doc/Run request for Yunlu.md">request 8</a>
 */
public class CodesOnlyQuickHuskySort<X extends Comparable<X>> extends QuickHuskySort<X> {

    public CodesOnlyQuickHuskySort(final HuskyCoder<X> huskyCoder, final boolean mayBeSorted, final boolean useInsertionSort) {
        super(huskyCoder, mayBeSorted, useInsertionSort);
    }

    /**
     * Exchange the codes at i and j and nothing else, where {@link QuickHuskySort#swap} would also
     * exchange the corresponding object references.
     *
     * @param xs    the X array, deliberately left alone.
     * @param longs the long array.
     * @param i     the index of one element to be swapped.
     * @param j     the index of the other element to be swapped.
     */
    @Override
    protected void swap(final X[] xs, final long[] longs, final int i, final int j) {
        final long temp = longs[i];
        longs[i] = longs[j];
        longs[j] = temp;
    }
}
