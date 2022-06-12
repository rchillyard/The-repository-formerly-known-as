/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * Class to count inversions for an array of Xs.
 * NOTE: invoking getInversions will mutate the array passed in.
 */
@SuppressWarnings("rawtypes")
public class InversionCounter {
    private final Comparable[] arr;

    public InversionCounter(final Comparable[] arr) {
        this.arr = arr;
    }

    public long getInversions() {
        return _inversionsRecursive(arr, new Comparable[arr.length], 0, arr.length - 1);
    }

    /* An auxiliary recursive method that sorts the input array and
      returns the number of inversions in the array. */
    private long _inversionsRecursive(final Comparable[] arr, final Comparable[] temp, final int left, final int right) {
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
    private long inversionsMerge(final Comparable[] arr, final Comparable[] temp, final int left, final int mid, final int right) {
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
