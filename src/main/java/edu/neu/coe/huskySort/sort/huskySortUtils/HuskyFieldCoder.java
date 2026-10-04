/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalDate;
import java.util.function.ToIntFunction;

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
     * NOTE "exact" is <b>per value</b>, which is what distinguishes it from "perfect"; see
     * {@link HuskyCoder} for the three scopes and why conflating them has already cost two
     * defects. {@link CompositeHuskyCoder} folds this over fields and then over elements to reach
     * the per-array {@link Coding#perfect}.
     *
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
     * NOTE the type parameter is the <i>field's</i> boxed type, not {@code Long}, and is inferred
     * from where the coder is used: {@code add(Permit::getLot, ofRange("lot", 0, 9999))} infers
     * {@code Integer} and type-checks against an {@code int} accessor. It was fixed to
     * {@code Long} until 2026-09-29, which made every integral field need a widening lambda ---
     * {@code add(p -> (long) p.lot(), ...)} --- for no reason the caller could see.
     *
     * @param name the field's name.
     * @param min  the least value expected.
     * @param max  the greatest.
     * @param <N>  the field's type, any boxed integral type. Values are read through
     *             {@link Number#longValue()}, so a floating-point field would be truncated; this
     *             is for integral fields.
     * @return a coder for it.
     */
    static <N extends Number> HuskyFieldCoder<N> ofRange(final String name, final long min, final long max) {
        if (max < min) throw new IllegalArgumentException(name + ": max " + max + " is below min " + min);
        // A span wider than Long.MAX_VALUE cannot be held in the long arithmetic below: max - min
        // wraps, and bitsFor would then be asked for the width of a negative number. Rejecting is
        // right rather than pedantic, because the case that reaches here is a real one -- a full
        // 64-bit field -- and it has its own coder.
        final long span = max - min;
        if (span < 0)
            throw new IllegalArgumentException(name + ": the range [" + min + ", " + max + "] spans more than"
                    + " Long.MAX_VALUE, which ofRange cannot express. Use ofLong for a full-width long field,"
                    + " or narrow the range.");
        final int width = bitsFor(span);
        return new HuskyFieldCoder<>() {
            public int bits() {
                return width;
            }

            public long encode(final N value) {
                final long v = value.longValue();
                return v <= min ? 0L : v >= max ? max - min : v - min;
            }

            public boolean exact(final N value) {
                final long v = value.longValue();
                return v >= min && v <= max;
            }

            public String name() {
                return name;
            }
        };
    }

    /**
     * A field whose values are characters known to lie in {@code [lowest, highest]}.
     * <p>
     * The shared machinery behind {@link English}, {@link Ascii} and {@link ExtendedAscii}: each
     * of those is a one-character value type whose constructor refuses anything outside its
     * window, so the width is a property of the type rather than something a caller declares.
     * <p>
     * NOTE {@link #ofRange} cannot serve, because it is bounded by {@code N extends Number} and
     * a {@code Character} is not a {@code Number} --- which is also why a hand-built composite
     * over a {@code char} accessor needs a cast today. This takes the character out of the value
     * with a {@link ToIntFunction} instead.
     *
     * @param name    the field's name.
     * @param lowest  the least character the type admits.
     * @param highest the greatest.
     * @param charOf  how to read the character out of a value.
     * @param <T>     the value type.
     * @return a coder for it.
     */
    static <T> HuskyFieldCoder<T> ofCharacterWindow(final String name, final char lowest, final char highest,
                                                    final ToIntFunction<T> charOf) {
        if (highest < lowest)
            throw new IllegalArgumentException(name + ": highest " + (int) highest + " is below lowest " + (int) lowest);
        final int width = bitsFor(highest - lowest);
        return new HuskyFieldCoder<>() {
            public int bits() {
                return width;
            }

            public long encode(final T value) {
                final int c = charOf.applyAsInt(value);
                // The type's constructor has already excluded anything outside the window, so
                // the clamp is unreachable. Kept because it costs two comparisons once per
                // element and the alternative, if a constructor were ever loosened, is a code
                // wider than its declared width -- which corrupts the fields ABOVE it and
                // inverts the composite ordering. See CompositeHuskyCoder.Field.checkWidth.
                return c <= lowest ? 0L : c >= highest ? highest - lowest : c - lowest;
            }

            public boolean exact(final T value) {
                final int c = charOf.applyAsInt(value);
                return c >= lowest && c <= highest;
            }

            public String name() {
                return name;
            }
        };
    }

    /**
     * A field holding any {@code long} at all: the full 64-bit signed range, exactly.
     * <p>
     * Distinct from {@code ofRange(name, Long.MIN_VALUE, Long.MAX_VALUE)}, which cannot work --
     * that span is {@code 2^64 - 1} and overflows the {@code long} arithmetic
     * {@link #ofRange} does. More to the point, a full-width field needs no range at all: it needs
     * the signed-to-unsigned bias, which is what this applies.
     * <p>
     * It occupies the whole budget, so a composite holding one has room for nothing else -- which
     * is the honest answer, a {@code long} carrying 64 bits of ordering.
     *
     * @param name the field's name.
     * @return a coder for it.
     */
    static HuskyFieldCoder<Long> ofLong(final String name) {
        return new HuskyFieldCoder<>() {
            public int bits() {
                return 64;
            }

            public long encode(final Long value) {
                // Signed order into unsigned order, so that the concatenation's unsigned
                // comparison gives the order intended. The composite then biases the whole fold
                // back into signed space; for a lone long field the two cancel, and the code is
                // the value, which is what HuskyCoderFactory.longCoder returns.
                return value ^ Long.MIN_VALUE;
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
        // Precomputed char -> code, rather than an indexOf scan of the alphabet per character.
        // Both encode and exact run per character per element, and indexOf is linear in the
        // alphabet, so this was the whole-sort difference between 1.78x and 1.25x of a
        // hand-written coder on the permits. It is item 44's rank table at a smaller scale: when
        // the domain is enumerable, a lookup replaces a search. The table spans only up to the
        // alphabet's largest symbol -- 91 entries for the permit blocks -- and anything above that
        // takes the top code, since every symbol is then at or below it.
        final char highest = alphabet.charAt(alphabet.length() - 1);
        final char[] codes = new char[highest + 1];
        final boolean[] member = new boolean[highest + 1];
        for (char c = 0; c <= highest; c++) {
            final int index = alphabet.indexOf(c);
            member[c] = index >= 0;
            codes[c] = (char) (index >= 0 ? index + 1 : below(c, alphabet));
        }
        final char aboveAll = (char) alphabet.length();
        return new HuskyFieldCoder<>() {
            public int bits() {
                return width * perChar;
            }

            public long encode(final String value) {
                final int n = Math.min(value.length(), width);
                long result = 0L;
                for (int i = 0; i < n; i++) {
                    final char c = value.charAt(i);
                    result = (result << perChar) | (c <= highest ? codes[c] : aboveAll);
                }
                return result << ((long) perChar * (width - n));
            }

            public boolean exact(final String value) {
                if (value.length() > width) return false;
                for (int i = 0; i < value.length(); i++) {
                    final char c = value.charAt(i);
                    if (c > highest || !member[c]) return false;
                }
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
     * @return the code of the largest symbol strictly below x, or zero if there is none. Used to
     * build the lookup table, so that a character outside the alphabet weakens the ordering to a
     * tie rather than inverting it.
     */
    private static int below(final char x, final String alphabet) {
        int below = 0;
        for (int i = 0; i < alphabet.length() && alphabet.charAt(i) < x; i++) below = i + 1;
        return below;
    }
}
