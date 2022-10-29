package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.util.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuskySortBenchmarkTest {

    HuskySortBenchmark benchmark;
    final static String[] args = new String[]{"1000"};

    @BeforeClass
    public static void setUpClass() {
    }

    @Before
    public void setUp() throws Exception {
        HuskySortBenchmark.Tuple.setRandom(new Random(0L));
        final Config config = Config.load(HuskySortBenchmark.class);
        benchmark = new HuskySortBenchmark(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void sortTuples() {
        benchmark.sortTuples(20, 1000000);
    }

    @Test
    public void sortNumerics() {
        benchmark.sortNumerics(100, 100000);
    }

    @Test
    public void sortStrings() {
        benchmark.sortStrings(Arrays.stream(args).map(Integer::parseInt), 10000);
    }

    @Test
    public void sortLocalDateTimes() {
        benchmark.sortLocalDateTimes(100, 100000);
    }

    @Test
    public void minComparisons() {
        final double v = HuskySortBenchmark.minComparisons(1024);
        assertEquals(8769.01, v, 1E-2);
    }

    @Test
    public void meanInversions() {
        assertEquals(10.0 * 9 / 4, HuskySortBenchmark.meanInversions(10), 1E-7);
    }

    @Test
    public void tupleCompareTo() {
        assertTrue(new HuskySortBenchmark.Tuple(1971, 57058, "okay").compareTo(new HuskySortBenchmark.Tuple(1978, 29469, "portray")) < 0);
        assertTrue(new HuskySortBenchmark.Tuple(1972, 57058, "okay").compareTo(new HuskySortBenchmark.Tuple(1971, 57058, "portray")) > 0);
        assertTrue(new HuskySortBenchmark.Tuple(1972, 57057, "okaz").compareTo(new HuskySortBenchmark.Tuple(1972, 57058, "okay")) < 0);
        assertTrue(new HuskySortBenchmark.Tuple(1972, 57058, "okaz").compareTo(new HuskySortBenchmark.Tuple(1972, 57058, "okay")) > 0);
    }

    @Test
    public void tupleHuskyCode() {
        assertTrue(new HuskySortBenchmark.Tuple(1971, 57058, "okay").huskyCode() < new HuskySortBenchmark.Tuple(1978, 29469, "portray").huskyCode());
        assertTrue(new HuskySortBenchmark.Tuple(1972, 57058, "okay").huskyCode() > new HuskySortBenchmark.Tuple(1971, 57058, "portray").huskyCode());
        assertTrue(new HuskySortBenchmark.Tuple(1972, 57057, "okaz").huskyCode() < new HuskySortBenchmark.Tuple(1972, 57058, "okay").huskyCode());
        assertTrue(new HuskySortBenchmark.Tuple(1972, 57058, "okaz").huskyCode() > new HuskySortBenchmark.Tuple(1972, 57058, "okay").huskyCode());

    }

    @Test
    public void tupleCreate() {
        final HuskySortBenchmark.Tuple tuple = HuskySortBenchmark.Tuple.create();
        assertEquals(new HuskySortBenchmark.Tuple(1971, 57058, "okay"), tuple);
    }
}