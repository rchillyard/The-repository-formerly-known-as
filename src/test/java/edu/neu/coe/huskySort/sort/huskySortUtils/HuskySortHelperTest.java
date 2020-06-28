package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.util.PrivateMethodTester;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.asciiToLong;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.utf8ToLong;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

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

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testAsciiCoder2() {
        HuskySequenceCoder<String> coder = HuskySortHelper.asciiCoder;
        final String apostroph = "apostroph";
        final long expected = 0x61E1BF9F4E5BF868L;
        final long actual = coder.huskyEncode(apostroph);
        assertEquals(expected, actual);
        assertTrue(coder.perfect(apostroph));
        assertEquals(expected, coder.huskyEncode(apostroph + "e"));
        assertFalse(coder.perfect(apostroph + "e"));
    }

    @Test
    public void testEnglishCoder1() {
        HuskyCoder<String> coder = HuskySortHelper.englishCoder;
        assertEquals(0x0840000000000000L, coder.huskyEncode("a"));
        assertEquals(0x0880000000000000L, coder.huskyEncode("b"));
        assertEquals(0x0040000000000000L, coder.huskyEncode("A"));
        assertEquals(0x0080000000000000L, coder.huskyEncode("B"));
    }

    @Test
    public void testEnglishCoder2() {
        HuskySequenceCoder<String> coder = HuskySortHelper.englishCoder;
        final String apostrophe = "apostrophe";
        final long expected = 0x870BF3D32BF0A25L;
        assertEquals(expected, coder.huskyEncode(apostrophe));
        assertTrue(coder.perfect(apostrophe));
        assertEquals(expected, coder.huskyEncode(apostrophe + "s"));
        assertFalse(coder.perfect(apostrophe + "s"));
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

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testUnicodeCoder() {
        HuskySequenceCoder<String> coder = HuskySortHelper.unicodeCoder;
        final String ase = "Åse";
        assertEquals(0x7FE1FFC280398032L, coder.huskyEncode(ase));
        assertTrue(coder.perfect(ase));
        assertEquals(0x7FE1FFC280398032L, coder.huskyEncode(ase + "x"));
        final String moskva = "Mосква";
        assertEquals(0x26FFE87FDF7FE8L, coder.huskyEncode(moskva));
        assertFalse(coder.perfect(moskva));
        final String srebrenica = "Сребреница";
        assertEquals(0x7FE87FD0FFE8FFC0L, coder.huskyEncode(srebrenica));
        assertFalse(coder.perfect(srebrenica));
    }

    @Test
    public void testLongCoder() {
        HuskyCoder<Long> coder = HuskySortHelper.longCoder;
        assertTrue(coder.perfect());
        assertEquals(Long.MAX_VALUE, coder.huskyEncode(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, coder.huskyEncode(Long.MIN_VALUE));
    }

    @Test
    public void testDoubleCoder() {
        HuskyCoder<Double> coder = HuskySortHelper.doubleCoder;
        assertFalse(coder.perfect());
        final long minusMaxLong = -4503599627370497L;
        final long zeroLong = 0L;
        final long minLong = 1L;
        final long oneLong = 4607182418800017408L;
        final long maxLong = 9218868437227405311L;
        assertEquals(zeroLong, coder.huskyEncode(0.0));
        assertEquals(oneLong, coder.huskyEncode(1.0));
        assertEquals(minusMaxLong, coder.huskyEncode(-Double.MAX_VALUE));
        assertEquals(maxLong, coder.huskyEncode(Double.MAX_VALUE));
        assertEquals(minLong, coder.huskyEncode(Double.MIN_VALUE));
        List<Double> doubles = Arrays.asList(0.0, Double.MAX_VALUE, -Double.MAX_VALUE, 1.0, Double.MIN_VALUE);
        final Object[] result = doubles.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(new Long[]{minusMaxLong, zeroLong, minLong, oneLong, maxLong}, result);
    }
}
