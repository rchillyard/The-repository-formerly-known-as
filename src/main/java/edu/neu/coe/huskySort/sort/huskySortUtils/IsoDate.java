/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalDate;

/**
 * A date in the four-digit ISO years, 0001-01-01 through 9999-12-31, which is 22 bits of ordering.
 *
 * <h2>Why this window and not an epoch</h2>
 * {@link HuskyFieldCoder#ofDate} and {@code @HuskyField(epoch = ..., days = ...)} ask the caller
 * for an epoch and a span, because the right ones depend entirely on the data: {@code PermitCoder}
 * wants 2013-01-01 and 1,879 days, another corpus would want something else. That is the correct
 * answer when you know your data and want the narrowest possible field --- eleven bits, in the
 * permit case, against the twenty-two here.
 * <p>
 * This type is for when you do not, or do not want to say. The four-digit ISO year is the one
 * window that is <b>natural rather than chosen</b>: it is the range a date is normally written in,
 * it needs no declaration, and at 3,652,058 days it costs only twenty-two bits --- so a date and a
 * time to the second fit a machine word together with twenty-five to spare. A {@link LocalDate} can
 * hold years out to {@code +-999,999,999}, so the constructor has real work to do, but no realistic
 * business date is outside this window.
 *
 * <h2>The invariant, and what it buys</h2>
 * As with {@link English}: the constructor refuses anything outside the window, so a value of this
 * type codes exactly, so a record of such components is {@code perfect} --- and a husky sort skips
 * its cleanup pass entirely. Use {@link #clamp} when the data cannot be guaranteed; the loss then
 * happens in the caller's own code, where it can be seen.
 *
 * @param value the date, whose year must lie in {@code [1, 9999]}.
 * @see IsoTimestamp
 */
public record IsoDate(LocalDate value) implements Comparable<IsoDate> {

    /**
     * The earliest date this type admits.
     */
    public static final LocalDate LOWEST = LocalDate.of(1, 1, 1);

    /**
     * The latest date this type admits.
     */
    public static final LocalDate HIGHEST = LocalDate.of(9999, 12, 31);

    /**
     * The width, which is a property of the type: 22 bits, being 3,652,058 days.
     */
    public static final int BITS = 22;

    /**
     * The coder, found reflectively by {@link RecordHuskyCoder}, so that a component of this type
     * needs no annotation.
     */
    public static final HuskyFieldCoder<IsoDate> HUSKY_CODER =
            HuskyFieldCoder.ofWindow("IsoDate", LOWEST.toEpochDay(), HIGHEST.toEpochDay(),
                    x -> x.value.toEpochDay());

    /**
     * @throws IllegalArgumentException if the date falls outside the four-digit ISO years.
     */
    public IsoDate {
        if (value.isBefore(LOWEST) || value.isAfter(HIGHEST))
            throw new IllegalArgumentException("IsoDate: " + value + " is outside [" + LOWEST + ", " + HIGHEST
                    + "]. Use IsoDate.clamp to saturate, or @HuskyField(epoch=..., days=...) on a"
                    + " plain LocalDate for a narrower window of your own choosing.");
    }

    /**
     * @param date any date.
     * @return it as an {@link IsoDate}, saturated to the nearer end of the window if outside.
     */
    public static IsoDate clamp(final LocalDate date) {
        return new IsoDate(date.isBefore(LOWEST) ? LOWEST : date.isAfter(HIGHEST) ? HIGHEST : date);
    }

    /**
     * @param date any date.
     * @return true if it lies in the window, so that {@code new IsoDate(date)} would succeed.
     */
    public static boolean admits(final LocalDate date) {
        return !date.isBefore(LOWEST) && !date.isAfter(HIGHEST);
    }

    public int compareTo(final IsoDate that) {
        return value.compareTo(that.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
