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
import java.util.Random;

import static org.junit.Assert.*;

public class QuickHuskySortTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @Test
    public void testSortString1() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.unicodeCoder, false, false);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString2() {
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString3() {
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
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
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString5() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Welcome"};
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString6() {
        // order:       453922  252568   145313   673679   181452   31014   988329   659494    923995   890721   744769   293165   520163   199395   669978   765753
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅", "杨腊香", "许凤山", "王广风", "黄锡鸿", "罗庆富", "顾芳芳", "宋雪光", "王诗卉"};
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.chineseEncoderCollator, false, false);
        sorter.sort(xs);
        System.out.println(Arrays.toString(xs));
        // order:           31014   145313   181452   199395   252568   293165   453922  520163   659494    669978   673679  744769   765753   890721   923995    988329
        final String[] sorted = {"曹玉德", "樊辉辉", "高民政", "顾芳芳", "洪文胜", "黄锡鸿", "刘持平", "罗庆富", "舒冬梅", "宋雪光", "苏会敏", "王广风", "王诗卉", "许凤山", "杨腊香", "袁继鹏"};
        assertArrayEquals(sorted, xs);
    }

    @Test
    public void testSortString7() {
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅", "杨腊香", "许凤山", "王广风", "黄锡鸿", "罗庆富", "顾芳芳", "宋雪光", "王诗卉"};
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.chineseEncoderPinyin, false, false);
        sorter.sort(xs);
        System.out.println(Arrays.toString(xs));
        // NOTE: shu correctly comes before song in Hanyu (fixed 2026-07-23: HuskyCoderChinesePinyin
        // now packs a compact per-syllable ordinal instead of spelling pinyin out as ASCII text, and
        // the cleanup pass uses a proper pinyin-aware Collator instead of natural String order).
        final String[] sorted = {"曹玉德", "樊辉辉", "高民政", "顾芳芳", "洪文胜", "黄锡鸿", "刘持平", "罗庆富", "舒冬梅", "宋雪光", "苏会敏", "王广风", "王诗卉", "许凤山", "杨腊香", "袁继鹏"};
        for (final String name : xs) System.out.println(name + ": " + ChineseCharacter.convertToPinyin(name));
        assertArrayEquals(sorted, xs);
    }

    @Test
    public void testFloorLg() {
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(QuickHuskySort.class);
        assertEquals(1, privateMethodInvoker.invokePrivate("floor_lg", 3));
        assertEquals(2, privateMethodInvoker.invokePrivate("floor_lg", 5));
    }

    @Test
    public void testWithInsertionSort() {
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, false, true);
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(sorter);
        //noinspection unchecked
        final HuskyCoder<String> huskyCoder = (HuskyCoder<String>) privateMethodInvoker.invokePrivate("getHuskyCoder");
        final int N = 100;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final Coding coding = huskyCoder.huskyEncode(xs);
        QuickHuskySort.insertionSort(xs, coding.longs, 0, N);
        assertEquals(0, helper.inversions(xs));
    }

    @Test
    public void testInsertionSort() {
        final QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, false, false);
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(sorter);
        //noinspection unchecked
        final HuskyCoder<String> huskyCoder = (HuskyCoder<String>) privateMethodInvoker.invokePrivate("getHuskyCoder");
        final int N = 100;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final Coding coding = huskyCoder.huskyEncode(xs);
        QuickHuskySort.insertionSort(xs, coding.longs, 0, N);
        assertEquals(0, helper.inversions(xs));
    }

    /**
     * Regression test for the OPTIMIZED (binary-search-based) swapIntoSorted path, added
     * 2026-08-14 alongside the fix for its missing tie-handling scan (the same bug class fixed
     * in ComparisonSortHelper.swapIntoSorted by commit 408011c, which never reached this class's
     * own separate copy of the method). The existing insertionSort tests above use distinct
     * random Strings, so a stability regression here would never have shown up in any of them --
     * this uses many duplicate keys specifically to exercise the tie-scan.
     */
    @Test
    public void testInsertionSortStableForDuplicateKeys() {
        final Random random = new Random(3);
        final int n = 2000;
        final Tagged[] objects = new Tagged[n];
        final long[] longs = new long[n];
        for (int i = 0; i < n; i++) {
            final long key = random.nextInt(20);
            longs[i] = key;
            objects[i] = new Tagged(key, i);
        }
        QuickHuskySort.insertionSort(objects, longs, 0, n);
        for (int i = 1; i < n; i++) {
            assertTrue("key order violated at index " + i, longs[i - 1] <= longs[i]);
            assertEquals("objects/longs out of sync at index " + i, objects[i].key, longs[i]);
            if (longs[i - 1] == longs[i])
                assertTrue("stability violated at index " + i + ": originalIndex " +
                                objects[i - 1].originalIndex + " should precede " + objects[i].originalIndex,
                        objects[i - 1].originalIndex < objects[i].originalIndex);
        }
    }

    private static final class Tagged implements Comparable<Tagged> {
        final long key;
        final int originalIndex;

        Tagged(final long key, final int originalIndex) {
            this.key = key;
            this.originalIndex = originalIndex;
        }

        public int compareTo(final Tagged other) {
            return Long.compare(key, other.key);
        }
    }
}
