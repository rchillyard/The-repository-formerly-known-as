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
 * <h2>"exact" and "perfect": they are not synonyms</h2>
 * Both words are used throughout this package and they say different things. Written down here
 * 2026-10-01 at Robin's request, the distinction having been held consistently but never stated.
 * <pre>
 *   exact(value)         ONE VALUE     does this value encode without loss?
 *   Coding.perfect       ONE ARRAY     did every element of this array encode without loss?
 *   perfect()            THE CODER     does every possible value of X encode without loss?
 *   perfectForLength(n)  THE CODER     would a sequence of this length fit? (a necessary
 *                                      condition on one aspect of a value, not a sufficient one)
 * </pre>
 * So <b>exact is per value, perfect is a quantifier over exact</b>: {@link Coding#perfect} is the
 * conjunction of {@code exact} over the elements of one array, and {@link #perfect()} is the
 * claim that {@code exact} holds for every value there could ever be, which is why a coder may
 * only assert it when its own construction guarantees it.
 *
 * <h3>Why the difference is worth keeping straight</h3>
 * {@code Coding.perfect} is the one the sorts actually read --- {@code AbstractHuskySort.postSort},
 * {@code QuickHuskySort.sort} and {@code MergeHuskySort.sort} all skip the cleanup pass when it is
 * true. The two directions are therefore not symmetric: reporting perfect when some element was
 * not exact turns a slow answer into a <b>wrong</b> one, while reporting imperfect when every
 * element was exact merely costs a pass that was not needed. Claim it only when it is computed or
 * proved.
 * <p>
 * This has already cost two defects. Item 48: {@code BaseHuskySequenceCoder} decided perfection
 * from {@code perfectForLength} alone, treating a necessary condition as sufficient, so a
 * narrowing coder returned an unsorted array and said it was perfect. Item 50:
 * {@code MergeHuskySort}'s merge had been wrong for four years and only a perfect coding exposed
 * it, every other coding having had the defect repaired by the cleanup pass it triggered.
 * <p>
 * NOTE one wart left alone, since renaming it would change a public interface:
 * {@link HuskySequenceCoder#perfectForLength} says "perfect" for what is really an {@code exact}
 * predicate applied to one dimension of a value. Read it as "could be exact at this length".
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
     * <p>
     * This is the strongest of the three claims described in this interface's documentation: not
     * that a particular value encodes exactly, nor that every element of one array did, but that
     * every value of X ever will. Default false, which is the safe answer --- it costs a cleanup
     * pass that may not be needed, where a wrong true costs correctness.
     *
     * @return true if the resulting longs are perfect for ANY value of X.
     * By default, this method returns false.
     */
    default boolean perfect() {
        return false;
    }
}
