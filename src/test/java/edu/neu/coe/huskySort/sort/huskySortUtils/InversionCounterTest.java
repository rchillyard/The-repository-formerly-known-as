package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.BaseHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class InversionCounterTest {

    @Test
    public void getInversionsEmpty() {
        List<Integer> xs = new ArrayList<>();
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsSingleton() {
        List<Integer> xs = new ArrayList<>();
        xs.add(1);
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsDoubleton0() {
        List<Integer> xs = new ArrayList<>();
        xs.add(1);
        xs.add(2);
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsDoubleton1() {
        List<Integer> xs = new ArrayList<>();
        xs.add(1);
        xs.add(0);
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(1L, inversions);
    }

    @Test
    public void getInversionsTripleton1() {
        List<Integer> xs = new ArrayList<>();
        xs.add(1);
        xs.add(0);
        xs.add(-1);
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(3L, inversions);
    }

    @Test
    public void getInversionsTripleton2() {
        List<Integer> xs = new ArrayList<>();
        xs.add(0);
        xs.add(0);
        xs.add(0);
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsTripleton3() {
        List<Integer> xs = new ArrayList<>();
        xs.add(-1);
        xs.add(0);
        xs.add(1);
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsRandom() {
        List<Integer> xs = new ArrayList<>();
        Random r = new Random(0L);
        for (int i = 6; i > 0; i--) xs.add(r.nextInt(100));
        InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        long inversions = counter.getInversions();
        assertEquals(10L, inversions);
    }

    @Test
    public void getInversionsRandomN() {
        int N = 1000;
        final BaseHelper<Integer> helper = new BaseHelper<>("InversionTest", N);
        Integer[] xs = helper.random(Integer.class, r -> r.nextInt(N));
        InversionCounter counter = new InversionCounter(xs);
        double meanInversions = 0.25 * N * (N - 1);
        long inversions = counter.getInversions();
        // NOTE: the safety factor here (36) is quite arbitrary and it's possible this test will fail occasionally.
        final double tolerance = 36 / Math.sqrt(meanInversions);
        System.out.println("ratio: " + inversions / meanInversions + ", tolerance: " + tolerance);
        assertTrue(inversions / meanInversions <= (1 + tolerance));
        assertTrue(inversions / meanInversions >= (1 - tolerance));
    }
}