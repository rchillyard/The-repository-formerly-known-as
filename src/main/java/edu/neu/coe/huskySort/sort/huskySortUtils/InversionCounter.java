/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.util.Arrays;

/**
 * Class to count inversions for an array of Xs.
 * <p>
 * NOTE getInversions used to mutate the array passed in -- it counts by merge
 * sorting, and sorted what it was given. It works on a copy now, so the caller's
 * array comes back as it went in.
 */
@SuppressWarnings("rawtypes")

public class InversionCounter {
    private final Comparable[] arr;

    public InversionCounter(final Comparable[] arr) {
        this.arr = arr;
    }

    public long getInversions() {
        // NOTE the copy. _inversionsRecursive is a merge sort which counts as it
        // merges, so it SORTS what it is given -- and this class is called
        // InversionCounter, so a caller has every reason to expect its array back
        // untouched. The trap that would set is a quiet one: a benchmark which
        // counted the inversions in an array and then timed a sort on it would be
        // timing already-sorted data, and the result would look wonderful.
        //
        // The copy costs nothing that matters: the algorithm already allocates a
        // temporary of the same length on the next line.
        final Comparable[] copy = Arrays.copyOf(arr, arr.length);
        return _inversionsRecursive(copy, new Comparable[arr.length], 0, arr.length - 1);
    }

    /* An auxiliary recursive method that sorts the input array and
      returns the number of inversions in the array. */
    private static long _inversionsRecursive(final Comparable[] arr, final Comparable[] temp, final int left, final int right) {
        final int mid;
        long result = 0;
        if (right > left) {
            /* Divide the array into two parts and call _mergeSortAndCountInv()
           for each of the parts */
            mid = (right + left) / 2;

            /* Inversion count will be sum of inversions in left-part, right-part
          and number of inversions in merging */
            result = _inversionsRecursive(arr, temp, left, mid);
            result += _inversionsRecursive(arr, temp, mid + 1, right);

            /*Merge the two parts*/
            result += inversionsMerge(arr, temp, left, mid + 1, right);
        }
        return result;
    }

    /* This method merges two sorted arrays and returns inversion count in
       the arrays.*/
    private static long inversionsMerge(final Comparable[] arr, final Comparable[] temp, final int left, final int mid, final int right) {
        int i, j, k;
        long result = 0;

        i = left; /* i is index for left subarray*/
        j = mid; /* j is index for right subarray*/
        k = left; /* k is index for resultant merged subarray*/
        while ((i <= mid - 1) && (j <= right)) {
            //noinspection unchecked
            if (arr[i].compareTo(arr[j]) <= 0) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];

                /*this is tricky -- see above explanation/diagram for merge()*/
                result = result + (mid - i);
            }
        }

        /* Copy the remaining elements of left subarray
       (if there are any) to temp*/
        while (i <= mid - 1)
            temp[k++] = arr[i++];

        /* Copy the remaining elements of right subarray
       (if there are any) to temp*/
        while (j <= right)
            temp[k++] = arr[j++];

        /*Copy back the merged elements to original array*/
        for (i = left; i <= right; i++)
            arr[i] = temp[i];

        return result;
    }

}
