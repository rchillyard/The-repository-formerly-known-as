package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.HelperFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.util.Config;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseCountingSort<X extends StringComparable<X, Y>, Y extends Comparable<Y>> implements TransformingSort<X, String> {

    /**
     * Method to get a suitable Helper for this TransformingSort.
     *
     * @return the Helper.
     */
    public TransformingHelper<X> getHelper() {
        return helper;
    }

    /**
     * Method to recover the original unicode Strings from an array of Xs.
     * This is essentially the inverse of getStringComparableStrings.
     * <p>
     * TODO make recoverString a generic function of X -> Y
     *
     * @param ws   an array of Strings which will be over-written starting at index from.
     * @param from the starting index.
     * @param n    the number of strings to recover.
     * @param xs   the array of StringComparable (i.e. X) objects.
     */
    public void recoverStrings(final String[] ws, final int from, final int n, final X[] xs) {
        for (int i = 0; i < n; i++) ws[i + from] = xs[i].recoverString();
    }

    /**
     * Method to convert an array (ws) of unicode Strings into an array of Xs (which are StringComparable).
     *
     * @param clazz              the class of X.
     * @param ys                 the input array of unicode Strings.
     * @param from               the starting index.
     * @param n                  the number of elements to process and return.
     * @param toStringComparable a function which takes a String and returns an X.
     * @return an X[] of length n.
     */
    @Override
    public X[] getStringComparableStrings(final Class<X> clazz, final String[] ys, final int from, final int n, final Function<String, X> toStringComparable) {
        @SuppressWarnings("unchecked") final X[] xs = (X[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) xs[i] = toStringComparable.apply(ys[i + from]);
        return xs;
    }

    /**
     * Abstract class constructor.
     *
     * @param characterMap the character map.
     * @param helper       a counting sort helper of type CountingSortHelper of X, Y.
     */
    public BaseCountingSort(final CharacterMap characterMap, final CountingSortHelper<X, Y> helper) {
        this.characterMap = characterMap;
        this.helper = helper;
    }

    /**
     * NOTE: callers of this method should consider arranging for the helper to be closed on close of the sorter.
     */
    private static <Q extends Comparable<Q>> HuskyHelper<Q> createHelper(final String name, final int n, final HuskyCoder<Q> huskyCoder, final Consumer<Q[]> postSorter, final boolean instrumentation, final Config config) {
        return instrumentation ? new HuskyHelper<>(HelperFactory.create("Delegate CountingSortHelper", n, config), huskyCoder, postSorter, false) : new HuskyHelper<>(name, n, huskyCoder, postSorter);
    }


    private final CharacterMap characterMap; // CONSIDER do we need this?
    private final CountingSortHelper<X, Y> helper;
}
