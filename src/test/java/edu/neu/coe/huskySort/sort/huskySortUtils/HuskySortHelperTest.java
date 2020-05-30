package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.util.PrivateMethodTester;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.asciiToLong;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.utf8ToLong;
import static junit.framework.TestCase.assertEquals;

public class HuskySortHelperTest {

    @Test
    public void testStringToLong() {
        final PrivateMethodTester tester = new PrivateMethodTester(HuskySortHelper.class);
        assertEquals(0x48cbb36000000000L, ((Long)tester.invokePrivate("stringToLong", "Hell", 9, 7)).longValue());
        assertEquals(0x48cbb366f0000000L, ((Long)tester.invokePrivate("stringToLong", "Hello", 9, 7)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long)tester.invokePrivate("stringToLong", "Hello, Go", 9, 7)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long)tester.invokePrivate("stringToLong", "Hello, Goodbye", 9, 7)).longValue());
    }

    @Test
    public void testAsciiToLong() {
        String word = "a";
        assertEquals(6989586621679009792L, asciiToLong(word));
    }

    // FIXME fix what's wrong with this test
    @Ignore
    public void testUTF8ToLong() {
        String[] words = {"中文", "太长的中文", "asdfghjkl", "¥", "c", "a"};
        long[] codes = new long[6];
        int bitWidth = 8;
        long[] expected = {
                0xE4B8ADE69687L << (bitWidth),  // 中文
                0xE5A4AAE995BFE7L,                  // 太长的中文
                0x6173646667686AL,                  // asdfghjkl
                0xC2A5L << (5 * bitWidth),          // ¥
                0x63L << (6 * bitWidth),            // c
                0x61L << (6 * bitWidth),            // a
        };
        for (int i = 0; i < words.length; i++) {
            codes[i] = utf8ToLong(words[i]);
        }

        Assert.assertArrayEquals(expected, codes);
        Arrays.sort(codes);
        long[] sortedExpected = {
                0x61L << (6 * bitWidth),            // a
                0x6173646667686AL,                  // asdfghjkl
                0x63L << (6 * bitWidth),            // c
                0xC2A5L << (5 * bitWidth),          // ¥
                0xE4B8ADE69687L << (bitWidth),  // 中文
                0xE5A4AAE995BFE7L,                  // 太长的中文
        };
        Assert.assertArrayEquals(sortedExpected, codes);
    }
}
