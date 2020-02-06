package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

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
}