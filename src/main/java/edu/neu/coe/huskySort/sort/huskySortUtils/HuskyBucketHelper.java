package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.bqs.Bag;
import edu.neu.coe.huskySort.bqs.Bag_Array;
import edu.neu.coe.huskySort.sort.Helper;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Consumer;

public class HuskyBucketHelper<X extends Comparable<X>> extends HuskyHelper<X> {

    /**
     * Method to unload and sort the buckets into the array xs.
     *
     * @param buckets an array of Bag of X elements.
     * @param xs      an array of X elements to be filled.
     * @param helper  a helper whose compare method we will use.
     * @param <X>     the underlying type of the array and the Helper.
     */
    @SuppressWarnings("unchecked")
    public static <X extends Comparable<X>> void unloadBuckets(final Bag<X>[] buckets, final X[] xs, final Helper<X> helper) {
        final Index index = new Index();
        Arrays.stream(buckets).forEach(xes -> {
            final Object[] objects = xes.asArray();
            Arrays.sort(objects, (o, t1) -> helper.compare((X) o, (X) t1));
            for (final Object x : objects) xs[index.getNext()] = (X) x;
        });
    }

    /**
     * Method to unload the buckets.
     *
     * @param xs the array of Xs in which to unload the buckets.
     */
    public void unloadBuckets(final X[] xs) {
        unloadBuckets(buckets, xs, this);
    }

    /**
     * Check the buckets.
     *
     * @return the spread.
     */
    public int checkBuckets() {
        return getSpread();
    }

    public int loadBuckets(final X[] xs) {
        // CONSIDER is this redundant?
        doCoding(xs);
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        final long[] longs = getLongs();
        for (final long x : longs) {
            if (x > max) max = x;
            if (x < min) min = x;
        }
        final int nBuckets = buckets.length;
        final BigInteger stride = BigInteger.valueOf(max).add(BigInteger.valueOf(min).negate()).divide(BigInteger.valueOf(nBuckets)).add(BigInteger.ONE);
        for (int i = 0; i < xs.length; i++) {
            final int k = BigInteger.valueOf(longs[i]).add(BigInteger.valueOf(min).negate()).divide(stride).intValue();
            if (0 <= k && k < nBuckets) buckets[k].add(xs[i]);
            else throw new RuntimeException("Logic error: k=" + k + ", with " + nBuckets + " buckets");
        }
        return getTotal();
    }

    /**
     * Constructor for HuskyBucketHelper.
     * <p>
     * NOTE: that this will never be instrumented.
     * NOTE: used by unit tests only.
     *
     * @param description a description
     * @param m           mean bucket size
     * @param n           number of elements to sort
     * @param coder       the coder
     * @param postSorter  the post-sorter
     * @param seed        the random seed
     * @param makeCopy    whether to make a copy or not
     */
    @SuppressWarnings("unchecked")
    public HuskyBucketHelper(final String description, final int m, final int n, final HuskyCoder<X> coder, final Consumer<X[]> postSorter, final long seed, final boolean makeCopy) {
        super(description, n, coder, postSorter, seed, makeCopy);
        // CONSIDER merge with HuskyBucketHelper lines 103-104
        buckets = (Bag<X>[]) Array.newInstance(Bag.class, n / m);
        for (int i = 0; i < buckets.length; i++) buckets[i] = new Bag_Array<>();
    }

    /**
     * Constructor for HuskyBucketHelper
     *
     * @param description a description
     * @param m           mean bucket size
     * @param n           number of elements to sort
     * @param coder       the coder
     * @param postSorter  the post-sorter
     */
    @SuppressWarnings("unchecked")
    public HuskyBucketHelper(final String description, final int m, final int n, final HuskyCoder<X> coder, final Consumer<X[]> postSorter) {
        super(description, n, coder, postSorter);
        buckets = (Bag<X>[]) Array.newInstance(Bag.class, n / m);
        for (int i = 0; i < buckets.length; i++) buckets[i] = new Bag_Array<>();
    }

    static class Index {
        int index = 0;

        int getNext() {
            return index++;
        }
    }

    private int getTotal() {
        int result = 0;
        for (final Bag<X> bucket : buckets) result += bucket.size();
        return result;
    }

    private int getSpread() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (final Bag<X> bucket : buckets) {
            final int size = bucket.size();
            if (size > max) max = size;
            if (size < min) min = size;
        }
        return max - min;
    }

    final Bag<X>[] buckets;
}
