/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * This interface extends HuskySort for object which are subclasses of CharSequence.
 *
 * @param <X> the underlying type for this coder, which extends CharSequence and thus has a length.
 */
public interface HuskySequenceCoder<X extends CharSequence> extends HuskyCoder<X> {

    /**
     * Method to determine if this Husky Coder is perfect for a sequence of the given length.
     * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
     * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
     *
     * NOTE despite the name this is a <b>necessary, not a sufficient</b> condition for a value to
     * be exact: it tests one aspect of a value, its length, and says nothing about its characters.
     * Item 48 was precisely the error of treating it as sufficient --- a narrowing coder returned
     * an unsorted array and reported it perfect. Read it as "could be exact at this length", and
     * see {@link HuskyCoder} for the distinction between exact and perfect.
     *
     * @param length the length of a particular sequence.
     * @return false if the resulting long for the String will likely not be unique.
     */
    boolean perfectForLength(int length);

}
