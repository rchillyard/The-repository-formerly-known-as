package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.util.PrivateMethodInvoker;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.asciiToLong;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.utf8ToLong;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class HuskyCoderFactoryTest {

    @Test
    public void testStringToLong() {
        final PrivateMethodInvoker invoker = new PrivateMethodInvoker(HuskyCoderFactory.class);
        assertEquals(0x48cbb36000000000L, ((Long) invoker.invokePrivate("stringToLong", "Hell", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f0000000L, ((Long) invoker.invokePrivate("stringToLong", "Hello", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long) invoker.invokePrivate("stringToLong", "Hello, Go", 9, 7, 0x7F)).longValue());
        assertEquals(0xC8CBB366F58823EFL, ((Long) invoker.invokePrivate("stringToLong", "Hello, Goodbye", 9, 7, 0x7F)).longValue());
    }

    @Test
    public void testAsciiToLong() {
        String word = "a";
        assertEquals(0x6100000000000000L, asciiToLong(word));
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testAsciiCoder2() {
        final String apostroph = "apostroph";
        final long expected = 0x61E1BF9F4E5BF868L;
        HuskySequenceCoder<String> coder = HuskyCoderFactory.asciiCoder;
        assertEquals(expected, coder.huskyEncode(apostroph));
        assertTrue(coder.perfect(apostroph));
        assertEquals(expected, coder.huskyEncode(apostroph + "e"));
        assertFalse(coder.perfect(apostroph + "e"));
    }

    @Test
    public void testEnglishCoder1() {
        HuskyCoder<String> coder = HuskyCoderFactory.englishCoder;
        assertEquals(0x0840000000000000L, coder.huskyEncode("a"));
        assertEquals(0x0880000000000000L, coder.huskyEncode("b"));
        assertEquals(0x0040000000000000L, coder.huskyEncode("A"));
        assertEquals(0x0080000000000000L, coder.huskyEncode("B"));
    }

    @Test
    public void testEnglishCoder2() {
        final String apostrophe = "apostrophe";
        final long expected = 0x870BF3D32BF0A25L;
        HuskySequenceCoder<String> coder = HuskyCoderFactory.englishCoder;
        assertEquals(expected, coder.huskyEncode(apostrophe));
        assertTrue(coder.perfect(apostrophe));
        assertEquals(expected, coder.huskyEncode(apostrophe + "s"));
        assertFalse(coder.perfect(apostrophe + "s"));
    }

    @Test
    public void testUTF8ToLong() {
        String[] words = {"中文", "太长的中文", "asdfghjkl", "¥", "c", "a𐍈", "𝒑𝒒"};
        long[] codes = new long[7];
        int bitWidth = 8;
        long[] expected = {
                // Here we manually encode some strings to utf-8 format.
                // Chinese string
                (0xE4B8ADE69687L << (2 * bitWidth)) >>> 1,  // 中文
                // Too long Chinese string
                0xE5A4AAE995BFE79AL >>> 1,                  // 太长的中文
                // Too long English string
                0x6173646667686A6BL >>> 1,                  // asdfghjkl
                // A special symbol
                (0xC2A5L << (6 * bitWidth)) >>> 1,          // ¥
                // short enough English string
                (0x63L << (7 * bitWidth)) >>> 1,            // c
                // English and a special character which takes 4 bytes to encode in UTF-8
                (0x61F0908D88L << (3 * bitWidth)) >>> 1,    // a𐍈
                // Special characters which take 4 bytes to encode in UTF-8
                0xf09d9291f09d9292L >>> 1
        };

        // We test if they are correctly encoded.
        for (int i = 0; i < words.length; i++) {
            codes[i] = utf8ToLong(words[i]);
        }

        Assert.assertArrayEquals(expected, codes);
        Arrays.sort(codes);

        // We test if they are correctly sorted.
        long[] sortedExpected = {
                0x6173646667686A6BL >>> 1,                  // asdfghjkl
                (0x61F0908D88L << (3 * bitWidth)) >>> 1,    // a𐍈
                (0x63L << (7 * bitWidth)) >>> 1,            // c
                (0xC2A5L << (6 * bitWidth)) >>> 1,          // ¥
                (0xE4B8ADE69687L << (2 * bitWidth)) >>> 1,  // 中文
                0xE5A4AAE995BFE79AL >>> 1,                  // 太长的中文
                0xf09d9291f09d9292L >>> 1                   // 𝒑𝒒
        };
        Assert.assertArrayEquals(sortedExpected, codes);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testUnicodeCoder() {
        HuskySequenceCoder<String> coder = HuskyCoderFactory.unicodeCoder;
        boolean java8 = HuskySortHelper.isPreJava11;
        final String sAase = "Åse";
        long expectedAase1 = 0x62803980328000L;
        assertEquals(expectedAase1, coder.huskyEncode(sAase));
        assertTrue(coder.perfect(sAase));
        long expectedAase2 = 0x6280398032803CL;
        assertEquals(expectedAase2, coder.huskyEncode(sAase + "x"));
        final String sMoskva = "Mосква";
        long expectedM = 0x4026821F0220821DL;
        assertEquals(expectedM, coder.huskyEncode(sMoskva));
        assertFalse(coder.perfect(sMoskva));
        final String sSrebrenica = "Сребреница";
        long expectedS = 0x42108220021A8218L;
        assertEquals(expectedS, coder.huskyEncode(sSrebrenica));
        assertFalse(coder.perfect(sSrebrenica));
    }

    @Test
    public void testLongCoder() {
        HuskyCoder<Long> coder = HuskyCoderFactory.longCoder;
        assertTrue(coder.perfect());
        assertEquals(Long.MAX_VALUE, coder.huskyEncode(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, coder.huskyEncode(Long.MIN_VALUE));
    }

    @Test
    public void testDoubleCoder() {
        Long[] expectedOrder = {ldMinusMax, ldZero, ldMin, ldOne, ldMax};
        HuskyCoder<Double> coder = HuskyCoderFactory.doubleCoder;
        assertFalse(coder.perfect());
        assertEquals(ldZero, coder.huskyEncode(dZero));
        assertEquals(ldOne, coder.huskyEncode(dOne));
        assertEquals(ldMinusMax, coder.huskyEncode(dMaxMinus));
        assertEquals(ldMax, coder.huskyEncode(dMax));
        assertEquals(ldMin, coder.huskyEncode(dMin));
        List<Double> doubles = Arrays.asList(dZero, dMax, dMaxMinus, dOne, dMin);
        final Object[] result = doubles.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testDoubleCoder2() {
        double bigMaxMinus = BigInteger.valueOf(Long.MAX_VALUE).negate().doubleValue();
        double bigOneMinus = BigInteger.ONE.negate().doubleValue();
        double bigZero = BigInteger.ZERO.doubleValue();
        double bigRedOne = BigInteger.ONE.doubleValue();
        double bigMax = BigInteger.valueOf(Long.MAX_VALUE).doubleValue();
        Long[] expectedOrder = {llMaxMinus, llOneMinus, llZero, llOne, llMax};
        HuskyCoder<Double> coder = HuskyCoderFactory.doubleCoder;
        assertFalse(coder.perfect());
        assertEquals(llZero, coder.huskyEncode(bigZero));
        assertEquals(llOne, coder.huskyEncode(bigRedOne));
        assertEquals(llMaxMinus, coder.huskyEncode(bigMaxMinus));
        assertEquals(llMax, coder.huskyEncode(bigMax));
        assertEquals(llOneMinus, coder.huskyEncode(bigOneMinus));
        List<Double> doubles = Arrays.asList(bigZero, bigMax, bigMaxMinus, bigRedOne, bigOneMinus);
        final Object[] result = doubles.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testBigDecimalCoder() {
        Long[] expectedOrder = {ldMinusMax, ldZero, ldMin, ldOne, ldMax};
        HuskyCoder<BigDecimal> coder = HuskyCoderFactory.bigDecimalCoder;
        assertFalse(coder.perfect());
        assertEquals(ldZero, coder.huskyEncode(BigDecimal.valueOf(dZero)));
        assertEquals(ldOne, coder.huskyEncode(BigDecimal.valueOf(dOne)));
        assertEquals(ldMinusMax, coder.huskyEncode(BigDecimal.valueOf(dMaxMinus)));
        assertEquals(ldMax, coder.huskyEncode(BigDecimal.valueOf(dMax)));
        assertEquals(ldMin, coder.huskyEncode(BigDecimal.valueOf(dMin)));
        List<BigDecimal> bigDecimals = Arrays.asList(BigDecimal.valueOf(0.0), BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal.valueOf(-Double.MAX_VALUE), BigDecimal.valueOf(1.0), BigDecimal.valueOf(Double.MIN_VALUE));
        final Object[] result = bigDecimals.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testBigIntegerCoder() {
        Long[] expectedOrder = {llMaxMinus, llOneMinus, llZero, llOne, llMax};
        HuskyCoder<BigInteger> coder = HuskyCoderFactory.bigIntegerCoder;
        assertFalse(coder.perfect());
        assertEquals(llZero, coder.huskyEncode(BigInteger.ZERO));
        assertEquals(llOne, coder.huskyEncode(BigInteger.ONE));
        assertEquals(llMaxMinus, coder.huskyEncode(BigInteger.valueOf(Long.MAX_VALUE).negate()));
        assertEquals(llMax, coder.huskyEncode(BigInteger.valueOf(Long.MAX_VALUE)));
        assertEquals(llOneMinus, coder.huskyEncode(BigInteger.ONE.negate()));
        List<BigInteger> bigints = Arrays.asList(BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE).negate(), BigInteger.ONE, BigInteger.ONE.negate());
        final Object[] result = bigints.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testUTF8EncodingFromStringFromFile()
            throws IOException {
        String file = "src/test/resources/SentiWS_v2.0_Positive.txt";
        String encoding = "UTF-8";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        String line1 = reader.readLine();
        String line2 = reader.readLine();
        reader.close();
        long long1 = utf8ToLong(line1);
        long long2 = utf8ToLong(line2);
        // "               A-b-m-a-c-h-u-n-g|NN\t0.0040\tAbmachungen";
        long expected1 = 0x41626D616368756EL >>> 1;
        assertEquals(expected1, long1);
        // "               A-b-s-c-h-l-u-ß-|NN\t0.0040\tAbschluss,Abschlusse,Abschlusses,Abschlüsse,Abschlüssen";
        long expected2 = 0x41627363686C75C3L >>> 1;
        assertEquals(expected2, long2);
        assertTrue(long1 < long2);
    }

    static final long llMaxMinus = 0xBC20000000000000L; // -4890909195324358656
    static final long llOneMinus = 0xC010000000000000L; // -4616189618054758400
    static final long llZero = 0L;
    static final long llOne = 0x3FF0000000000000L; // 4607182418800017408
    static final long llMax = 0x43E0000000000000L; // 4890909195324358656
    static final double dZero = 0.0;
    static final double dOne = 1.0;
    static final double dMax = Double.MAX_VALUE;
    static final double dMaxMinus = -dMax;
    static final double dMin = Double.MIN_VALUE;
    static final long ldMinusMax = 0x8010000000000001L; // -9218868437227405311
    static final long ldZero = 0L;
    static final long ldMin = 1L;
    static final long ldOne = 0x3FF0000000000000L; // 4607182418800017408
    static final long ldMax = 0x7FEFFFFFFFFFFFFFL; // 9218868437227405311

}
