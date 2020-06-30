/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class HuskyBucketSortTest {

    @Test
    public void sort4() throws Exception {
        final List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(2);
        list.add(1);
        Integer[] xs = list.toArray(new Integer[0]);
        HuskyBucketSort<Integer> sorter = new HuskyBucketSort<>(2, HuskyCoderFactory.integerCoder, config);
        sorter.preProcess(xs);
        Integer[] ys = sorter.sort(xs);
        assertTrue(sorter.getHelper().sorted(ys));
        System.out.println(sorter.toString());
    }

    @Test
    public void sortN() throws Exception {
        int N = 10000;
        Integer[] xs = new Integer[N];
        Random random = new Random();
        for (int i = 0; i < N; i++) xs[i] = random.nextInt(10000);
        HuskyBucketSort<Integer> sorter = new HuskyBucketSort<>(16, HuskyCoderFactory.integerCoder, config);
        sorter.preProcess(xs);
        Integer[] ys = sorter.sort(xs);
        assertTrue(sorter.getHelper().sorted(ys));
        System.out.println(sorter.toString());
    }

    // NOTE: this test makes no sense because we build the buckets twice
    @Test
    public void doubleSortN() throws Exception {
        int N = 10240;
        Integer[] xs = new Integer[N];
        Random random = new Random();
        for (int i = 0; i < N; i++) xs[i] = random.nextInt(10000);
        HuskyBucketSort<Integer> sorter = new HuskyBucketSort<>(16, HuskyCoderFactory.integerCoder, config);
        sorter.preProcess(xs);
        Integer[] ys1 = sorter.sort(xs);
        assertTrue(sorter.getHelper().sorted(ys1));
        sorter.preProcess(xs);
        Integer[] ys2 = sorter.sort(xs);
        assertTrue(sorter.getHelper().sorted(ys2));
    }


    @BeforeClass
    public static void before() throws IOException {
        config = Config.load(HuskyBucketSortTest.class);
    }

    private static Config config;
}