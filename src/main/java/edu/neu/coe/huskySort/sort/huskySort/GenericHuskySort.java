package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortable;
import edu.neu.coe.huskySort.util.Config;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Class to perform HuskySort on objects belong to a sub-class of HuskySortable.
 *
 * @param <X> the type of object to be sorted -- must implement HuskySortable.
 */
public final class GenericHuskySort<X extends HuskySortable<X>> extends IntroHuskySort<X> {

    /**
     * Primary constructor for GenericHuskySort.
     *
     * @param postSorter the sorter to remove remaining inversions.
     * @param config     the configuration.
     */
    public GenericHuskySort(final Consumer<X[]> postSorter, final Config config) {
        super("Generic HuskySort", HuskyCoderFactory.createGenericCoder(), postSorter, config);
    }

    /**
     * Secondary constructor for GenericHuskySort.
     *
     * @param config the configuration.
     */
    public GenericHuskySort(final Config config) {
        this(Arrays::sort, config);
    }
}
