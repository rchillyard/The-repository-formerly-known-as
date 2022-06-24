package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.util.Helper;

public interface TransformingHelper<X> extends Helper<X> {
    /**
     * Method to do any required preProcessing.
     *
     * @param xs the array to be sorted.
     * @return the array after any pre-processing.
     */
    default X[] preProcess(final X[] xs) {
        // CONSIDER invoking init from here.
        // NOTE: not called by UnicodeMSDStringSort
        return xs;
    }
}
