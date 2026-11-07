package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.ComparisonSortHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;

import java.util.Arrays;
import static edu.neu.coe.huskySort.util.Config.shouldRecurse;

/**
 * Class to implement Merge Sort.
 * NOTE: this implementation does NOT use the insertion swap mechanism,
 *
 * @param <X> the underlying type to be sorted.
 */
public class MergeSortBasic<X extends Comparable<X>> extends SortWithHelper<X> {

    /**
     * Method to prepare for sorting.
     * The default method invokes init with the length of the array xs then makes a copy of the array if appropriate.
     *
     * @param xs       the original array to be sorted.
     * @param makeCopy true if we need to work on a copy of the array.
     * @return either the original or a copy of the array.
     */
    @Override
    public X[] preSort(final X[] xs, final boolean makeCopy) {
        // CONSIDER don't copy but just allocate according to the xs/aux interchange optimization
        aux = Arrays.copyOf(xs, xs.length);
        return super.preSort(xs, makeCopy);
    }

    /**
     * Method to sort a sub-array.
     *
     * @param xs   the array to be sorted.
     * @param from the index of the first element of the sub-array.
     * @param to   the index of the first element of the sub-array NOT to sort.
     */
    public void sort(final X[] xs, final int from, final int to) {
        int n = to - from;
        if (!shouldRecurse(n, getHelper().getCutoff())) {
            insertionSort.sort(xs, from, to);
            return;
        }
        // NOTE aux is normally allocated by preSort, but this is the method the
        // Sort interface requires, so a caller may reach it directly -- and used to
        // get a NullPointerException from the arraycopy below when it did.
        // Allocating here costs nothing on the recursive calls, since aux is
        // already big enough by then. INFO6205's MergeSortBasic had the same defect
        // and the same fix.
        if (aux == null || aux.length < xs.length) aux = Arrays.copyOf(xs, xs.length);
        final int mid = from + n / 2;
        sort(xs, from, mid);
        sort(xs, mid, to);
        System.arraycopy(xs, from, aux, from, n);
        getHelper().incrementCopies(n);
        merge(aux, xs, from, mid, to);
    }

    public static final String DESCRIPTION = "MergeSort";

    /**
     * Constructor for MergeSort
     * <p>
     * NOTE this is used only by unit tests, using its own instrumented helper.
     *
     * @param helper an explicit instance of ComparisonSortHelper to be used.
     */
    public MergeSortBasic(final ComparisonSortHelper<X> helper) {
        super(helper);
        insertionSort = new InsertionSort<>(helper);
    }

    /**
     * Constructor for MergeSort
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public MergeSortBasic(final int N, final Config config) {
        super(DESCRIPTION, N, config);
        insertionSort = new InsertionSort<>(getHelper());
    }

    /**
     * Merges two sorted subarrays into a single sorted array.
     * The two subarrays are defined within the `aux` array as:
     * - The first subarray ranges from `from` (inclusive) to `mid` (exclusive).
     * - The second subarray ranges from `mid` (inclusive) to `to` (exclusive).
     * The merged result is stored in the `a` array in the range from `from` to `to`.
     *
     * @param aux  the auxiliary array containing two sorted subarrays to merge.
     * @param a    the array where the merged result will be stored.
     * @param from the starting index of the first subarray in `aux`.
     * @param mid  the starting index of the second subarray in `aux`.
     * @param to   the index one past the last element to merge in `aux`.
     */
    private void merge(final X[] aux, final X[] a, final int from, final int mid, final int to) {
        final ComparisonSortHelper<X> helper = getHelper();
        int i = from;
        int j = mid;
        int k = from;
        for (; k < to; k++)
            if (i >= mid) helper.copy(aux, j++, a, k);
            else if (j >= to) helper.copy(aux, i++, a, k);
            else if (helper.inverted(aux[i], aux[j])) {
                helper.incrementFixes(mid - i);
                helper.copy(aux, j++, a, k);
            } else helper.copy(aux, i++, a, k);
    }

    private X[] aux = null;
    private final InsertionSort<X> insertionSort;
}

