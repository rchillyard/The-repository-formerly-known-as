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
        BaseComparisonSortHelper<Integer> helper = new BaseComparisonSortHelper<>("InsertionSort", xs.length);
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
        BaseComparisonSortHelper<Integer> helper = new BaseComparisonSortHelper<>("InsertionSort", xs.length);
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
        BaseComparisonSortHelper<Integer> helper = new BaseComparisonSortHelper<>("InsertionSort", xs.length);
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
        BaseComparisonSortHelper<Integer> helper = new BaseComparisonSortHelper<>("InsertionSort", xs.length);
        InsertionSort<Integer> sorter = new InsertionSort<Integer>(helper);
        sorter.mutatingSort(xs);
        assertTrue(helper.sorted(xs));
    }

    @Test
    public void sort2() throws Exception {
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "");
        int n = 100;
        ComparisonSortHelper<Integer> helper = HelperFactory.create("SelectionSort", n, config);
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
        // NOTE: these are suppoed to match within about 12%.
        // Since we set a specific seed, this should always succeed.
        // If we use true random see and this test fails, just increase the delta a little.
        assertEquals(1.0, 4.0 * compares / n / (n - 1), 0.12);
        final int inversions = (int) statPack.getStatistics(Instrumenter.INVERSIONS).mean();
        final int fixes = (int) statPack.getStatistics(Instrumenter.FIXES).mean();
        System.out.println(statPack);
        assertEquals(inversions, fixes);
    }
}