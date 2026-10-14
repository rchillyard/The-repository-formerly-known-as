package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * Class to combine the long codes for an array of objects with a determination of coding perfection.
 * <p>
 * {@link #perfect} here is <b>per array</b>: it is the conjunction of "this value encoded without
 * loss" over the elements actually coded, not a claim about the coder. See
 * {@link HuskyCoder} for how that differs from {@code HuskyCoder.perfect()}, which quantifies over
 * every value of X, and from the per-value {@code exact}.
 * <p>
 * NOTE this is the field the sorts read to decide whether to skip the cleanup pass, so a wrong
 * true is a wrong answer and a wrong false is only a slow one.
 */
public class Coding {
    public Coding(final long[] longs, final boolean perfect) {
        this.longs = longs;
        this.perfect = perfect;
    }

    public final long[] longs;
    public final boolean perfect;
}
