/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
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
public class QuickBenchmarkIntegrationTest {

    private final static Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");
    private static Logger logger = new LazyLogger(QuickBenchmarkIntegrationTest.class);
    private static HuskySortBenchmark benchmark;
    private static Config config;

    @BeforeClass
    public static void BeforeClass() throws IOException {
        config = Config.load();
        benchmark = new HuskySortBenchmark(config);
        String name = config.get("huskysort", "version");
        logger.info("HuskySortBenchmark.main: " + name);
    }

    @Test
    public void testStrings1K() throws Exception {
        // NOTE: this is a very quick version of the other integration tests.
        benchmark.benchmarkStringSorters(getWords("eng-uk_web_2002_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 1000, 100);
    }

    @Test
    public void testDates1K() throws Exception {
        benchmark.sortLocalDateTimes(1000);
    }

    @Test
    public void testStrings1KInstrumented() throws Exception {
        // NOTE: this is a very quick version of the other integration tests.
        benchmark.benchmarkStringSortersInstrumented(getWords("eng-uk_web_2002_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 1000, 100);
    }
}