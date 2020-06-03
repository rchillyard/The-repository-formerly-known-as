package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Config;

public abstract class SortWithHelper<X extends Comparable<X>> implements Sort<X> {


    public SortWithHelper(Helper<X> helper) {
        this.helper = helper;
    }

    public SortWithHelper(String description, int N, boolean instrumenting, Config config) {
        this(HelperFactory.create(description, N, instrumenting, config));
        closeHelper = true;
    }

    /**
     * Get the Helper associated with this Sort.
     *
     * @return the Helper
     */
    public Helper<X> getHelper() {
        return helper;
    }

    /**
     * Perform initializing step for this Sort.
     *
     * @param n the number of elements to be sorted.
     */
    @Override
    public void init(int n) {
        getHelper().init(n);
    }

    /**
     * Perform pre-processing step for this Sort.
     *
     * @param xs the elements to be pre-processed.
     */
    @Override
    public X[] preProcess(X[] xs) {
        return helper.preProcess(xs);
    }

    /**
     * Method to post-process an array after sorting.
     * <p>
     * In this implementation, we delegate the post-processing to the helper.
     *
     * @param xs the array to be post-processed.
     */
    public void postProcess(X[] xs) {
        helper.postProcess(xs);
    }

    @Override
    public String toString() {
        return helper.toString();
    }

    public void close() {
        if (closeHelper) helper.close();
    }

    public static boolean getInstrumented(Config config) {
        return config.getBoolean("helper", "instrument");
    }

    private final Helper<X> helper;
    private boolean closeHelper = false;

}