/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * Interface defining the behavior of a type X which can be HuskySorted.
 * Beyond the requirement that such objects are Comparable, we require a method huskyCode which returns a long.
 *
 * @param <X> the generic type to be sorted.
 */
public interface HuskySortable<X> extends Comparable<X> {
    /**
     * This method returns a quasi-monotonically increasing long value corresponding to this.
     *
     * @return a long such that when comparison is done by longs, it is approximately 90% accurate.
     */
    long huskyCode();
}
