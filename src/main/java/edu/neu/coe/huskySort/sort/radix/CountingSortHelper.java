package edu.neu.coe.huskySort.sort.radix;

/**
 * CountingSortHelper interface.
 * <p>
 * A ComparisonSortHelper provides all the utilities that are needed by sort methods, for example, compare and swap.
 * <p>
 * CONSIDER having the concept of a current sub-array, then we could dispense with the lo, hi parameters.
 *
 * @param <X>
 */
public interface CountingSortHelper<X extends StringComparable<X, Y>, Y extends Comparable<Y>> extends TransformingHelper<String, X> {

    /**
     * Compare values v and w and return true if v is less than w.
     *
     * @param v the first value.
     * @param w the second value.
     * @param d the position of interest.
     * @return true if v is less than w.
     */
    default boolean less(final X v, final X w, final int d) {
        return v.compareFromD(w, d) < 0;
    }

    /**
     * Compare values v and w and return true if v is less than w.
     *
     * @param xs an array of X elements.
     * @param i  the index of the first value.
     * @param w  the second value.
     * @param d  the position of interest.
     * @return true if xs[i] is less than w.
     */
    default boolean less(final X[] xs, final int i, final X w, final int d) {
        return less(xs[i], w, d);
    }

    /**
     * Compare values v and w and return true if v is less than w.
     *
     * @param xs an array of X elements.
     * @param i  the index of the first value.
     * @param j  the index of the right-hand element.
     * @param d  the position of interest.
     * @return true if xs[i] is less than xs[j].
     */
    default boolean less(final X[] xs, final int i, final int j, final int d) {
        return less(xs, i, xs[j], d);
    }

    /**
     * Compare value v with value w.
     *
     * @param v the first value.
     * @param w the second value.
     * @param d the position of interest.
     * @return -1 if v is less than w; 1 if v is greater than w; otherwise 0.
     */
    default int compare(final X v, final X w, final int d) {
        return v.charAt(d).compareTo(w.charAt(d));
    }

    /**
     * Compare the elements i and j of array xs (at position d).
     *
     * @param xs an array of X elements.
     * @param i  the index of the left-hand element.
     * @param j  the index of the right-hand element.
     * @param d  the position of interest.
     * @return -1 if xs[i] is less than xs[j]; 1 if xs[i] is greater than xs[j]; otherwise 0.
     */
    default int compare(final X[] xs, final int i, final int j, final int d) {
        return compare(xs[i], xs[j], d);
    }

    /**
     * Method to swap two elements.
     * Even though this is a helper for counting sorts, we typically have a cutoff to insertion sort for,
     * e.g. MSD Radix sort.
     *
     * @param xs an array of Xs.
     * @param j  one element to be swapped.
     * @param i  the other element to be swapped.
     */
    default void swap(final X[] xs, final int j, final int i) {
        final X temp = xs[j];
        xs[j] = xs[i];
        xs[i] = temp;
    }

    /**
     * Method to determine the cutoff value for switching to insertion sort.
     *
     * @return 7 by default.
     */
    default int getCutoff() {
        return 7;
    }

    default void registerDepth(final int depth) {
    }

    default int maxDepth() {
        return 0;
    }
}
