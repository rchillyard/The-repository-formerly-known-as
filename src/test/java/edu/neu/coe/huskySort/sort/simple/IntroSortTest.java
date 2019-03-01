/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.util.PrivateMethodTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class IntroSortTest {

    @Test
    public void testSort() throws Exception {
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
    public void testHeapSort() throws Exception {
        IntroSort<Integer> sorter = new IntroSort<>();
        PrivateMethodTester t = new PrivateMethodTester(sorter);
        Integer[] xs = {15, 3, -1, 2, 4, 1, 0, 5, 8, 6, 1, 9, 17, 7, 11};
        Class[] classes = {Comparable[].class, int.class, int.class};
        t.invokePrivateExplicit("heapSort", classes, xs, 0, xs.length - 1);
        assertTrue(sorter.getHelper().sorted(xs));
    }

    @Test
    public void testInsertionSort() throws Exception {
        IntroSort<Integer> sorter = new IntroSort<>();
        PrivateMethodTester t = new PrivateMethodTester(sorter);
        Integer[] xs = {15, 3, -1, 2, 4, 1, 0, 5, 8, 6, 1, 9, 17, 7, 11};
        Class[] classes = {Comparable[].class, int.class, int.class};
        t.invokePrivateExplicit("insertionSort", classes, xs, 0, xs.length - 1);
        assertTrue(sorter.getHelper().sorted(xs));
    }

    @Test
    public void testPartition() throws Exception {
        String testString = "PABXWPPVPDPCYZ";
        char[] charArray = testString.toCharArray();
        Character[] array = new Character[charArray.length];
        for (int i = 0; i < array.length; i++) array[i] = charArray[i];
        Sort<Character> s = new IntroSort<>();
        IntroSort.Partition p = ((IntroSort<Character>) s).partition(array, 0, array.length - 1);
        assertEquals(4, p.lt);
        assertEquals(8, p.gt);
        assertEquals(Character.valueOf('A'), array[0]);
        assertEquals(Character.valueOf('X'), array[array.length - 1]);
    }

}