package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.util.PrivateMethodTester;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.asciiToLong;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.utf8ToLong;
import static junit.framework.TestCase.assertEquals;

public class HuskySortHelperTest {

    @Test
    public void testStringToLong() {
        final PrivateMethodTester tester = new PrivateMethodTester(HuskySortHelper.class);
        assertEquals(0x48cbb36000000000L, ((Long) tester.invokePrivate("stringToLong", "Hell", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f0000000L, ((Long) tester.invokePrivate("stringToLong", "Hello", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long) tester.invokePrivate("stringToLong", "Hello, Go", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long) tester.invokePrivate("stringToLong", "Hello, Goodbye", 9, 7, 0x7F)).longValue());
    }

    @Test
    public void testAsciiToLong() {
        String word = "a";
        long actual = asciiToLong(word);
        assertEquals(0x6100000000000000L, actual);
    }

    @Test
    public void testPrintableAsciiToLong() {
        HuskyCoder<String> coder = HuskySortHelper.printableAsciiCoder;
        assertEquals(0x0840000000000000L, coder.huskyEncode("a"));
        assertEquals(0x0880000000000000L, coder.huskyEncode("b"));
        assertEquals(0x0040000000000000L, coder.huskyEncode("A"));
        assertEquals(0x0080000000000000L, coder.huskyEncode("B"));
    }

    @Test
    public void testUTF8ToLong() {
        String[] words = {"中文", "太长的中文", "asdfghjkl", "¥", "c", "a"};
        long[] codes = new long[6];
        int bitWidth = 8;
        long[] expected = {
                (0xE4B8ADE69687L << (2 * bitWidth)) >>> 1,  // 中文
                0xE5A4AAE995BFE79AL >>> 1,                  // 太长的中文
                0x6173646667686A6BL >>> 1,                  // asdfghjkl
                (0xC2A5L << (6 * bitWidth)) >>> 1,          // ¥
                (0x63L << (7 * bitWidth)) >>> 1,            // c
                (0x61L << (7 * bitWidth)) >>> 1,            // a
        };
        for (int i = 0; i < words.length; i++) {
            codes[i] = utf8ToLong(words[i]);
        }

        Assert.assertArrayEquals(expected, codes);
        Arrays.sort(codes);
        long[] sortedExpected = {
                (0x61L << (7 * bitWidth)) >>> 1,            // a
                0x6173646667686A6BL >>> 1,                  // asdfghjkl
                (0x63L << (7 * bitWidth)) >>> 1,            // c
                (0xC2A5L << (6 * bitWidth)) >>> 1,          // ¥
                (0xE4B8ADE69687L << (2 * bitWidth)) >>> 1,  // 中文
                0xE5A4AAE995BFE79AL >>> 1,                  // 太长的中文
        };
        Assert.assertArrayEquals(sortedExpected, codes);
    }
}
