package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.*;

import java.util.Random;

import static edu.neu.coe.huskySort.util.Instrumenter.INVERSIONS;
import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * ComparisonSortHelper class for sorting methods with instrumentation of compares and swaps, and in addition, bounds checks.
 * This ComparisonSortHelper class may be used for analyzing sort methods but will run at slightly slower speeds than the super-class.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public final class InstrumentedComparisonSortHelper<X extends Comparable<X>> extends BaseComparisonSortHelper<X> implements Instrumented {

    final static LazyLogger logger = new LazyLogger(InstrumentedComparisonSortHelper.class);

    public static <Y extends Comparable<Y>> InstrumentedComparisonSortHelper<Y> getInstrumentedHelper(final ComparisonSortHelper<Y> helper, final InstrumentedComparisonSortHelper<Y> alternative) {
        return InstrumentedComparisonSortHelper.class.isAssignableFrom(helper.getClass()) ? (InstrumentedComparisonSortHelper<Y>) helper : alternative;
    }

    public boolean instrumented() {
        return true;
    }

    /**
     * Method to determine if one X value is less than another.
     *
     * @param v the candidate element.
     * @param w the comparand element.
     * @return true only if v is less than w.
     */
    public boolean less(final X v, final X w) {
        instrumenter.incrementCompares();
        return v.compareTo(w) < 0;
    }

    /**
     * Swap the elements of array "a" at indices i and j.
     *
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     */
    public void swap(final X[] xs, final int i, final int j) {
        if (i == j) return;
        instrumenter.incrementSwaps();
        final X v = xs[i];
        final X w = xs[j];
        instrumenter.incrementHits(4);
        if (instrumenter.isCountFixes()) updateFixes(xs, i, j, v, w);
        xs[i] = w;
        xs[j] = v;
    }

    /**
     * Method to perform a stable swap using half-exchanges,
     * i.e. between xs[i] and xs[j] such that xs[j] is moved to index i,
     * and xs[i] thru xs[j-1] are all moved up one.
     * This type of swap is used by insertion sort.
     *
     * TEST me
     *
     * @param xs the array of Xs.
     * @param i  the index of the destination of xs[j].
     * @param j  the index of the right-most element to be involved in the swap.
     */
    @Override
    public void swapInto(final X[] xs, final int i, final int j) {
        instrumenter.incrementSwaps(j - 1);
        if (instrumenter.isCountFixes())
            instrumenter.fixes += (j - i);
        instrumenter.incrementHits((j - i + 1) * 2);
        super.swapInto(xs, i, j);
    }

    /**
     * Method to perform a stable swap, but only if xs[i] is less than xs[i-1], i.e. out of order.
     *
     * @param xs the array of elements under consideration
     * @param i  the index of the lower element.
     * @param j  the index of the upper element.
     * @return true if there was an inversion (i.e. the order was wrong and had to be fixed).
     */
    @Override
    public boolean swapConditional(final X[] xs, final int i, final int j) {
        instrumenter.incrementCompares();
        final int cf = xs[i].compareTo(xs[j]);
        if (cf > 0)
            swap(xs, i, j);
        return cf > 0;
    }

    /**
     * Method to perform a stable swap, but only if xs[i] is less than xs[i-1], i.e. out of order.
     *
     * @param xs the array of elements under consideration
     * @param i  the index of the upper element.
     * @return true if there was an inversion (i.e. the order was wrong and had to be fixed).
     */
    @Override
    public boolean swapStableConditional(final X[] xs, final int i) {
        // CONSIDER invoke super-method
        final X v = xs[i];
        final X w = xs[i - 1];
        instrumenter.incrementHits(2);
        final boolean result = v.compareTo(w) < 0;
        instrumenter.incrementCompares();
        if (result) {
            xs[i] = w;
            xs[i - 1] = v;
            instrumenter.incrementSwaps();
            instrumenter.incrementHits(2);
            if (instrumenter.isCountFixes())
                instrumenter.fixes++;
        }
        return result;

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

    // NOTE: the following private methods are only for testing.

    /**
     * Compare elements of an array.
     * <p>
     * // NOTE same as supertype
     *
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     * @return the result of compare(xs[i], xs[j]).
     */
    public int compare(final X[] xs, final int i, final int j) {
        // CONSIDER using compareTo method if it improves performance.
        return compare(xs[i], xs[j]);
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
        instrumenter.incrementCompares();
        return v.compareTo(w);
    }

    /**
     * If instrumenting, increment the number of fixes by n.
     *
     * @param n the number of copies made.
     */
    @Override
    public void incrementFixes(final int n) {
        if (instrumenter.isCountFixes())
            instrumenter.fixes += n;
    }

    /**
     * If instrumenting, increment the number of copies by n.
     *
     * @param n the number of copies made.
     */
    @Override
    public void incrementCopies(final int n) {
        instrumenter.incrementCopies(n);
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
        return "Instrumenting helper for " + getDescription() + " with " + formatWhole(getN()) + " elements";
    }

    /**
     * Initialize this ComparisonSortHelper.
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
        logger.debug(() -> "Closing ComparisonSortHelper: " + getDescription() + " with statPack: " + getStatPack());
        super.close();
    }

    /**
     * Get the statistics pack for this Instrumented Helper.
     *
     * @return a StatPack.
     */
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
    public InstrumentedComparisonSortHelper(final String description, final int n, final Random random, final Config config) {
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
    public InstrumentedComparisonSortHelper(final String description, final int n, final Config config) {
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
    public InstrumentedComparisonSortHelper(final String description, final int n, final long seed, final Config config) {
        this(description, n, new Random(seed), config);
    }

    /**
     * Constructor to create a ComparisonSortHelper with a random seed and an n value of 0.
     * <p>
     * NOTE: this constructor is used only by unit tests
     *
     * @param description the description of this ComparisonSortHelper (for humans).
     */
    public InstrumentedComparisonSortHelper(final String description, final Config config) {
        this(description, 0, config);
    }

    private void updateFixes(final X[] xs, final int i, final int j, final X v, final X w) {
        final int sense = Integer.signum(v.compareTo(w));
        instrumenter.fixes += sense;
        for (int k = i + 1; k < j; k++) {
            final X x = xs[k];
            if (w.compareTo(x) < 0 && x.compareTo(v) < 0) instrumenter.fixes += 2 * sense;
        }
    }

    private final int cutoff;
    private int maxDepth = 0;

    @Override
    public Instrumenter getInstrumenter() {
        return instrumenter;
    }

    private final Instrumenter instrumenter;
}
