package edu.neu.coe.huskySort.sort.radix;

import java.util.function.Function;

/**
 * Interface to define a sort method which sorts an array of Xs by temporarily transforming them into an array of Ys.
 *
 * @param <X> the element type of the temporary array.
 * @param <Y> the element type of the input/output arrays (must implement Comparable of Y.
 */
public interface TransformingSort<X, Y extends Comparable<Y>> {

    /**
     * Method to get a suitable Helper for this TransformingSort.
     *
     * @return the Helper.
     */
    TransformingHelper<X> getHelper();

    /**
     * Method to recover the original unicode Strings from an array of Xs.
     * This is essentially the inverse of getStringComparableStrings.
     *
     * @param ws   an array of Strings which will be over-written starting at index from.
     * @param from the starting index.
     * @param n    the number of strings to recover.
     * @param xs   the array of StringComparable (i.e. X) objects.
     */
    void recoverStrings(String[] ws, int from, int n, X[] xs);

    /**
     * Method to convert an array (ys) of Y into an array of Xs (which are StringComparable).
     *
     * @param clazz              the class of X.
     * @param ys                 the input array of Ys.
     * @param from               the starting index.
     * @param n                  the number of elements to process and return.
     * @param toStringComparable a function which takes a Y and returns an X.
     * @return an X[] of length n.
     */
    X[] getStringComparableStrings(Class<X> clazz, Y[] ys, int from, int n, Function<Y, X> toStringComparable);
}
