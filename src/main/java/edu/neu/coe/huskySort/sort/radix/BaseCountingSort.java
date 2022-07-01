package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.HelperFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.util.Config;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Abstract base class for "string" (counting) sorts.
 * It extends TransformingSort of String and X.
 *
 * @param <X> the temporary type which will actually be sorted (X must extend StringComparable).
 * @param <Y> an element (character) of X which must extend Comparable.
 */
public abstract class BaseCountingSort<X extends StringComparable<X, Y>, Y extends Comparable<Y>> implements TransformingSort<String, X> {
    /**
     * Method to get a suitable Helper for this TransformingSort.
     * <p>
     * TEST not currently invoked.
     *
     * @return a TransformingHelper of String and X.
     */
    public TransformingHelper<String, X> getHelper() {
        return helper;
    }

    /**
     * Generic, non-mutating sort method which allows for explicit determination of the makeCopy option.
     * The three steps are:
     * <ol>
     *     <li>invoke preSort(X[], boolean)</li>
     *     <li>invoke sort(X[], int, int)</li>
     *     <li>return value of postSort(X[])</li>
     * </ol>
     *  @param ws       sort the array ws (mutating ws).
     *
     * @param clazz     the class of X.
     * @param transform function to transform a String into an X.
     * @param recover   function to transform an X into a String.
     * @return true if the sort was successful.
     */
    protected boolean sortAll(final Class<X> clazz, final String[] ws, final Function<String, X> transform, final Function<X, String> recover) {
        final int n = ws.length;
        final X[] xs0 = helper.transformXToT(clazz, ws, 0, n, transform);
        helper.init(n);
        final X[] xs1 = helper.preProcess(xs0);
        helper.incrementCopies(2 * n); // these are for the transformations.
        sort(xs1, 0, n); // XXX n should be the same as xs1.length
        final boolean ok = helper.postProcess(xs1);
        helper.recoverXFromT(helper.postSort(xs1), ws, 0, n, recover);
        return ok;
    }

    /**
     * Generic, non-mutating sort method which allows for explicit determination of the makeCopy option.
     * <p>
     * TEST this is not invoked.
     *
     * @param xs       sort the array xs, returning the sorted result, leaving xs unchanged.
     * @param makeCopy if set to true, we make a copy first and sort that.
     */
    public X[] sort(final X[] xs, final boolean makeCopy) {
        final X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        sort(result, 0, result.length);
        return result;
    }

    /**
     * Abstract class constructor.
     *
     * @param helper a counting sort helper of type CountingSortHelper of X, Y.
     */
    public BaseCountingSort(final CountingSortHelper<X, Y> helper) {
        this.helper = helper;
    }

    /**
     * NOTE: callers of this method should consider arranging for the helper to be closed on close of the sorter.
     * <p>
     * CONSIDER do we need this?
     */
    private static <Q extends Comparable<Q>> HuskyHelper<Q> createHelper(final String name, final int n, final HuskyCoder<Q> huskyCoder, final Consumer<Q[]> postSorter, final boolean instrumentation, final Config config) {
        return instrumentation ? new HuskyHelper<>(HelperFactory.create("Delegate CountingSortHelper", n, config), huskyCoder, postSorter, false) : new HuskyHelper<>(name, n, huskyCoder, postSorter);
    }

    private final CountingSortHelper<X, Y> helper;
}
