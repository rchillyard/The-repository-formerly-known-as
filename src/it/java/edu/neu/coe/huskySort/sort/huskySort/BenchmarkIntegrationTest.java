/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySequenceCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;
import edu.neu.coe.huskySort.util.ProcessorDependentTimeout;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Benchmark Integration Test.
 * This is suitable for inclusion in integration tests, but not unit tests.
 *
 * <p>
 * The expected time for a pure quicksort of N items and M runs is 1.39 k M N lg N (where lg represents log to the base 2).
 * Bear in mind that the Benchmark code does M/10 warmup runs also.
 */

@SuppressWarnings("ALL")
public class BenchmarkIntegrationTest {

    @BeforeClass
    public static void BeforeClass() throws IOException {
        config = Config.load();
        benchmark = new HuskySortBenchmark(config);
        String huskysort = "huskysort";
        String name = config.get(huskysort, "version");
        huskyCoder = HuskySortHelper.getSequenceCoderByName(config.get(huskysort, "huskycoder", "Unicode"));
        logger.info("HuskySortBenchmark.main: " + name);
    }

    @Rule
    public Timeout timeoutBuilder = new ProcessorDependentTimeout(10, TimeUnit.SECONDS, config);

    @Test
    public void testStrings10K() throws Exception {
        String corpus = "eng-uk_web_2002_10K-sentences.txt";
        benchmark.benchmarkStringSorters(corpus, getWordsLeipzig(corpus), 10000, 4100, huskyCoder);
    }

    private final static String[] getWordsLeipzig(String s) throws FileNotFoundException {
        return HuskySortBenchmarkHelper.getWords(s, line -> HuskySortBenchmarkHelper.splitLineIntoStrings(line, REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER));
    }

    @Test
    public void testStrings100K() throws Exception {
        // NOTE: you cannot include insertionSort among the sort methods to be used: it WILL time out here.
        String corpus = "eng-uk_web_2002_100K-sentences.txt";
        benchmark.benchmarkStringSorters(corpus, getWordsLeipzig(corpus), 100000, 175, huskyCoder);
    }

    @Test
    public void testDates10K() throws Exception {
        benchmark.sortLocalDateTimes(37000, 1000000);
    }

    @Test
    public void testDates100K() throws Exception {
        benchmark.sortLocalDateTimes(44000, 1000000);
    }

    @Test
    public void testStrings10KInstrumented() throws Exception {
        benchmark.benchmarkStringSortersInstrumented(getWordsLeipzig("eng-uk_web_2002_10K-sentences.txt"), 10000, 950, huskyCoder);
    }

//    @Test(timeout = 140000)
//    public void testStrings100KInstrumented() throws Exception {
//        // NOTE: you cannot include insertionSort among the sort methods to be used: it WILL time out here.
//        huskyCoder = asciiCoder;
//        benchmark.benchmarkStringSortersInstrumented(getWords("eng-uk_web_2002_100K-sentences.txt", line -> getWords(regexLeipzig, line)), 100000, 200, huskyCoder);
//    }

    private final static Pattern REGEX_LEIPZIG = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");
    private static Logger logger = new LazyLogger(BenchmarkIntegrationTest.class);
    private static HuskySortBenchmark benchmark;
    private static Config config;
    private static HuskySequenceCoder<String> huskyCoder;
}