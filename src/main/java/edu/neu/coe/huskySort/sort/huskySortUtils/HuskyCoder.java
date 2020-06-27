/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

public interface HuskyCoder<X> {

    long huskyEncode(X x);

    // TEST
    default long[] huskyEncode(X[] xs) {
        long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) result[i] = huskyEncode(xs[i]);
        return result;
    }

    /**
     * Method to determine if this Husky Coder is imperfect for a String of the given length.
     * If the result is true for any String, it implies that inversions will remain after the first pass of Husky Sort.
     * If the result is false for all Strings, then the second pass of Husky Sort would be superfluous.
     *
     * @param length the length of a String
     * @return true if the resulting long for the String will likely not be unique.
     */
    boolean imperfect(int length);
}
