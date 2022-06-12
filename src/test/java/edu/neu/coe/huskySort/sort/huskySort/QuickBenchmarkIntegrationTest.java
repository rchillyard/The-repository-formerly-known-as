/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySequenceCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Quick Benchmark Integration Test.
 * This is suitable for inclusion in unit tests.
 *
 * <p>
 * The expected time for a pure quicksort of N items and M runs is 1.39 k M N lg N (where lg represents log to the base 2).
 * Bear in mind that the Benchmark code does M/10 warmup runs also.
 */

@SuppressWarnings("ALL")
public class QuickBenchmarkIntegrationTest {

    @BeforeClass
    public static void BeforeClass() throws IOException {
        config = Config.load();
        benchmark = new HuskySortBenchmark(config);
        String huskysort = "huskysort";
        String name = config.get(huskysort, "version");
        huskyCoder = HuskySortHelper.getSequenceCoderByName(config.get(huskysort, "huskycoder", "Unicode"));
        logger.info("HuskySortBenchmark.main: " + name);
    }

    @Test
    public void testStrings1K() throws Exception {
        // NOTE: this is a very quick version of the other integration tests.
        String corpus = "eng-uk_web_2002_10K-sentences.txt";
        benchmark.benchmarkStringSorters(corpus, HuskySortBenchmarkHelper.getWords(corpus, line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, regexLeipzig, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER)), 1000, 100, huskyCoder);
    }

    @Test
    public void testDatesHalfK() throws Exception {
        benchmark.sortLocalDateTimes(500, 1000000);
    }

    @Test
    public void testStrings1KInstrumented() throws Exception {
        // NOTE: this is a very quick version of the other integration tests.
        benchmark.benchmarkStringSortersInstrumented(HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_10K-sentences.txt", line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, regexLeipzig, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER)), 1000, 100, huskyCoder);
    }

    private final static Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");
    private static Logger logger = new LazyLogger(QuickBenchmarkIntegrationTest.class);
    private static HuskySortBenchmark benchmark;
    private static Config config;
    private static HuskySequenceCoder<String> huskyCoder;
}