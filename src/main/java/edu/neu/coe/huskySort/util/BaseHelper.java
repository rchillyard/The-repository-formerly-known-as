package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.BaseComparisonSortHelper;

import java.util.Random;

public abstract class BaseHelper<X> implements Helper<X> {

    public static final String INSTRUMENT = "instrument";

    /**
     * Method to post-process the array xs after sorting.
     * By default, this method does nothing.
     *
     * @param xs the array to be tested.
     * @return true.
     */
    @Override
    public boolean postProcess(final X[] xs) {
        return true;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @param n the size to be managed.
     * @throws BaseComparisonSortHelper.HelperException if n is inconsistent.
     */
    public void init(final int n) {
        if (this.n == 0 || this.n == n) this.n = n;
        else
            throw new BaseComparisonSortHelper.HelperException("ComparisonSortHelper: n is already set to a different value");
    }

    /**
     * Get the current value of N.
     *
     * @return the value of N.
     */
    @Override
    public int getN() {
        return n;
    }

    /**
     * Close this ComparisonSortHelper, freeing up any resources used.
     */
    @Override
    public void close() {
    }

    public BaseHelper(final String description, final Random random, final int n) {
        this.description = description;
        this.random = random;
        this.n = n;
    }

    protected final String description;
    protected final Random random;
    protected int n;

    public static class HelperException extends RuntimeException {

        public HelperException(final String message) {
            super(message);
        }

        public HelperException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public HelperException(final Throwable cause) {
            super(cause);
        }

        public HelperException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
