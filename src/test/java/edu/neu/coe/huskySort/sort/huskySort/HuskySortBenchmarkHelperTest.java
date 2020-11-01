package edu.neu.coe.huskySort.sort.huskySort;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HuskySortBenchmarkHelperTest {

    @SuppressWarnings("EmptyMethod")
    @Test
    public void testGetWords() {
        String[] words = HuskySortBenchmarkHelper.getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        assertEquals(2998, words.length);
        assertEquals("abandon", words[0]);
    }

    @Test
    public void testLogNormalizedTime() {
        HuskySortBenchmarkHelper.logNormalizedTime(1.0, "test", x -> x);
    }

    @SuppressWarnings("EmptyMethod")
    @Test
    public void testGenerateRandomStringArray() {
        // TEST
    }
}