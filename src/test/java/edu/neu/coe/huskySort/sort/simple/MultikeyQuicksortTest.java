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
public class MultikeyQuicksortTest {

    @Test
    public void sortsRandomAsciiStringsCorrectly() {
        final Random random = new Random(1);
        for (final int n : new int[]{0, 1, 2, 3, 10, 100, 1000, 10_000}) {
            for (int trial = 0; trial < 5; trial++) {
                final String[] xs = randomStrings(random, n, 0, 20, 'a', 'z');
                final String[] expected = Arrays.copyOf(xs, n);
                Arrays.sort(expected);
                MultikeyQuicksort.sort(xs);
                assertArrayEquals("n=" + n + " trial=" + trial, expected, xs);
            }
        }
    }

    @Test
    public void sortsHeavyDuplicatesCorrectly() {
        final Random random = new Random(2);
        // A tiny alphabet and short max length guarantees many exact-duplicate strings.
        for (final int n : new int[]{10, 100, 1000, 10_000}) {
            final String[] xs = randomStrings(random, n, 0, 3, 'a', 'c');
            final String[] expected = Arrays.copyOf(xs, n);
            Arrays.sort(expected);
            MultikeyQuicksort.sort(xs);
            assertArrayEquals("n=" + n, expected, xs);
        }
    }

    @Test
    public void sortsVaryingLengthStringsIncludingPrefixesOfEachOther() {
        final String[] xs = {"banana", "ban", "ba", "b", "", "bandana", "banana", "banan"};
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        MultikeyQuicksort.sort(xs);
        assertArrayEquals(expected, xs);
    }

    @Test
    public void sortsEmptyAndSingletonArrays() {
        final String[] empty = {};
        MultikeyQuicksort.sort(empty);
        assertArrayEquals(new String[]{}, empty);

        final String[] singleton = {"x"};
        MultikeyQuicksort.sort(singleton);
        assertArrayEquals(new String[]{"x"}, singleton);
    }

    @Test
    public void sortsAllIdenticalStringsCorrectly() {
        final String[] xs = new String[5000];
        Arrays.fill(xs, "same");
        final String[] expected = Arrays.copyOf(xs, xs.length);
        MultikeyQuicksort.sort(xs);
        assertArrayEquals(expected, xs);
    }

    /**
     * Adversarial case matching this document's earlier "shared long common prefix" scenario:
     * every string shares the same first 5,000 characters before differing. A naive recursive
     * implementation with no depth limit recurses once per shared character in the "equal"
     * partition, so this also checks that 5,000 shared characters does not overflow the stack.
     */
    @Test
    public void sortsLongSharedPrefixStringsCorrectly() {
        final Random random = new Random(3);
        final String prefix = "a".repeat(5000);
        final String[] xs = new String[500];
        for (int i = 0; i < xs.length; i++) xs[i] = prefix + random.nextInt(1000);
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        MultikeyQuicksort.sort(xs);
        assertArrayEquals(expected, xs);
    }

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

    /**
     * Explicit true-homonym check: 郗 and 奚 are both pronounced "xi1" -- identical syllable and
     * tone -- so pinyin order alone cannot distinguish them; NAME_ORDER's documented fallback is
     * Unicode code point. Confirms sortByPinyin reproduces that exact tie-break, not just "some"
     * order for characters pinyin can't distinguish.
     */
    @Test
    public void breaksTrueHomonymTiesByCodePointLikeNameOrderDoes() {
        final String[] xs = {"奚", "郗", "奚", "郗"};
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected, HuskyCoderChinesePinyin.NAME_ORDER);
        MultikeyQuicksort.sortByPinyin(xs);
        assertArrayEquals(expected, xs);
    }

    @Test
    public void sortsSyntheticPinyinNamesWithDuplicatesAndVaryingLengths() {
        final Random random = new Random(4);
        final String[] characters = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴"};
        final String[] xs = new String[2000];
        for (int i = 0; i < xs.length; i++) {
            final int len = 2 + random.nextInt(2);
            final StringBuilder sb = new StringBuilder(len);
            for (int j = 0; j < len; j++) sb.append(characters[random.nextInt(characters.length)]);
            xs[i] = sb.toString();
        }
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected, HuskyCoderChinesePinyin.NAME_ORDER);
        MultikeyQuicksort.sortByPinyin(xs);
        assertArrayEquals(expected, xs);
    }

    private static String[] randomStrings(final Random random, final int n, final int minLen, final int maxLen, final char from, final char to) {
        final String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            final int len = minLen + random.nextInt(maxLen - minLen + 1);
            final StringBuilder sb = new StringBuilder(len);
            for (int j = 0; j < len; j++) sb.append((char) (from + random.nextInt(to - from + 1)));
            result[i] = sb.toString();
        }
        return result;
    }
}
