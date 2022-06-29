package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.sort.simple.MergeSortBasic;
import edu.neu.coe.huskySort.util.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class InstrumentedComparisonSortHelperTest {

    @Test
    public void testInstrumented() {
        assertTrue(new InstrumentedComparisonSortHelper<String>("test", config).instrumented());
    }

    @Test
    public void testLess() {
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(2);
        assertTrue(helper.less("a", "b"));
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(1, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testCompare() {
        final String[] xs = new String[]{"a", "b"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        assertEquals(-1, helper.compare(xs, 0, 1));
        assertEquals(0, helper.compare(xs, 0, 0));
        assertEquals(1, helper.compare(xs, 1, 0));
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(3, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSwap1() {
        final String[] xs = new String[]{"b", "a"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(1, helper.inversions(xs));
        assertEquals(0, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        assertEquals(0, helper.inversions(xs));
        assertEquals(1, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, 1);
        assertEquals(1, helper.inversions(xs));
        assertArrayEquals(new String[]{"b", "a"}, xs);
        // NOTE that we do not check fixes here because we did a non-fixing swap which will have generated an incorrect total.
        assertEquals(0, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(2, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSwap2() {
        final String[] xs = new String[]{"c", "b", "a"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(3, helper.inversions(xs));
        assertEquals(0, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, 2);
        assertArrayEquals(new String[]{"a", "b", "c"}, xs);
        assertEquals(0, helper.inversions(xs));
        assertEquals(3, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, 1);
        assertArrayEquals(new String[]{"b", "a", "c"}, xs);
        // NOTE that we do not check fixes here because we did a non-fixing swap which will have generated an incorrect total.
        assertEquals(0, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(2, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSwap3() {
        final String[] xs = new String[]{"c", "b", "d", "a"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(4, helper.inversions(xs));
        assertEquals(0, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, 3);
        assertArrayEquals(new String[]{"a", "b", "d", "c"}, xs);
        assertEquals(1, helper.inversions(xs));
        assertEquals(3, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 2, 3);
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, xs);
        assertEquals(0, helper.inversions(xs));
        assertEquals(4, privateMethodInvoker.invokePrivate("getFixes"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(2, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSwap4() {
        final String[] xs = new String[]{"c", "e", "b", "d", "a"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(7, helper.inversions(xs));
        assertEquals(0, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, 4);
        assertArrayEquals(new String[]{"a", "e", "b", "d", "c"}, xs);
        assertEquals(4, helper.inversions(xs));
        assertEquals(3, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 1, 4);
        assertArrayEquals(new String[]{"a", "c", "b", "d", "e"}, xs);
        assertEquals(1, helper.inversions(xs));
        assertEquals(6, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 1, 2);
        assertArrayEquals(new String[]{"a", "b", "c", "d", "e"}, xs);
        assertEquals(0, helper.inversions(xs));
        assertEquals(7, privateMethodInvoker.invokePrivate("getFixes"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(3, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSwap5() {
        final String[] xs = new String[]{"f", "e", "d", "c", "b", "a"};
        final int n = xs.length;
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(n);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        final int inversions = n * (n - 1) / 2;
        assertEquals(inversions, helper.inversions(xs));
        assertEquals(0, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, n - 1);
        assertArrayEquals(new String[]{"a", "e", "d", "c", "b", "f"}, xs);
        final int fixes = 2 * n - 3;
        assertEquals(fixes, privateMethodInvoker.invokePrivate("getFixes"));
        assertEquals(inversions - fixes, helper.inversions(xs));
        assertEquals(1, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSwap6() {
        final String[] xs = new String[]{"g", "f", "e", "d", "c", "b", "a"};
        final int n = xs.length;
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(n);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        final int inversions = n * (n - 1) / 2;
        assertEquals(inversions, helper.inversions(xs));
        assertEquals(0, privateMethodInvoker.invokePrivate("getFixes"));
        helper.swap(xs, 0, n - 1);
        assertArrayEquals(new String[]{"a", "f", "e", "d", "c", "b", "g"}, xs);
        final int fixes = 2 * n - 3;
        assertEquals(fixes, privateMethodInvoker.invokePrivate("getFixes"));
        assertEquals(inversions - fixes, helper.inversions(xs));
        assertEquals(1, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSorted() {
        final String[] xs = new String[]{"a", "b"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        assertTrue(helper.sorted(xs));
        helper.swap(xs, 0, 1);
        assertFalse(helper.sorted(xs));
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(0, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(1, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testInversions() {
        final String[] xs = new String[]{"a", "b"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        assertEquals(0, helper.inversions(xs));
        helper.swap(xs, 0, 1);
        assertEquals(1, helper.inversions(xs));
    }

    @Test
    public void testPostProcess1() {
        final String[] xs = new String[]{"a", "b"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(3);
        helper.postProcess(xs);
    }

    @Test(expected = ComparableSortHelper.HelperException.class)
    public void testPostProcess2() {
        final String[] xs = new String[]{"b", "a"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.postProcess(xs);
    }

    @Test
    public void testRandom() {
        final String[] words = new String[]{"Hello", "World"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", 3, 0L, config);
        final String[] strings = helper.random(String.class, r -> words[r.nextInt(2)]);
        assertArrayEquals(new String[]{"World", "World", "Hello"}, strings);
    }

    @Test
    public void testToString() {
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", 3, config);
        assertEquals("Instrumenting helper for test with 3 elements", helper.toString());
    }

    @Test
    public void testGetDescription() {
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", 3, config);
        assertEquals("test", helper.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testGetSetN() {
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", 3, config);
        assertEquals(3, helper.getN());
        helper.init(4);
        assertEquals(4, helper.getN());
    }

    @Test
    public void testGetSetNBis() {
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        assertEquals(0, helper.getN());
        helper.init(4);
        assertEquals(4, helper.getN());
    }

    @Test
    public void testClose() {
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.close();
    }

    @Test
    public void testSwapStable() {
        final String[] xs = new String[]{"a", "b"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        helper.swapStable(xs, 1);
        assertArrayEquals(new String[]{"b", "a"}, xs);
        helper.swapStable(xs, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(0, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(2, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testFixInversion1() {
        final String[] xs = new String[]{"a", "b"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        helper.fixInversion(xs, 1);
        assertEquals(1, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getSwaps"));
        assertArrayEquals(new String[]{"a", "b"}, xs);
        helper.swapStable(xs, 1);
        assertArrayEquals(new String[]{"b", "a"}, xs);
        helper.fixInversion(xs, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        assertEquals(2, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(2, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testFixInversion2() {
        final String[] xs = new String[]{"a", "b"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        helper.init(xs.length);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        helper.fixInversion(xs, 0, 1);
        assertEquals(1, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getSwaps"));
        assertArrayEquals(new String[]{"a", "b"}, xs);
        helper.swap(xs, 0, 1);
        assertArrayEquals(new String[]{"b", "a"}, xs);
        helper.fixInversion(xs, 0, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        assertEquals(2, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(2, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testMergeSort() {
        final int N = 8;
        final ComparisonSortHelper<Integer> helper = new InstrumentedComparisonSortHelper<>("test", config);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        final Sort<Integer> s = new MergeSortBasic<>(helper);
        s.init(N);
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000));
        s.sort(xs);
        final int compares = (Integer) privateMethodInvoker.invokePrivate("getCompares");
        assertTrue(compares <= 20 && compares >= 11);
    }

    @Ignore // TODO fix this test
    public void testMergeSortMany() {
        final int N = 8;
        final int m = 10;
        final ComparisonSortHelper<Integer> helper = new InstrumentedComparisonSortHelper<>("test", config);
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(helper);
        final Sort<Integer> s = new MergeSortBasic<>(helper);
        s.init(N);
        for (int i = 0; i < m; i++) {
            final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000));
            final Integer[] ys = s.sort(xs);
            helper.postProcess(ys);
        }
        final StatPack statPack = (StatPack) privateMethodInvoker.invokePrivate("getStatPack");
        final Statistics statistics = statPack.getStatistics(Instrumenter.COMPARES);
        System.out.println(statistics);
        final int compares = statPack.getCount(Instrumenter.COMPARES);
        System.out.println(statPack);
        assertTrue(12 <= compares && compares <= 17);
    }

    @BeforeClass
    public static void beforeClass() {
        config = ConfigTest.setupConfig("true", "0", "10", "1", "");
    }

    private static Config config;

    @Test
    public void swapInto() {
    }

    @Test
    public void testSwapConditional1() {
        final String[] xs = new String[]{"c", "b", "a"};
        final ComparisonSortHelper<String> helper = new InstrumentedComparisonSortHelper<>("test", config);
        assertFalse(helper.sorted(xs));
        helper.init(xs.length);
        helper.swapConditional(xs, 0, 2);
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(1, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(1, privateMethodInvoker.invokePrivate("getSwaps"));
        assertTrue(helper.sorted(xs));
    }

    @Test
    public void swapStableConditional() {
    }

    @Test
    public void copy() {
    }

    @Test
    public void incrementCopies() {
    }

    @Test
    public void incrementFixes() {
    }

    @Test
    public void testCompare1() {
    }

    @Test
    public void cutoff() {
    }

    @Test
    public void testToString1() {
    }

    @Test
    public void init() {
    }

    @Test
    public void preProcess() {
    }

    @Test
    public void postProcess() {
    }
}