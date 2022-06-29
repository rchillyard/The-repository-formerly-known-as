package edu.neu.coe.huskySort.sort;

import java.util.Random;

/**
 * Concrete implementation of ComparisonSortHelper.
 * <p>
 * NOTE that this Helper is not affected in any way by the configuration.
 *
 * @param <X> the type of elements to be compared (must be Comparable).
 */
public class ComparableSortHelper<X extends Comparable<X>> extends AbstractComparableSortHelper<X> {

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

    @Override
    public boolean inverted(final X v, final X w) {
        return v.compareTo(w) > 0;
    }

    /**
     * Compare elements i and j of xs within the subarray lo...hi
     * // NOTE same as supertype
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
    public int compare(final X v, final X w) {
        return v.compareTo(w);
    }

    @Override
    public String toString() {
        return "ComparisonSortHelper for " + getDescription() + " with " + getN() + " elements";
    }

    /**
     * Method to determine if v and w are inverted.
     * NOTE: This MUST be a non-instrumenting comparison.
     *
     * @param v the first (left) value of X.
     * @param w the second (right) value of X.
     * @return v > w.
     */
    public boolean invertedPure(final X v, final X w) {
        return v.compareTo(w) > 0;
    }

    /**
     * Constructor for explicit random number generator.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param random      a random number generator.
     */
    public ComparableSortHelper(final String description, final int n, final Random random) {
        super(description, random, n);
    }

    /**
     * Constructor for explicit seed.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator.
     */
    public ComparableSortHelper(final String description, final int n, final long seed) {
        this(description, n, new Random(seed));
    }

    /**
     * Constructor to create a ComparisonSortHelper with a random seed.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public ComparableSortHelper(final String description, final int n) {
        this(description, n, System.currentTimeMillis());
    }

    /**
     * Constructor to create a ComparisonSortHelper with a random seed and an n value of 0.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     */
    public ComparableSortHelper(final String description) {
        this(description, 0);
    }

}
