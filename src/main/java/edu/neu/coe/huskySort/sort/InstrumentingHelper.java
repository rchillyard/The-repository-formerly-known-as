package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.StatPack;

/**
 * Helper class for sorting methods with instrumentation of compares and swaps, and in addition, bounds checks.
 * This Helper class may be used for analyzing sort methods but will run at slightly slower speeds than the super-class.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class InstrumentingHelper<X extends Comparable<X>> extends BaseHelper<X> {

    /**
     * Constructor to create a Helper
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator
     */
    public InstrumentingHelper(String description, int n, long seed) {
        super(description, n, seed);
    }

    /**
     * Constructor to create a Helper with a random seed.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public InstrumentingHelper(String description, int n) {
        this(description, n, System.currentTimeMillis());
    }

    /**
     * Constructor to create a Helper with a random seed and an n value of 0.
     *
     * @param description the description of this Helper (for humans).
     */
    public InstrumentingHelper(String description) {
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
     *
     * @param xs the array.
     * @param lo the lowest index of interest (only used for checking).
     * @param hi one more than the highest index of interest (only used for checking).
     * @param i  one of the indices.
     * @param j  the other index.
     */
    public void swap(X[] xs, int lo, int hi, int i, int j) {
        swaps++;
        if (i < lo) throw new RuntimeException("i is out of range: i; " + i + "; lo=" + lo);
        if (j > hi) throw new RuntimeException("j is out of range: j; " + j + "; hi=" + hi);
        X temp = xs[i];
        xs[i] = xs[j];
        xs[j] = temp;
    }

    public int compare(X[] xs, int lo, int hi, int i, int j) {
        compares++;
        if (i < lo) throw new RuntimeException("i is out of range: i; " + i + "; lo=" + lo);
        if (j > hi) throw new RuntimeException("j is out of range: j; " + j + "; hi=" + hi);
        return xs[i].compareTo(xs[j]);
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
        if (statPack == null) throw new RuntimeException("InstrumentingHelper.postProcess: no StatPack");
        statPack.add(COMPARES, compares);
        statPack.add(SWAPS, swaps);
    }

    @Override
    public void close() {
        logger.debug(() -> "Closing Helper: " + description + " with statPack: " + statPack);
        super.close();
    }

    final static LazyLogger logger = new LazyLogger(InstrumentingHelper.class);

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
