package edu.neu.coe.huskySort.sort;

import java.util.Random;
import java.util.function.Function;

import static java.util.Arrays.binarySearch;

/**
 * CONSIDER having the concept of current sub-array, then we could dispense with the lo, hi parameters.
 *
 * @param <X>
 */
public interface Helper<X extends Comparable<X>> {

    /**
     * @return true if this is an instrumented Helper.
     */
    boolean instrumented();

    /**
     * Compare elements i and j of xs within the subarray lo..hi
     *
     * @param xs the array.
     * @param i  one of the indices.
     * @param j  the other index.
     * @return the result of comparing xs[i] to xs[j]
     */
    int compare(X[] xs, int i, int j);

    /**
     * Compare values v and w and return true if v is less than w.
     *
     * @param v the first value.
     * @param w the second value.
     * @return true if v is less than w.
     */
    boolean less(X v, X w);

    int compare(X v, X w);

    /**
     * Method to perform a general swap, i.e. between xs[i] and xs[j]
     *
     * @param xs the array of X elements.
     * @param i  the index of the lower of the elements to be swapped.
     * @param j  the index of the higher of the elements to be swapped.
     */
    void swap(X[] xs, int i, int j);

    /**
     * Method to perform a stable swap, i.e. between xs[i] and xs[i-1]
     *
     * @param xs the array of X elements.
     * @param i  the index of the higher of the adjacent elements to be swapped.
     */
    default void swapStable(X[] xs, int i) {
        swap(xs, i - 1, i);
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
    void swapInto(X[] xs, int i, int j);

    /**
     * Method to perform a stable swap using half-exchanges, and binary search.
     * i.e. x[i] is moved leftwards to its proper place and all elements from
     * the destination of x[i] thru x[i-1] are moved up one place.
     * This type of swap is used by insertion sort.
     *
     * @param xs the array of X elements, whose elements 0 thru i-1 MUST be sorted.
     * @param i  the index of the higher of the adjacent elements to be swapped.
     */
    default void swapIntoSorted(X[] xs, int i) {
        int j = binarySearch(xs, 0, i, xs[i]);
        if (j < 0) j = -j - 1;
        if (j < i) swapInto(xs, j, i);
    }

    /**
     * Method to fix a potentially unstable inversion.
     *
     * @param xs the array of X elements.
     * @param i  the index of the lower of the elements to be swapped.
     * @param j  the index of the higher of the elements to be swapped.
     */
    default void fixInversion(X[] xs, int i, int j) {
        if (less(xs[j], xs[i])) swap(xs, i, j);
    }

    /**
     * Method to fix a stable inversion.
     *  @param xs the array of X elements.
     * @param i  the index of the higher of the adjacent elements to be swapped.
     */
    default void fixInversion(X[] xs, int i) {
        if (less(xs[i], xs[i - 1])) swapStable(xs, i);
    }

    /**
     * Return true if xs is sorted, i.e. has no inversions.
     *
     * @param xs an array of Xs.
     * @return true if there are no inversions, else false.
     */
    boolean sorted(X[] xs);

    /**
     * Count the number of inversions of this array.
     *
     * @param xs   an array of Xs.
     * @return the number of inversions.
     */
    int inversions(X[] xs);

    /**
     * Method to post-process the array xs after sorting.
     *
     * @param xs the array that has been sorted.
     */
    void postProcess(X[] xs);

    /**
     * Method to generate an array of randomly chosen X elements.
     * @param clazz the class of X.
     * @param f a function which takes a Random and generates a random value of X.
     * @return an array of X of length determined by the current value according to setN.
     */
    X[] random(Class<X> clazz, Function<Random, X> f);

    /**
     * @return the description of this Helper.
     */
    String getDescription();

    /**
     * Set the size of the array to be managed by this Helper.
     * @param n the size to be managed.
     * @throws RuntimeException if the size hasn't been set.
     */
    void setN(int n);

    /**
     * Get the current value of N.
     * @return the value of N.
     */
    int getN();

    /**
     * Close this Helper, freeing up any resources used.
     */
    void close();
}
