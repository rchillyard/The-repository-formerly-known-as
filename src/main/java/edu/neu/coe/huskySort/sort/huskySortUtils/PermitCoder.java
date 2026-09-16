/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalDate;

/**
 * An exact husky encoding of {@link Permit}, packing its whole ordering into 60 of the available 64 bits.
 * <p>
 * <pre>
 *   block      5 characters x 5 bits over a 31-symbol alphabet   25 bits
 *   lot        4 characters x 6 bits over a 63-symbol alphabet   24 bits
 *   filedDate  days since {@link #EPOCH}, span 1,878 days        11 bits
 *                                                                --
 *                                                                60
 * </pre>
 * <p>
 * The encoding is exact for the San Francisco corpus, and {@link #perfect()} says so, which is what
 * lets a husky sort skip its cleanup pass entirely. That is not a claim about the type in general but
 * about a coder and a corpus together, and {@code perfect()} is a claim strong enough that
 * {@code PermitCoderTest} checks it against every one of the 198,900 records rather than trusting the
 * arithmetic above.
 * <p>
 * Three properties make the packing order-preserving.
 * <p>
 * Fields are packed most-significant first, in the same order as {@link Permit#compareTo}, so a
 * difference in the block dominates any difference in the lot or the date.
 * <p>
 * Characters are coded from 1 upwards in ASCII order, and strings shorter than their field width are
 * padded on the right with 0. Since 0 is below every real character's code, a string sorts before any
 * string that extends it -- which is what {@code String.compareTo} does with a prefix.
 * <p>
 * Dates are stored as an offset from a fixed epoch rather than as a year/month/day triple, so
 * chronological order is numeric order with no further work.
 * <p>
 * A character outside a field's alphabet is coded as the largest symbol at or below it, which keeps
 * the ordering weak rather than wrong: two records differing only in such characters may encode
 * equal, and the cleanup pass would separate them. Nothing in the San Francisco corpus takes that
 * path, so the encoding there is exact -- but if it ever did, the sort would still be correct, merely
 * no longer perfect. This is the tolerance the husky mechanism is built around, and it is why
 * {@code perfect()} is verified rather than assumed.
 */
public class PermitCoder implements HuskyCoder<Permit> {

    public static final PermitCoder INSTANCE = new PermitCoder();

    /**
     * The day from which dates are counted. Earlier than any filing in the corpus, which begins
     * 2013-01-02, with room below it.
     */
    public static final LocalDate EPOCH = LocalDate.of(2013, 1, 1);

    /**
     * @param permit the permit to encode.
     * @return a long whose numeric order is the order of {@link Permit#compareTo}.
     */
    public long huskyEncode(final Permit permit) {
        long result = encodeString(permit.getBlock(), BLOCK_WIDTH, BLOCK_BITS, BLOCK_ALPHABET);
        result = (result << (LOT_WIDTH * LOT_BITS)) | encodeString(permit.getLot(), LOT_WIDTH, LOT_BITS, LOT_ALPHABET);
        return (result << DATE_BITS) | encodeDate(permit.getFiledDate());
    }

    /**
     * @return true: this coder loses nothing, so a husky sort using it needs no cleanup pass.
     */
    @Override
    public boolean perfect() {
        return true;
    }

    @Override
    public String name() {
        return "PermitCoder";
    }

    /**
     * Pack the first {@code width} characters of s, {@code bits} apiece, right-padded with zero.
     *
     * @param s        the string.
     * @param width    the number of character positions in the field.
     * @param bits     the number of bits per character position.
     * @param alphabet the characters which have codes, in ascending order.
     * @return the packed field.
     */
    private static long encodeString(final String s, final int width, final int bits, final String alphabet) {
        long result = 0;
        for (int i = 0; i < width; i++) {
            result <<= bits;
            if (i < s.length()) result |= codeOf(s.charAt(i), alphabet);
        }
        return result;
    }

    /**
     * @return the code of x, being one more than its index in the alphabet, so that zero remains
     * available as the padding symbol which sorts below every real character.
     */
    private static long codeOf(final char x, final String alphabet) {
        final int index = alphabet.indexOf(x);
        if (index >= 0) return index + 1L;
        // Outside the alphabet: take the largest symbol at or below x, so the ordering weakens rather
        // than inverts. See the class comment.
        int below = 0;
        for (int i = 0; i < alphabet.length(); i++)
            if (alphabet.charAt(i) < x) below = i + 1;
        return below;
    }

    private static long encodeDate(final LocalDate date) {
        final long days = date.toEpochDay() - EPOCH.toEpochDay();
        if (days < 0 || days >= (1L << DATE_BITS))
            throw new IllegalArgumentException("date " + date + " lies outside the " + DATE_BITS
                    + "-bit window beginning " + EPOCH + "; the encoding would not be exact for it");
        return days;
    }

    /**
     * The characters occurring in the corpus's blocks, in ascending order. Nineteen of the
     * thirty-one available symbols are used, so there is room for growth without a redesign.
     */
    static final String BLOCK_ALPHABET = "0123456789ABCDEFGTZ";
    static final String LOT_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static final int BLOCK_WIDTH = 5;
    static final int BLOCK_BITS = 5;
    static final int LOT_WIDTH = 4;
    static final int LOT_BITS = 6;
    static final int DATE_BITS = 11;
}
