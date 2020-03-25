/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.TimeLogger;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.getWords;

/**
 * NOTE: JUnit does not allow variables to be used for the timeout. That means that we cannot adjust these timeout for the speed of a particular machine.
 * The values given are for a MacBook Pro 2.8 GHz Intel Core i7 (4 cores) with 16 GB 2133 MHz LPDDRP.
 * Java version is 1.8.0_152.
 *
 * The expected time for a pure quicksort of N items and M runs is 1.39 k M N lg N (where lg represents log to the base 2).
 * Bear in mind that the Benchmark code does M/10 warmup runs also.
 */

@SuppressWarnings("ALL")
public class BenchmarkIntegrationTest {

    Config config = new Config("config.ini");
    HuskySortBenchmark benchmark = new HuskySortBenchmark(config);

    final Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");

    public BenchmarkIntegrationTest() throws IOException {
    }

    @Test
    public void test10K() throws Exception {
        benchmark.benchmarkStringSorters(getWords("eng-uk_web_2002_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 10000, 1000, new TimeLogger[]{new TimeLogger("Normalized time per run: ", (time, n) -> time / n / Math.log(n.doubleValue()) * 1e6)});
    }

    @Test(timeout = 70000)
    public void test100K() throws Exception {
        benchmark.benchmarkStringSorters(getWords("eng-uk_web_2002_100K-sentences.txt", line -> getWords(regexLeipzig, line)), 100000, 200, new TimeLogger[]{new TimeLogger("Normalized time per run: ", (time, n) -> time / n / Math.log(n.doubleValue()) * 1e6)});
    }
}