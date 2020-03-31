package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.StatPack;

/**
 * Helper class for sorting methods with instrumentation of compares and swaps, and in addition, bounds checks.
 * This Helper class may be used for analyzing sort methods but will run at slightly slower speeds than the super-class.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class InstrumentedHelper<X extends Comparable<X>> extends BaseHelper<X> {

    /**
     * Constructor to create a Helper
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator
     */
    public InstrumentedHelper(String description, int n, long seed) {
        super(description, n, seed);
    }

    /**
     * Constructor to create a Helper with a random seed.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public InstrumentedHelper(String description, int n) {
        this(description, n, System.currentTimeMillis());
    }

    /**
     * Constructor to create a Helper with a random seed and an n value of 0.
     *
     * @param description the description of this Helper (for humans).
     */
    public InstrumentedHelper(String description) {
        this(description, 0);
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
    public boolean less(X v, X w) {
        compares++;
        return v.compareTo(w) < 0;
    }

    /**
     * Swap the elements of array a at indices i and j.
     *  @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     */
    public void swap(X[] xs, int i, int j) {
        swaps++;
        X temp = xs[i];
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
    @Override
    public void swapInto(X[] xs, int i, int j) {
        swaps += (j - i);
        super.swapInto(xs, i, j);
    }

    public int compare(X[] xs, int i, int j) {
        // CONSIDER using the other compare method.
        compares++;
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
    public int compare(X v, X w) {
        compares++;
        return v.compareTo(w);
    }

    @Override
    public String toString() {
        return "Helper for " + description + " with " + n + " elements: compares=" + compares + ", swaps=" + swaps;
    }

    public void setN(int n) {
        super.setN(n);
        statPack = new StatPack(n, COMPARES, SWAPS);
    }

    /**
     * Method to check that an array is sorted.
     *
     * @param xs the array to be tested.
     *           TODO log the message
     *           TODO show the number of inversions
     */
    @Override
    public void postProcess(X[] xs) {
        super.postProcess(xs);
        if (statPack == null) throw new RuntimeException("InstrumentedHelper.postProcess: no StatPack");
        statPack.add(COMPARES, compares);
        statPack.add(SWAPS, swaps);
    }

    @Override
    public void close() {
        logger.debug(() -> "Closing Helper: " + description + " with statPack: " + statPack);
        super.close();
    }

    final static LazyLogger logger = new LazyLogger(InstrumentedHelper.class);

    public static final String SWAPS = "swaps";
    public static final String COMPARES = "compares";

    // NOTE: the following private methods are only for testing.

    private StatPack getStatPack() {
        return statPack;
    }

    private int getCompares() {
        return compares;
    }

    private int getSwaps() {
        return swaps;
    }

    private StatPack statPack;
    private int compares = 0;
    private int swaps = 0;

}
