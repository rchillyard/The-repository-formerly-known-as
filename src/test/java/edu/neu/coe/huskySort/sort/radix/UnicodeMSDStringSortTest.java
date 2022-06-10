package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.util.Benchmark;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.CHINESE_NAMES_CORPUS;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.getWordSupplier;
import static org.junit.Assert.assertArrayEquals;

public class UnicodeMSDStringSortTest {

    static CharacterMap characterMap;

    @BeforeClass
    public static void beforeClass() {
        characterMap = new CharacterMap(ChineseCharacter::new, '阿'); // NOTE: this is an attempt to ensure that the pinyin4j library gets initialized
    }

    @Test
    public void sort0() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        sorter.sort(new String[0]);
    }

    @Test
    public void sort1() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"阿"};
        sorter.sort(strings);
        assertArrayEquals(new String[]{"阿"}, strings);
    }

    @Test
    public void sort2() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings1 = {"阿", "朝"};
        sorter.sort(strings1);
        assertArrayEquals(new String[]{"阿", "朝"}, strings1);
        final String[] strings2 = {"朝", "阿"};
        sorter.sort(strings2);
        assertArrayEquals(new String[]{"阿", "朝"}, strings2);
    }

    @Test
    public void sort2A() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"卞", "毕"};
        sorter.sort(strings);
        assertArrayEquals(new String[]{"毕", "卞"}, strings);
    }

    @Test
    public void sort2B() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"涛", "林"};
        sorter.sort(strings);
        assertArrayEquals(new String[]{"林", "涛"}, strings);
    }

    @Test
    public void sortM1() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"邓世林", "邓世涛"};
        sorter.sort(strings);
        assertArrayEquals(new String[]{"邓世林", "邓世涛"}, strings);
    }

    @Test
    public void sortM2() {
        UnicodeMSDStringSort.setCutoff(0);
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"邓世林", "邓世涛"};
        sorter.sort(strings);
        assertArrayEquals(new String[]{"邓世林", "邓世涛"}, strings);
    }

    @Test
    public void sortN1() {
        final String[] words = HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final Random random = new Random(0L);
        final Supplier<String[]> wordSupplier = getWordSupplier(words, 1000, random);
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final Benchmark<String[]> benchmark = new Benchmark<>("TestN1", null, sorter::sort, HuskySortBenchmark::checkChineseSorted);
        final double time = benchmark.run(wordSupplier, 1);
        System.out.println("Time: " + time);

    }

    @Test
    public void sortN1SystemSort() {
        final String[] words = HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final Random random = new Random(0L);
        final Supplier<String[]> wordSupplier = getWordSupplier(words, 1000, random);
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final Benchmark<String[]> benchmark = new Benchmark<>("TestN1", null, xs -> Arrays.sort(xs, characterMap.stringComparator), null);
        final double time = benchmark.run(wordSupplier, 1);
        System.out.println("Time: " + time);
    }

    @Test
    public void reset() {
    }

    @Test
    public void getCharacterMap() {
    }

    @Test
    public void setCutoff() {
    }
}