package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.SortWithHelper;

import java.util.Arrays;

public class MergeSortBasic<X extends Comparable<X>> extends SortWithHelper<X> {

    public static final String DESCRIPTION = "MergeSort";
    public static final int CUTOFF = 7;

    /**
     * Constructor for MergeSort
     * <p>
     * NOTE this is used only by unit tests, using its own instrumented helper.
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public MergeSortBasic(Helper<X> helper) {
        super(helper);
        insertionSort = new InsertionSort<>(helper);
    }

    /**
     * Constructor for MergeSort
     *
     * @param N            the number elements we expect to sort.
     * @param instrumented whether or not we want an instrumented helper class.
     */
    public MergeSortBasic(int N, boolean instrumented) {
        super(DESCRIPTION, N, instrumented);
        insertionSort = new InsertionSort<>();
    }

    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        getHelper().setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        aux = Arrays.copyOf(xs, xs.length); // TODO don't copy but just allocate
        sort(result, 0, result.length);
        return result;
    }

    @Override
    public void sort(X[] a, int from, int to) {
        @SuppressWarnings("UnnecessaryLocalVariable") int lo = from;
        int hi = to;
        if (hi <= lo + CUTOFF) {
            insertionSort.sort(a, from, to);
            return;
        }
        int mid = from + (to - from) / 2;
        sort(a, lo, mid);
        sort(a, mid, hi);
        System.arraycopy(a, from, aux, from, to - from);
        merge(aux, a, lo, mid, hi);
    }

    private void merge(X[] aux, X[] a, int lo, int mid, int hi) {
        final Helper<X> helper = getHelper();
        int i = lo;
        int j = mid;
        for (int k = lo; k < hi; k++)
            if (i >= mid) a[k] = aux[j++];
            else if (j >= hi) a[k] = aux[i++];
            else if (helper.less(aux[j], aux[i])) a[k] = aux[j++];
            else a[k] = aux[i++];
    }

    private X[] aux = null;
    private final InsertionSort<X> insertionSort;
}

