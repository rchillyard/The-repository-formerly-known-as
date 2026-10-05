/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A date and time from 0001-01-01T00:00:00 through 9999-12-31T23:59:59 inclusive: the four-digit
 * ISO years, to the whole second.
 * <p>
 * <b>315,537,897,600 distinct values, so 39 bits</b> of ordering, which is 3,652,059 days of
 * 86,400 seconds. Equivalently {@link IsoDate}'s 22 bits plus {@link SecondOfDay}'s 17, and
 * encoded as one number rather than two fields for the reason given below.
 *
 * <h2>The combination that fits</h2>
 * A date costs 22 bits ({@link IsoDate}) and a time to the second costs 17 ({@link SecondOfDay}),
 * so a timestamp to the second costs 39 and leaves 25 of the budget for whatever else the record
 * holds. At full precision it would not fit at all: 22 + 47 = 69, five bits over. That is not a
 * limitation of this implementation but of the method --- a husky code is 64 bits, and a
 * nanosecond-resolution timestamp over four-digit years needs 69 of them. Something has to give,
 * and dropping sub-second precision is usually the cheapest thing to lose.
 * <p>
 * Encoded as one contiguous quantity, {@code (epochDay - epochDay(0001-01-01)) * 86400 + secondOfDay},
 * rather than as two concatenated fields. The two are equivalent in ordering; one number is
 * simpler, and makes it obvious that the width is 39 and not "22 then 17".
 *
 * <h2>The invariant</h2>
 * Both halves: the year must lie in {@code [1, 9999]} and the nano-of-second must be zero. A value
 * of this type therefore codes exactly, so a record of such components is {@code perfect} and a
 * husky sort skips its cleanup pass. {@link #truncate} applies both corrections deliberately.
 *
 * @param value the timestamp, in the ISO years and on a whole second.
 * @see IsoDate
 * @see SecondOfDay
 * @see TimeOfDay
 */
public record IsoTimestamp(LocalDateTime value) implements Comparable<IsoTimestamp> {

    /**
     * The earliest timestamp this type admits.
     */
    public static final LocalDateTime LOWEST = LocalDateTime.of(IsoDate.LOWEST, LocalTime.MIN);

    /**
     * The latest timestamp this type admits, which is a whole second.
     */
    public static final LocalDateTime HIGHEST = LocalDateTime.of(IsoDate.HIGHEST, LocalTime.of(23, 59, 59));

    /**
     * Seconds in a day, the multiplier that flattens a date and a time into one quantity.
     */
    private static final long SECONDS_PER_DAY = 86_400L;

    /**
     * The width, which is a property of the type: 39 bits.
     */
    public static final int BITS = 39;

    /**
     * The coder, found reflectively by {@link RecordHuskyCoder}.
     */
    public static final HuskyFieldCoder<IsoTimestamp> HUSKY_CODER =
            HuskyFieldCoder.ofWindow("IsoTimestamp", 0, seconds(HIGHEST), x -> seconds(x.value));

    /**
     * @throws IllegalArgumentException if the year is outside the ISO window, or the timestamp
     *                                  carries a fraction of a second.
     */
    public IsoTimestamp {
        if (!IsoDate.admits(value.toLocalDate()))
            throw new IllegalArgumentException("IsoTimestamp: " + value + " is outside [" + LOWEST + ", "
                    + HIGHEST + "]. Use IsoTimestamp.truncate to saturate and round.");
        if (value.getNano() != 0)
            throw new IllegalArgumentException("IsoTimestamp: " + value + " carries " + value.getNano()
                    + " nanoseconds, which this type would discard. Use IsoTimestamp.truncate to drop"
                    + " them deliberately. There is no 64-bit husky code that keeps them beside a date:"
                    + " 22 bits of date and 47 of nanoseconds need 69.");
    }

    /**
     * @param timestamp any timestamp.
     * @return it as an {@link IsoTimestamp}: saturated into the ISO years if outside them, and
     * with any fraction of a second discarded.
     */
    public static IsoTimestamp truncate(final LocalDateTime timestamp) {
        final LocalDate date = timestamp.toLocalDate();
        if (date.isBefore(IsoDate.LOWEST)) return new IsoTimestamp(LOWEST);
        if (date.isAfter(IsoDate.HIGHEST)) return new IsoTimestamp(HIGHEST);
        return new IsoTimestamp(timestamp.withNano(0));
    }

    /**
     * @param timestamp any timestamp.
     * @return true if it lies in the window and is a whole second.
     */
    public static boolean admits(final LocalDateTime timestamp) {
        return IsoDate.admits(timestamp.toLocalDate()) && timestamp.getNano() == 0;
    }

    /**
     * @param timestamp a timestamp in the window.
     * @return seconds since the start of the window, which is what gets encoded.
     */
    private static long seconds(final LocalDateTime timestamp) {
        return (timestamp.toLocalDate().toEpochDay() - IsoDate.LOWEST.toEpochDay()) * SECONDS_PER_DAY
                + timestamp.toLocalTime().toSecondOfDay();
    }

    public int compareTo(final IsoTimestamp that) {
        return value.compareTo(that.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
