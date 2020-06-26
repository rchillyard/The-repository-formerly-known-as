package edu.neu.coe.huskySort.util;

import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

public class MyTimeoutBuilder extends org.junit.rules.Timeout {
    /**
     * Create a {@code Timeout} instance with the timeout specified
     * in milliseconds.
     * <p>
     * This constructor is deprecated.
     * <p>
     * Instead use {@link #Timeout(long, TimeUnit)},
     * {@link Timeout#millis(long)}, or {@link Timeout#seconds(long)}.
     *
     * @param millis      the maximum time in milliseconds to allow the
     *                    test to run before it should timeout
     * @param speedFactor
     */
    public MyTimeoutBuilder(int millis, double speedFactor) {
        super((int) Math.round(millis / speedFactor));
    }

    /**
     * Create a {@code Timeout} instance with the timeout specified
     * at the timeUnit of granularity of the provided {@code TimeUnit}.
     *
     * @param timeout  the maximum time to allow the test to run
     *                 before it should timeout
     * @param timeUnit the time unit for the {@code timeout}
     * @since 4.12
     */
    public MyTimeoutBuilder(long timeout, TimeUnit timeUnit) {
        super(timeout, timeUnit);
    }

    /**
     * Create a {@code Timeout} instance initialized with values form
     * a builder.
     *
     * @param builder
     * @since 4.12
     */
    protected MyTimeoutBuilder(Builder builder) {
        super(builder);
    }

    public static Timeout create(int millis, double speedFactor) {
        return new MyTimeoutBuilder(millis, speedFactor);
    }
}
