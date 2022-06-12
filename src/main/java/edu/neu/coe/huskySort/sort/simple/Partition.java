package edu.neu.coe.huskySort.sort.simple;

/**
 * Implementation of a Partition.
 *
 * @param <X> the underlying element type.
 */
public class Partition<X extends Comparable<X>> {
    public final X[] xs;
    public final int from;
    public final int to;

    public Partition(final X[] xs, final int from, final int to) {
        this.xs = xs;
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "Partition{" +
                "xs: " + xs.length + " elements" +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}
