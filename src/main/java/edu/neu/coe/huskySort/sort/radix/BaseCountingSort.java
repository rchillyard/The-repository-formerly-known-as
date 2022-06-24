package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.HelperFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.util.Config;

import java.util.function.Consumer;

public abstract class BaseCountingSort<X extends StringComparable<X, Y>, Y extends Comparable<Y>> implements TransformingSort<String, X> {

    /**
     * Method to get a suitable Helper for this TransformingSort.
     *
     * @return the Helper.
     */
    public TransformingHelper<X> getHelper() {
        return helper;
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
