package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.sort.huskySortUtils.Coding;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.PrivateMethodInvoker;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;

public class PureHuskySortTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @Test
    public void testSortString1() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.unicodeCoder, false, false);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString2() {
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString3() {
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> {
            final int x = r.nextInt(1000000000);
            final BigInteger b = BigInteger.valueOf(x).multiply(BigInteger.valueOf(1000000));
            return b.toString();
        });
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString4() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString5() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Welcome"};
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString6() {
        // order:       453922  252568   145313   673679   181452   31014   988329   659494    923995   890721   744769   293165   520163   199395   669978   765753
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅", "杨腊香", "许凤山", "王广风", "黄锡鸿", "罗庆富", "顾芳芳", "宋雪光", "王诗卉"};
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.chineseEncoderCollator, false, false);
        sorter.sort(xs);
        System.out.println(Arrays.toString(xs));
        // order:           31014   145313   181452   199395   252568   293165   453922  520163   659494    669978   673679  744769   765753   890721   923995    988329
        final String[] sorted = {"曹玉德", "樊辉辉", "高民政", "顾芳芳", "洪文胜", "黄锡鸿", "刘持平", "罗庆富", "舒冬梅", "宋雪光", "苏会敏", "王广风", "王诗卉", "许凤山", "杨腊香", "袁继鹏"};
        assertArrayEquals(sorted, xs);
    }

    // FIXME this test should work correctly.
    @Test
    public void testSortString7() {
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅", "杨腊香", "许凤山", "王广风", "黄锡鸿", "罗庆富", "顾芳芳", "宋雪光", "王诗卉"};
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.chineseEncoderPinyin, false, false);
        sorter.sort(xs);
        System.out.println(Arrays.toString(xs));
        // TODO the order isn't quite right, in particular, shu should come before song in Hanyu.
        final String[] sorted = {"曹玉德", "樊辉辉", "高民政", "顾芳芳", "洪文胜", "黄锡鸿", "刘持平", "罗庆富", "舒冬梅", "宋雪光", "苏会敏", "王广风", "王诗卉", "许凤山", "杨腊香", "袁继鹏"};
        for (final String name : xs) System.out.println(name + ": " + ChineseCharacter.convertToPinyin(name));
        assertArrayEquals(sorted, xs);
    }

    @Test
    public void testFloorLg() {
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(PureHuskySort.class);
        assertEquals(1, privateMethodInvoker.invokePrivate("floor_lg", 3));
        assertEquals(2, privateMethodInvoker.invokePrivate("floor_lg", 5));
    }

    @Test
    public void testWithInsertionSort() {
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.asciiCoder, false, true);
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(sorter);
        //noinspection unchecked
        final HuskyCoder<String> huskyCoder = (HuskyCoder<String>) privateMethodInvoker.invokePrivate("getHuskyCoder");
        final int N = 100;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final Coding coding = huskyCoder.huskyEncode(xs);
        PureHuskySort.insertionSort(xs, coding.longs, 0, N);
        assertEquals(0, helper.inversions(xs));
    }

    @Test
    public void testInsertionSort() {
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(sorter);
        //noinspection unchecked
        final HuskyCoder<String> huskyCoder = (HuskyCoder<String>) privateMethodInvoker.invokePrivate("getHuskyCoder");
        final int N = 100;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final Coding coding = huskyCoder.huskyEncode(xs);
        PureHuskySort.insertionSort(xs, coding.longs, 0, N);
        assertEquals(0, helper.inversions(xs));
    }
}
