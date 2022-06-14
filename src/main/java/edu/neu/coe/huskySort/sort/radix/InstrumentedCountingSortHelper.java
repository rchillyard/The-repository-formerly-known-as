package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.BaseComparisonSortHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Instrumenter;
import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.StatPack;

import java.util.Random;

import static edu.neu.coe.huskySort.util.Instrumenter.INVERSIONS;
import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * ComparisonSortHelper class for sorting methods with instrumentation of compares and swaps, and in addition, bounds checks.
 * This ComparisonSortHelper class may be used for analyzing sort methods but will run at slightly slower speeds than the super-class.
 *
 * @param <X> the underlying type (must be StringComparable).
 * @param <Y> the underlying type (must be Comparable).
 */
public final class InstrumentedCountingSortHelper<X extends StringComparable<X, Y>, Y extends Comparable<Y>> extends BasicCountingSortHelper<X, Y> {

    final static LazyLogger logger = new LazyLogger(InstrumentedCountingSortHelper.class);

    public static <Q extends StringComparable<Q, R>, R extends Comparable<R>> InstrumentedCountingSortHelper<Q, R> getInstrumentedCountingSortHelper(final CountingSortHelper<Q, R> helper, final InstrumentedCountingSortHelper<Q, R> alternative) {
        return InstrumentedCountingSortHelper.class.isAssignableFrom(helper.getClass()) ? (InstrumentedCountingSortHelper<Q, R>) helper : alternative;
    }

    /**
     * Method to swap two elements.
     * Even though this is a helper for counting sorts, we typically have a cutoff to insertion sort for,
     * e.g. MSD Radix sort.
     *
     * @param xs an array of Xs.
     * @param j  one element to be swapped.
     * @param i  the other element to be swapped.
     */
    @Override
    public void swap(final X[] xs, final int j, final int i) {
        instrumenter.incrementSwaps();
        instrumenter.incrementHits(4);
        super.swap(xs, j, i);
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
        instrumenter.incrementCopies();
        instrumenter.incrementHits(2);
        target[j] = source[i];
    }

    /**
     * Compare the elements i and j of array xs (at position d).
     *
     * @param xs an array of X elements.
     * @param i  the index of the left-hand element.
     * @param j  the index of the right-hand element.
     * @param d  the position of interest.
     * @return -1 if xs[i] is less than xs[j]; 1 if xs[i] is greater than xs[j]; otherwise 0.
     */
    @Override
    public int compare(final X[] xs, final int i, final int j, final int d) {
        instrumenter.incrementCompares();
        instrumenter.incrementHits(2);
        return super.compare(xs[i], xs[j], d);
    }

    /**
     * Compare value v with value w.
     *
     * @param v the first value.
     * @param w the second value.
     * @param d the position of interest.
     * @return -1 if v is less than w; 1 if v is greater than w; otherwise 0.
     */
    @Override
    public int compare(final X v, final X w, final int d) {
        instrumenter.incrementCompares();
        return super.compare(v, w, d);
    }

    /**
     * Compare values v and w and return true if v is less than w.
     *
     * @param v the first value.
     * @param w the second value.
     * @param d the position of interest.
     * @return true if v is less than w.
     */
    @Override
    public boolean less(final X v, final X w, final int d) {
        instrumenter.incrementCompares();
        return super.less(v, w, d);
    }

    /**
     * Get the configured cutoff value.
     *
     * @return a value for cutoff.
     */
    @Override
    public int getCutoff() {
        // NOTE that a cutoff value of 0 or less will result in an infinite recursion for any recursive method that uses it.
        return (cutoff >= 1) ? cutoff : super.getCutoff();
    }

    @Override
    public String toString() {
        return "Instrumenting counting sort helper for " + getDescription() + " with " + formatWhole(getN()) + " elements";
    }

    /**
     * Initialize this InstrumentedCountingSortHelper.
     * <p>
     * TODO this is identical with BaseComparisonSortHelper: merge them.
     *
     * @param n the size to be managed.
     */
    public void init(final int n) {
        instrumenter.init(n);
        // NOTE: it's an error to reset the StatPack if we've been here before
        if (n == this.getN() && getStatPack() != null) return;
        super.init(n);
    }

    /**
     * Method to do any required preProcessing.
     *
     * @param xs the array to be sorted.
     * @return the array after any pre-processing.
     */
    @Override
    public X[] preProcess(final X[] xs) {
        final X[] result = super.preProcess(xs);
        // NOTE: because counting inversions is so slow, we only do if for a (configured) number of samples.
        if (instrumenter.countInversions-- > 0) {
            if (getStatPack() != null) getStatPack().add(INVERSIONS, inversions(result));
            else throw new RuntimeException("InstrumentedComparisonSortHelper.postProcess: no StatPack");
        }
        return result;
    }

    /**
     * Method to post-process the array xs after sorting.
     * By default, this method checks that an array is sorted.
     * <p>
     * TODO this is identical with InstrumentedComparisonSortHelper: merge them.
     * <p>
     *                                                   TODO log the message
     *                                                   TODO show the number of inversions
     *
     * @param xs the array to be tested.
     * @return the result of invoking super.postProcess(xs).
     */
    @Override
    public boolean postProcess(final X[] xs) {
        final boolean result = super.postProcess(xs);
        if (!sorted(xs)) throw new BaseComparisonSortHelper.HelperException("Array is not sorted");
        instrumenter.updateStats();
        return result;
    }

    @Override
    public void registerDepth(final int depth) {
        if (depth > maxDepth) maxDepth = depth;
    }

    @Override
    public int maxDepth() {
        return maxDepth;
    }

    @Override
    public void close() {
        logger.debug(() -> "Closing CountingSortHelper: " + description + " with statPack: " + getStatPack());
        super.close();
    }

    public StatPack getStatPack() {
        return instrumenter.getStatPack();
    }

    /**
     * Constructor for explicit random number generator.
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param random      a random number generator.
     * @param config      the configuration (note that the seed value is ignored).
     */
    public InstrumentedCountingSortHelper(final String description, final int n, final Random random, final Config config) {
        super(description, n, random);
        this.instrumenter = new Instrumenter(n, config);
        this.cutoff = config.getInt("helper", "cutoff", 0);
    }

    /**
     * Constructor to create a ComparisonSortHelper
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param config      The configuration.
     */
    public InstrumentedCountingSortHelper(final String description, final int n, final Config config) {
        this(description, n, config.getLong("helper", "seed", System.currentTimeMillis()), config);
    }

    /**
     * Constructor to create a ComparisonSortHelper
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator.
     * @param config      the configuration.
     */
    public InstrumentedCountingSortHelper(final String description, final int n, final long seed, final Config config) {
        this(description, n, new Random(seed), config);
    }

    /**
     * Constructor to create a ComparisonSortHelper with a random seed and an n value of 0.
     * <p>
     * NOTE: this constructor is used only by unit tests
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     */
    public InstrumentedCountingSortHelper(final String description, final Config config) {
        this(description, 0, config);
    }

    private final int cutoff;
    private int maxDepth = 0;

    @Override
    public Instrumenter getInstrumenter() {
        return instrumenter;
    }

    private final Instrumenter instrumenter;
}
