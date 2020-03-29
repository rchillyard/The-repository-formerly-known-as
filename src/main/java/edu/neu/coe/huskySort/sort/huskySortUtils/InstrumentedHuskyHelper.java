package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.InstrumentingHelper;

import java.util.function.Consumer;

/**
 * Helper class for sorting methods with special technique of HuskySort.
 * IF you want to count compares and swaps then you need to extend InstrumentingHelper.
 * <p>
 * CONSIDER using a delegation model for the Helper here. That would allow dynamic choice of InstrumentingHelper.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class InstrumentedHuskyHelper<X extends Comparable<X>> extends HuskyHelper<X> {
    // Delegate methods on helper

    public boolean less(X v, X w) {
        compares++;
        return super.less(v, w);
    }

    // CONSIDER having a method less which compares the longs rather than having direct access to the longs array in sub-classes
    public void swap(X[] xs, int i, int j) {
        swaps++;
        super.swap(xs, i, j);
    }


    /**
     * Constructor to create a HuskyHelper
     *
     * @param helper     the Helper.
     * @param coder      the coder to be used.
     * @param postSorter the postSorter Consumer function.
     * @param makeCopy   explicit setting of the makeCopy value used in sort(X[] xs)
     */
    public InstrumentedHuskyHelper(Helper<X> helper, HuskyCoder<X> coder, Consumer<X[]> postSorter, boolean makeCopy) {
        super(helper, coder, postSorter, makeCopy);
    }

    /**
     * Constructor to create a Helper
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param coder       the HuskyCoder to be used
     * @param postSorter  the postSorter to use
     * @param seed        the seed for the random number generator
     * @param makeCopy    explicit setting of the makeCopy value used in sort(X[] xs)
     */
    public InstrumentedHuskyHelper(String description, int n, HuskyCoder<X> coder, Consumer<X[]> postSorter, long seed, boolean makeCopy) {
        this(new InstrumentingHelper<>(description, n, seed), coder, postSorter, makeCopy);
    }

    /**
     * Constructor to create a Helper with random seed.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public InstrumentedHuskyHelper(String description, int n, HuskyCoder<X> coder, Consumer<X[]> postSorter) {
        this(description, n, coder, postSorter, System.currentTimeMillis(), false);
    }

    private int swaps;
    private int compares;
}
