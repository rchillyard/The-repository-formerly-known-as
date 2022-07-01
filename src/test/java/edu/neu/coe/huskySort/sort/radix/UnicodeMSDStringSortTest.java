package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.HelperFactory;
import edu.neu.coe.huskySort.sort.Sorter;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import edu.neu.coe.huskySort.util.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.CHINESE_NAMES_CORPUS;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.getWordSupplier;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class UnicodeMSDStringSortTest {

    static CharacterMap characterMap;

    @BeforeClass
    public static void beforeClass() {
        characterMap = new CharacterMap(ChineseCharacter::new, "Hanyu", '阿'); // NOTE: this is an attempt to ensure that the pinyin4j library gets initialized
    }

    @Test
    public void sort0() {
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        sorter.sortArray(new String[0]);
    }

    @Test
    public void sort1() {
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"阿"};
        sorter.sortArray(strings);
        assertArrayEquals(new String[]{"阿"}, strings);
    }

    @Test
    public void sort2() {
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings1 = {"阿", "朝"};
        sorter.sortArray(strings1);
        assertArrayEquals(new String[]{"阿", "朝"}, strings1);
        final String[] strings2 = {"朝", "阿"};
        sorter.sortArray(strings2);
        assertArrayEquals(new String[]{"阿", "朝"}, strings2);
    }

    @Test
    public void sort2A() {
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"卞", "毕"};
        sorter.sortArray(strings);
        assertArrayEquals(new String[]{"毕", "卞"}, strings);
    }

    @Test
    public void sort2B() {
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"涛", "林"};
        sorter.sortArray(strings);
        assertArrayEquals(new String[]{"林", "涛"}, strings);
    }

    @Test
    public void sortM1() {
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"邓世林", "邓世涛"};
        sorter.sortArray(strings);
        assertArrayEquals(new String[]{"邓世林", "邓世涛"}, strings);
    }

    @Test
    public void sortM2() throws IOException {
        final Config config = Config.load(UnicodeMSDStringSort.class).copy("helper", "cutoff", "0");
        final CountingSortHelper<UnicodeString, UnicodeCharacter> helper = HelperFactory.createCountingSortHelper("UnicodeMSDStringSort", 0, true, config);
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap, helper);
        final String[] strings = {"邓世林", "邓世涛"};
        sorter.sortArray(strings);
        assertArrayEquals(new String[]{"邓世林", "邓世涛"}, strings);
    }

    @Test
    public void sortM3() {
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "");
        final CountingSortHelper<UnicodeString, UnicodeCharacter> helper = HelperFactory.createCountingSortHelper("UnicodeMSDStringSort", 0, true, config);
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap, helper);
        final String[] strings = {"卞燕燕", "卞艳红"}; // bian4 yan4 yan4 AND bian4 yan4 hong2
        helper.init(strings.length);
        sorter.sortArray(strings);
        assertArrayEquals(new String[]{"卞艳红", "卞燕燕"}, strings);
        final StatPack statPack = ((Instrumented) helper).getStatPack();
        final int compares = (int) statPack.getStatistics(Instrumenter.COMPARES).mean();
        final int fixes = (int) statPack.getStatistics(Instrumenter.FIXES).mean();
        final int swaps = (int) statPack.getStatistics(Instrumenter.SWAPS).mean();
        final int copies = (int) statPack.getStatistics(Instrumenter.COPIES).mean();
        assertEquals(1, compares);
        assertEquals(0, fixes);
        assertEquals(1, swaps);
        assertEquals(4, copies);
    }

    @Test
    public void sortN1() {
        final String[] words = HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final Random random = new Random(0L);
        final Supplier<String[]> wordSupplier = getWordSupplier(words, 1000, random);
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        final Benchmark<String[]> benchmark = new Benchmark<>("TestN1", null, sorter::sortArray, HuskySortBenchmark::checkChineseSorted);
        final double time = benchmark.run(wordSupplier, 1);
        System.out.println("Time: " + time);

    }

    @Test
    public void sortN1SystemSort() {
        final String[] words = HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final Random random = new Random(0L);
        final Supplier<String[]> wordSupplier = getWordSupplier(words, 1000, random);
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap);
        final Benchmark<String[]> benchmark = new Benchmark<>("TestN1", null, xs -> Arrays.sort(xs, characterMap.stringComparator), HuskySortBenchmark::checkChineseSorted);
        final double time = benchmark.run(wordSupplier, 1);
        System.out.println("Time: " + time);
    }

    @Test
    public void sortNInstrumented() {
        final int n = 1000;
        final Config config = ConfigTest.setupConfig("true", "0", "10", "1", "");
        final CountingSortHelper<UnicodeString, UnicodeCharacter> helper = HelperFactory.createCountingSortHelper("basic counting sort helper", n, true, config);
        final Sorter<String> sorter = new UnicodeMSDStringSort(characterMap, helper);
        helper.init(n);
        final String[] words = HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final Supplier<String[]> wordSupplier = getWordSupplier(words, n, new Random(0L));
        final Benchmark<String[]> benchmark = new Benchmark<>("sortNInstrumented", null, sorter::sortArray, HuskySortBenchmark::checkChineseSorted);
        final double time = benchmark.run(wordSupplier, 1);
        System.out.println("Time: " + time);
        final double r = 180; // This is an estimate of the number of distinct pinyin forms that we find in our sample of n names.
        final double depth = Math.ceil(Math.log(n) / Math.log(r)); // This assumes that strings are generally longer than depth (else we should use mean length).
        final double estimatedCopies = (2 * n + r) * depth + 2 * n;
        final double estimatedHits = (5 * n + r) * depth + 4 * n;
        final StatPack statPack = ((Instrumented) helper).getStatPack();
        // NOTE that we do never cut over to insertion sort, so the number of compares, fixes, swaps will all be zero.
        final double copies = statPack.getStatistics(Instrumenter.COPIES).mean();
        final double hits = statPack.getStatistics(Instrumenter.HITS).mean();
        assertEquals(estimatedCopies, copies, 500);
        assertEquals(estimatedHits, hits, 1000);
    }
}