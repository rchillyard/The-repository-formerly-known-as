package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.sort.radix.BasicCountingSortHelper;
import edu.neu.coe.huskySort.sort.radix.CountingSortHelper;
import edu.neu.coe.huskySort.sort.radix.InstrumentedCountingSortHelper;
import edu.neu.coe.huskySort.sort.radix.StringComparable;
import edu.neu.coe.huskySort.util.Config;

public final class HelperFactory {

    /**
     * Factory method to create a ComparisonSortHelper.
     *
     * @param description the description of the ComparisonSortHelper.
     * @param nElements   the number of elements to be sorted.
     * @param config      the configuration.
     * @param <X>         the underlying type.
     * @return a ComparisonSortHelper<X></X>
     */
    public static <X extends Comparable<X>> ComparisonSortHelper<X> create(final String description, final int nElements, final Config config) {
        return create(description, nElements, config.isInstrumented(), config);
    }

    /**
     * CONSIDER eliminating this signature.
     *
     * @param description  the description of the ComparisonSortHelper.
     * @param nElements    the number of elements to be sorted.
     * @param instrumented an explicit value of instrumented, not derived from the config.
     * @param config       the configuration.
     * @param <X>          the underlying type.
     * @return a ComparisonSortHelper<X></X>
     */
    public static <X extends Comparable<X>> ComparisonSortHelper<X> create(final String description, final int nElements, final boolean instrumented, final Config config) {
        return instrumented ? new InstrumentedComparisonSortHelper<>(description, nElements, config) : new ComparableSortHelper<>(description, nElements);
    }

    /**
     * Factory method to create a CountingSortHelper.
     *
     * @param description the description of the CountingSortHelper.
     * @param nElements   the number of elements to be sorted.
     * @param config      the configuration.
     * @param <X>         the underlying type.
     * @return a CountingSortHelper<X></X>
     */
    public static <X extends StringComparable<X, Y>, Y extends Comparable<Y>> CountingSortHelper<X, Y> createCountingSortHelper(final String description, final int nElements, final Config config) {
        return createCountingSortHelper(description, nElements, config.isInstrumented(), config);
    }

    /**
     * @param description  the description of the CountingSortHelper.
     * @param nElements    the number of elements to be sorted.
     * @param instrumented an explicit value of instrumented, not derived from the config.
     * @param config       the configuration (ignored if instrumented is false).
     * @param <X>          the underlying type.
     * @return a CountingSortHelper<X></X>
     */
    public static <X extends StringComparable<X, Y>, Y extends Comparable<Y>> CountingSortHelper<X, Y> createCountingSortHelper(final String description, final int nElements, final boolean instrumented, final Config config) {
        return instrumented ? new InstrumentedCountingSortHelper<>(description, nElements, config) : new BasicCountingSortHelper<>(description, nElements);
    }

}
