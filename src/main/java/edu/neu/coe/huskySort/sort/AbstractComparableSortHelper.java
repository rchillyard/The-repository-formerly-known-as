package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.BaseHelper;
import edu.neu.coe.huskySort.util.Instrumenter;

import java.util.Random;

public abstract class AbstractComparableSortHelper<X> extends BaseHelper<X> implements ComparisonSortHelper<X> {
    /**
     * Method to get the Instrumenter.
     *
     * @return null.
     */
    public Instrumenter getInstrumenter() {
        return null;
    }

    /**
     * Swap the elements of array "a" at indices i and j.
     *
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     */
    public void swap(final X[] xs, final int i, final int j) {
        final X temp = xs[i];
        xs[i] = xs[j];
        xs[j] = temp;
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
    public void swapInto(final X[] xs, final int i, final int j) {
        if (j > i) {
            final X x = xs[j];
            System.arraycopy(xs, i, xs, i + 1, j - i);
            xs[i] = x;
        }
    }

    public AbstractComparableSortHelper(final String description, final Random random, final int n) {
        super(description, random, n);
    }
}
