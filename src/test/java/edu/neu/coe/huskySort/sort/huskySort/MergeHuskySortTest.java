package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class MergeHuskySortTest {

    private final BaseHelper<String> helper = new BaseHelper<>("dummy helper");

    @Test
    public void testSortString1() {
        String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.unicodeCoder);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString2() {
        MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> nextPositiveLongString(r));
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString3() {
        MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> {
            int x = r.nextInt(1000000000);
            final BigInteger b = BigInteger.valueOf(x).multiply(BigInteger.valueOf(1000000));
            return b.toString();
        });
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString4() {
        String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString5() {
        String[] xs = {"Hello", "Goodbye", "Ciao", "Welcome"};
        MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString6() {
        MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        final int N = 32;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> nextPositiveLongString(r));
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    private String nextPositiveLongString(Random r) {
        long l = r.nextLong();
        long result = l >= 0L ? l : l == Long.MIN_VALUE ? 0L : -l;
        return String.format("%19d", result);
    }
}
