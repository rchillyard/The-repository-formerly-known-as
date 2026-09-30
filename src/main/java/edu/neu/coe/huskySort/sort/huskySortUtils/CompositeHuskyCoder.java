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
 * <h2>The budget, and what happens when it is exceeded</h2>
 * Sixty-three bits, not sixty-four: the top bit is left clear so that every code is non-negative
 * and numeric order is the order intended.
 * <p>
 * Fields that do not fit are <b>truncated, not rejected</b>. Robin's framing, 2026-09-29: running
 * out of bits is not a different kind of problem from a field that saturates or a string that is
 * too long -- all three are the encoding being imperfect, and this mechanism is built to tolerate
 * that. Refusing to build would impose an exactness requirement a husky sort does not need.
 * <p>
 * <b>What must be got right is which bits go.</b> The budget is spent most significant field first,
 * so a field straddling the boundary keeps its top bits and loses its bottom ones, and a field past
 * the boundary contributes nothing. Losing low bits weakens the ordering to ties among values those
 * bits would have separated, and a tie is what the cleanup pass exists to resolve. Losing high bits
 * would <i>invert</i> the ordering, which nothing downstream can repair. So the degradation is
 * graceful in the way that matters: the sort still orders by the fields of highest comparison
 * priority, and the cleanup settles the rest.
 * <p>
 * Item 29's concern is met differently as a result. {@code Tuple} packs 8 + 17 + 38 = 63 bits
 * exactly, with its widths as literals inside a shift expression, so widening any field by one bit
 * pushes the code negative and inverts the ordering. Here the same widening costs a bit off the
 * least significant field and sets {@code perfect} false -- weakened, not inverted, and reported
 * rather than silent.
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
            int declared = 0, used = 0;
            final List<Field<X, ?>> placed = new ArrayList<>(fields.size());
            for (final Field<X, ?> f : fields) {
                final int w = f.coder.bits();
                declared += w;
                // Most significant field first, so what runs out is the room for the LOW bits.
                final int take = Math.max(0, Math.min(w, BUDGET - used));
                placed.add(f.taking(take));
                used += take;
            }
            return new CompositeHuskyCoder<>(name, List.copyOf(placed), used, declared);
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
        for (final Field<X, ?> f : fields) result = (result << f.take()) | f.encodeTaken(x);
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
        // NOTE one pass, not two. Computing exact(x) and then huskyEncode(x) separately reads every
        // accessor and runs every field coder twice, which measured as a 2.18x whole-sort penalty
        // against PermitCoder where the encode alone was 1.75x. Folding them together brings the
        // array path back to one encode per field per element.
        final boolean[] stillPerfect = {true};
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            final X x = xs[i];
            long code = 0L;
            for (final Field<X, ?> f : fields) code = (code << f.take()) | f.place(x, stillPerfect);
            result[i] = code;
        }
        return new Coding(result, stillPerfect[0]);
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
     * @return true if the declared fields exceeded {@link #BUDGET}, so that the least significant
     * of them lost bits. Such a coder can still be exact for a particular element -- only if every
     * bit it dropped happened to be zero -- but it cannot be exact in general.
     */
    public boolean truncating() {
        return declaredBits > bits;
    }

    /**
     * @return the sum of the declared field widths, which exceeds {@link #bits()} exactly when the
     * coder is {@link #truncating()}.
     */
    public int declaredBits() {
        return declaredBits;
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
        for (final Field<X, ?> f : fields) {
            sb.append(" ").append(f.coder.name()).append("=").append(f.coder.bits());
            if (f.dropped() > 0) sb.append(f.take() == 0 ? "(dropped)" : "(-" + f.dropped() + ")");
        }
        if (truncating()) sb.append("; declared ").append(declaredBits).append(", so never perfect");
        return sb.append("]").toString();
    }

    private CompositeHuskyCoder(final String name, final List<Field<X, ?>> fields, final int bits, final int declaredBits) {
        this.name = name;
        this.fields = fields;
        this.bits = bits;
        this.declaredBits = declaredBits;
    }

    private final String name;
    private final List<Field<X, ?>> fields;
    private final int bits;
    private final int declaredBits;

    /**
     * One field: how to read it, and how to encode what was read. The type parameter is captured
     * here so that the accessor and the coder cannot be mismatched, which a list of raw pairs
     * would allow.
     */
    private record Field<X, T>(Function<X, T> accessor, HuskyFieldCoder<T> coder, int take) {

        Field(final Function<X, T> accessor, final HuskyFieldCoder<T> coder) {
            this(accessor, coder, coder.bits());
        }

        /**
         * @param n how many of this field's bits the budget could afford, counted from the top.
         * @return the same field, placed.
         */
        Field<X, T> taking(final int n) {
            return new Field<>(accessor, coder, n);
        }

        /**
         * @return how many low bits were dropped for want of room; zero when the field fits.
         */
        int dropped() {
            return coder.bits() - take;
        }

        long encode(final X x) {
            return coder.encode(accessor.apply(x));
        }

        /**
         * @return the top {@link #take()} bits of this field's code. A field that got no room at
         * all contributes zero, and one that got all of its bits is shifted by nothing.
         * @throws IllegalStateException if the field coder returned a value its own declared width
         *                               cannot hold. See {@link #checkWidth}.
         */
        long encodeTaken(final X x) {
            final long code = encode(x);
            checkWidth(code, x);
            return code >>> dropped();
        }

        /**
         * Encode for the array path: the accessor is read once and the field coder run once, with
         * exactness taken from the same code rather than recomputed.
         *
         * @param x            the element.
         * @param stillPerfect a single-element flag for the whole array, cleared the first time a
         *                     value is found inexact. Once cleared the exactness work is skipped
         *                     entirely, since the answer cannot change back.
         * @return the top {@link #take()} bits of this field's code.
         */
        long place(final X x, final boolean[] stillPerfect) {
            final T value = accessor.apply(x);
            final long code = coder.encode(value);
            checkWidth(code, x);
            final int d = dropped();
            if (stillPerfect[0] && (!coder.exact(value) || (d != 0 && (code & ((1L << d) - 1)) != 0L)))
                stillPerfect[0] = false;
            return code >>> d;
        }

        /**
         * A field that returns more bits than it declared corrupts the fields <b>above</b> it, and
         * corrupting a higher-priority field inverts the composite ordering rather than weakening
         * it. So this is checked rather than trusted, and it throws rather than masking.
         * <p>
         * Masking would be cheaper and would confine the damage to the offending field, but it
         * would also hide a defect that is always a programming error: {@link HuskyFieldCoder}'s
         * first contract clause says the code lies in {@code [0, 2^bits)}, and the coders this
         * class ships all honour it by construction. Only a hand-written one can break it, and the
         * failure is worth hearing about -- measured before the check existed, a field declaring
         * seven bits and returning eight inverted 66,048 of the pairs in a four-by-256 grid, while
         * {@code exact()} reported true throughout.
         * <p>
         * NOTE a negative code is caught too, since the unsigned shift of a negative long is large.
         */
        private void checkWidth(final long code, final X x) {
            final int w = coder.bits();
            if (w < 64 && (code >>> w) != 0)
                throw new IllegalStateException("field " + coder.name() + " declares " + w
                        + " bits but returned " + code + " for " + x
                        + ", which needs " + (64 - Long.numberOfLeadingZeros(code))
                        + "; a field wider than it declares corrupts the fields above it and inverts the ordering");
        }

        /**
         * Exact for this element when the field coder says so <i>and</i> nothing was lost to the
         * budget. The second half is per value rather than per field on purpose: a truncated field
         * whose dropped bits happen to be zero has lost nothing for <i>this</i> element, and
         * reporting it inexact would make a husky sort run a cleanup pass it does not need.
         */
        boolean exact(final X x) {
            final T value = accessor.apply(x);
            if (!coder.exact(value)) return false;
            final long code = coder.encode(value);
            checkWidth(code, x);
            final int d = dropped();
            return d == 0 || (code & ((1L << d) - 1)) == 0L;
        }
    }
}
