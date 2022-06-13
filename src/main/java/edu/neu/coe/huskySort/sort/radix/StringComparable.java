package edu.neu.coe.huskySort.sort.radix;

/**
 * Interface to allow comparison of generic strings.
 *
 * @param <X> the type of the string to be compared.
 * @param <Y> the type of an element of the string.
 */
public interface StringComparable<X extends StringLike<X, Y>, Y extends Comparable<Y>> extends StringLike<X, Y> {

    /**
     * Method to compare this StringComparable with that StringComparable, but only at the character specified by d.
     *
     * @param that another X.
     * @param d    the index of the characters to be compared.
     * @return an int according to the y.compareTo(y).
     */
    default int compareTo(final X that, final int d) {
        return charAt(d).compareTo(that.charAt(d));
    }

    /**
     * Method to compare this X with other -- starting at the dth character, but if necessary continuing to later characters.
     * This is particularly used by the insertion sort mechanism.
     *
     * @param that another X.
     * @param d    the offset of the first character to compare in each of the strings.
     * @return negative, zero, or positive according to this less than, = or greater than other.
     */
    int compareFromD(final X that, final int d);

    /**
     * Method to compare this StringComparable with that StringComparable, taking all characters into consideration.
     *
     * @param that the object to be compared.
     * @return the result of invoking compareFromD(that, 0).
     */
    default int compareTo(final X that) {
        return compareFromD(that, 0);
    }
}
