/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalDate;

/**
 * An order-preserving encoding of one field of a composite key into a fixed number of bits.
 * <p>
 * This is the piece {@link CompositeHuskyCoder} concatenates. Splitting it out is what lets the
 * budget be checked and the ordering obligation be discharged mechanically, rather than left to a
 * comment asking the author to remember -- which is how {@code PermitCoder} and
 * {@code HuskySortBenchmark.Tuple}, the two hand-written composite coders in this project, both do
 * it today. See TODO.md items 29, 30 and 31, and appendix A.4 of the paper.
 * <p>
 * <b>The contract, all three parts of which the concatenation relies on.</b>
 * <ol>
 * <li>{@link #encode} returns a value in {@code [0, 2^bits)}. Nothing outside that range, because a
 *     field that overflows its width corrupts the field above it rather than merely losing
 *     precision.</li>
 * <li>{@code encode} is non-decreasing in the field's own ordering: if {@code a} sorts at or before
 *     {@code b} then {@code encode(a) <= encode(b)}. Ties are allowed -- they cost the cleanup pass
 *     a little work -- but an inversion is not, because it cannot be repaired by anything the
 *     composite does.</li>
 * <li>{@link #exact} says whether this particular value survived without loss. A coder may narrow,
 *     saturate or truncate, and is expected to; what it may not do is claim exactness it does not
 *     have. The composite's {@code perfect} is the conjunction over every field of every element,
 *     and a husky sort skips its cleanup pass when that is true, so an optimistic answer here does
 *     not produce a slow sort but a wrong one. The same mistake cost this project item 48.</li>
 * </ol>
 *
 * @param <T> the field's type.
 */
public interface HuskyFieldCoder<T> {

    /**
     * @return the number of bits this coder occupies, which must be the same for every value: the
     * concatenation is order-preserving only because each field has a fixed width. A variable-width
     * field would let a short value's neighbour bleed into the bits above it, which is the defect
     * {@code HuskyCoder.huskyEncode(byte[])} carried until item 46.
     */
    int bits();

    /**
     * @param value the field value.
     * @return its code, in {@code [0, 2^bits())}, non-decreasing in the field's ordering.
     */
    long encode(T value);

    /**
     * @param value the field value.
     * @return true if {@code encode(value)} loses nothing, so that this value is ordered exactly
     * against every other value this coder reports exact.
     */
    default boolean exact(final T value) {
        return true;
    }

    /**
     * @return a short name, used in the composite's name and in budget messages.
     */
    String name();

    // ---------- factories ----------

    /**
     * A field holding an integer known to lie in {@code [min, max]}.
     * <p>
     * The width is the smallest that spans the range, and the code is {@code value - min}, so the
     * declared range is doing real work: {@code Tuple} packs a zip code in 17 bits only because zips
     * stop at 99,999, and a year in 8 only because it subtracts 1850 first (item 29). A value
     * outside the range is clamped to the nearer end and reported inexact, which keeps the ordering
     * weak rather than wrong.
     *
     * @param name the field's name.
     * @param min  the least value expected.
     * @param max  the greatest.
     * @return a coder for it.
     */
    static HuskyFieldCoder<Long> ofRange(final String name, final long min, final long max) {
        if (max < min) throw new IllegalArgumentException(name + ": max " + max + " is below min " + min);
        final int width = bitsFor(max - min);
        return new HuskyFieldCoder<>() {
            public int bits() {
                return width;
            }

            public long encode(final Long value) {
                final long v = value;
                return v <= min ? 0L : v >= max ? max - min : v - min;
            }

            public boolean exact(final Long value) {
                return value >= min && value <= max;
            }

            public String name() {
                return name;
            }
        };
    }

    /**
     * A field holding a date known to fall in {@code [epoch, epoch + days]}.
     * <p>
     * Stored as an offset from the epoch rather than as a year/month/day triple, so that
     * chronological order is numeric order with no further work -- the trick {@code PermitCoder}
     * uses to fit the San Francisco filing dates into eleven bits.
     *
     * NOTE {@code days} is the greatest <i>offset from the epoch</i> that must be representable,
     * not the span between the earliest and latest dates. The distinction is easy to lose and cost
     * an off-by-one when this class was first tested against {@code PermitCoder}, whose comment
     * reads "span 1,878 days": the San Francisco filings run from offset 1 to offset 1879, so 1878
     * is their span and 1879 is the bound. An offset is what the field actually stores, so it is
     * what the declaration takes.
     *
     * @param name  the field's name.
     * @param epoch the day from which dates are counted; no date before it is exact.
     * @param days  the greatest offset from the epoch, in days, that must be representable.
     * @return a coder for it.
     */
    static HuskyFieldCoder<LocalDate> ofDate(final String name, final LocalDate epoch, final long days) {
        final HuskyFieldCoder<Long> offsets = ofRange(name, 0L, days);
        return new HuskyFieldCoder<>() {
            public int bits() {
                return offsets.bits();
            }

            public long encode(final LocalDate value) {
                return offsets.encode(value.toEpochDay() - epoch.toEpochDay());
            }

            public boolean exact(final LocalDate value) {
                return offsets.exact(value.toEpochDay() - epoch.toEpochDay());
            }

            public String name() {
                return name;
            }
        };
    }

