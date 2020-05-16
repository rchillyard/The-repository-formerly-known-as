package edu.neu.coe.huskySort.sort;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class SortTest {

    static class TestSorter extends SortWithHelper<Integer> {
        public TestSorter(String description, int N, boolean instrumenting) {
            super(description, N, instrumenting);
        }

        /**
         * Generic, mutating sort method which operates on a sub-array
         *
         * @param xs   sort the array xs from "from" to "to".
         * @param from the index of the first element to sort
         * @param to   the index of the first element not to sort
         */
        @Override
        public void sort(Integer[] xs, int from, int to) {
            Arrays.sort(xs, from, to);
        }
    }

    @Test
    public void testSort1() {
        final TestSorter sorter = new TestSorter("test", 100, true);
        final Helper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        final Integer[] ys = sorter.sort(xs);
        assertTrue(ys[0] < ys[1]);
        helper.postProcess(ys); // test that ys is properly sorted.
    }

    @Test
    public void testSort2() {
        final TestSorter sorter = new TestSorter("test", 100, true);
        final Helper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        sorter.sort(xs, 0, xs.length);
        assertTrue(xs[0] < xs[1]);
        helper.postProcess(xs); // test that xs is properly sorted.
    }

    @Test
    public void testSort3() {
        final Sort<Integer> sorter = new SortWithHelper<Integer>("test", 100, true) {
            @Override
            public void sort(Integer[] xs, int from, int to) {
                // Do nothing.
            }
        };
        final Helper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        sorter.sort(xs, 0, xs.length);
        try {
            helper.postProcess(xs); // test that xs is properly sorted.
            fail("array should not be sorted - except under extremely rare circumstances");
        } catch (Exception e) {
            // Everything is as expected
        }
    }

    @Test
    public void mutatingSort() {
        final TestSorter sorter = new TestSorter("test", 100, true);
        final Helper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        sorter.mutatingSort(xs);
        assertTrue(xs[0] < xs[1]);
        helper.postProcess(xs); // test that xs is properly sorted.
    }

    @Test
    public void testSort4() {
        final TestSorter sorter = new TestSorter("test", 100, true);
        final Helper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        final Integer[] ys = sorter.sort(xs, false);
        assertArrayEquals(xs, ys);
        assertTrue(ys[0] < ys[1]);
        helper.postProcess(ys); // test that xs is properly sorted.
    }

    @Test
    public void testSort5() {
        final TestSorter sorter = new TestSorter("test", 100, true);
        final Helper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        final List<Integer> list = Arrays.asList(xs);
        final Iterable<Integer> ys = sorter.sort(list);
        final Iterator<Integer> iterator = ys.iterator();
        int first = iterator.next();
        int second = iterator.next();
        assertTrue(first < second);
    }

    @Test
    public void getHelper() {
    }

    @Test
    public void init() {
    }

    @Test
    public void preProcess() {
    }

    @Test
    public void close() {
    }
}