/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.Helper;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class AbstractHuskySort<X extends Comparable<X>> {

    public AbstractHuskySort() {
        helper = new Helper<>("HuskySort Helper");
    }

    public void sort(X[] xs, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter) {
        final long[] longs = getLongArray(xs, huskyCoder);
        preSort(xs, longs, 0, xs.length - 1);
        postSorter.accept(xs);
    }

    public void sort(X[] xs, HuskyCoder<X> huskyCoder) {
        sort(xs, huskyCoder, Arrays::sort);
    }

    private long[] getLongArray(X[] array, HuskyCoder<X> coder) {
        long[] longArray = new long[array.length];
        for (int i = 0; i < array.length; i++) longArray[i] = coder.huskyEncode(array[i]);
        return longArray;
    }

    final protected Helper<X> helper;

    static final HuskyCoder<String> UNICODE_CODER = HuskySortHelper.unicodeCoder;

    public Helper<X> getHelper() {
        return helper;
    }

    protected abstract void preSort(Object[] objects, long[] longs, int from, int to);

    protected static void swap(Object[] objects, long[] longs, int i, int j) {
        long temp1 = longs[i];
        longs[i] = longs[j];
        longs[j] = temp1;
        Object temp2 = objects[i];
        objects[i] = objects[j];
        objects[j] = temp2;
    }
}
