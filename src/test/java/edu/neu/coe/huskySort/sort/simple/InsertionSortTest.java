/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.*;
import edu.neu.coe.huskySort.util.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class InsertionSortTest {

    @Test
    public void sort_() throws Exception {
        final List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(2);
        list.add(1);
        Integer[] xs = list.toArray(new Integer[0]);
        ComparableSortHelper<Integer> helper = new ComparableSortHelper<>("InsertionSort", xs.length);
        helper.setCheckSorted(true);
        Sorter<Integer> sorter = new InsertionSort<Integer>(helper);
        boolean ys = sorter.sortArray(xs);
        assertTrue(ys);
        System.out.println(sorter.toString());
    }

    @Test
    public void sort0() throws Exception {
        final List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(2);
        list.add(1);
        Integer[] xs = list.toArray(new Integer[0]);
        ComparableSortHelper<Integer> helper = new ComparableSortHelper<>("InsertionSort", xs.length);
        helper.setCheckSorted(true);
        Sorter<Integer> sorter = new InsertionSort<Integer>(helper);
        boolean ys = sorter.sortArray(xs);
        assertTrue(ys);
        System.out.println(sorter.toString());
    }

    @Test
    public void sort1() throws Exception {
        final List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(2);
        list.add(1);
        Integer[] xs = list.toArray(new Integer[0]);
        ComparableSortHelper<Integer> helper = new ComparableSortHelper<>("InsertionSort", xs.length);
        InsertionSort<Integer> sorter = new InsertionSort<Integer>(helper);
        Integer[] ys = sorter.sort(xs);
        assertTrue(helper.sorted(ys));
        System.out.println(sorter.toString());
    }

    @Test
    public void testMutatingInsertionSort() {
        final List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(2);
        list.add(1);
        Integer[] xs = list.toArray(new Integer[0]);
        ComparableSortHelper<Integer> helper = new ComparableSortHelper<>("InsertionSort", xs.length);
        InsertionSort<Integer> sorter = new InsertionSort<Integer>(helper);
        sorter.mutatingSort(xs);
        assertTrue(helper.sorted(xs));
    }

    @Test
    public void sort2() throws Exception {
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "");
        int n = 128;
        ComparisonSortHelper<Integer> helper = HelperFactory.create("InsertionSort", n, config);
        helper.init(n);

        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(helper);
        final StatPack statPack = (StatPack) privateMethodInvoker.invokePrivate("getStatPack");
        Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000));
        SortWithHelper<Integer> sorter = new InsertionSort<Integer>(helper);
        sorter.preProcess(xs);
        Integer[] ys = sorter.sort(xs);
        sorter.postProcess(ys);
        assertTrue(helper.sorted(ys));
        final int compares = (int) statPack.getStatistics(Instrumenter.COMPARES).mean();
        // Binary insertion sort's comparison count converges to lg(n!), not n*lg(n) -- the
        // latter omits a large constant-factor correction term (Stirling's approximation).
        // NOTE: lg(n!) is only the ties-free theoretical minimum. With keys drawn from a range
        // as narrow as nextInt(1000) at n=128, a few duplicate keys are expected, each costing a
        // handful of extra tie-breaking comparisons (see ComparisonSortHelper.swapIntoSorted).
        // As with the original (pre-binary-search) version of this assertion, we check the ratio
        // of actual to expected compares against 1.0, rather than an absolute delta, so the
        // tolerance scales with n instead of being pinned to one specific n.
        final double logNminus1 = Utilities.lg(n - 1);
        final double expectedCompares = logNminus1 * (n - 1) - 1.44 * n + 0.5 * logNminus1 + 1.33;
        assertEquals(1.0, compares / expectedCompares, 0.12);
        final int inversions = (int) statPack.getStatistics(Instrumenter.INVERSIONS).mean();
        final int fixes = (int) statPack.getStatistics(Instrumenter.FIXES).mean();
        System.out.println(statPack);
        assertEquals(inversions, fixes);
    }
}