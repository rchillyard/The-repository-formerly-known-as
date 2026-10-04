/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * A single character from {@code NUL} (0) through 255 inclusive: a byte's worth, which over
 * Latin-1 reaches {@code 'ÿ'}.
 * <p>
 * <b>256 distinct values, so exactly 8 bits</b>, with nothing wasted. One bit wider than
 * {@link Ascii} and in exchange it holds the accented Latin letters. Beyond that lies the rest
 * of Unicode, for which a plain {@code char} component is already 16 bits and exact, needing no
 * type at all.
 *
 * <h2>What a type gives that an annotation cannot</h2>
 * {@code @HuskyField(min = 0, max = 255) char} declares the same window and yields the same
 * 8 bits. The difference is that a declaration is a claim about data the compiler never sees,
 * where this is an <b>invariant</b>: the constructor refuses anything outside the window, so a
 * value of this type is in the window, so its code is exact. {@link #HUSKY_CODER} can therefore
 * report {@code exact} for every value it is ever given, which makes a record of such components
 * {@code perfect} --- and a perfect coding lets a husky sort skip the cleanup pass altogether.
 * An annotated {@code char} cannot promise that, because nothing stops the array holding a
 * character outside the declared range.
 * <p>
 * That matters beyond tidiness. {@code HuskyCoderFactory.asciiCoder} is only <i>quasi</i>
 * order-preserving, because it narrows with a mask, and masking is not monotonic for a character
 * outside the window --- the masking-versus-saturation question of TODO.md item 43G. Here the
 * question does not arise: there is no such character to narrow.
 *
 * <h2>When the data is not clean</h2>
 * Use {@link #clamp}, which saturates to the nearer end of the window. The loss then happens at
 * construction, in the caller's own code, where it is visible --- rather than silently inside an
 * encoder. What comes back is still a value in the window, so the invariant holds and the sort
 * still skips its cleanup; what it is not is the character you started with.
 *
 * @param value the character, which must lie in the window.
 * @see Ascii
 * @see ExtendedAscii
 */
public record ExtendedAscii(char value) implements Comparable<ExtendedAscii> {

    /**
     * The least character this type admits: {@code 0}.
     */
    public static final char LOWEST = 0;

    /**
     * The greatest character this type admits: {@code 255}.
     */
    public static final char HIGHEST = 255;

    /**
     * The width, which is a property of the type rather than of any value: 8 bits.
     */
    public static final int BITS = 8;

    /**
     * The coder for this type, found reflectively by {@link RecordHuskyCoder}, so that a record
     * component of this type needs no annotation and no special case in the derivation.
     */
    public static final HuskyFieldCoder<ExtendedAscii> HUSKY_CODER =
            HuskyFieldCoder.ofCharacterWindow("ExtendedAscii", LOWEST, HIGHEST, x -> x.value);

    /**
     * @throws IllegalArgumentException if value lies outside {@code [LOWEST, HIGHEST]}. Refusing
     *                                  is the point: see this class's documentation, and
     *                                  {@link #clamp} for the saturating alternative.
     */
    public ExtendedAscii {
        if (value < LOWEST || value > HIGHEST)
            throw new IllegalArgumentException("ExtendedAscii: " + (int) value + " ('" + value + "') is outside ["
                    + (int) LOWEST + ", " + (int) HIGHEST + "]. Use ExtendedAscii.clamp to saturate instead,"
                    + " or @HuskyField on a plain char if the window cannot be guaranteed.");
    }

    /**
     * @param c any character.
     * @return c as a {@link ExtendedAscii}, saturated to the nearer end of the window if it falls
     * outside. Order-preserving only weakly: every character at or below {@link #LOWEST} maps to
     * the same value, as does every character at or above {@link #HIGHEST}.
     */
    public static ExtendedAscii clamp(final char c) {
        return new ExtendedAscii(c < LOWEST ? LOWEST : c > HIGHEST ? HIGHEST : c);
    }

    /**
     * @param c any character.
     * @return true if c lies in the window, so that {@code new ExtendedAscii(c)} would succeed.
     */
    public static boolean admits(final char c) {
        return c >= LOWEST && c <= HIGHEST;
    }

    public int compareTo(final ExtendedAscii that) {
        return Character.compare(value, that.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
