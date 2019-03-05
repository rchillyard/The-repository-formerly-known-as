/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class AbstractHuskySort<X extends Comparable<X>> implements Sort<X> {

    public AbstractHuskySort(String name, int n, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter) {
        helper = new HuskyHelper<>(name, n, huskyCoder, postSorter);
    }

    public AbstractHuskySort(int n, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter) {
        this("HuskySort Helper", n, huskyCoder, postSorter);
    }

    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        helper.setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        helper.initLongArray(result);
        sort(result, 0, result.length);
        getHelper().getPostSorter().accept(result);
        return result;
    }

    @Override
    public X[] sort(X[] xs) {
        return sort(xs, helper.isMakeCopy());
    }

    final protected HuskyHelper<X> helper;

    static final HuskyCoder<String> UNICODE_CODER = HuskySortHelper.unicodeCoder;

    public HuskyHelper<X> getHelper() {
        return helper;
    }

    protected void swap(X[] objects, int i, int j) {
        helper.swap(objects, i, j);
    }



//    protected abstract void preSort(Object[] objects, long[] longs, int from, int to);

//    protected static void swap(Object[] objects, long[] longs, int i, int j) {
//        long temp1 = longs[i];
//        longs[i] = longs[j];
//        longs[j] = temp1;
//        Object temp2 = objects[i];
//        objects[i] = objects[j];
//        objects[j] = temp2;
//    }
}
