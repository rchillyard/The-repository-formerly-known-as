package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Config;

/**
 * Base class for a comparison-sorter that uses a ComparisonSortHelper.
 *
 * @param <X> the underlying type to be sorted.
 */
public abstract class SortWithHelper<X extends Comparable<X>> implements Sort<X>, Sorter<X> {
    /**
     * Perform the entire process of sorting the given array, including all pre- and post-processing.
     *
     * @param xs an array of Xs which will be mutated.
     * @return a boolean which indicates the success of the sort.
     */

    public boolean sortArray(final X[] xs) {
        final X[] xes = helper.preProcess(xs);
        mutatingSort(xes);
        return helper.postProcess(xes);
    }

    /**
     * Get the ComparisonSortHelper associated with this Sort.
     *
     * @return the ComparisonSortHelper
     */
    public ComparisonSortHelper<X> getHelper() {
        return helper;
    }

    /**
     * Perform initializing step for this Sort.
     *
     * @param n the number of elements to be sorted.
     */
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
     * @return the result of calling helper.postProcess(xs).
     */
    public boolean postProcess(final X[] xs) {
        return helper.postProcess(xs);
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
    public SortWithHelper(final ComparisonSortHelper<X> helper) {
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

    private final ComparisonSortHelper<X> helper;
    protected boolean closeHelper = false;

}
