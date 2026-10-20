/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalTime;

/**
 * A time of day from 00:00:00.000000000 through 23:59:59.999999999 inclusive, at
 * {@link LocalTime}'s full nanosecond resolution.
 * <p>
 * <b>86,400,000,000,000 distinct values, so 47 bits</b> of ordering. Thirty bits dearer than
 * {@link SecondOfDay}, which is what sub-second precision costs and why it does not fit beside a
 * date.
 *
 * <h2>The one window type that needs no invariant</h2>
 * {@link LocalTime}'s entire domain is 0 through 86,399,999,999,999 nanoseconds, and that fits in
 * 47 bits. So unlike {@link IsoDate} or {@link English}, this type's constructor has nothing to
 * refuse: <b>every</b> {@code LocalTime} codes exactly, and {@link #HUSKY_CODER} reports
 * {@code exact} unconditionally because there is no value for which it could not.
 *
 * <h2>It does not combine with a date</h2>
 * 22 + 47 = 69, so {@code record R(IsoDate d, TimeOfDay t)} overruns the budget by five bits.
 * {@link CompositeHuskyCoder} handles that correctly --- it truncates from the low end, losing
 * resolution in the time rather than inverting anything, and reports the coding imperfect --- but
 * if you want a timestamp, {@link IsoTimestamp} is the type that fits, at 39 bits. Use this one
 * for times alone, or alongside fields narrow enough to leave room.
 *
 * @param value the time, at whatever resolution it carries.
 * @see SecondOfDay
 * @see IsoTimestamp
 */
public record TimeOfDay(LocalTime value) implements Comparable<TimeOfDay> {

    /**
     * The width, which is a property of the type: 47 bits, being the whole of a day in
     * nanoseconds.
     */
    public static final int BITS = 47;

    /**
     * The coder, found reflectively by {@link RecordHuskyCoder}.
     */
    public static final HuskyFieldCoder<TimeOfDay> HUSKY_CODER =
            HuskyFieldCoder.ofWindow("TimeOfDay", LocalTime.MIN.toNanoOfDay(), LocalTime.MAX.toNanoOfDay(),
                    x -> x.value.toNanoOfDay());

    public int compareTo(final TimeOfDay that) {
        return value.compareTo(that.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
