/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

public interface HuskySortable<X> extends Comparable<X> {
    /**
     * This method returns a quasi-monotonically increasing long value corresponding to this.
     *
     * @return a long such that when comparison is done by longs, it is approximately 90% accurate.
     */
    long huskyCode();

    /**
     * implementation of compareTo based on the huskyCode of each comparand.
     * Note that the huskyCode must be re-evaluated each time it is used in a compare,
     * unless the object itself caches the huskyCode.
     *
     * @param x the object to be compared
     * @return an int according to the ordering of this and x
     */
    @Override
    default int compareTo(X x) {
        if (HuskySortable.class.isAssignableFrom(x.getClass()))
            return Long.compare(huskyCode(), ((HuskySortable) x).huskyCode());
        else
            throw new UnsupportedOperationException("compareTo must be implemented for " + getClass());
    }
}
