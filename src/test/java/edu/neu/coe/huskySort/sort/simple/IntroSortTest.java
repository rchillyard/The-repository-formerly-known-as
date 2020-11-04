/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.*;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.ConfigTest;
import edu.neu.coe.huskySort.util.PrivateMethodInvoker;
import edu.neu.coe.huskySort.util.StatPack;
import org.junit.Test;

import java.util.List;

import static edu.neu.coe.huskySort.util.Utilities.round;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class IntroSortTest {

    @Test
    public void testSort1() throws Exception {
        Integer[] xs = new Integer[4];
        xs[0] = 3;
        xs[1] = 4;
        xs[2] = 2;
        xs[3] = 1;
        Sort<Integer> s = new IntroSort<>();
        Integer[] ys = s.sort(xs);
        assertEquals(Integer.valueOf(1), ys[0]);
        assertEquals(Integer.valueOf(2), ys[1]);
        assertEquals(Integer.valueOf(3), ys[2]);
        assertEquals(Integer.valueOf(4), ys[3]);
    }

    @Test
    public void testSortDetailed1() throws Exception {
        int k = 7;
        int N = (int) Math.pow(2, k);
        // NOTE this depends on the cutoff value for quick sort.
        int levels = k - 2;
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "");
        final BaseHelper<Integer> helper = (BaseHelper<Integer>) HelperFactory.create("intro sort", N, config);
        System.out.println(helper);
        SortWithHelper<Integer> s = new IntroSort<>(helper);
        s.init(N);
        final Integer[] xs = new Integer[N];
        for (int i = 0; i < N; i++) xs[i] = i;
        helper.preProcess(xs);
        Integer[] ys = s.sort(xs);
        assertTrue(helper.sorted(ys));
        helper.postProcess(ys);
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(helper);
        final StatPack statPack = (StatPack) privateMethodInvoker.invokePrivate("getStatPack");
        System.out.println(statPack);
        final int compares = (int) statPack.getStatistics(InstrumentedHelper.COMPARES).mean();
        final int inversions = (int) statPack.getStatistics(InstrumentedHelper.INVERSIONS).mean();
        final int fixes = (int) statPack.getStatistics(InstrumentedHelper.FIXES).mean();
        final int swaps = (int) statPack.getStatistics(InstrumentedHelper.SWAPS).mean();
        final int copies = (int) statPack.getStatistics(InstrumentedHelper.COPIES).mean();
        final int worstCompares = round(2.0 * N * Math.log(N));
        System.out.println("compares: " + compares + ", worstCompares: " + worstCompares);
        assertEquals(13, helper.maxDepth());
    }

    @Test
    public void testSortDetailed2() throws Exception {
        int k = 7;
        int N = (int) Math.pow(2, k);
        // NOTE this depends on the cutoff value for quick sort.
        int levels = k - 2;
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "");
        final BaseHelper<Integer> helper = (BaseHelper<Integer>) HelperFactory.create("intro sort", N, config);
        System.out.println(helper);
        SortWithHelper<Integer> s = new IntroSort<>(helper);
        s.init(N);
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(10000));
        assertEquals(Integer.valueOf(1360), xs[0]);
        helper.preProcess(xs);
        Integer[] ys = s.sort(xs);
        assertTrue(helper.sorted(ys));
        helper.postProcess(ys);
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(helper);
        final StatPack statPack = (StatPack) privateMethodInvoker.invokePrivate("getStatPack");
        System.out.println(statPack);
        final int compares = (int) statPack.getStatistics(InstrumentedHelper.COMPARES).mean();
        final int inversions = (int) statPack.getStatistics(InstrumentedHelper.INVERSIONS).mean();
        final int fixes = (int) statPack.getStatistics(InstrumentedHelper.FIXES).mean();
        final int swaps = (int) statPack.getStatistics(InstrumentedHelper.SWAPS).mean();
        final int copies = (int) statPack.getStatistics(InstrumentedHelper.COPIES).mean();
        assertEquals(4, helper.maxDepth());
        final int worstCompares = round(2.0 * N * Math.log(N));
        System.out.println("compares: " + compares + ", worstCompares: " + worstCompares);
        assertTrue(compares <= worstCompares);
        assertTrue(inversions <= fixes);
    }

    @Test
    public void testHeapSort() throws Exception {
        IntroSort<Integer> sorter = new IntroSort<>();
        PrivateMethodInvoker t = new PrivateMethodInvoker(sorter);
        Integer[] xs = {15, 3, -1, 2, 4, 1, 0, 5, 8, 6, 1, 9, 17, 7, 11};
        Class[] classes = {Comparable[].class, int.class, int.class};
        t.invokePrivateExplicit("heapSort", classes, xs, 0, xs.length);
        assertTrue(sorter.getHelper().sorted(xs));
    }

    @Test
    public void testIntroSort() throws Exception {
        IntroSort<Integer> sorter = new IntroSort<>();
        PrivateMethodInvoker t = new PrivateMethodInvoker(sorter);
        Integer[] xs = {15, 3, -1, 2, 4, 1, 0, 5, 8, 6, 1, 9, 17, 7, 11};
        sorter.mutatingSort(xs);
        assertTrue(sorter.getHelper().sorted(xs));
    }

    @Test
    public void testInsertionSort() throws Exception {
        IntroSort<Integer> sorter = new IntroSort<>();
        PrivateMethodInvoker t = new PrivateMethodInvoker(sorter);
        Integer[] xs = {15, 3, -1, 2, 4, 1, 0, 5, 8, 6, 1, 9, 17, 7, 11};
        Sort<Integer> insertionSort = (Sort<Integer>) t.invokePrivate("getInsertionSort");
        insertionSort.sort(xs, 0, xs.length);
        assertTrue(sorter.getHelper().sorted(xs));
    }

    @Test
    public void testPartition() throws Exception {
        String testString = "PABXWPPVPDPCYZ";
        char[] charArray = testString.toCharArray();
        Character[] array = new Character[charArray.length];
        for (int i = 0; i < array.length; i++) array[i] = charArray[i];
        Sort<Character> s = new IntroSort<>();
        Partition<Character> partition = new Partition<>(array, 0, array.length);
        List<Partition<Character>> partitions = ((IntroSort<Character>) s).partitioner.partition(partition);
        assertEquals(0, partitions.get(0).from);
        assertEquals(4, partitions.get(0).to);
        assertEquals(5, partitions.get(1).from);
        assertEquals(13, partitions.get(1).to);
        assertEquals(14, partitions.get(2).from);
        assertEquals(array.length, partitions.get(2).to);
        assertEquals(Character.valueOf('C'), array[0]);
        assertEquals(Character.valueOf('Z'), array[array.length - 1]);
    }
}