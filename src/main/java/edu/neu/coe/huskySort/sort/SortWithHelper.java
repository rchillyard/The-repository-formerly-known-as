package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Config;

/**
 * Base class for a sorter that uses a Helper.
 *
 * @param <X> the underlying type to be sorted.
 */
public abstract class SortWithHelper<X extends Comparable<X>> implements Sort<X> {

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
    public void init(final int n) {
        getHelper().init(n);
    }

    /**
     * Perform pre-processing step for this Sort.
     *
     * @param xs the elements to be pre-processed.
     */
    @Override
    public X[] preProcess(final X[] xs) {
        return helper.preProcess(xs);
    }

    /**
     * Method to post-process an array after sorting.
     * <p>
     * In this implementation, we delegate the post-processing to the helper.
     *
     * @param xs the array to be post-processed.
     */
    public void postProcess(final X[] xs) {
        helper.postProcess(xs);
    }

    /**
     * Close this sorter.
     */
    public void close() {
        if (closeHelper) helper.close();
    }

    @Override
    public String toString() {
        return helper.toString();
    }

    /**
     * Constructor.
     *
     * @param helper the helper to use.
     */
    public SortWithHelper(final Helper<X> helper) {
        this.helper = helper;
    }

    /**
     * Constructor.
     *
     * @param description the description.
     * @param N           the number of elements expected.
     * @param config      the configuration.
     */
    public SortWithHelper(final String description, final int N, final Config config) {
        this(HelperFactory.create(description, N, config));
        closeHelper = true;
    }

    private final Helper<X> helper;
    protected boolean closeHelper = false;

}
