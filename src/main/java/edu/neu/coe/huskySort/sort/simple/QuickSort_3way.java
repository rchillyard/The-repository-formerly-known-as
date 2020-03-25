package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;

import java.util.Arrays;

public class QuickSort_3way<X extends Comparable<X>> extends SortWithHelper<X> {
    /**
     * Constructor for QuickSort_3way
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public QuickSort_3way(BaseHelper<X> helper) {
        super(helper);
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param N            the number elements we expect to sort.
     * @param instrumented whether or not we want an instrumented helper class.
     */
    public QuickSort_3way(int N, boolean instrumented) {
        super(DESCRIPTION, N, instrumented);
    }

    public QuickSort_3way() {
        this(new BaseHelper<>(DESCRIPTION));
    }

    static class Partition {
        final int lt;
        final int gt;

        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }

    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        getHelper().setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        // TODO make this consistent with other uses of sort where the upper limit of the range is result.length
        sort(result, 0, result.length - 1);
        return result;
    }

    @Override
    public void sort(X[] a, int from, int to) {
        @SuppressWarnings("UnnecessaryLocalVariable") int lo = from;
        int hi = to;
        if (hi <= lo) return;
        Partition partition = partition(a, lo, hi);
        sort(a, lo, partition.lt - 1);
        sort(a, partition.gt + 1, hi);
    }

    public Partition partition(X[] a, int lo, int hi) {
        int lt = lo, gt = hi;
        if (a[lo].compareTo(a[hi]) > 0) swap(a, lo, hi);
        X v = a[lo];
        int i = lo + 1;
        while (i <= gt) {
            int cmp = a[i].compareTo(v);
            if (cmp < 0) swap(a, lt++, i++);
            else if (cmp > 0) swap(a, i, gt--);
            else i++;
        }
        return new Partition(lt, gt);
    }

    public static final String DESCRIPTION = "QuickSort 3 way";

    // exchange a[i] and a[j]
    private static void swap(Object[] a, int i, int j) {
        Object temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

}

