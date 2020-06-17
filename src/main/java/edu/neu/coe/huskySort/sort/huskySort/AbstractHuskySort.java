/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.HelperFactory;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class AbstractHuskySort<X extends Comparable<X>> extends SortWithHelper<X> {

    private AbstractHuskySort(String name, HuskyHelper<X> helper) {
        super(helper);
        this.name = name;
        this.huskyHelper = helper;
    }

    public AbstractHuskySort(String name, int n, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config) {
        this(name, createHelper(name, n, huskyCoder, postSorter, config.isInstrumented(), config));
        closeHelper = true;
    }

    public X[] sort(X[] xs, boolean makeCopy) {
        huskyHelper.init(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        huskyHelper.initLongArray(result);
        sort(result, 0, result.length);
        huskyHelper.getPostSorter().accept(result);
        return result;
    }

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

    protected final static LazyLogger logger = new LazyLogger(AbstractHuskySort.class);

    protected final String name;

    /**
     * Method to get the Helper, but as a HuskyHelper.
     *
     * @return a HuskyHelper.
     */
    public HuskyHelper<X> getHelper() {
        return huskyHelper;
    }

    /**
     * NOTE: callers of this method should consider arranging for the helper to be closed on close of the sorter.
     */
    private static <Y extends Comparable<Y>> HuskyHelper<Y> createHelper(String name, int n, HuskyCoder<Y> huskyCoder, Consumer<Y[]> postSorter, boolean instrumentation, Config config) {
        return instrumentation ? new HuskyHelper<>(HelperFactory.create("Husky Delegate Helper", n, config), huskyCoder, postSorter, false) : new HuskyHelper<>(name, n, huskyCoder, postSorter);
    }

    protected final HuskyHelper<X> huskyHelper;
}
