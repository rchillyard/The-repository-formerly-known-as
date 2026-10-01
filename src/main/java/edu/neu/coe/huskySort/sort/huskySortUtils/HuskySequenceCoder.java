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
     * Method to determine whether a sequence of the given length is short enough to be encoded
     * exactly --- that is, whether it fits the coder's window without truncation.
     * <p>
     * <b>Necessary, not sufficient</b>, which is what the name is for. It tests one aspect of a
     * value, its length, and says nothing whatever about its characters: a string that fits may
     * still be inexact because a narrowing coder cannot represent the characters it holds. Item
     * 48 was exactly the error of reading this as sufficient --- the array coder decided
     * perfection from length alone, so a narrowing coder returned an unsorted array and reported
     * it perfect. {@code exactlyEncodable} is the predicate that answers the whole question; this
     * answers one part of it.
     * <p>
     * Called {@code perfectForLength} until 2026-10-01, which said "perfect" for what is really
     * an {@code exact} predicate on one dimension of a value. See {@link HuskyCoder} for the
     * distinction.
     *
     * @param length the length of a particular sequence.
     * @return true if a sequence of this length fits, so that its length alone costs it nothing.
     */
    boolean couldBeExactAtLength(int length);

}
