/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.ComparisonSortHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;

import java.util.Comparator;

/**
 * Class to implement insertion sort in its <b>adaptive</b> form: each element is compared with its
 * predecessors from the right and the run of larger elements is shifted up to make room for it.
 * <p>
 * The cost is therefore {@code (n - 1) + X} comparisons and {@code X} element moves, where X is the
 * number of inversions in the input. That is the algorithm the discussion of $p_{crit}$ has in mind
 * when it writes the cleanup pass as {@code k(N + pX)}: linear when the input is nearly ordered,
 * and degrading to quadratic only as X approaches its random-input average of N(N-1)/4.
 * <p>
 * NOTE this is a different algorithm from {@link InsertionSort}, which locates each element with a
 * <i>binary search</i> over the whole sorted prefix (see {@code ComparisonSortHelper.swapIntoSorted})
 * and so performs {@code n log n} comparisons <b>however nearly ordered its input is</b>. That makes
 * InsertionSort a poor cleanup pass by construction -- it cannot exploit the very property the
 * cleanup pass exists to exploit -- and measurements comparing "insertion sort" against Timsort as
 * the cleanup should say which of the two they mean. Measured on the Leipzig english corpus after
 * the radix phase, binary insertion loses to Timsort by about 4.5x at n = 200,000, while this class
 * beats Timsort by 1.12x there and 1.24x at n = 1,000,000. See TODO.md item 38.
 * <p>
 * Shifting rather than swapping: both are adaptive and perform the same comparisons, but a swap
 * writes three times per inversion where a shift writes once.
 *
 * @param <X> the underlying type to be sorted.
 */
public class AdaptiveInsertionSort<X extends Comparable<X>> extends SortWithHelper<X> {

    public static final String DESCRIPTION = "Adaptive insertion sort";

    /**
     * Sort the sub-array xs:from:to using adaptive insertion sort.
     *
     * @param xs   sort the array xs from "from" to "to".
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(final X[] xs, final int from, final int to) {
        final ComparisonSortHelper<X> helper = getHelper();
        for (int i = from + 1; i < to; i++) {
            final X v = xs[i];
            int j = i - 1;
            // NOTE: the comparison is strict, so a run of elements equal to v is not disturbed and
            // the sort is stable.
            while (j >= from && helper.compare(xs[j], v) > 0) {
                xs[j + 1] = xs[j];
                j--;
            }
            if (j + 1 != i) {
                xs[j + 1] = v;
                helper.incrementCopies(i - j);
            }
        }
    }

    /**
     * Method to sort ys in place, by its natural ordering.
     *
     * @param ys  the array to be sorted.
     * @param <Y> the underlying type, which must be Comparable.
     */
    public static <Y extends Comparable<Y>> void mutatingAdaptiveInsertionSort(final Y[] ys) {
        new AdaptiveInsertionSort<Y>().mutatingSort(ys);
    }

    /**
     * Method to sort ys in place according to the given comparator, rather than by natural ordering.
     * <p>
     * Needed wherever the husky coder's own ordering is not the natural one -- the pinyin coder
     * being the case in this project -- since a cleanup pass in the wrong ordering produces a
     * sorted array that is sorted by the wrong thing. This is a plain static rather than a helper
     * path, so it records no instrumentation.
     *
     * @param ys         the array to be sorted.
     * @param comparator the ordering to sort by.
     * @param <Y>        the underlying type.
     */
    public static <Y> void sort(final Y[] ys, final Comparator<? super Y> comparator) {
        for (int i = 1; i < ys.length; i++) {
            final Y v = ys[i];
            int j = i - 1;
            while (j >= 0 && comparator.compare(ys[j], v) > 0) {
                ys[j + 1] = ys[j];
                j--;
            }
            ys[j + 1] = v;
        }
    }

    /**
     * Constructor for AdaptiveInsertionSort.
     *
     * @param N      the number of elements we expect to sort.
     * @param config the configuration.
     */
    public AdaptiveInsertionSort(final int N, final Config config) {
        super(DESCRIPTION, N, config);
    }

    public AdaptiveInsertionSort() {
        this(new ComparableSortHelper<>(DESCRIPTION));
    }

    /**
     * Constructor for AdaptiveInsertionSort.
     *
     * @param helper an explicit instance of ComparisonSortHelper to be used.
     */
    public AdaptiveInsertionSort(final ComparisonSortHelper<X> helper) {
        super(helper);
    }
}
