package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.util.BaseHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Instrumenter;

import java.util.Random;

/**
 * Concrete implementation of CountingSortHelper.
 * See also ComparisonSortHelper, which is very similar.
 * CONSIDER merging the two helpers further.
 * <p>
 * NOTE that the cutoff is the one aspect of this Helper which the configuration does affect. That is
 * deliberate: a timed run is uninstrumented, so if only InstrumentedCountingSortHelper read the
 * configured cutoff, every timed benchmark would silently use the default while the instrumented
 * counts moved. See CutoffIsHonouredOnBothPathsTest.
 *
 * @param <X> the type of the "string."
 * @param <Y> the type of the "characters" that form the string, e.g. decimal digits or DNA bases.
 */
public class BasicCountingSortHelper<X extends StringComparable<X, Y>, Y extends Comparable<Y>> extends BaseHelper<X> implements CountingSortHelper<X, Y> {

    public Instrumenter getInstrumenter() {
        return null;
    }

    /**
     * Get the configured cutoff value, so that a timed (and therefore uninstrumented) run uses the
     * same value as an instrumented one.
     *
     * @return the configured cutoff if there is one, otherwise the interface default.
     */
    @Override
    public int getCutoff() {
        // NOTE that a cutoff value of 0 or less would result in an infinite recursion, so it means "unset."
        return (cutoff >= 1) ? cutoff : CountingSortHelper.super.getCutoff();
    }

    @Override
    public void incrementCopies(final int n) {
        final Instrumenter instrumenter = getInstrumenter();
        if (instrumenter != null) {
            instrumenter.incrementCopies(n);
        }
    }

    /**
     * Method to determine if the given array (xs) is sorted.
     *
     * @param xs an array of Xs.
     * @return false as soon as an inversion is found; otherwise return true.
     */
    @Override
    public boolean sorted(final X[] xs) {
        return isStringSorted(xs);
    }

    /**
     * Method to determine if x1 and x2 are inverted.
     * <p>
     * NOTE: This MUST be a non-instrumenting comparison.
     * <p>
     * CONSIDER defining this in terms of a non-overridable compare function.
     *
     * @param x1 the first (left) value of X.
     * @param x2 the second (right) value of X.
     * @return x1 > x2.
     */
    public boolean invertedPure(final X x1, final X x2) {
        return x1.inverted(x2);
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

    /**
     * Check that the given array is sorted.
     *
     * @param xs the array to be checked.
     * @return false as soon as an inversion is found; otherwise return true.
     */
    private boolean isStringSorted(final X[] xs) {
        for (int i = 1; i < xs.length; i++) if (xs[i - 1].compareTo(xs[i]) > 0) return false;
        return true;
    }

    @Override
    public String toString() {
        return "BasicCountingSortHelper for " + description + " with " + n + " elements";
    }

    /**
     * Constructor which reads the cutoff from the given configuration.
     *
     * @param description the description of this helper.
     * @param n           the number of strings expected to be compared.
     * @param random      a source of random numbers.
     * @param config      the configuration, from which the cutoff is read; null means "take the default."
     */
    public BasicCountingSortHelper(final String description, final int n, final Random random, final Config config) {
        super(description, random, n);
        this.cutoff = config == null ? 0 : config.getInt("helper", "cutoff", 0);
    }

    /**
     * Constructor which reads the cutoff from the given configuration.
     *
     * @param description the description of this helper.
     * @param n           the number of strings expected to be compared.
     * @param config      the configuration, from which the cutoff is read.
     */
    public BasicCountingSortHelper(final String description, final int n, final Config config) {
        this(description, n, new Random(), config);
    }

    /**
     * Default constructor for BasicCountingSortHelper. The cutoff takes its default value, because
     * there is no configuration here to read one from.
     *
     * @param description the description of this helper.
     * @param n           the number of strings expected to be compared.
     * @param random      a source of random numbers.
     */
    public BasicCountingSortHelper(final String description, final int n, final Random random) {
        this(description, n, random, null);
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

    private final int cutoff;
}
