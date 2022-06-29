package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
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
        final List<Integer> xs = new ArrayList<>();
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsSingleton() {
        final List<Integer> xs = new ArrayList<>();
        xs.add(1);
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsDoubleton0() {
        final List<Integer> xs = new ArrayList<>();
        xs.add(1);
        xs.add(2);
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsDoubleton1() {
        final List<Integer> xs = new ArrayList<>();
        xs.add(1);
        xs.add(0);
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(1L, inversions);
    }

    @Test
    public void getInversionsTripleton1() {
        final List<Integer> xs = new ArrayList<>();
        xs.add(1);
        xs.add(0);
        xs.add(-1);
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(3L, inversions);
    }

    @Test
    public void getInversionsTripleton2() {
        final List<Integer> xs = new ArrayList<>();
        xs.add(0);
        xs.add(0);
        xs.add(0);
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsTripleton3() {
        final List<Integer> xs = new ArrayList<>();
        xs.add(-1);
        xs.add(0);
        xs.add(1);
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(0L, inversions);
    }

    @Test
    public void getInversionsRandom() {
        final List<Integer> xs = new ArrayList<>();
        final Random r = new Random(0L);
        for (int i = 6; i > 0; i--) xs.add(r.nextInt(100));
        final InversionCounter counter = new InversionCounter(xs.toArray(new Integer[0]));
        final long inversions = counter.getInversions();
        assertEquals(10L, inversions);
    }

    @Test
    public void getInversionsRandomN() {
        final int N = 1000;
        final ComparableSortHelper<Integer> helper = new ComparableSortHelper<>("InversionTest", N);
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(N));
        final InversionCounter counter = new InversionCounter(xs);
        final double meanInversions = 0.25 * N * (N - 1);
        final long inversions = counter.getInversions();
        // NOTE: the safety factor here (36) is quite arbitrary and it's possible this test will fail occasionally.
        final double tolerance = 36 / Math.sqrt(meanInversions);
        System.out.println("ratio: " + inversions / meanInversions + ", tolerance: " + tolerance);
        assertTrue(inversions / meanInversions <= (1 + tolerance));
        assertTrue(inversions / meanInversions >= (1 - tolerance));
    }
}