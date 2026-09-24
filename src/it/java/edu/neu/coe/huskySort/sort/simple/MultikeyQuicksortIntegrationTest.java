/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

/**
 * Correctness checks for MultikeyQuicksort (three-way radix quicksort, Bentley and Sedgewick
 * 1997), added as a new baseline for the paper's classic string-sorting literature comparison.
 * Verified against Arrays.sort as ground truth throughout, per this repo's established practice
 * of not trusting a new sort implementation until it has been checked this way.
 */
public class MultikeyQuicksortIntegrationTest {

    /**
     * sortByPinyin, checked against the same ground truth this codebase already trusts for
     * pinyin ordering: HuskyCoderChinesePinyin.NAME_ORDER, the comparator RadixHuskySort's own
     * cleanup pass relies on for correctness (see doc/Radix Sort Benchmark Results.md's "Chinese
     * names" section).
     */
    @Test
    public void sortsRealChineseNamesCorrectlyByPinyin() throws Exception {
        final String[] names = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final String[] xs = Arrays.copyOf(names, names.length);
        final String[] expected = Arrays.copyOf(names, names.length);
        Arrays.sort(expected, HuskyCoderChinesePinyin.NAME_ORDER);
        MultikeyQuicksort.sortByPinyin(xs);
        assertArrayEquals(expected, xs);
    }
}
