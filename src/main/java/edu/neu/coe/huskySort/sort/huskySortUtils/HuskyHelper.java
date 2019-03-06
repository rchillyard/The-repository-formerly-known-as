package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.Helper;

import java.util.function.Consumer;

/**
 * Helper class for sorting methods with special technique of HuskySort
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class HuskyHelper<X extends Comparable<X>> extends Helper<X> {

    /**
     * Constructor to create a Helper
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator
     * @param makeCopy    explicit setting of the makeCopy value used in sort(X[] xs)
     */
    public HuskyHelper(String description, int n, HuskyCoder<X> coder, Consumer<X[]> postSorter, long seed, boolean makeCopy) {
        super(description, n, seed);
        this.coder = coder;
        longs = new long[n];
        this.postSorter = postSorter;
        this.makeCopy = makeCopy;
    }

    /**
     * Constructor to create a Helper with random seed.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public HuskyHelper(String description, int n, HuskyCoder<X> coder, Consumer<X[]> postSorter) {
        this(description, n, coder, postSorter, System.currentTimeMillis(), false);
    }

    // TODO this needes to be unit-tested
    public HuskyCoder<X> getCoder() {
        return coder;
    }

    // TODO this needes to be unit-tested
    public int getN() {
        return n;
    }

    @Override
    public void setN(int n) {
        if (n != this.n) longs = new long[n];
        super.setN(n);
    }

    public void initLongArray(X[] array) {
        for (int i = 0; i < array.length; i++) longs[i] = coder.huskyEncode(array[i]);
    }

    // CONSIDER having a method less which compares the longs rather than having direct access to the longs array in sub-classes
    public void swap(X[] xs, int i, int j) {
        // Swap longs
        long temp1 = longs[i];
        longs[i] = longs[j];
        longs[j] = temp1;
        // Swap xs
        X temp2 = xs[i];
        xs[i] = xs[j];
        xs[j] = temp2;
    }

    public Consumer<X[]> getPostSorter() {
        return postSorter;
    }

    public boolean isMakeCopy() {
        return makeCopy;
    }

    public long[] getLongs() {
        return longs;
    }

    private final HuskyCoder<X> coder;
    private long[] longs;
    private final Consumer<X[]> postSorter;
    private final boolean makeCopy;
}
