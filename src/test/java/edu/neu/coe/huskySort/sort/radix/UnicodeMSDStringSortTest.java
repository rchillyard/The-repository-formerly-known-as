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
import static edu.neu.coe.huskySort.util.Utilities.fillRandomArray;
import static org.junit.Assert.assertArrayEquals;

public class UnicodeMSDStringSortTest {

    static CharacterMap characterMap = new CharacterMap(ChineseCharacter::new, '阿');

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
    public void sortN1() {
        final String[] words = HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final Random random = new Random(0L);
        final Supplier<String[]> wordSupplier = getWordSupplier(words, 1000, random);
        final Benchmark<String[]> benchmark = new Benchmark<>("TestN1", null, xs -> Arrays.sort(xs, (o1, o2) -> {
            final CharacterMap.UnicodeString unicodeString1 = characterMap.new UnicodeString(o1);
            final CharacterMap.UnicodeString unicodeString2 = characterMap.new UnicodeString(o2);
            return unicodeString1.compare(unicodeString2, 0); // should test for the other positions, too.
        }), null);
        final double time = benchmark.run(wordSupplier, 1);

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

    static Supplier<String[]> getWordSupplier(final String[] words, final int nWords, final Random random) {
        return () -> fillRandomArray(String.class, random, nWords, r -> words[r.nextInt(words.length)]);
    }

}