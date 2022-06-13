package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.BaseComparisonSortHelper;
import edu.neu.coe.huskySort.util.Utilities;

import java.util.Random;
import java.util.function.Function;

/**
 * Concrete implementation of CountingSortHelper.
 * See also ComparisonSortHelper, which is very similar.
 * CONSIDER merging the two helpers further.
 *
 * @param <X> the type of the "string."
 * @param <Y> the type of the "characters" that form the string, e.g. decimal digits or DNA bases.
 */
public class BasicCountingSortHelper<X extends StringComparable<X, Y>, Y extends Comparable<Y>> implements CountingSortHelper<X, Y> {
    /**
     * @return true if this is an instrumented ComparisonSortHelper.
     */
    @Override
    public boolean instrumented() {
        return false;
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
        if (n <= 0) throw new BaseComparisonSortHelper.HelperException("ComparisonSortHelper.random: not initialized");
        return Utilities.fillRandomArray(clazz, random, n, f);
    }

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

    @Override
    public String toString() {
        return "ComparisonSortHelper for " + description + " with " + n + " elements";
    }

    public String getDescription() {
        return description;
    }

    /**
     * Initialize this ComparisonSortHelper with the size of the array to be managed.
     *
     * @param n the size to be managed.
     * @throws BaseComparisonSortHelper.HelperException if n is inconsistent.
     */
    @Override
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
     * Close this CountingSortHelper, freeing up any resources used.
     */
    @Override
    public void close() {
    }

    /**
     * Default constructor for BasicCountingSortHelper.
     *
     * @param description the description of this helper.
     * @param n           the number of strings expected to be compared.
     * @param random      a source of random numbers.
     */
    public BasicCountingSortHelper(final String description, final int n, final Random random) {
        this.description = description;
        this.n = n;
        this.random = random;
    }

    /**
     * Constructor for explicit seed.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator.
     */
    public BasicCountingSortHelper(final String description, final int n, final long seed) {
        this(description, n, new Random(seed));
    }

    /**
     * Constructor to create a ComparisonSortHelper with a random seed.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public BasicCountingSortHelper(final String description, final int n) {
        this(description, n, System.currentTimeMillis());
    }

    /**
     * Constructor to create a ComparisonSortHelper with a random seed and an n value of 0.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     */
    public BasicCountingSortHelper(final String description) {
        this(description, 0);
    }

    protected final String description;
    protected int n;
    private final Random random;
}
