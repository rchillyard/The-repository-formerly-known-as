package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.sort.simple.MergeSortBasic;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.PrivateMethodTester;
import edu.neu.coe.huskySort.util.StatPack;
import edu.neu.coe.huskySort.util.Statistics;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class InstrumentedHelperTest {

    @Test
    public void instrumented() {
        assertTrue(new InstrumentedHelper<String>("test", config).instrumented());
    }

    @Test
    public void less() {
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        assertTrue(helper.less("a", "b"));
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        assertEquals(1, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(0, privateMethodTester.invokePrivate("getSwaps"));
    }

    @Test
    public void compare() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        assertEquals(-1, helper.compare(xs, 0, 1));
        assertEquals(0, helper.compare(xs, 0, 0));
        assertEquals(1, helper.compare(xs, 1, 0));
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        assertEquals(3, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(0, privateMethodTester.invokePrivate("getSwaps"));
    }

    @Test
    public void swap() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        helper.swap(xs, 0, 1);
        assertArrayEquals(new String[]{"b", "a"}, xs);
        helper.swap(xs, 0, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        assertEquals(0, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(2, privateMethodTester.invokePrivate("getSwaps"));
    }

    @Test
    public void sorted() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        assertTrue(helper.sorted(xs));
        helper.swap(xs, 0, 1);
        assertFalse(helper.sorted(xs));
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        assertEquals(0, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(1, privateMethodTester.invokePrivate("getSwaps"));
    }

    @Test
    public void inversions() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        assertEquals(0, helper.inversions(xs));
        helper.swap(xs, 0, 1);
        assertEquals(1, helper.inversions(xs));
    }

    @Test
    public void postProcess1() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        helper.init(3);
        helper.postProcess(xs);
    }

    @Test(expected = BaseHelper.HelperException.class)
    public void postProcess2() {
        String[] xs = new String[]{"b", "a"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        helper.postProcess(xs);
    }

    @Test
    public void random() {
        String[] words = new String[]{"Hello", "World"};
        final Helper<String> helper = new InstrumentedHelper<>("test", 3, 0L, config);
        final String[] strings = helper.random(String.class, r -> words[r.nextInt(2)]);
        assertArrayEquals(new String[]{"World", "World", "Hello"}, strings);
    }

    @Test
    public void testToString() {
        final Helper<String> helper = new InstrumentedHelper<>("test", 3, config);
        assertEquals("Helper for test with 3 elements", helper.toString());
    }

    @Test
    public void getDescription() {
        final Helper<String> helper = new InstrumentedHelper<>("test", 3, config);
        assertEquals("test", helper.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void getSetN() {
        final Helper<String> helper = new InstrumentedHelper<>("test", 3, config);
        assertEquals(3, helper.getN());
        helper.init(4);
        assertEquals(4, helper.getN());
    }

    @Test
    public void getSetNBis() {
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        assertEquals(0, helper.getN());
        helper.init(4);
        assertEquals(4, helper.getN());
    }

    @Test
    public void close() {
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        helper.close();
    }

    @Test
    public void swapStable() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        helper.swapStable(xs, 1);
        assertArrayEquals(new String[]{"b", "a"}, xs);
        helper.swapStable(xs, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        assertEquals(0, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(2, privateMethodTester.invokePrivate("getSwaps"));
    }

    @Test
    public void fixInversion1() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        helper.fixInversion(xs, 1);
        assertEquals(1, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(0, privateMethodTester.invokePrivate("getSwaps"));
        assertArrayEquals(new String[]{"a", "b"}, xs);
        helper.swapStable(xs, 1);
        assertArrayEquals(new String[]{"b", "a"}, xs);
        helper.fixInversion(xs, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        assertEquals(2, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(2, privateMethodTester.invokePrivate("getSwaps"));
    }

    @Test
    public void testFixInversion2() {
        String[] xs = new String[]{"a", "b"};
        final Helper<String> helper = new InstrumentedHelper<>("test", config);
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        helper.fixInversion(xs, 0, 1);
        assertEquals(1, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(0, privateMethodTester.invokePrivate("getSwaps"));
        assertArrayEquals(new String[]{"a", "b"}, xs);
        helper.swap(xs, 0, 1);
        assertArrayEquals(new String[]{"b", "a"}, xs);
        helper.fixInversion(xs, 0, 1);
        assertArrayEquals(new String[]{"a", "b"}, xs);
        assertEquals(2, privateMethodTester.invokePrivate("getCompares"));
        assertEquals(2, privateMethodTester.invokePrivate("getSwaps"));
    }

    @Test
    public void testMergeSort() {
        int N = 8;
        final Helper<Integer> helper = new InstrumentedHelper<>("test", config);
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        Sort<Integer> s = new MergeSortBasic<>(helper);
        s.init(N);
        final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000));
        s.sort(xs);
        final int compares = (Integer) privateMethodTester.invokePrivate("getCompares");
//        System.out.println("compares: "+compares);
        assertTrue(compares <= 20 && compares >= 11);
    }

    @Ignore // TODO fix this test
    public void testMergeSortMany() {
        int N = 8;
        int m = 10;
        final Helper<Integer> helper = new InstrumentedHelper<>("test", config);
        final PrivateMethodTester privateMethodTester = new PrivateMethodTester(helper);
        Sort<Integer> s = new MergeSortBasic<>(helper);
        s.init(N);
        for (int i = 0; i < m; i++) {
            final Integer[] xs = helper.random(Integer.class, r -> r.nextInt(1000));
            Integer[] ys = s.sort(xs);
            helper.postProcess(ys);
        }
        final StatPack statPack = (StatPack) privateMethodTester.invokePrivate("getStatPack");
        final Statistics statistics = statPack.getStatistics("compares");
        System.out.println(statistics);
        final int compares = statPack.getCount("compares");
        System.out.println(statPack);
        assertTrue(12 <= compares && compares <= 17);
//        assert
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(InstrumentedHelperTest.class);
    }

    private static Config config;

}