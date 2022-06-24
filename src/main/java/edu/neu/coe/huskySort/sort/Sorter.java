package edu.neu.coe.huskySort.sort;

public interface Sorter<X extends Comparable<X>> {
    /**
     * Perform the entire process of sorting the given array, including all pre- and post-processing.
     *
     * @param xs an array of Xs which will be mutated.
     */
    void sortArray(X[] xs);
}
