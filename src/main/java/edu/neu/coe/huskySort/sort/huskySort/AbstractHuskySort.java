/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.HelperFactory;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.function.Consumer;

/**
 * Base class for HuskySort implementations.
 *
 * @param <X> the underlying type to be sorted.
 */
public abstract class AbstractHuskySort<X extends Comparable<X>> extends SortWithHelper<X> {

    /**
     * Init HuskyHelper and initialize the long array.
     * Make copy if appropriate.
     *
     * @param xs       the original array to be sorted.
     * @param makeCopy true if we need to work on a copy of the array.
     * @return the xs or a copy.
     */
    @Override
    public X[] preSort(X[] xs, boolean makeCopy) {
        // NOTE: Prepare for first pass where we code to longs and sort according to those.
        X[] result = super.preSort(xs, makeCopy);
        huskyHelper.doCoding(result);
        return result;
    }

    /**
     * This post-sort process is where HuskySort performs the second sorting pass, if necessary.
     *
     * @param xs the array sorted by the first pass.
     * @return either the array passed in or the result of invoking the post-sorter on that array.
     */
    @Override
    public X[] postSort(X[] xs) {
        if (huskyHelper.getCoding().perfect)
            return xs;

        // NOTE: Second pass to fix any remaining inversions.
        huskyHelper.getPostSorter().accept(xs);
        return xs;
    }

    /**
     * Sort array xs, making a copy if stipulated by huskyHelper.
     *
     * @param xs sort the array xs, returning the sorted result, leaving xs unchanged.
     * @return the sorted version of xs (or its copy).
     */
    @Override
    public X[] sort(X[] xs) {
        return sort(xs, huskyHelper.isMakeCopy());
    }

    /**
     * Method to get the Helper, but as a HuskyHelper.
     *
     * @return a HuskyHelper.
     */
    @Override
    public HuskyHelper<X> getHelper() {
        return huskyHelper;
    }

    // CONSIDER showing coder and postSorter (would need extra String for that).
    @Override
    public String toString() {
        return name;
    }

    /**
     * Method to do a swap for HuskySort.
     * Delegate to huskyHelper.
     *
     * @param xs the array being sorted.
     * @param i  the first index.
     * @param j  the second index.
     */
    protected void swap(X[] xs, int i, int j) {
        huskyHelper.swap(xs, i, j);
    }

    /**
     * Constructor for AbstractHuskySort
     *
     * @param name       name of sorter.
     * @param n          number of elements expected.
     * @param huskyCoder coder.
     * @param postSorter post-sorter.
     * @param config     configuration.
     */
    protected AbstractHuskySort(String name, int n, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config) {
        this(name, createHelper(name, n, huskyCoder, postSorter, config.isInstrumented(), config));
        closeHelper = true;
    }

    static final HuskyCoder<String> UNICODE_CODER = HuskyCoderFactory.unicodeCoder;

    protected final static LazyLogger logger = new LazyLogger(AbstractHuskySort.class);

    /**
     * NOTE: callers of this method should consider arranging for the helper to be closed on close of the sorter.
     */
    private static <Y extends Comparable<Y>> HuskyHelper<Y> createHelper(String name, int n, HuskyCoder<Y> huskyCoder, Consumer<Y[]> postSorter, boolean instrumentation, Config config) {
        return instrumentation ? new HuskyHelper<>(HelperFactory.create("Husky Delegate Helper", n, config), huskyCoder, postSorter, false) : new HuskyHelper<>(name, n, huskyCoder, postSorter);
    }

    protected final HuskyHelper<X> huskyHelper;
    protected final String name;

    private AbstractHuskySort(String name, HuskyHelper<X> helper) {
        super(helper);
        this.name = name;
        this.huskyHelper = helper;
    }

}
