package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.StatPack;

import java.util.Random;

import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * Helper class for sorting methods with instrumentation of compares and swaps, and in addition, bounds checks.
 * This Helper class may be used for analyzing sort methods but will run at slightly slower speeds than the super-class.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public final class InstrumentedHelper<X extends Comparable<X>> extends BaseHelper<X> {

    final static LazyLogger logger = new LazyLogger(InstrumentedHelper.class);

    public static <Y extends Comparable<Y>> InstrumentedHelper<Y> getInstrumentedHelper(final Helper<Y> helper, final InstrumentedHelper<Y> alternative) {
        return InstrumentedHelper.class.isAssignableFrom(helper.getClass()) ? (InstrumentedHelper<Y>) helper : alternative;
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
        if (countCompares)
            compares++;
        return v.compareTo(w) < 0;
    }

    /**
     * Swap the elements of array a at indices i and j.
     *
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     */
    public void swap(final X[] xs, final int i, final int j) {
        if (i == j) return;
        if (countSwaps)
            swaps++;
        final X v = xs[i];
        final X w = xs[j];
        if (countFixes) {
            final int sense = Integer.signum(v.compareTo(w));
            fixes += sense;
            for (int k = i + 1; k < j; k++) {
                final X x = xs[k];
                if (w.compareTo(x) < 0 && x.compareTo(v) < 0) fixes += 2 * sense;
            }
        }
        xs[i] = w;
        xs[j] = v;
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
    @Override
    public void swapInto(final X[] xs, final int i, final int j) {
        if (countSwaps)
            swaps += (j - i);
        if (countFixes)
            fixes += (j - i);
        super.swapInto(xs, i, j);
    }

    /**
     * Method to perform a stable swap, but only if xs[i] is less than xs[i-1], i.e. out of order.
     *
     * @param xs the array of elements under consideration
     * @param i  the index of the lower element.
     * @param j  the index of the upper element.
     * @return true if there was an inversion (i.e. the order was wrong and had to be be fixed).
     */
    @Override
    public boolean swapConditional(final X[] xs, final int i, final int j) {
        if (countCompares)
            compares++;
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
     * @return true if there was an inversion (i.e. the order was wrong and had to be be fixed).
     */
    @Override
    public boolean swapStableConditional(final X[] xs, final int i) {
        // CONSIDER invoke super-method
        final X v = xs[i];
        final X w = xs[i - 1];
        final boolean result = v.compareTo(w) < 0;
        if (countCompares)
            compares++;
        if (result) {
            xs[i] = w;
            xs[i - 1] = v;
            if (countSwaps)
                swaps++;
            if (countFixes)
                fixes++;
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
        if (countCopies)
            copies++;
        target[j] = source[i];
    }

    /**
     * If instrumenting, increment the number of copies by n.
     *
     * @param n the number of copies made.
     */
    @Override
    public void incrementCopies(final int n) {
        if (countCopies) copies += n;
    }

    // NOTE: the following private methods are only for testing.

    /**
     * If instrumenting, increment the number of fixes by n.
     *
     * @param n the number of copies made.
     */
    @Override
    public void incrementFixes(final int n) {
        if (countFixes) fixes += n;
    }

    /**
     * Compare elements of an array.
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
        if (countCompares)
            compares++;
        return v.compareTo(w);
    }

    /**
     * Get the configured cutoff value.
     *
     * @return a value for cutoff.
     */
    @Override
    public int cutoff() {
        // NOTE that a cutoff value of 0 or less will result in an infinite recursion for any recursive method that uses it.
        return (cutoff >= 1) ? cutoff : super.cutoff();
    }

    @Override
    public String toString() {
        return "Instrumenting helper for " + description + " with " + formatWhole(n) + " elements";
    }

    /**
     * Initialize this Helper.
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
            else throw new RuntimeException("InstrumentedHelper.postProcess: no StatPack");
        }
        return result;
    }

    /**
     * Method to post-process the array xs after sorting.
     * By default, this method checks that an array is sorted.
     *
     * @param xs the array to be tested.
     *           TODO log the message
     *           TODO show the number of inversions
     */
    @Override
    public void postProcess(final X[] xs) {
        super.postProcess(xs);
        if (!sorted(xs)) throw new BaseHelper.HelperException("Array is not sorted");
        if (statPack == null) throw new RuntimeException("InstrumentedHelper.postProcess: no StatPack");
        if (countCompares)
            statPack.add(COMPARES, compares);
        if (countSwaps)
            statPack.add(SWAPS, swaps);
        if (countCopies)
            statPack.add(COPIES, copies);
        if (countFixes)
            statPack.add(FIXES, fixes);
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
        logger.debug(() -> "Closing Helper: " + description + " with statPack: " + statPack);
        super.close();
    }

    public StatPack getStatPack() {
        return statPack;
    }

    /**
     * Constructor for explicit random number generator.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param random      a random number generator.
     * @param config      the configuration (note that the seed value is ignored).
     */
    public InstrumentedHelper(final String description, final int n, final Random random, final Config config) {
        super(description, n, random);
        this.countCopies = config.getBoolean(INSTRUMENTING, COPIES);
        this.countSwaps = config.getBoolean(INSTRUMENTING, SWAPS);
        this.countCompares = config.getBoolean(INSTRUMENTING, COMPARES);
        this.countInversions = config.getInt(INSTRUMENTING, INVERSIONS, 0);
        this.countFixes = config.getBoolean(INSTRUMENTING, FIXES);
        this.cutoff = config.getInt("helper", "cutoff", 0);
    }

    /**
     * Constructor to create a Helper
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param config      The configuration.
     */
    public InstrumentedHelper(final String description, final int n, final Config config) {
        this(description, n, config.getLong("helper", "seed", System.currentTimeMillis()), config);
    }

    /**
     * Constructor to create a Helper
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator.
     * @param config      the configuration.
     */
    public InstrumentedHelper(final String description, final int n, final long seed, final Config config) {
        this(description, n, new Random(seed), config);
    }

    /**
     * Constructor to create a Helper with a random seed and an n value of 0.
     * <p>
     * NOTE: this constructor is used only by unit tests
     *
     * @param description the description of this Helper (for humans).
     */
    public InstrumentedHelper(final String description, final Config config) {
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
