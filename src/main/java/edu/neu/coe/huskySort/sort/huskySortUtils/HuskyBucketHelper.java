package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.bqs.Bag;
import edu.neu.coe.huskySort.bqs.Bag_Array;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.function.Consumer;

public class HuskyBucketHelper<X extends Comparable<X>> extends HuskyHelper<X> {
    /**
     * Constructor for HuskyBucketHelper
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
    public HuskyBucketHelper(String description, int m, int n, HuskyCoder<X> coder, Consumer<X[]> postSorter, long seed, boolean makeCopy) {
        super(description, n, coder, postSorter, seed, makeCopy);
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
    public HuskyBucketHelper(String description, int m, int n, HuskyCoder<X> coder, Consumer<X[]> postSorter) {
        super(description, n, coder, postSorter);
        buckets = (Bag<X>[]) Array.newInstance(Bag.class, n / m);
        for (int i = 0; i < buckets.length; i++) buckets[i] = new Bag_Array<>();
    }

    public int loadBuckets(X[] xs) {
        // CONSIDER is this redundant?
        initLongArray(xs);
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long[] longs = getLongs();
        for (long x : longs) {
            if (x > max) max = x;
            if (x < min) min = x;
        }
        int nBuckets = buckets.length;
        BigInteger stride = BigInteger.valueOf(max).add(BigInteger.valueOf(min).negate()).divide(BigInteger.valueOf(nBuckets)).add(BigInteger.ONE);
        for (int i = 0; i < xs.length; i++) {
            int k = BigInteger.valueOf(longs[i]).add(BigInteger.valueOf(min).negate()).divide(stride).intValue();
            if (0 <= k && k < nBuckets) buckets[k].add(xs[i]);
            else throw new RuntimeException("Logic error: k=" + k + ", with " + nBuckets + " buckets");
        }
        return getTotal();
    }

    private int getTotal() {
        int result = 0;
        for (Bag<X> bucket : buckets) result += bucket.size();
        return result;
    }

    private int getSpread() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Bag<X> bucket : buckets) {
            int size = bucket.size();
            if (size > max) max = size;
            if (size < min) min = size;
        }
        return max - min;
    }

    public void unloadBuckets(X[] xs) {
        int i = 0;
        for (Bag<X> bucket : buckets) for (X x : bucket) xs[i++] = x;
    }

    final Bag<X>[] buckets;

    public int checkBuckets() {
        return getSpread();
    }
}
