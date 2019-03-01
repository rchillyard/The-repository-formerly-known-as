/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.bigIntegerCoder;

/**
 * Class to implement Husky Sort by which an array is sorted according to the
 * long "husky code" of the element(s).
 * If the husky code is imperfect, we will do a final TimSort to ensure that the order is correct.
 * <p>
 * NOTE: HuskySort cannot be used to compare primitives, but primitive sorting is not one of its use cases
 * because QuickSort will always be faster. Simply invoke Arrays.sort on your array.
 *
 * @param <X>
 */
public class HuskySort<X extends Comparable<X>> implements Sort<X> {

    /**
     * Construct a HuskySort with an explicit Helper and an explicit HuskyCoder
     *
     * @param helper the Helper
     * @param coder  the HuskyCoder
     */
    public HuskySort(Helper<X> helper, HuskyCoder<X> coder) {
        this.helper = helper;
        this.coder = coder;
    }

    /**
     * Construct a HuskySort with an explicit Helper and a null HuskyCoder
     *
     * @param helper the Helper
     */
    public HuskySort(Helper<X> helper) {
        this(helper, null);
    }

    /**
     * Construct a HuskySort with the default Helper and an explicit HuskyCoder
     *
     * @param coder the HuskyCoder
     */
    public HuskySort(HuskyCoder<X> coder) {
        this(new Helper<>("HuskySort"), coder);
    }

