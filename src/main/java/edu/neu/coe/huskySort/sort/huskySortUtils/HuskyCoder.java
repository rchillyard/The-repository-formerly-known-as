/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.text.CollationKey;
import java.text.Collator;

/**
 * This interface models the essence of the Husky Sort mechanism.
 * Elements in a collection are encoded using the huskyEncode method.
 * As far as possible, the codes should be monotonically increasing with the values.
 * But, all is not lost if that is not the case, for the result is a partially-sorted array
 * with a relatively small number of inversions which can be cleared up in phase two of the sort,
 * in linear time.
 *
 * @param <X> the underlying type for this coder.
 */
public interface HuskyCoder<X> {

    /**
     * @return the name of this coder.
     */
    default String name() {
        return "HuskyCoder";
    }

    /**
     * Encode x as a long.
     * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
     * If this cannot be guaranteed, then the result of imperfect(z) will be true.
     *
     * @param x the X value to encode.
     * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
     */
    long huskyEncode(X x);


    /**
     * Encode a byte array as a long, most significant byte first, using the first 7 bytes and
     * padding on the right with zeroes.
     * <p>
     * The padding is what makes the result order-preserving across arrays of <i>different</i>
     * length, and it was missing until 2026-09-24 (TODO.md item 46). Without it a short array
     * landed in the low bits and so always coded below a longer one whatever the bytes said:
     * {@code "b"} is {@code 0x62} and {@code "ab"} is {@code 0x6162}, so {@code "b"} coded first
     * although {@code "ab"} sorts first. {@link HuskyCoderFactory}'s {@code stringToLong} has
     * always shifted by {@code bitWidth * padding} at the end for exactly this reason; this path
     * never did.
     * <p>
     * Seven bytes rather than eight because the eighth would run into the sign bit. Arrays longer
     * than seven bytes are truncated, which is a genuine loss of information and is why
     * {@link #huskyEncode(CollationKey[])} reports such a coding imperfect.
     *
     * @param bs the byte array to encode.
     * @return a long based on the first seven of the given bytes, left-aligned.
     */
    default long huskyEncode(final byte[] bs) {
        final int n = Math.min(bs.length, 7);
        long result = 0L;
        for (int i = 0; i < n; i++) result = (result << 8) | bs[i] & 0xFF;
        return result << 8 * (7 - n);
    }

    /**
     * Encode an array of Xs.
     *
     * @param xs an array of X elements.
     * @return an array of longs corresponding to the Husky codes of the X elements.
     */
    default Coding huskyEncode(final X[] xs) {
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) result[i] = huskyEncode(xs[i]);
        return new Coding(result, perfect());
    }

    default Collator getCollator() {
        return null;
    }

    /**
     * Encode an array of CollationKeys.
     *
     * @param xs an array of CollationKeys.
     * @return an array of longs corresponding to the Husky codes of the X elements.
     */
    default Coding huskyEncode(final CollationKey[] xs) {
        boolean perfect = true;
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            final byte[] byteArray = xs[i].toByteArray();
            final long code = getCode(byteArray);
            if (byteArray.length > 7) perfect = false;
            result[i] = code;
        }
        return new Coding(result, perfect);
    }

    default long getCode(final byte[] byteArray) {
        return huskyEncode(byteArray);
    }

    /**
     * Method to determine if this Husky Coder is perfect for a class of objects (X).
     *
     * @return true if the resulting longs are perfect for ANY value of X.
     * By default, this method returns false.
     */
    default boolean perfect() {
        return false;
    }
}
