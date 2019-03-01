/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort;

import java.lang.reflect.Array;
import java.util.Random;
import java.util.function.Function;

/**
 * Helper class for sorting methods.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class Helper<X extends Comparable<X>> {

    public Helper(String description, int n, long seed) {
        this.n = n;
        this.description = description;
        this.random = new Random(seed);
    }

    public Helper(String description, int n) {
        this(description, n, System.currentTimeMillis());
    }

    public Helper(String description) {
        this(description, 0);
    }

    /**
     * Method to determine if one X value is less than another.
     *
     * @param v   the candidate element.
     * @param w   the comparand element.
     * @return true only if v is less than w.
     */
    public boolean less(X v, X w) {
        compares++;
        return v.compareTo(w) < 0;
    }

    /**
     * Swap the elements of array a at indices i and j.
     *
     * @param a   the array.
     * @param lo  the lowest index of interest (only used for checking).
     * @param hi  one more than the highest index of interest (only used for checking).
     * @param i   one of the indices.
     * @param j   the other index.
     */
    public void swap(X[] a, int lo, int hi, int i, int j) {
        swaps++;
        if (i < lo) throw new RuntimeException("i is out of range: i; " + i + "; lo=" + lo);
        if (j > hi) throw new RuntimeException("j is out of range: j; " + j + "; hi=" + hi);
        X temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public boolean sorted(X[] a) {
        for (int i = 1; i < a.length; i++) if (a[i-1].compareTo(a[i])>0) return false;
        return true;
    }

    public X[] random(int n, Class<X> clazz, Function<Random, X> f) {
        setN(n);
        X[] result = (X[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) result[i] = f.apply(random);
        return result;
    }

    public X[] random(Class<X> clazz, Function<Random, X> f) {
        return random(n, clazz, f);
    }

    @Override
    public String toString() {
        return "Helper for "+description+" with "+n+" elements: compares="+compares+", swaps="+swaps;
    }

    public void setN(int n) {
        if (this.n == 0 || this.n == n) this.n = n;
        else throw new RuntimeException("Helper: n is already set to a different value");
    }

    private int compares = 0;
    private int swaps = 0;

    private int n;
    private final String description;
    private final Random random;

    public long getInversions(X xs[])
    {
        int array_size = xs.length;
        X aux[] = (X[]) new Object[array_size];
        return _mergeSort(xs, aux, 0, array_size - 1);
    }

    /* An auxiliary recursive method that sorts the input array and
      returns the number of inversions in the array. */
    private long _mergeSort(X xs[], X aux[], int left, int right)
    {
        int mid;
        long inv_count = 0;
        if (right > left) {
            /* Divide the array into two parts and call _mergeSortAndCountInv()
           for each of the parts */
            mid = (right + left) / 2;

            /* Inversion count will be sum of inversions in left-part, right-part
          and number of inversions in merging */
            inv_count = _mergeSort(xs, aux, left, mid);
            inv_count += _mergeSort(xs, aux, mid + 1, right);

            /*Merge the two parts*/
            inv_count += merge(xs, aux, left, mid + 1, right);
        }
        return inv_count;
    }

    /* This method merges two sorted arrays and returns inversion count in
       the arrays.*/
    private long merge(X xs[], X aux[], int left, int mid, int right)
    {
        int i, j, k;
        long inv_count = 0;

        i = left; /* i is index for left subarray*/
        j = mid; /* j is index for right subarray*/
        k = left; /* k is index for resultant merged subarray*/
        while ((i <= mid - 1) && (j <= right)) {
            if (xs[i].compareTo(xs[j]) <= 0) {
                aux[k++] = xs[i++];
            }
            else {
                aux[k++] = xs[j++];

                /*this is tricky -- see above explanation/diagram for merge()*/
                inv_count = inv_count + (mid - i);
            }
        }

        /* Copy the remaining elements of left subarray
       (if there are any) to temp*/
        while (i <= mid - 1)
            aux[k++] = xs[i++];

        /* Copy the remaining elements of right subarray
       (if there are any) to temp*/
        while (j <= right)
            aux[k++] = xs[j++];

        /*Copy back the merged elements to original array*/
        for (i = left; i <= right; i++)
            xs[i] = aux[i];

        return inv_count;
    }
}
