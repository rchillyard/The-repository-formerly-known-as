/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares how one component of a record should be encoded by {@link RecordHuskyCoder}.
 * <p>
 * <b>It is always optional.</b> Without it a component is encoded as widely as its type allows,
 * which is exact for {@code boolean}, an enum, {@code byte}, {@code short}, {@code char} and
 * {@code int}, and which for a {@code String} or a {@code LocalDate} falls back to a general
 * default that is unlikely to be exact. Since the budget is spent most significant field first and
 * anything left over is truncated from the bottom, a record of several unannotated {@code int}s
 * still sorts correctly; it simply stops discriminating on its least significant fields and reports
 * {@code perfect} false.
 * <p>
 * So this annotation buys <i>exactness</i>, not correctness. That is the same bargain the rest of
 * the mechanism offers, and it is why the derivation does not insist on being told anything: a
 * caller who knows their zip codes stop at 99,999 can say so and get seventeen bits instead of
 * thirty-two, and a caller who does not know still gets a working sort.
 *
 * <h2>Which attributes apply to which component type</h2>
 * <pre>
 *   integral (byte, short, int, long, char)   min, max
 *   String                                    chars, alphabet
 *   LocalDate                                 epoch, days
 *   boolean, enum                             none -- the type fixes the width
 * </pre>
 * An attribute given for a type that does not use it is an error rather than being ignored, since
 * a silently ignored width declaration is exactly the kind of thing this class exists to prevent.
 *
 * @see RecordHuskyCoder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface HuskyField {

    /**
     * @return the least value this integral component takes. Defaults to the component type's own
     * minimum.
     */
    long min() default Long.MIN_VALUE;

    /**
     * @return the greatest value this integral component takes. Defaults to the component type's
     * own maximum.
     */
    long max() default Long.MAX_VALUE;

    /**
     * @return how many character positions of this String component to encode. Characters beyond
     * it are dropped, and the value is then not exact.
     */
    int chars() default 0;

    /**
     * @return the characters this String component draws on, <b>in ascending order</b>. A narrower
     * alphabet costs fewer bits per position: nineteen symbols need five bits where the full ASCII
     * range needs seven.
     */
    String alphabet() default "";

    /**
     * @return the earliest date this LocalDate component takes, in ISO-8601 form, e.g.
     * {@code "2013-01-01"}. Dates before it are not exact.
     */
    String epoch() default "";

    /**
     * @return the greatest offset from {@link #epoch()}, in days, that must be representable.
     * NOTE this is the greatest offset, not the span between the earliest and latest dates; see
     * {@link HuskyFieldCoder#ofDate}, where the distinction has already cost one off-by-one.
     */
    long days() default 0L;
}
