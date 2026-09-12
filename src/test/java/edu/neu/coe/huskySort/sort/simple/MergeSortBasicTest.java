/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.ComparisonSortHelper;
import edu.neu.coe.huskySort.sort.HelperFactory;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.util.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class MergeSortBasicTest {

    @Test
    public void testSort1() throws Exception {
        Integer[] xs = new Integer[4];
        xs[0] = 3;
        xs[1] = 4;
        xs[2] = 2;
        xs[3] = 1;
        // NOTE: first we ensure that there is no cutoff to insertion sort going on.
        final Config config = ConfigTest.setupConfig("true", "", "0", "1", "");
        Sort<Integer> s = new MergeSortBasic<>(xs.length, config);
        Integer[] ys = s.sort(xs);
        assertEquals(Integer.valueOf(1), ys[0]);
        assertEquals(Integer.valueOf(2), ys[1]);
        assertEquals(Integer.valueOf(3), ys[2]);
        assertEquals(Integer.valueOf(4), ys[3]);
    }

    @Test
    public void testSort2() throws Exception {
        int k = 7;
        int N = (int) Math.pow(2, k);
        // NOTE this depends on the cutoff value for merge sort.
        int levels = k - 2;
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "");
        final ComparisonSortHelper<Integer> helper = HelperFactory.create("merge sort", N, config);
        System.out.println(helper);
        Sort<Integer> s = new MergeSortBasic<>(helper);
        s.init(N);
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(10000));
        assertEquals(Integer.valueOf(1360), xs[0]);
        helper.preProcess(xs);
        Integer[] ys = s.sort(xs);
        helper.postProcess(ys);
        Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        final StatPack statPack = instrumenter.getStatPack();
        System.out.println(statPack);
        final int compares = (int) statPack.getStatistics(Instrumenter.COMPARES).mean();
        final int inversions = (int) statPack.getStatistics(Instrumenter.INVERSIONS).mean();
        final int fixes = (int) statPack.getStatistics(Instrumenter.FIXES).mean();
        final int swaps = (int) statPack.getStatistics(Instrumenter.SWAPS).mean();
        final int copies = (int) statPack.getStatistics(Instrumenter.COPIES).mean();
        final int worstCompares = N * k - N + 1;
        assertTrue(compares <= worstCompares);
        assertEquals(inversions, fixes);
        assertEquals(levels * 2 * N, copies);
    }

    @Test
    public void testSort3() throws Exception {
        int k = 7;
        int N = (int) Math.pow(2, k);
        final ComparisonSortHelper<Integer> helper1 = HelperFactory.create("insertion sort", N, ConfigTest.setupConfig("true", "0", "1", "", ""));
        System.out.println(helper1);
        final Integer[] xs = helper1.random(Integer.class, r -> r.nextInt(10000));
        assertEquals(Integer.valueOf(1360), xs[0]);
        new InsertionSort<Integer>(helper1).mutatingSort(xs);
        helper1.postProcess(xs);
        final ComparisonSortHelper<Integer> helper2 = HelperFactory.create("merge sort", N, ConfigTest.setupConfig("true", "", "0", "1", ""));
        System.out.println(helper2);
        Sort<Integer> mergeSort = new MergeSortBasic<>(helper2);
        mergeSort.init(N);
        helper2.preProcess(xs);
        Integer[] ys = mergeSort.sort(xs);
        helper2.postProcess(ys);
        Instrumenter instrumenter1 = helper1.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker1 = new PrivateMethodInvoker(instrumenter1);
        final StatPack statPack1 = (StatPack) privateMethodInvoker1.invokePrivate("getStatPack");
        final int inversions = (int) statPack1.getStatistics(Instrumenter.INVERSIONS).mean();
        Instrumenter instrumenter2 = helper2.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker2 = new PrivateMethodInvoker(instrumenter2);
        final StatPack statPack2 = (StatPack) privateMethodInvoker2.invokePrivate("getStatPack");
        System.out.println(statPack2);
        final int compares = (int) statPack2.getStatistics(Instrumenter.COMPARES).mean();
        final int fixes = (int) statPack2.getStatistics(Instrumenter.FIXES).mean();
        final int swaps = (int) statPack2.getStatistics(Instrumenter.SWAPS).mean();
        final int copies = (int) statPack2.getStatistics(Instrumenter.COPIES).mean();
        final int expectedCompares = N * k / 2;
        assertEquals(expectedCompares, compares);
        assertEquals(inversions, fixes);
        assertEquals(k * 2 * N, copies);
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(MergeSortBasicTest.class);
    }

    private static Config config;

    /**
     * sort(xs, from, to) is the method the Sort interface requires, so a caller may
     * reach it directly rather than through the lifecycle. It used to throw a
     * NullPointerException when they did, because the auxiliary array is allocated
     * in preSort and the merge read it without checking. aux is now allocated
     * lazily; INFO6205's MergeSortBasic had the same defect and the same fix.
     */
    @Test
    public void sortASubArrayWithoutPreSort() throws IOException {
        final ComparisonSortHelper<Integer> helper =
                HelperFactory.create("MergeSortBasic", 5, Config.load(MergeSortBasicTest.class));
        helper.init(5);
        final MergeSortBasic<Integer> sorter = new MergeSortBasic<>(helper);
        final Integer[] xs = {5, 4, 3, 2, 1};
        sorter.sort(xs, 0, xs.length);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, xs);
    }

    @Test
    public void sortAProperSubRangeWithoutPreSort() throws IOException {
        final ComparisonSortHelper<Integer> helper =
                HelperFactory.create("MergeSortBasic", 6, Config.load(MergeSortBasicTest.class));
        helper.init(6);
        final MergeSortBasic<Integer> sorter = new MergeSortBasic<>(helper);
        final Integer[] xs = {9, 5, 4, 3, 2, 9};
        sorter.sort(xs, 1, 5);
        assertArrayEquals("the range outside from..to must be untouched",
                new Integer[]{9, 2, 3, 4, 5, 9}, xs);
    }
}