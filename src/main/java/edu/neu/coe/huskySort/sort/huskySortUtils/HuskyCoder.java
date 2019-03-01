/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

public interface HuskyCoder<X> {

    long huskyEncode(X x);

    boolean imperfect();
}
