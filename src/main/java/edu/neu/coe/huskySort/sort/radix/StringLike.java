package edu.neu.coe.huskySort.sort.radix;

public interface StringLike<X, Y extends Comparable<Y>> extends Comparable<X> {

    /**
     * Method to yield the dth character of this StringComparable.
     *
     * @param d the index of the character to be returned (0 <= d < N where N is string length).
     * @return an instance of Y (if d is too large, it may be the "null" Y).
     */
    Y charAt(int d);
}
