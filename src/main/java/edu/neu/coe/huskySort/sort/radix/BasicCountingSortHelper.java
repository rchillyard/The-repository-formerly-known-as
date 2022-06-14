package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.util.BaseHelper;
import edu.neu.coe.huskySort.util.Instrumenter;

import java.util.Random;

/**
 * Concrete implementation of CountingSortHelper.
 * See also ComparisonSortHelper, which is very similar.
 * CONSIDER merging the two helpers further.
 * <p>
 * NOTE that this Helper is not affected in any way by the configuration.
 *
 * @param <X> the type of the "string."
 * @param <Y> the type of the "characters" that form the string, e.g. decimal digits or DNA bases.
 */
public class BasicCountingSortHelper<X extends StringComparable<X, Y>, Y extends Comparable<Y>> extends BaseHelper<X> implements CountingSortHelper<X, Y> {

    @Override
    public Instrumenter getInstrumenter() {
        return null;
    }

    @Override
    public String toString() {
        return "BasicCountingSortHelper for " + description + " with " + n + " elements";
    }

    /**
     * Default constructor for BasicCountingSortHelper.
     *
     * @param description the description of this helper.
     * @param n           the number of strings expected to be compared.
     * @param random      a source of random numbers.
     */
    public BasicCountingSortHelper(final String description, final int n, final Random random) {
        super(description, random, n);
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
}
