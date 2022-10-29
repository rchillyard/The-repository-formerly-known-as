package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.sort.simple.MergeSortBasicTest;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class SortTest {

    static class TestSorter extends SortWithHelper<Integer> {
        public TestSorter(final String description, final int N, final boolean instrumenting, final Config config) {
            super(description, N, config);
        }

        /**
         * Generic, mutating sort method which operates on a sub-array
         *
         * @param xs   sort the array xs from "from" to "to".
         * @param from the index of the first element to sort
         * @param to   the index of the first element not to sort
         */
        @Override
        public void sort(final Integer[] xs, final int from, final int to) {
            Arrays.sort(xs, from, to);
        }
    }

    @Test
    public void testSort1() {
        final TestSorter sorter = new TestSorter("test", 100, true, config);
        final ComparisonSortHelper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        final Integer[] ys = sorter.sort(xs);
        assertTrue(ys[0] <= ys[1]);
        helper.postProcess(ys); // test that ys is properly sorted.
    }

    @Test
    public void testSort2() {
        final int N = 100;
        final TestSorter sorter = new TestSorter("test", N, true, config);
        final ComparisonSortHelper<Integer> helper = sorter.getHelper();
        helper.init(N);
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        sorter.sort(xs, 0, xs.length);
        assertTrue(xs[0] <= xs[1]);
        helper.postProcess(xs); // test that xs is properly sorted.
    }

    @Test
    public void testSort3() throws IOException {
        final Config config = Config.load(getClass());
        final SortWithHelper<Integer> sorter = new SortWithHelper<>("test", 100, config) {
            @Override
            public void sort(final Integer[] xs, final int from, final int to) {
                // Do nothing.
            }

            /**
             * Method to post-process an array after sorting.
             * <p>
             * In this implementation, the post-processing verifies that xs is sorted.
             *
             * @param xs the array to be post-processed.
             * @return true.
             * @throws ComparableSortHelper.HelperException if the array xs is not sorted.
             */
            @Override
            public boolean postProcess(final Integer[] xs) {
                if (!getHelper().sorted(xs)) throw new ComparableSortHelper.HelperException("Array is not sorted");
                return true;
            }
        };
        final ComparisonSortHelper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        sorter.sort(xs, 0, xs.length);
        assertFalse("array should not be sorted - except under extremely rare circumstances", helper.sorted(xs));
    }

    @Test
    public void mutatingSort() {
        final TestSorter sorter = new TestSorter("test", 100, true, config);
        final ComparisonSortHelper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        sorter.mutatingSort(xs);
        assertTrue(xs[0] < xs[1]);
        helper.postProcess(xs); // test that xs is properly sorted.
    }

    @Test
    public void testSort4() {
        final TestSorter sorter = new TestSorter("test", 100, true, config);
        final ComparisonSortHelper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        final Integer[] ys = sorter.sort(xs, false);
        assertArrayEquals(xs, ys);
        assertTrue(ys[0] < ys[1]);
        helper.postProcess(ys); // test that xs is properly sorted.
    }

    @Test
    public void testSort5() {
        final TestSorter sorter = new TestSorter("test", 100, true, config);
        final ComparisonSortHelper<Integer> helper = sorter.getHelper();
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000000));
        final List<Integer> list = Arrays.asList(xs);
        final Iterable<Integer> ys = sorter.sort(list);
        final Iterator<Integer> iterator = ys.iterator();
        final int first = iterator.next();
        final int second = iterator.next();
        assertTrue(first < second);
    }

    private static Config config;

    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(MergeSortBasicTest.class);
    }

}