package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MSDStringSortTest {

    @Test
    public void sort() {
        final String[] input = "she sells seashells by the seashore the shells she sells are surely seashells".split(" ");
        final String[] expected = "are by seashells seashells seashore sells sells she she shells surely the the".split(" ");

        MSDStringSort msdStringSort = new MSDStringSort(Alphabet.ASCII);
        msdStringSort.sort(input);
        System.out.println(Arrays.toString(input));
        assertArrayEquals(expected, input);
    }

    @Test
    public void sort1() {
        final Helper<String> helper = new BaseHelper<>("test", 1000, 1L);
        final String[] words = HuskySortBenchmarkHelper.getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        final String[] xs = helper.random(String.class, r -> words[r.nextInt(words.length)]);
        assertEquals(1000, xs.length);
        MSDStringSort msdStringSort = new MSDStringSort(Alphabet.ASCII);
        msdStringSort.sort(xs);
        assertEquals("African-American", xs[0]);
        assertEquals("Palestinian", xs[16]);
    }

    @Test
    public void sort2() {
        final Helper<String> helper = new BaseHelper<>("test", 1000, 1L);
        final String[] words = HuskySortBenchmarkHelper.getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        final String[] xs = helper.random(String.class, r -> words[r.nextInt(words.length)]);
        assertEquals(1000, xs.length);
        MSDStringSort msdStringSort = new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE));
        msdStringSort.sort(xs);
        assertEquals("African-American", xs[0]);
        assertEquals("Palestinian", xs[16]);
    }
}