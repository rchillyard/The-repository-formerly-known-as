/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalTime;

/**
 * A time of day to the whole second, which is 17 bits of ordering.
 *
 * <h2>Thirty bits cheaper than the nanoseconds you probably do not have</h2>
 * {@link TimeOfDay} spends 47 bits to keep a {@link LocalTime}'s full resolution. Most data does
 * not have it --- a timestamp parsed from a log, a filing time, a departure time --- and paying
 * for nanoseconds that are always zero is what leaves no room for the date beside it.
 * <p>
 * So this type refuses a time with a non-zero nano-of-second, rather than quietly dropping it.
 * Dropping is what {@code @HuskyField} would have to do, and a field that silently discards
 * precision is a field whose coding is imperfect without saying why. Use {@link #truncate} to
 * discard it deliberately, in the caller's own code.
 *
 * @param value the time, whose nano-of-second must be zero.
 * @see TimeOfDay
 * @see IsoTimestamp
 */
public record SecondOfDay(LocalTime value) implements Comparable<SecondOfDay> {

    /**
     * The width, which is a property of the type: 17 bits, being 86,399 seconds.
     */
    public static final int BITS = 17;

    /**
     * The coder, found reflectively by {@link RecordHuskyCoder}.
     */
    public static final HuskyFieldCoder<SecondOfDay> HUSKY_CODER =
            HuskyFieldCoder.ofWindow("SecondOfDay", 0, LocalTime.MAX.toSecondOfDay(), x -> x.value.toSecondOfDay());

    /**
     * @throws IllegalArgumentException if the time carries a fraction of a second.
     */
    public SecondOfDay {
        if (value.getNano() != 0)
            throw new IllegalArgumentException("SecondOfDay: " + value + " carries " + value.getNano()
                    + " nanoseconds, which this type would discard. Use SecondOfDay.truncate to drop them"
                    + " deliberately, or TimeOfDay to keep them at a cost of 47 bits rather than 17.");
    }

    /**
     * @param time any time.
     * @return it as a {@link SecondOfDay}, with any fraction of a second discarded.
     */
    public static SecondOfDay truncate(final LocalTime time) {
        return new SecondOfDay(time.withNano(0));
    }

    /**
     * @param time any time.
     * @return true if it is a whole number of seconds.
     */
    public static boolean admits(final LocalTime time) {
        return time.getNano() == 0;
    }

    public int compareTo(final SecondOfDay that) {
        return value.compareTo(that.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
