package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Benchmark;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.TimeLogger;
import edu.neu.coe.huskySort.util.Utilities;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.timeLoggersLinearithmic;
import static edu.neu.coe.huskySort.util.ProcessorDependentTimeout.getFactoredTimeout;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.junit.Assert.assertEquals;

public class PureHuskySortIntegrationTest {

    @BeforeClass
    public static void doBeforeClass() throws IOException {
        config = Config.load();
    }

    @Test
    public void testSortString4() {
        final int N = 1000;
        String[] words = HuskySortBenchmarkHelper.getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        Random random = new Random();
        PureHuskySort<String> pureHuskySort = new PureHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        Benchmark<String[]> benchmark = new Benchmark<>("PureHuskySort", null, pureHuskySort::sort, null);
        final double time = benchmark.run(() -> Utilities.fillRandomArray(String.class, random, N, r -> words[r.nextInt(words.length)]), 200);
        double expected = getFactoredTimeout(475, MICROSECONDS, config, MICROSECONDS);
        assertEquals(expected, time * 1000, 400);
        for (TimeLogger timeLogger : timeLoggersLinearithmic) timeLogger.log(time, N);
    }

    @Test
    public void testSortString5() {
        final int N = 1000;
        String[] words = HuskySortBenchmarkHelper.getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        Random random = new Random();
        PureHuskySort<String> pureHuskySort = new PureHuskySort<>(HuskyCoderFactory.englishCoder, false, false);
        Benchmark<String[]> benchmark = new Benchmark<>("PureHuskySort", null, pureHuskySort::sort, null);
        final double time = benchmark.run(() -> Utilities.fillRandomArray(String.class, random, N, r -> words[r.nextInt(words.length)]), 200);
        double expected = getFactoredTimeout(325, MICROSECONDS, config, MICROSECONDS);
        assertEquals(expected, time * 1000, 300);
        for (TimeLogger timeLogger : timeLoggersLinearithmic) timeLogger.log(time, N);
    }

    private static Config config;

    private final BaseHelper<String> helper = new BaseHelper<>("dummy helper");

}
