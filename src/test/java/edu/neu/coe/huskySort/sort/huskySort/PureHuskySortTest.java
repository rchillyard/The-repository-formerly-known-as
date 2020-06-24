package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.util.Benchmark;
import edu.neu.coe.huskySort.util.TimeLogger;
import edu.neu.coe.huskySort.util.Utilities;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Random;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.timeLoggersLinearithmic;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.getWords;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PureHuskySortTest {

    private final BaseHelper<String> helper = new BaseHelper<>("dummy helper");

    @Test
    public void testSortString1() {
        String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        PureHuskySort<String> sorter = new PureHuskySort<>(HuskySortHelper.unicodeCoder);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString2() {
        PureHuskySort<String> sorter = new PureHuskySort<>(HuskySortHelper.asciiCoder);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString3() {
        PureHuskySort<String> sorter = new PureHuskySort<>(HuskySortHelper.asciiCoder);
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
    public void testSortString4() throws FileNotFoundException {
        final int N = 1000;
        String[] words = getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        Random random = new Random();
        PureHuskySort<String> pureHuskySort = new PureHuskySort<>(HuskySortHelper.asciiCoder);
        Benchmark<String[]> benchmark = new Benchmark<>("PureHuskySort", null, pureHuskySort::sort, null);
        final double time = benchmark.run(() -> Utilities.fillRandomArray(String.class, random, N, r -> words[r.nextInt(words.length)]), 200);
        assertEquals(0.25, time, 0.07);
        for (TimeLogger timeLogger : timeLoggersLinearithmic) timeLogger.log(time, N);
    }

    @Test
    public void testSortString5() throws FileNotFoundException {
        final int N = 1000;
        String[] words = getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        Random random = new Random();
        PureHuskySort<String> pureHuskySort = new PureHuskySort<>(HuskySortHelper.printableAsciiCoder);
        Benchmark<String[]> benchmark = new Benchmark<>("PureHuskySort", null, pureHuskySort::sort, null);
        final double time = benchmark.run(() -> Utilities.fillRandomArray(String.class, random, N, r -> words[r.nextInt(words.length)]), 200);
        assertEquals(0.24, time, 0.07);
        for (TimeLogger timeLogger : timeLoggersLinearithmic) timeLogger.log(time, N);
    }
}
