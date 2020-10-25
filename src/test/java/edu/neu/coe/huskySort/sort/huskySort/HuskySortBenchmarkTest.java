package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuskySortBenchmarkTest {

    HuskySortBenchmark benchmark;
    final static String[] args = new String[]{"1000"};

    @BeforeClass
    public static void setUpClass() throws Exception {
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
    public void sortProbabilistic() {
        HuskyCoder<Byte> byteCoder = HuskyCoderFactory.createProbabilisticCoder(0.2);
        HuskySortBenchmark.compareSystemAndPureHuskySorts(1000 + " Bytes", HuskySortBenchmark.getSupplier(1000, Byte.class, HuskySortBenchmark.byteFunction), byteCoder, null, s -> true, 100);
        HuskySortBenchmark.compareSystemAndPureHuskySorts(2000 + " Bytes", HuskySortBenchmark.getSupplier(2000, Byte.class, HuskySortBenchmark.byteFunction), byteCoder, null, s -> true, 100);
        HuskySortBenchmark.compareSystemAndPureHuskySorts(5000 + " Bytes", HuskySortBenchmark.getSupplier(5000, Byte.class, HuskySortBenchmark.byteFunction), byteCoder, null, s -> true, 100);
        HuskySortBenchmark.compareSystemAndPureHuskySorts(10000 + " Bytes", HuskySortBenchmark.getSupplier(10000, Byte.class, HuskySortBenchmark.byteFunction), byteCoder, null, s -> true, 100);
    }

    @Test
    public void sortStrings() throws IOException {
        benchmark.sortStrings(Arrays.stream(args).map(Integer::parseInt), 10000);
    }

    @Test
    public void sortLocalDateTimes() {
        benchmark.sortLocalDateTimes(100, 100000);
    }

    @Test
    public void sortNumeric() {
    }

    @Test
    public void benchmarkStringSorters() {
    }

    @Test
    public void benchmarkStringSortersInstrumented() {
    }

    @Test
    public void runStringSortBenchmark() {
    }

    @Test
    public void testRunStringSortBenchmark() {
    }

    @Test
    public void minComparisons() {
        double v = HuskySortBenchmark.minComparisons(1024);
        assertEquals(8769.01, v, 1E-2);
    }

    @Test
    public void meanInversions() {
        assertEquals(10.0 * 9 / 4, HuskySortBenchmark.meanInversions(10), 1E-7);
    }

    @Test
    public void lineAsList() {
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
        HuskySortBenchmark.Tuple tuple = HuskySortBenchmark.Tuple.create();
        assertEquals(new HuskySortBenchmark.Tuple(1971, 57058, "okay"), tuple);
    }
}