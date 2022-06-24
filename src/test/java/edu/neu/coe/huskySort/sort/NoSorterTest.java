/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class NoSorterTest {

    @Test
    public void sort_0() throws Exception {
        final List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(2);
        list.add(1);
        Integer[] xs = list.toArray(new Integer[0]);
        BaseComparisonSortHelper<Integer> helper = new BaseComparisonSortHelper<>("NoSort", xs.length);
        helper.setCheckSorted(true);
        Sorter<Integer> sorter = new NoSorter<>(helper);
        boolean ys = sorter.sortArray(xs);
        assertFalse(ys);
    }

    @Test
    public void sort_1() throws Exception {
        final List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(2);
        list.add(1);
        Integer[] xs = list.toArray(new Integer[0]);
        BaseComparisonSortHelper<Integer> helper = new BaseComparisonSortHelper<>("NoSort", xs.length);
        Sorter<Integer> sorter = new NoSorter<>(helper);
        boolean ys = sorter.sortArray(xs);
        assertTrue(ys);
    }

}