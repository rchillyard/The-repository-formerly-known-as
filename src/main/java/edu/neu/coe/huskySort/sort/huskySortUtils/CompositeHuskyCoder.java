/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * A husky coder for a composite key, built by concatenating one {@link HuskyFieldCoder} per field.
 * <p>
 * This is stage one of TODO.md item 31, whose framing was "combinator first, derivation second".
 * The paper's appendix A.4 argues that a composite-key facility is what adoption of the method
 * would require; the two composite coders this project already has, {@code PermitCoder} and
 * {@code HuskySortBenchmark.Tuple}, are both hand-written, and between them they show why.
 *
 * <h2>Why concatenation works</h2>
 * Pack each field's code into a fixed width, most significant field first, in the same order the
 * comparison uses. Then the composite is order-preserving, and the argument is just lexicographic:
 * if the first {@code i-1} fields compare equal their bits are equal, so the comparison of the
 * codes falls through to field {@code i}'s bits, which order as that field orders.
 * <p>
 * Three conditions carry that argument, and each has already cost this project a defect when it
 * was not met:
 * <ul>
 * <li><b>Fixed width per field.</b> A variable-width field lets a short value's neighbour bleed
 *     upwards. That is item 46 exactly: {@code huskyEncode(byte[])} did not pad, so {@code "b"}
 *     coded below {@code "ab"} although it sorts after it.</li>
 * <li><b>Packing order equal to comparison order.</b> {@code Tuple} states this obligation in a
 *     comment from 2020 and relies on a human to honour it. Here the builder fixes the order, and
 *     {@link #comparator()} derives the matching comparison from the same list, so the two cannot
 *     disagree.</li>
 * <li><b>Order-preserving within the width.</b> Delegated to the field coders, whose contract says
 *     so. A signed quantity must therefore be biased into an unsigned range by its field coder, as
 *     {@link HuskyFieldCoder#ofRange} does by subtracting the minimum.</li>
 * </ul>
 *
 * <h2>The budget</h2>
 * Sixty-three bits, not sixty-four: the top bit is left clear so that every code is non-negative
 * and numeric order is the order intended. {@link Builder#build} refuses to produce a coder whose
 * fields do not fit, naming the fields and their widths. That check is the whole point of item 29:
 * {@code Tuple} packs 8 + 17 + 38 = 63 bits exactly, so widening any field by one bit would push
 * the code negative and <i>invert</i> the ordering rather than degrade it -- and its widths are
 * literals inside a shift expression, with nothing to assert against.
 *
 * <h2>Perfection is computed, not declared</h2>
 * {@code perfect} is the conjunction over every field of every element, taken from each field
 * coder's {@code exact}. A husky sort skips its cleanup pass when the coding says perfect, so this
 * is the one claim that turns a slow answer into a wrong one -- which is what item 48 was, and why
 * it is computed here per element rather than asserted once.
 *
 * @param <X> the composite type being encoded.
 */
public class CompositeHuskyCoder<X> implements HuskyCoder<X> {

    /**
     * The usable width. Bit 63 stays clear so that codes are non-negative and their numeric order
     * is their intended order.
     */
    public static final int BUDGET = 63;

    /**
     * @param <X> the composite type.
     * @return a builder, to which fields are added most significant first.
     */
    public static <X> Builder<X> builder() {
        return new Builder<>();
    }

    /**
     * Adds fields in order of decreasing significance -- the same order the comparison applies
     * them in, which is what makes the concatenation order-preserving.
     *
     * @param <X> the composite type.
     */
    public static class Builder<X> {
        private final List<Field<X, ?>> fields = new ArrayList<>();
        private String name = "CompositeHuskyCoder";

        /**
         * @param accessor how to read the field from an X.
         * @param coder    how to encode it.
         * @param <T>      the field's type.
         * @return this builder.
         */
        public <T> Builder<X> add(final Function<X, T> accessor, final HuskyFieldCoder<T> coder) {
            fields.add(new Field<>(accessor, coder));
            return this;
        }

        /**
         * @param name a name for the resulting coder.
         * @return this builder.
         */
        public Builder<X> named(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return the coder.
         * @throws IllegalStateException if no fields were added, or if their widths exceed
         *                               {@link #BUDGET}. The message names every field and its
         *                               width, because the useful question at that point is which
         *                               field to narrow.
         */
        public CompositeHuskyCoder<X> build() {
            if (fields.isEmpty()) throw new IllegalStateException(name + ": no fields were added");
            int total = 0;
            for (final Field<X, ?> f : fields) total += f.coder.bits();
            if (total > BUDGET) {
                final StringBuilder sb = new StringBuilder(name + ": the fields need " + total
                        + " bits, which exceeds the " + BUDGET + " available by " + (total - BUDGET) + ". Widths are");
                for (final Field<X, ?> f : fields) sb.append(" ").append(f.coder.name()).append("=").append(f.coder.bits());
                throw new IllegalStateException(sb.append(". Narrow a field's declared range, or drop one."). toString());
            }
            return new CompositeHuskyCoder<>(name, List.copyOf(fields), total);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Fields are folded most significant first. The result is right-aligned within the budget,
     * which is safe here in a way it was not for item 46: every code this coder produces has the
     * same layout, so the low bits below the fields are zero for all of them and cannot affect a
     * comparison. It is only codes of <i>differing</i> widths that must be left-aligned.
     */
    public long huskyEncode(final X x) {
        long result = 0L;
        for (final Field<X, ?> f : fields) result = (result << f.coder.bits()) | f.encode(x);
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Perfect only if every field of every element is exact. Note that this overrides the
     * interface's default, which reports {@link #perfect()} -- a per-coder answer, where this is a
     * per-array one, since whether a composite is exact depends on the values it is given.
     */
    @Override
    public Coding huskyEncode(final X[] xs) {
        boolean isPerfect = true;
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            if (isPerfect) isPerfect = exact(xs[i]);
            result[i] = huskyEncode(xs[i]);
        }
        return new Coding(result, isPerfect);
    }

    /**
     * @param x an element.
     * @return true if every field of x encodes without loss.
     */
    public boolean exact(final X x) {
        for (final Field<X, ?> f : fields) if (!f.exact(x)) return false;
        return true;
    }

    /**
     * The comparison this encoding is order-preserving with respect to, derived from the same field
     * list in the same order.
     * <p>
     * Offered because the obligation it discharges is the one a human gets wrong. A composite coder
     * is correct only relative to a comparison, and nothing in the type system ties the two
     * together: {@code Tuple} names the obligation in a comment and leaves it there. Taking both
     * from one list removes the possibility of disagreement rather than testing for it.
     * <p>
     * NOTE this compares by <i>code</i>, field by field, so it agrees with the encoding by
     * construction -- including where a field coder ties two values the field's own ordering would
     * separate. It is therefore the right comparator for checking the encoding, and the wrong one
     * for use as the cleanup pass, which must use the type's true ordering.
     *
     * @return the comparator.
     */
    public Comparator<X> comparator() {
        Comparator<X> result = null;
        for (final Field<X, ?> f : fields) {
            final Comparator<X> byField = Comparator.comparingLong(f::encode);
            result = result == null ? byField : result.thenComparing(byField);
        }
        return result;
    }

    /**
     * @return the number of bits the fields occupy, at most {@link #BUDGET}.
     */
    public int bits() {
        return bits;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(name).append("[").append(bits).append(" of ").append(BUDGET).append(" bits:");
        for (final Field<X, ?> f : fields) sb.append(" ").append(f.coder.name()).append("=").append(f.coder.bits());
        return sb.append("]").toString();
    }

    private CompositeHuskyCoder(final String name, final List<Field<X, ?>> fields, final int bits) {
        this.name = name;
        this.fields = fields;
        this.bits = bits;
    }

    private final String name;
    private final List<Field<X, ?>> fields;
    private final int bits;

    /**
     * One field: how to read it, and how to encode what was read. The type parameter is captured
     * here so that the accessor and the coder cannot be mismatched, which a list of raw pairs
     * would allow.
     */
    private record Field<X, T>(Function<X, T> accessor, HuskyFieldCoder<T> coder) {
        long encode(final X x) {
            return coder.encode(accessor.apply(x));
        }

        boolean exact(final X x) {
            return coder.exact(accessor.apply(x));
        }
    }
}
