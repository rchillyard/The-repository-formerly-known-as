package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper class for sorting methods with special technique of HuskySort.
 * IF you want to count compares and swaps then you need to extend InstrumentedHelper.
 * <p>
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class HuskyHelper<X extends Comparable<X>> implements Helper<X> {

    /**
     * @return the post-sorter.
     */
    public Consumer<X[]> getPostSorter() {
        return postSorter;
    }

    /**
     * @return the value of makeCopy.
     */
    public boolean isMakeCopy() {
        return makeCopy;
    }

    /**
     * CONSIDER eliminating this method
     *
     * @return the array of longs.
     */
    public long[] getLongs() {
        return coding.longs;
    }

    /**
     * @return the Helper.
     */
    public Helper<X> getHelper() {
        return helper;
    }

    /**
     * @param v the first value.
     * @param w the second value.
     * @return true if v is less than w.
     */
    public boolean less(final X v, final X w) {
        return helper.less(v, w);
    }

    /**
     * @param xs an array of Xs.
     * @return true if xs is sorted.
     */
    public boolean sorted(final X[] xs) {
        return helper.sorted(xs);
    }

    /**
     * @param xs an array of Xs.
     * @return the number of inversions in xs.
     */
    public int inversions(final X[] xs) {
        return helper.inversions(xs);
    }

    /**
     * @param xs the array that has been sorted.
     */
    public void postProcess(final X[] xs) {
        helper.postProcess(xs);
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
        return helper.swapConditional(xs, i, j);
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
        return helper.swapStableConditional(xs, i);
    }

    /**
     * Method to perform a stable swap using half-exchanges, and binary search.
     * i.e. x[i] is moved leftwards to its proper place and all elements from
     * the destination of x[i] thru x[i-1] are moved up one place.
     * This type of swap is used by insertion sort.
     *
     * @param xs the array of X elements, whose elements 0 thru i-1 MUST be sorted.
     * @param i  the index of the element to be swapped into the ordered array xs[0..i-1].
     */
    @Override
    public void swapIntoSorted(final X[] xs, final int i) {
        helper.swapIntoSorted(xs, i);
    }

    /**
     * TODO eliminate this method as it has been superseded by swapConditional. However, maybe the latter is a better name.
     * Method to fix a potentially unstable inversion.
     *
     * @param xs the array of X elements.
     * @param i  the index of the lower of the elements to be swapped.
     * @param j  the index of the higher of the elements to be swapped.
     */
    @Override
    public void fixInversion(final X[] xs, final int i, final int j) {
        helper.fixInversion(xs, i, j);
    }

    /**
     * TODO eliminate this method as it has been superseded by swapStableConditional. However, maybe the latter is a better name.
     * Method to fix a stable inversion.
     *
     * @param xs the array of X elements.
     * @param i  the index of the higher of the adjacent elements to be swapped.
     */
    @Override
    public void fixInversion(final X[] xs, final int i) {
        helper.fixInversion(xs, i);
    }

    /**
     * Get the cutoff value.
     *
     * @return the cutoff value.
     */
    @Override
    public int cutoff() {
        return helper.cutoff();
    }

    /**
     * If instrumenting, increment the number of copies by n.
     *
     * @param n the number of copies made.
     */
    @Override
    public void incrementCopies(final int n) {
        helper.incrementCopies(n);
    }

    /**
     * If instrumenting, increment the number of fixes by n.
     *
     * @param n the number of copies made.
     */
    @Override
    public void incrementFixes(final int n) {
        helper.incrementFixes(n);
    }

    /**
     * Method to do any required preProcessing.
     *
     * @param xs the array to be sorted.
     * @return the array after any pre-processing.
     */
    @Override
    public X[] preProcess(final X[] xs) {
        return helper.preProcess(xs);
    }

    /**
     * Method to register the current recursion depth.
     *
     * @param depth the depth.
     */
    @Override
    public void registerDepth(final int depth) {
        helper.registerDepth(depth);
    }

    /**
     * Get the maximum depth so far registered.
     *
     * @return the maximum recursion depth.
     */
    @Override
    public int maxDepth() {
        return helper.maxDepth();
    }

    /**
     * @param clazz the class of X.
     * @param f     a function which takes a Random and generates a random value of X.
     * @return an array of randomly chosen X values.
     */
    public X[] random(final Class<X> clazz, final Function<Random, X> f) {
        return helper.random(clazz, f);
    }

    /**
     * @return the description.
     */
    public String getDescription() {
        return helper.getDescription();
    }

    /**
     * @return the number of elements.
     */
    public int getN() {
        return helper.getN();
    }

    /**
     * Close this Helper.
     */
    public void close() {
        helper.close();
    }

    /**
     * @return true if this helper is instrumented.
     */
    public boolean instrumented() {
        return helper.instrumented();
    }

    /**
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     * @return the result of comparing xs[i] with xs[j].
     */
    public int compare(final X[] xs, final int i, final int j) {
        return helper.compare(xs, i, j);
    }

    /**
     * @param v the first value.
     * @param w the second value.
     * @return The result of comparing v with w.
     */
    public int compare(final X v, final X w) {
        return helper.compare(v, w);
    }

    // CONSIDER having a method less which compares the longs rather than having direct access to the longs array in sub-classes
    public void swap(final X[] xs, final int i, final int j) {
        final long[] longs = coding.longs;
        // Swap longs
        final long temp1 = longs[i];
        longs[i] = longs[j];
        longs[j] = temp1;
        // CONSIDER incrementing the swaps here since we are in fact doing two swaps.
        helper.swap(xs, i, j);
    }

    /**
     * Method to perform a stable swap, i.e. between xs[i] and xs[i-1]
     *
     * @param xs the array of Y elements.
     * @param i  the index of the higher of the adjacent elements to be swapped.
     */
    public void swapStable(final X[] xs, final int i) {
        helper.swapStable(xs, i);
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
        helper.swapInto(xs, i, j);
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
        helper.copy(source, i, target, j);
    }

    /**
     * @return the Husky coder.
     */
    public HuskyCoder<X> getCoder() {
        return coder;
    }

    /**
     * @param n the size to be managed.
     */
    public void init(final int n) {
        helper.init(n);
    }

    /**
     * TODO this should be package private but we have to get the classes in the same package first.
     *
     * @param array the array from which we build a long array by encoding.
     */
    public void doCoding(final X[] array) {
        coding = coder.huskyEncode(array);
    }

    public Coding getCoding() {
        return coding;
    }

    /**
     * Constructor to create a HuskyHelper
     *
     * @param helper     the Helper.
     * @param coder      the coder to be used.
     * @param postSorter the postSorter Consumer function.
     * @param makeCopy   explicit setting of the makeCopy value used in sort(Y[] xs)
     */
    public HuskyHelper(final Helper<X> helper, final HuskyCoder<X> coder, final Consumer<X[]> postSorter, final boolean makeCopy) {
        this.helper = helper;
        this.coder = coder;
        this.postSorter = postSorter;
        this.makeCopy = makeCopy;
    }

    /**
     * Constructor to create an uninstrumented Husky Helper with explicit seed.
     * <p>
     * NOTE used by unit tests only.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator
     * @param makeCopy    explicit setting of the makeCopy value used in sort(Y[] xs)
     */
    public HuskyHelper(final String description, final int n, final HuskyCoder<X> coder, final Consumer<X[]> postSorter, final long seed, final boolean makeCopy) {
        this(new BaseHelper<>(description, n, seed), coder, postSorter, makeCopy);
    }

    /**
     * Constructor to create an uninstrumented Husky Helper with random seed.
     * <p>
     * NOTE used by unit tests only.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public HuskyHelper(final String description, final int n, final HuskyCoder<X> coder, final Consumer<X[]> postSorter) {
        this(description, n, coder, postSorter, System.currentTimeMillis(), false);
    }

    protected final Helper<X> helper;

    // Delegate methods on helper
    private final HuskyCoder<X> coder;
    private final Consumer<X[]> postSorter;
    private final boolean makeCopy;

    private Coding coding;
}
