/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

public interface HuskyCoder<X> {

    long huskyEncode(X x);

    // TODO this needes to be unit-tested
    default long[] huskyEncode(X[] xs) {
        long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) result[i] = huskyEncode(xs[i]);
        return result;
    }

    boolean imperfect();
}
