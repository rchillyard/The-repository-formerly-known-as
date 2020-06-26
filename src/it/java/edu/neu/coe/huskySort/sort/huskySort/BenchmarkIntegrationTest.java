/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.ProcessorDependentTimeout;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.getWords;

/**
 * NOTE: JUnit does not allow variables to be used for the timeout. That means that we cannot adjust these timeout for the speed of a particular machine.
 * The values given are for a MacBook Pro 2.8 GHz Intel Core i7 (4 cores) with 16 GB 2133 MHz LPDDRP.
 * Java version is 1.8.0_152.
 * <p>
 * The expected time for a pure quicksort of N items and M runs is 1.39 k M N lg N (where lg represents log to the base 2).
 * Bear in mind that the Benchmark code does M/10 warmup runs also.
 */

@SuppressWarnings("ALL")
public class BenchmarkIntegrationTest {

    private final static Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");
    private static Logger logger = new LazyLogger(BenchmarkIntegrationTest.class);
    private static HuskySortBenchmark benchmark;
    private static Config config;
    private final static double MacBookPro_2_8_GHz_Quad_Core_Intel_Core_i7 = 0.9;
    private final static double MacBookAir_1_6_GH_Dual_Core_Intel_Core_i5 = 0.68;
    private final static double YunluProcessor = 1.0;

    @Rule
    public ProcessorDependentTimeout timeoutBuilder = new ProcessorDependentTimeout(10, TimeUnit.SECONDS, MacBookAir_1_6_GH_Dual_Core_Intel_Core_i5);

    @BeforeClass
    public static void BeforeClass() throws IOException {
        config = Config.load();
        benchmark = new HuskySortBenchmark(config);
        String name = config.get("huskysort", "version");
        logger.info("HuskySortBenchmark.main: " + name);
    }

    @Test
    public void testStrings10K() throws Exception {
        benchmark.benchmarkStringSorters(getWords("eng-uk_web_2002_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 10000, 1600);
    }

    @Test
    public void testStrings100K() throws Exception {
        // NOTE: you cannot include insertionSort among the sort methods to be used: it WILL time out here.
        benchmark.benchmarkStringSorters(getWords("eng-uk_web_2002_100K-sentences.txt", line -> getWords(regexLeipzig, line)), 100000, 75);
    }

    @Test
    public void testDates10K() throws Exception {
        benchmark.sortLocalDateTimes(37000);
    }

    @Test
    public void testDates100K() throws Exception {
        benchmark.sortLocalDateTimes(40000);
    }

    @Test
    public void testStrings10KInstrumented() throws Exception {
        benchmark.benchmarkStringSortersInstrumented(getWords("eng-uk_web_2002_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 10000, 1000);
    }

//    @Test(timeout = 140000)
//    public void testStrings100KInstrumented() throws Exception {
//        // NOTE: you cannot include insertionSort among the sort methods to be used: it WILL time out here.
//        benchmark.benchmarkStringSortersInstrumented(getWords("eng-uk_web_2002_100K-sentences.txt", line -> getWords(regexLeipzig, line)), 100000, 200);
//    }
}