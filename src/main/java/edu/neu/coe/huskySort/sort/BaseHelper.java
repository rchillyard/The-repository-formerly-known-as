package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Utilities;

import java.util.Random;
import java.util.function.Function;

public class BaseHelper<X extends Comparable<X>> implements Helper<X> {

    /**
     * @return false
     */
    public boolean instrumented() {
        return false;
    }

    /**
     * Method to determine if one X value is less than another.
     *
     * @param v the candidate element.
     * @param w the comparand element.
     * @return true only if v is less than w.
     */
    public boolean less(final X v, final X w) {
        return v.compareTo(w) < 0;
    }

    /**
     * Compare elements i and j of xs within the subarray lo..hi
     *
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     * @return the result of comparing xs[i] to xs[j]
     */
    public int compare(final X[] xs, final int i, final int j) {
        // CONSIDER invoking the other compare signature
        return xs[i].compareTo(xs[j]);
    }

    /**
     * Compare v and w
     *
     * @param v the first X.
     * @param w the second X.
     * @return the result of comparing v and w.
     */
    @Override
    public int compare(final X v, final X w) {
        return v.compareTo(w);
    }

    /**
     * Swap the elements of array a at indices i and j.
     *
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     */
    public void swap(final X[] xs, final int i, final int j) {
        final X temp = xs[i];
        xs[i] = xs[j];
        xs[j] = temp;
    }

    /**
     * Method to perform a stable swap using half-exchanges,
     * i.e. between xs[i] and xs[j] such that xs[j] is moved to index i,
     * and xs[i] thru xs[j-1] are all moved up one.
     * This type of swap is used by insertion sort.
     *
     * @param xs the array of Xs.
     * @param i  the index of the destination of xs[j].
     * @param j  the index of the right-most element to be involved in the swap.
     */
    public void swapInto(final X[] xs, final int i, final int j) {
        if (j > i) {
            final X x = xs[j];
            System.arraycopy(xs, i, xs, i + 1, j - i);
            xs[i] = x;
        }
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

    /**
     * Method to determine if the given array (xs) is sorted.
     *
     * @param xs an array of Xs.
     * @return false as soon as an inversion is found; otherwise return true.
     */
    public boolean sorted(final X[] xs) {
        return Utilities.isSorted(xs);
    }

    /**
     * Method to count the total number of inversions in the given array (xs).
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
        if (n <= 0) throw new HelperException("Helper.random: not initialized");
        return Utilities.fillRandomArray(clazz, random, n, f);
    }

    /**
     * Method to post-process the array xs after sorting.
     * By default, this method does nothing.
     *
     * @param xs the array to be tested.
     */
    @Override
    public void postProcess(final X[] xs) {
    }

    @Override
    public String toString() {
        return "Helper for " + description + " with " + n + " elements";
    }

    public String getDescription() {
        return description;
    }

    /**
     * @param n the size to be managed.
     * @throws HelperException if n is inconsistent.
     */
    public void init(final int n) {
        if (this.n == 0 || this.n == n) this.n = n;
        else throw new HelperException("Helper: n is already set to a different value");
    }

    public int getN() {
        return n;
    }

    public void close() {
    }

    /**
     * Constructor for explicit random number generator.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param random      a random number generator.
     */
    public BaseHelper(final String description, final int n, final Random random) {
        this.n = n;
        this.description = description;
        this.random = random;
    }

    /**
     * Constructor for explicit seed.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator.
     */
    public BaseHelper(final String description, final int n, final long seed) {
        this(description, n, new Random(seed));
    }

    /**
     * Constructor to create a Helper with a random seed.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public BaseHelper(final String description, final int n) {
        this(description, n, System.currentTimeMillis());
    }

    /**
     * Constructor to create a Helper with a random seed and an n value of 0.
     *
     * @param description the description of this Helper (for humans).
     */
    public BaseHelper(final String description) {
        this(description, 0);
    }

    public static final String INSTRUMENT = "instrument";

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

    protected final String description;
    protected final Random random;
    protected int n;
}
