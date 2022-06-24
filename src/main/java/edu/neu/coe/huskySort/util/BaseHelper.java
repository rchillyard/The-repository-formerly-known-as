package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.BaseComparisonSortHelper;

import java.util.Random;
import java.util.function.Function;

public abstract class BaseHelper<X extends Comparable<X>> implements Helper<X> {

    public static final String INSTRUMENT = "instrument";

    /**
     * Method to register caring about whether a sort is successful.
     *
     * @param checkSorted true if you want to check the success of a sort.
     */
    public void setCheckSorted(final boolean checkSorted) {
        this.checkSorted = checkSorted;
    }

    /**
     * Method to post-process the array xs after sorting.
     * By default, this method does nothing.
     * If checkSorted is set then return the result of calling sorted(xs).
     *
     * @param xs the array to be tested.
     * @return whether sorted (or don't care).
     */
    @Override
    public boolean postProcess(final X[] xs) {
        return !checkSorted || sorted(xs);
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

    /**
     * Method to determine if the given array (xs) is sorted.
     *
     * @param xs an array of Xs.
     * @return false as soon as an inversion is found; otherwise return true.
     */
    public boolean sorted(final X[] xs) {
        for (int i = 1; i < xs.length; i++) if (xs[i - 1].compareTo(xs[i]) > 0) return false;
        return true;
    }

    /**
     * Method to count the total number of inversions in the given array (xs).
     * <p>
     * TODO this is identical with BasicCountingSortHelper: merge them.
     *
     * @param xs an array of Xs.
     * @return the number of inversions.
     */
    public int inversions(final X[] xs) {
        int result = 0;
        for (int i = 0; i < xs.length; i++)
            for (int j = i + 1; j < xs.length; j++)
                if (xs[i].compareTo(xs[j]) > 0) result++;
        return result;
    }

    public X[] random(final Class<X> clazz, final Function<Random, X> f) {
        if (getN() <= 0) throw new HelperException("ComparisonSortHelper.random: not initialized");
        return Utilities.fillRandomArray(clazz, random, getN(), f);
    }

    /**
     * Copy the element at source[j] into target[i]
     *
     * @param source the source array.
     * @param i      the target index.
     * @param target the target array.
     * @param j      the source index.
     */
    @Override
    public void copy(final X[] source, final int i, final X[] target, final int j) {
        target[j] = source[i];
    }

    public BaseHelper(final String description, final Random random, final int n) {
        this.description = description;
        this.random = random;
        this.n = n;
    }

    protected final String description;
    protected final Random random;
    protected int n;

    private boolean checkSorted = false;

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
