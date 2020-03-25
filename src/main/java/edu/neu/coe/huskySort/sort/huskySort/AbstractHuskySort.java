/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class AbstractHuskySort<X extends Comparable<X>> extends SortWithHelper<X> {

    private AbstractHuskySort(String name, HuskyHelper<X> helper) {
        super(helper);
        this.name = name;
        this.huskyHelper = helper;
    }

    public AbstractHuskySort(String name, int n, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, boolean instrumentation) {
        // CONSIDER doing this using a factory method (like is done for Helper).
        this(name, instrumentation ? new HuskyHelper<>(name, n, huskyCoder, postSorter) : new HuskyHelper<>(name, n, huskyCoder, postSorter));
    }

    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        huskyHelper.setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        huskyHelper.initLongArray(result);
        sort(result, 0, result.length);
        huskyHelper.getPostSorter().accept(result);
        return result;
    }

    @Override
    public X[] sort(X[] xs) {
        return sort(xs, huskyHelper.isMakeCopy());
    }

    static final HuskyCoder<String> UNICODE_CODER = HuskySortHelper.unicodeCoder;

    void swap(X[] objects, int i, int j) {
        huskyHelper.swap(objects, i, j);
    }

    // CONSIDER showing coder and postSorter (would need extra String for that).
    @Override
    public String toString() {
        return name;
    }

    protected final String name;

    @Override
    public HuskyHelper<X> getHelper() {
        return huskyHelper;
    }

    private final HuskyHelper<X> huskyHelper;
}
