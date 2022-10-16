/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * This interface extends HuskySort for object which are sub-classes of CharSequence.
 *
 * @param <X> the underlying type for this coder, which extends CharSequence and thus has a length.
 */
public interface HuskySequenceCoder<X extends CharSequence> extends HuskyCoder<X> {

    /**
     * Method to determine if this Husky Coder is perfect for a sequence of the given length.
     * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
     * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
     *
     * @param length the length of a particular sequence.
     * @return false if the resulting long for the String will likely not be unique.
     */
    boolean perfectForLength(int length);

//    /**
//     * Class to combine the long codes for an array of objects with a determination of coding perfection.
//     */
//    static class XCoding {
//        public XCoding(long[] longs, boolean perfect) {
//            this.longs = longs;
//            this.perfect = perfect;
//        }
//
//        public final long[] longs;
//        public final boolean perfect;
//    }
}
