/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * This interface models the essence of the Husky Sort mechanism.
 * Elements in a collection are encoded using the huskyEncode method.
 * As far as possible, the codes should be monotonically increasing with the values.
 * But, all is not lost if that is not the case, for the result is a partially-sorted array
 * with a relatively small number of inversions which can be cleared up in phase two of the sort,
 * in linear time.
 *
 * @param <X> the underlying type for this coder.
 */
public interface HuskyCoder<X> {

    /**
     * @return the name of this coder.
     */
    default String name() {
        return "HuskyCoder";
    }

    /**
     * Encode x as a long.
     * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
     * If this cannot be guaranteed, then the result of imperfect(z) will be true.
     *
     * @param x the X value to encode.
     * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
     */
    long huskyEncode(X x);

    /**
     * Encode an array of Xs.
     *
     * @param xs an array of X elements.
     * @return an array of longs corresponding to the the Husky codes of the X elements.
     */
    default Coding huskyEncode(X[] xs) {
        long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) result[i] = huskyEncode(xs[i]);
        return new Coding(result, perfect());
    }

    /**
     * Method to determine if this Husky Coder is perfect for a class of objects (X).
     *
     * @return true if the resulting longs are perfect for ANY value of X.
     * By default, this method returns false.
     */
    default boolean perfect() {
        return false;
    }
}