    /**
     * Construct a HuskySort with the default Helper and a null HuskyCoder
     */
    public HuskySort() {
        this((HuskyCoder<X>) null);
    }

    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        final X[] copy = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        if (copy.length<=1) return copy;
        if (coder == null && Number.class.isAssignableFrom(copy[0].getClass()))
            return doNumberSort(copy);
        if (coder != null) {
            final long[] longs = getLongArray(copy, coder);
            return doHuskySort(copy, longs);
        }
        if (HuskySortable.class.isAssignableFrom(copy[0].getClass())) {
            final long[] longs = getLongArray((HuskySortable[]) copy);
            return doHuskySort(copy, longs);
        }
        Arrays.sort(copy);
        return copy;
    }

    class Partition {
        final int lt;
        final int gt;

        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }

    @Override
    public void sort(X[] xs, int from, int to) {
        throw new UnsupportedOperationException("not implemented");
    }

    private X[] doHuskySort(X[] xs, long[] longs) {
        quickSort(xs, longs, 0, xs.length - 1);
        if (imperfect()) Arrays.sort(xs);
        return xs;
    }

    @Override
    public Helper<X> getHelper() {
        return helper;
    }

    private boolean imperfect() {
        return (coder == null) || coder.imperfect();
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "Duplicates"})
    private void quickSort(Object[] objects, long[] longs, int from, int to) {
        int lo = from, hi = to;
        if (hi <= lo) return;
        Partition partition = partition(objects, longs, lo, hi);
        quickSort(objects, longs, lo, partition.lt - 1);
        quickSort(objects, longs, partition.gt + 1, hi);
    }

    @SuppressWarnings("Duplicates")
    private Partition partition(Object[] objects, long[] longs, int lo, int hi) {
        int lt = lo, gt = hi;
        if (longs[lo] > longs[hi]) swap(objects, longs, lo, hi);
        long v = longs[lo];
        int i = lo + 1;
        while (i <= gt) {
            if (longs[i] < v) swap(objects, longs, lt++, i++);
            else if (longs[i] > v) swap(objects, longs, i, gt--);
            else i++;
        }
        return new Partition(lt, gt);
    }

    private static void swap(Object[] objects, long[] longs, int i, int j) {
        long temp1 = longs[i];
        longs[i] = longs[j];
        longs[j] = temp1;
        Object temp2 = objects[i];
        objects[i] = objects[j];
        objects[j] = temp2;
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "Duplicates"})
    private void quickSort(Object[] objects, double[] doubles, int from, int to) {
        int lo = from, hi = to;
        if (hi <= lo) return;
        Partition partition = partition(objects, doubles, lo, hi);
        quickSort(objects, doubles, lo, partition.lt - 1);
        quickSort(objects, doubles, partition.gt + 1, hi);
    }

    @SuppressWarnings("Duplicates")
    private Partition partition(Object[] objects, double[] doubles, int lo, int hi) {
        int lt = lo, gt = hi;
        if (doubles[lo] > doubles[hi]) swap(objects, doubles, lo, hi);
        double v = doubles[lo];
        int i = lo + 1;
        while (i <= gt) {
            if (doubles[i] < v) swap(objects, doubles, lt++, i++);
            else if (doubles[i] > v) swap(objects, doubles, i, gt--);
            else i++;
        }
        return new Partition(lt, gt);
    }

    private static void swap(Object[] objects, double[] doubles, int i, int j) {
        double temp1 = doubles[i];
        doubles[i] = doubles[j];
        doubles[j] = temp1;
        Object temp2 = objects[i];
        objects[i] = objects[j];
        objects[j] = temp2;
    }

    private long[] getLongArray(X[] array, HuskyCoder<X> coder) {
        long[] longArray = new long[array.length];
        for (int i = 0; i < array.length; i++) longArray[i] = coder.huskyEncode(array[i]);
        return longArray;
    }

    private long[] getLongArray(HuskySortable[] array) {
        long[] longArray = new long[array.length];
        for (int i = 0; i < array.length; i++) longArray[i] = array[i].huskyCode();
        return longArray;
    }

    private X[] doNumberSort(X[] xs) {
        if (Double.class.isAssignableFrom(xs[0].getClass()) || Float.class.isAssignableFrom(xs[0].getClass()) || BigDecimal.class.isAssignableFrom(xs[0].getClass()))
            return doDoubleSort(xs);
        if (Long.class.isAssignableFrom(xs[0].getClass()) || Integer.class.isAssignableFrom(xs[0].getClass()) || Short.class.isAssignableFrom(xs[0].getClass()))
            return doLongSort(xs);
        if (BigInteger.class.isAssignableFrom(xs[0].getClass()))
            //noinspection unchecked
            return (X[]) new HuskySort(bigIntegerCoder).sort(xs, false);
        Arrays.sort(xs);
        return xs;
    }

    private X[] doLongSort(X[] xs) {
        final long[] longs = new long[xs.length];
        if (Integer.class.isAssignableFrom(xs[0].getClass()))
            for (int i = 0; i < xs.length; i++) longs[i] = ((Integer) xs[i]).longValue();
        else if (Long.class.isAssignableFrom(xs[0].getClass()))
            for (int i = 0; i < xs.length; i++) longs[i] = (Long) xs[i];
        else if (Short.class.isAssignableFrom(xs[0].getClass()))
            for (int i = 0; i < xs.length; i++) longs[i] = ((Short) xs[i]).longValue();
        else
            throw new RuntimeException("logic error: doLongSort on " + xs[0].getClass());
        quickSort(xs, longs, 0, xs.length - 1);
        return xs;
    }

    private X[] doDoubleSort(X[] xs) {
        final double[] doubles = new double[xs.length];
        if (Double.class.isAssignableFrom(xs[0].getClass()))
            for (int i = 0; i < xs.length; i++) doubles[i] = (Double) xs[i];
        else if (Float.class.isAssignableFrom(xs[0].getClass()))
            for (int i = 0; i < xs.length; i++) doubles[i] = ((Float) xs[i]).doubleValue();
        else if (BigDecimal.class.isAssignableFrom(xs[0].getClass()))
            for (int i = 0; i < xs.length; i++) doubles[i] = ((BigDecimal) xs[i]).doubleValue();
        else
            throw new RuntimeException("logic error: doDoubleSort on " + xs[0].getClass());
        quickSort(xs, doubles, 0, xs.length - 1);
        return xs;
    }

    private final Helper<X> helper;
    private final HuskyCoder<X> coder;

}
