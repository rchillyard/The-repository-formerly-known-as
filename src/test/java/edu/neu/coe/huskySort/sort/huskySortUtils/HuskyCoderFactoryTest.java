package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.huskySort.PureHuskySort;
import edu.neu.coe.huskySort.util.PrivateMethodInvoker;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.asciiToLong;
import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.utf8ToLong;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@SuppressWarnings("SpellCheckingInspection")
public class HuskyCoderFactoryTest {

    @Test
    public void testStringToLong() {
        final PrivateMethodInvoker invoker = new PrivateMethodInvoker(HuskyCoderFactory.class);
        assertEquals(0x48cbb36000000000L, ((Long) invoker.invokePrivate("stringToLong", "Hell", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f0000000L, ((Long) invoker.invokePrivate("stringToLong", "Hello", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long) invoker.invokePrivate("stringToLong", "Hello, Go", 9, 7, 0x7F)).longValue());
        assertEquals(0x48CBB366F58823EFL, ((Long) invoker.invokePrivate("stringToLong", "Hello, Goodbye", 9, 7, 0x7F)).longValue());
    }

    @Test
    public void testAsciiToLong() {
        final String word = "a";
        assertEquals(0x6100000000000000L, asciiToLong(word));
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testAsciiCoder2() {
        final String apostroph = "apostroph";
        final int length = apostroph.length();
        final long expected1 = 0x61E1BF9F4E5BF868L;
        final HuskySequenceCoder<String> coder = HuskyCoderFactory.asciiCoder;
        assertEquals(expected1, coder.huskyEncode(apostroph));
        assertTrue(coder.perfectForLength(length));
        final long expected2 = 0x61E1BF9F4E5BF868L;
        assertEquals(expected2, coder.huskyEncode(apostroph + "e"));
        assertFalse(coder.perfectForLength(length + 1));
    }

    @Test
    public void testEnglishCoder1() {
        final HuskyCoder<String> coder = HuskyCoderFactory.englishCoder;
        assertEquals(0x0840000000000000L, coder.huskyEncode("a"));
        assertEquals(0x0880000000000000L, coder.huskyEncode("b"));
        assertEquals(0x0040000000000000L, coder.huskyEncode("A"));
        assertEquals(0x0080000000000000L, coder.huskyEncode("B"));
    }

    @Test
    public void testEnglishCoder2() {
        final String apostrophe = "apostrophe";
        final int length = apostrophe.length();
        final long expected1 = 0x870BF3D32BF0A25L;
        final long expected2 = 0x870BF3D32BF0A25L;
        final HuskySequenceCoder<String> coder = HuskyCoderFactory.englishCoder;
        assertEquals(expected1, coder.huskyEncode(apostrophe));
        assertTrue(coder.perfectForLength(length));
        assertEquals(expected2, coder.huskyEncode(apostrophe + "s"));
        assertFalse(coder.perfectForLength(length + 1));
    }

    @Test
    public void testChineseCoder() {
        final HuskySequenceCoder<String> coder = HuskyCoderFactory.chineseEncoderCollator;
        assertEquals(0x3E1101404E100L, coder.huskyEncode("曹玉德"));
        assertEquals(0x5890727072700L, coder.huskyEncode("樊辉辉"));
        assertEquals(0x6180A3410DD00L, coder.huskyEncode("高民政"));
    }

    @Test
    public void testUTF8ToLong() {
        final String[] words = {"中文", "太长的中文", "asdfghjkl", "¥", "c", "a𐍈", "𝒑𝒒"};
        final long[] codes = new long[7];
        final int bitWidth = 8;
        final long[] expected = {
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
        final long[] sortedExpected = {
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
    public void testUnicodeCoder1() {
        final HuskySequenceCoder<String> coder = HuskyCoderFactory.unicodeCoder;
        final boolean java8 = HuskySortHelper.isPreJava11;
        final String sAase = "Åse";
        final int lAase = sAase.length();
        final long expectedAase1 = 0x62803980328000L;
        assertEquals(expectedAase1, coder.huskyEncode(sAase));
        assertTrue(coder.perfectForLength(lAase));
        final long expectedAase2 = 0x6280398032803CL;
        assertEquals(expectedAase2, coder.huskyEncode(sAase + "x"));
        final String sMoskva = "Mосква";
        final int lMoskva = sMoskva.length();
        final long expectedM = 0x26821F0220821DL;
        assertEquals(expectedM, coder.huskyEncode(sMoskva));
        assertFalse(coder.perfectForLength(lMoskva));
        final String sSrebrenica = "Сребреница";
        final int lSrebrenica = sSrebrenica.length();
        final long expectedS = 0x2108220021A8218L;
        assertEquals(expectedS, coder.huskyEncode(sSrebrenica));
        assertFalse(coder.perfectForLength(lSrebrenica));
    }

    @Test
    public void testUnicodeCoder2() {
        final HuskySequenceCoder<String> coder = HuskyCoderFactory.unicodeCoder;
        final String s你好世界 = "你好世界";
        final int l你好世界 = s你好世界.length();
        final long expected你好世界 = 0x27B02CBEA70B3AA6L;
        assertEquals(expected你好世界, coder.huskyEncode(s你好世界));
        assertFalse(coder.perfectForLength(l你好世界));
        assertEquals(expected你好世界, coder.huskyEncode(s你好世界 + "人"));
    }

    @Test
    public void testUnicodeCoder3() {
        final HuskySequenceCoder<String> coder = HuskyCoderFactory.unicodeCoder;
        final String s送别 = "送别\n" + // requires 4 bytes of unicode
                "下馬飲君酒\n" + // requires 10 bytes of unicode of which we can only pack 7 bytes and 7 bits.
                "問君何所之\n" + // ditto
                "君言不得意\n" + // ditto
                "歸臥南山陲\n" + // ditto
                "但去莫復問\n" + // ditto
                "白雲無盡時"; // ditto
        final String[] xs = s送别.split("\n");
        final Coding coding = coder.huskyEncode(xs);
        final long[] longs = coding.longs;
        assertFalse(coding.perfect);
        assertEquals(7, longs.length);
        assertEquals(0x4800A91580000000L, longs[0]);
        assertEquals(0x2705CCD64C792A0DL, longs[1]);
        assertEquals(0x2AA7AA0DA7AAB120L, longs[2]);
        assertEquals(0x2A0DC5002706AFCBL, longs[3]);
        assertEquals(0x35BC40F2A9ABAE38L, longs[4]);
        assertEquals(0x27A329DDC1D5AFD4L, longs[5]);
        assertEquals(0x3B3ECB793890BB70L, longs[6]);
    }

    @Test
    public void testLongCoder() {
        final HuskyCoder<Long> coder = HuskyCoderFactory.longCoder;
        assertTrue(coder.perfect());
        assertEquals(Long.MAX_VALUE, coder.huskyEncode(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, coder.huskyEncode(Long.MIN_VALUE));
    }

    @Test
    public void testDoubleCoder() {
        final Long[] expectedOrder = {ldMinusMax, ldZero, ldMin, ldOne, ldMax};
        final HuskyCoder<Double> coder = HuskyCoderFactory.doubleCoder;
        assertFalse(coder.perfect());
        assertEquals(ldZero, coder.huskyEncode(dZero));
        assertEquals(ldOne, coder.huskyEncode(dOne));
        assertEquals(ldMinusMax, coder.huskyEncode(dMaxMinus));
        assertEquals(ldMax, coder.huskyEncode(dMax));
        assertEquals(ldMin, coder.huskyEncode(dMin));
        final List<Double> doubles = Arrays.asList(dZero, dMax, dMaxMinus, dOne, dMin);
        final Object[] result = doubles.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testDoubleCoder2() {
        final double bigMaxMinus = BigInteger.valueOf(Long.MAX_VALUE).negate().doubleValue();
        final double bigOneMinus = BigInteger.ONE.negate().doubleValue();
        final double bigZero = BigInteger.ZERO.doubleValue();
        final double bigRedOne = BigInteger.ONE.doubleValue();
        final double bigMax = BigInteger.valueOf(Long.MAX_VALUE).doubleValue();
        final Long[] expectedOrder = {llMaxMinus, llOneMinus, llZero, llOne, llMax};
        final HuskyCoder<Double> coder = HuskyCoderFactory.doubleCoder;
        assertFalse(coder.perfect());
        assertEquals(llZero, coder.huskyEncode(bigZero));
        assertEquals(llOne, coder.huskyEncode(bigRedOne));
        assertEquals(llMaxMinus, coder.huskyEncode(bigMaxMinus));
        assertEquals(llMax, coder.huskyEncode(bigMax));
        assertEquals(llOneMinus, coder.huskyEncode(bigOneMinus));
        final List<Double> doubles = Arrays.asList(bigZero, bigMax, bigMaxMinus, bigRedOne, bigOneMinus);
        final Object[] result = doubles.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testBigDecimalCoder() {
        final Long[] expectedOrder = {lllMin, 0L, ldZero, 0L, 0L, 1L, lllMax};
        final HuskyCoder<BigDecimal> coder = HuskyCoderFactory.bigDecimalCoder;
        assertFalse(coder.perfect());
        assertEquals(ldZero, coder.huskyEncode(BigDecimal.valueOf(dZero)));
        assertEquals(1L, coder.huskyEncode(BigDecimal.valueOf(dOne)));
        assertEquals(0L, coder.huskyEncode(BigDecimal.valueOf(dMaxMinus)));
        assertEquals(0L, coder.huskyEncode(BigDecimal.valueOf(dMax)));
        assertEquals(0L, coder.huskyEncode(BigDecimal.valueOf(dMin)));
        assertEquals(lllMax, coder.huskyEncode(BigDecimal.valueOf(Long.MAX_VALUE)));
        assertEquals(lllMin, coder.huskyEncode(BigDecimal.valueOf(Long.MIN_VALUE)));
        final List<BigDecimal> bigDecimals = Arrays.asList(BigDecimal.valueOf(0.0), BigDecimal.valueOf(Long.MAX_VALUE), BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal.valueOf(-Double.MAX_VALUE), BigDecimal.valueOf(1.0), BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.valueOf(Double.MIN_VALUE));
        final Object[] result = bigDecimals.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testScaledBigDecimalCoder() {
        final long lSmallPos = 163720000000000000L;
        final long lLargePos = 828340000000000000L;
        final long lLargeNeg = -636670000000000000L;
        final Long[] expectedOrder = {lLargeNeg, lSmallPos, lLargePos};
        // NOTE we make the resulting Longs 1000x bigger, thus better able to discriminate.
        final HuskyCoder<BigDecimal> coder = HuskyCoderFactory.scaledBigDecimalCoder(18);
        assertFalse(coder.perfect());
        final double smallPositive = 0.16372;
        final double largePositive = 0.82834;
        final double largeNegative = -0.63667;
        assertEquals(lSmallPos, coder.huskyEncode(BigDecimal.valueOf(smallPositive)));
        assertEquals(lLargePos, coder.huskyEncode(BigDecimal.valueOf(largePositive)));
        assertEquals(lLargeNeg, coder.huskyEncode(BigDecimal.valueOf(largeNegative)));
        final List<BigDecimal> bigDecimals = Arrays.asList(BigDecimal.valueOf(smallPositive), BigDecimal.valueOf(largeNegative), BigDecimal.valueOf(largePositive));
        final Object[] result = bigDecimals.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testBigIntegerCoder() {
        final Long[] expectedOrder = {llMaxMinus, llOneMinus, llZero, llOne, llMax};
        final HuskyCoder<BigInteger> coder = HuskyCoderFactory.bigIntegerCoder;
        assertFalse(coder.perfect());
        assertEquals(llZero, coder.huskyEncode(BigInteger.ZERO));
        assertEquals(llOne, coder.huskyEncode(BigInteger.ONE));
        assertEquals(llMaxMinus, coder.huskyEncode(BigInteger.valueOf(Long.MAX_VALUE).negate()));
        assertEquals(llMax, coder.huskyEncode(BigInteger.valueOf(Long.MAX_VALUE)));
        assertEquals(llOneMinus, coder.huskyEncode(BigInteger.ONE.negate()));
        final List<BigInteger> bigints = Arrays.asList(BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE).negate(), BigInteger.ONE, BigInteger.ONE.negate());
        final Object[] result = bigints.stream().map(coder::huskyEncode).sorted().toArray();
        assertArrayEquals(expectedOrder, result);
    }

    @Test
    public void testUTF8EncodingFromStringFromFile()
            throws IOException {
        final String file = "src/test/resources/SentiWS_v2.0_Positive.txt";
        final String encoding = "UTF-8";
        final BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(file)), encoding));
        final String line1 = reader.readLine();
        final String line2 = reader.readLine();
        reader.close();
        final long long1 = utf8ToLong(line1);
        final long long2 = utf8ToLong(line2);
        // "               A-b-m-a-c-h-u-n-g|NN\t0.0040\tAbmachungen";
        final long expected1 = 0x41626D616368756EL >>> 1;
        assertEquals(expected1, long1);
        // "               A-b-s-c-h-l-u-ß-|NN\t0.0040\tAbschluss,Abschlusse,Abschlusses,Abschlüsse,Abschlüssen";
        final long expected2 = 0x41627363686C75C3L >>> 1;
        assertEquals(expected2, long2);
        assertTrue(long1 < long2);
    }

    @Test
    public void testDoubleToLong() {
        compareEncodings(Math.PI, Math.E, HuskyCoderFactoryTest::doubleToLong, Double::compare);
        compareEncodings(-Math.PI, -Math.E, HuskyCoderFactoryTest::doubleToLong, Double::compare);
        compareEncodings(1E300, 1E301, HuskyCoderFactoryTest::doubleToLong, Double::compare);
        compareEncodings(1E-300, 1E-301, HuskyCoderFactoryTest::doubleToLong, Double::compare);
        compareEncodings(-1E300, -1E301, HuskyCoderFactoryTest::doubleToLong, Double::compare);
        compareEncodings(-1E-300, -1E-301, HuskyCoderFactoryTest::doubleToLong, Double::compare);
        compareEncodings(1E300, -1E301, HuskyCoderFactoryTest::doubleToLong, Double::compare);
        compareEncodings(1E-300, -1E-301, HuskyCoderFactoryTest::doubleToLong, Double::compare);
    }

    @Test
    public void testProbabilisticEncoder() {
        final HuskyCoder<Byte> byteCoder = new HuskyCoderFactory.ProbabilisticEncoder<Byte>(0.15, 1L) {
        };
        assertFalse(byteCoder.perfect());
        assertEquals(1L, byteCoder.huskyEncode((byte) 1));
        assertEquals(Byte.MIN_VALUE, byteCoder.huskyEncode(Byte.MIN_VALUE));
        assertEquals(0L, byteCoder.huskyEncode((byte) 0));
        assertEquals(Byte.MAX_VALUE, byteCoder.huskyEncode(Byte.MAX_VALUE));
        assertEquals(2L, byteCoder.huskyEncode((byte) 2));
        assertEquals(-4L, byteCoder.huskyEncode((byte) 3));
        assertEquals(4L, byteCoder.huskyEncode((byte) 4));
        assertEquals(5L, byteCoder.huskyEncode((byte) 5));
        assertEquals(6L, byteCoder.huskyEncode((byte) 6));
        assertEquals(1L, byteCoder.huskyEncode((byte) 1));
        final HuskyCoder<Integer> integerHuskyCoder = new HuskyCoderFactory.ProbabilisticEncoder<Integer>(0.15, 1L) {
        };
        assertEquals(Integer.MIN_VALUE, integerHuskyCoder.huskyEncode(Integer.MIN_VALUE));
        assertEquals(0L, integerHuskyCoder.huskyEncode(0));
        assertEquals(Integer.MAX_VALUE, integerHuskyCoder.huskyEncode(Integer.MAX_VALUE));
        assertEquals(2L, integerHuskyCoder.huskyEncode(2));
        assertEquals(3L, integerHuskyCoder.huskyEncode(3));
        assertEquals(-5L, integerHuskyCoder.huskyEncode(4));
        assertEquals(5L, integerHuskyCoder.huskyEncode(5));
        assertEquals(6L, integerHuskyCoder.huskyEncode(6));
    }

    @Test
    public void testBigDecimalEncoder() {
        compareHuskyEncodings(BigDecimal.valueOf(Math.PI), BigDecimal.valueOf(Math.E), HuskyCoderFactory.bigDecimalCoder, Comparator.naturalOrder());
        compareHuskyEncodings(BigDecimal.valueOf(Math.PI).negate(), BigDecimal.valueOf(Math.E).negate(), HuskyCoderFactory.bigDecimalCoder, Comparator.naturalOrder());
        compareHuskyEncodings(BigDecimal.valueOf(Math.PI).movePointLeft(100), BigDecimal.valueOf(Math.E).movePointLeft(100), HuskyCoderFactory.scaledBigDecimalCoder(118), Comparator.naturalOrder());
        compareHuskyEncodings(BigDecimal.valueOf(Math.PI).movePointRight(100), BigDecimal.valueOf(Math.E).movePointRight(100), HuskyCoderFactory.scaledBigDecimalCoder(-82), Comparator.naturalOrder());
        compareHuskyEncodings(BigDecimal.valueOf(Math.PI).movePointLeft(100).negate(), BigDecimal.valueOf(Math.E).movePointLeft(100).negate(), HuskyCoderFactory.scaledBigDecimalCoder(118), Comparator.naturalOrder());
        compareHuskyEncodings(BigDecimal.valueOf(Math.PI).movePointRight(100).negate(), BigDecimal.valueOf(Math.E).movePointRight(100).negate(), HuskyCoderFactory.scaledBigDecimalCoder(-82), Comparator.naturalOrder());
    }

    @Test
    public void testBigDecimalToLong() {
        // NOTE we don't have a scaled version of bigDecimalToLong
        compareEncodings(BigDecimal.valueOf(Math.PI), BigDecimal.valueOf(Math.E), HuskyCoderFactoryTest::bigDecimalToLong, Comparator.naturalOrder());
        compareEncodings(BigDecimal.valueOf(Math.PI).negate(), BigDecimal.valueOf(Math.E).negate(), HuskyCoderFactoryTest::bigDecimalToLong, Comparator.naturalOrder());
        compareEncodings(BigDecimal.valueOf(Math.PI).movePointRight(20), BigDecimal.valueOf(Math.E).movePointRight(20), HuskyCoderFactoryTest::bigDecimalToLong, Comparator.naturalOrder());
        compareEncodings(BigDecimal.valueOf(Math.PI).movePointRight(20).negate(), BigDecimal.valueOf(Math.E).movePointRight(20).negate(), HuskyCoderFactoryTest::bigDecimalToLong, Comparator.naturalOrder());
    }


    @Test
    public void testChineseEncoderPinyin1() {
        String 刘持平 = "刘持平";
        HuskyCoder<String> chineseEncoderPinyin = HuskyCoderFactory.chineseEncoderPinyin;
        long l刘持平 = chineseEncoderPinyin.huskyEncode(刘持平);
        assertEquals(0x519219026972L, l刘持平);
    }

    //    @Test
    public void testChineseEncoderPinyin2() {
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅", "杨腊香", "许凤山", "王广风", "黄锡鸿", "罗庆富", "顾芳芳", "宋雪光", "王诗卉"};
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.chineseEncoderPinyin, false, false);
        sorter.sort(xs);
        System.out.println(Arrays.toString(xs));
        // order:           31014   145313   181452   199395   252568   293165   453922  520163   659494    669978   673679  744769   765753   890721   923995    988329
        final String[] sorted = {"曹玉德", "樊辉辉", "高民政", "顾芳芳", "洪文胜", "黄锡鸿", "刘持平", "罗庆富", "舒冬梅", "宋雪光", "苏会敏", "王广风", "王诗卉", "许凤山", "杨腊香", "袁继鹏"};
        for (String name : xs) System.out.println(name + ": " + ChineseCharacter.convertToPinyin(name));
        assertArrayEquals(sorted, xs);
    }


    public static <X> void compareEncodings(final X x1, final X x2, final Function<X, Long> encoder, final Comparator<X> comparator) {
        assertEquals(comparator.compare(x1, x2), Long.compare(encoder.apply(x1), encoder.apply(x2)));
    }

    public static <X> void compareHuskyEncodings(final X x1, final X x2, final HuskyCoder<X> encoder, final Comparator<X> comparator) {
        assertEquals(comparator.compare(x1, x2), Long.compare(encoder.huskyEncode(x1), encoder.huskyEncode(x2)));
    }

    private static long doubleToLong(final double x) {
        return (Long) huskyCoderFactoryinvoker.invokePrivate("doubleToLong", x);
    }

    /**
     * NOTE that this will not work precisely for small or large BigDecimals.
     *
     * @param x a BigDecimal.
     * @return the value of x in the form of a long.
     */
    private static long bigDecimalToLong(final BigDecimal x) {
        return x.longValue();
    }


    static final PrivateMethodInvoker huskyCoderFactoryinvoker = new PrivateMethodInvoker(HuskyCoderFactory.class);
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

    public static final long lllMax = 0x7FFFFFFFFFFFFFFFL;
    public static final long lllMin = 0x8000000000000000L;
}