    /**
     * A field holding a string over a known alphabet, packed {@code width} characters at
     * {@code ceil(log2(alphabet.length() + 1))} bits apiece.
     * <p>
     * This is {@code PermitCoder.encodeString} promoted, which was TODO item 30 and is the
     * prerequisite for this class. It is the only order-preserving ordered-alphabet packer in the
     * project: {@code HuskyCoderFactory.stringToLong} masks raw char values instead, which gives
     * neither the padding property nor the out-of-range property a composite field needs.
     * <p>
     * Characters are coded from 1 upwards in the alphabet's own order, leaving 0 for padding. Since
     * 0 is below every real character, a string sorts before any string extending it, which is what
     * {@code String.compareTo} does with a prefix. A character outside the alphabet takes the code
     * of the largest symbol below it, so the ordering weakens to a tie rather than inverting -- and
     * {@code exact} returns false, so nothing downstream mistakes that tie for exactness.
     *
     * @param name     the field's name.
     * @param width    the number of character positions; longer strings are truncated and inexact.
     * @param alphabet the characters which have codes, <b>in ascending order</b>.
     * @return a coder for it.
     */
    static HuskyFieldCoder<String> ofString(final String name, final int width, final String alphabet) {
        if (width < 1) throw new IllegalArgumentException(name + ": width must be positive, not " + width);
        if (alphabet.isEmpty()) throw new IllegalArgumentException(name + ": the alphabet is empty");
        for (int i = 1; i < alphabet.length(); i++)
            if (alphabet.charAt(i - 1) >= alphabet.charAt(i))
                throw new IllegalArgumentException(name + ": the alphabet must ascend, but '"
                        + alphabet.charAt(i - 1) + "' is not below '" + alphabet.charAt(i) + "' at index " + i);
        final int perChar = bitsFor(alphabet.length());
        return new HuskyFieldCoder<>() {
            public int bits() {
                return width * perChar;
            }

            public long encode(final String value) {
                long result = 0L;
                for (int i = 0; i < width; i++) {
                    result <<= perChar;
                    if (i < value.length()) result |= codeOf(value.charAt(i), alphabet);
                }
                return result;
            }

            public boolean exact(final String value) {
                if (value.length() > width) return false;
                for (int i = 0; i < value.length(); i++)
                    if (alphabet.indexOf(value.charAt(i)) < 0) return false;
                return true;
            }

            public String name() {
                return name;
            }
        };
    }

    /**
     * A one-bit field. False sorts below true, matching {@link Boolean#compareTo}.
     *
     * @param name the field's name.
     * @return a coder for it.
     */
    static HuskyFieldCoder<Boolean> ofBoolean(final String name) {
        return new HuskyFieldCoder<>() {
            public int bits() {
                return 1;
            }

            public long encode(final Boolean value) {
                return value ? 1L : 0L;
            }

            public String name() {
                return name;
            }
        };
    }

    /**
     * A field holding an enum constant, coded by ordinal.
     * <p>
     * NOTE that this orders by declaration order, which is what {@link Enum#compareTo} does. If the
     * type's intended ordering is something else -- alphabetical by name, say -- this coder is the
     * wrong one and would invert rather than weaken.
     *
     * @param name the field's name.
     * @param type the enum class.
     * @param <E>  the enum type.
     * @return a coder for it.
     */
    static <E extends Enum<E>> HuskyFieldCoder<E> ofEnum(final String name, final Class<E> type) {
        final int count = type.getEnumConstants().length;
        final int width = bitsFor(Math.max(count - 1, 0));
        return new HuskyFieldCoder<>() {
            public int bits() {
                return width;
            }

            public long encode(final E value) {
                return value.ordinal();
            }

            public String name() {
                return name;
            }
        };
    }

    /**
     * @param span the greatest value to be represented.
     * @return the fewest bits that hold {@code 0..span}, and at least one.
     */
    static int bitsFor(final long span) {
        if (span < 0) throw new IllegalArgumentException("span must not be negative: " + span);
        return span == 0 ? 1 : 64 - Long.numberOfLeadingZeros(span);
    }

    /**
     * @return the code of x, one more than its index in the alphabet, so that zero stays available
     * as the padding symbol below every real character; or, for a character the alphabet does not
     * hold, the code of the largest symbol below it.
     */
    private static long codeOf(final char x, final String alphabet) {
        final int index = alphabet.indexOf(x);
        if (index >= 0) return index + 1L;
        int below = 0;
        for (int i = 0; i < alphabet.length() && alphabet.charAt(i) < x; i++) below = i + 1;
        return below;
    }
}
