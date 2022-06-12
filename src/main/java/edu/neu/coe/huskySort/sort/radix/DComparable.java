package edu.neu.coe.huskySort.sort.radix;

public interface DComparable<X> {

    int compareTo(X x, int d);

    /**
     * Method to compare this X with other -- starting at the dth character, but if necessary continuing to later characters.
     * This is particularly used by the insertion sort mechanism.
     *
     * @param other another X.
     * @param d     the offset of the first character to compare in each of the strings.
     * @return negative, zero, or positive according to this less than, = or greater than other.
     */
    int compareFromD(final X other, final int d);
}
