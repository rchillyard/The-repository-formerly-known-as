package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.GenericSort;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.Sorter;

/**
 * Interface to define a sort method which sorts an array of Xs by temporarily transforming them into an array of Ys.
 *
 * @param <X> the element type of the input/output arrays (must implement Comparable of X).
 * @param <T> the element type of the temporary array.
 */
public interface TransformingSort<X extends Comparable<X>, T> extends GenericSort<T>, Sorter<X> {

    /**
     * Method to get a suitable Helper for this TransformingSort.
     *
     * @return the Helper.
     */
    TransformingHelper<X, T> getHelper();

    /**
     * Close this sorter, releasing anything it holds -- which, for the counting sorts reached
     * through this interface, is nothing.
     * <p>
     * NOTE: a no-op default, rather than abstract, so that those sorts need not declare a close()
     * they have no use for. This is the narrowing of {@code AutoCloseable.close()} for the
     * transforming branch of the hierarchy; the branch under {@link edu.neu.coe.huskySort.sort.Sort}
     * re-declares it abstract instead, so every sorter that does hold a helper is still obliged to
     * say what closing means.
     */
    @Override
    default void close() {
    }

}
