package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.BaseComparisonSortHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.StatPack;

import java.util.Random;

import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * ComparisonSortHelper class for sorting methods with instrumentation of compares and swaps, and in addition, bounds checks.
 * This ComparisonSortHelper class may be used for analyzing sort methods but will run at slightly slower speeds than the super-class.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public final class InstrumentedCountingSortHelper<X extends StringComparable<X, Y>, Y extends Comparable<Y>> extends BasicCountingSortHelper<X, Y> {

    final static LazyLogger logger = new LazyLogger(InstrumentedCountingSortHelper.class);

    public static <Q extends StringComparable<Q, R>, R extends Comparable<R>> InstrumentedCountingSortHelper<Q, R> getInstrumentedCountingSortHelper(final CountingSortHelper<Q, R> helper, final InstrumentedCountingSortHelper<Q, R> alternative) {
        return InstrumentedCountingSortHelper.class.isAssignableFrom(helper.getClass()) ? (InstrumentedCountingSortHelper<Q, R>) helper : alternative;
    }

    public boolean instrumented() {
        return true;
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
        if (countCopies)
            copies++;
        super.copy(source, i, target, j);
    }


    /**
     * If instrumenting, increment the number of copies by n.
     *
     * @param n the number of copies made.
     */
    public void incrementCopies(final int n) {
        if (countCopies) copies += n;
    }

    // NOTE: the following private methods are only for testing.

    /**
     * If instrumenting, increment the number of fixes by n.
     *
     * @param n the number of copies made.
     */
    public void incrementFixes(final int n) {
        if (countFixes) fixes += n;
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
        if (countCompares)
            compares++;
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
        compares = 0;
        swaps = 0;
        copies = 0;
        fixes = 0;
        // NOTE: it's an error to reset the StatPack if we've been here before
        if (n == this.n && statPack != null) return;
        super.init(n);
        statPack = new StatPack(n, COMPARES, SWAPS, COPIES, INVERSIONS, FIXES, INTERIM_INVERSIONS);
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
        if (countInversions-- > 0) {
            if (statPack != null) statPack.add(INVERSIONS, inversions(result));
            else throw new RuntimeException("InstrumentedComparisonSortHelper.postProcess: no StatPack");
        }
        return result;
    }

    /**
     * Method to post-process the array xs after sorting.
     * By default, this method checks that an array is sorted.
     * <p>
     * TODO this is identical with InstrumentedComparisonSortHelper: merge them.
     *
     * @param xs the array to be tested.
     *                                                   TODO log the message
     *                                                   TODO show the number of inversions
     * @return the result of invoking super.postProcess(xs).
     */
    @Override
    public boolean postProcess(final X[] xs) {
        final boolean result = super.postProcess(xs);
        if (!sorted(xs)) throw new BaseComparisonSortHelper.HelperException("Array is not sorted");
        if (statPack == null) throw new RuntimeException("InstrumentedComparisonSortHelper.postProcess: no StatPack");
        if (countCompares)
            statPack.add(COMPARES, compares);
        if (countSwaps)
            statPack.add(SWAPS, swaps);
        if (countCopies)
            statPack.add(COPIES, copies);
        if (countFixes)
            statPack.add(FIXES, fixes);
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
        logger.debug(() -> "Closing CountingSortHelper: " + description + " with statPack: " + statPack);
        super.close();
    }

    public StatPack getStatPack() {
        return statPack;
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
        this.countCopies = config.getBoolean(INSTRUMENTING, COPIES);
        this.countSwaps = config.getBoolean(INSTRUMENTING, SWAPS);
        this.countCompares = config.getBoolean(INSTRUMENTING, COMPARES);
        this.countInversions = config.getInt(INSTRUMENTING, INVERSIONS, 0);
        this.countFixes = config.getBoolean(INSTRUMENTING, FIXES);
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

    public static final String SWAPS = "swaps";
    public static final String COMPARES = "compares";
    public static final String COPIES = "copies";
    public static final String INVERSIONS = "inversions";
    public static final String INTERIM_INVERSIONS = "interiminversions";
    public static final String FIXES = "fixes";
    public static final String INSTRUMENTING = "instrumenting";

    // NOTE: the following private methods are only for testing.

    private int getCompares() {
        return compares;
    }

    private int getSwaps() {
        return swaps;
    }

    private int getFixes() {
        return fixes;
    }

    private final int cutoff;
    private final boolean countCopies;
    private final boolean countSwaps;
    private final boolean countCompares;
    private final boolean countFixes;
    private StatPack statPack;
    private int compares = 0;
    private int swaps = 0;
    private int copies = 0;
    private int fixes = 0;
    private int countInversions;
    private int maxDepth = 0;
}
