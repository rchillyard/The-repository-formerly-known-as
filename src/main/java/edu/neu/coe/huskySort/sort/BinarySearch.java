package edu.neu.coe.huskySort.sort;

import java.util.Comparator;

/**
 * Class to provide the functionality of binary search.
 * NOTE that it is essentially the same as the Arrays.binarySearch method, except that it takes
 * an explicit Comparator (rather than relying on natural ordering), so that a caller's own
 * instrumented comparison method can be used and its counts preserved.
 */
public class BinarySearch {

    /**
     * Method to do binary search.
     *
     * @param a          the ordered array.
     * @param from       the first index of interest.
     * @param to         the first subsequent index that is NOT of interest.
     * @param key        the value we are searching for.
     * @param comparator the comparator to use for comparisons.
     * @param <X>        the underlying type.
     * @return the index of the element whose value is <code>key</code>, if it is contained in
     * the array within the specified range; otherwise, <code>-(insertion point) - 1</code>. The
     * insertion point is defined as the index of the first element greater than <code>key</code>,
     * or <code>to</code> if every element in the range is less than <code>key</code>. This matches
     * the convention used by <code>java.util.Arrays.binarySearch</code>.
     */
    static <X> int binarySearch(final X[] a, final int from, final int to, final X key, final Comparator<X> comparator) {
        int lo = from;
        int hi = to - 1;
        while (lo <= hi) {
            final int mid = (lo + hi) >>> 1;
            final int cf = comparator.compare(a[mid], key);
            if (cf < 0)
                lo = mid + 1;
            else if (cf > 0)
                hi = mid - 1;
            else
                return mid;
        }
        return -(lo + 1);
    }
}
