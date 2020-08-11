package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.bqs.Bag;
import edu.neu.coe.huskySort.util.PrivateMethodInvoker;
import org.junit.Test;

import java.util.Random;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.asciiToLong;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuskyBucketHelperTest {

    private final HuskyCoder<Integer> integerHuskyCoder = new HuskyCoder<Integer>() {
        public long huskyEncode(Integer o) {
            return o;
        }

        public boolean imperfect(int length) {
            return false;
        }
    };
    private final HuskyCoder<Short> shortHuskyCoder = new HuskyCoder<Short>() {
        public long huskyEncode(Short o) {
            return o;
        }

        public boolean imperfect(int length) {
            return false;
        }
    };
    private final HuskyCoder<Long> longHuskyCoder = new HuskyCoder<Long>() {
        public long huskyEncode(Long o) {
            return o;
        }

        public boolean imperfect(int length) {
            return false;
        }
    };

    @Test
    public void testLoadBuckets0() {
        int buckets = 16;
        int n = 256;
        // we are just going to use 8 bits of the shorts--unsigned bytes, really.
        HuskyBucketHelper<Short> helper = new HuskyBucketHelper<>("", buckets, n, shortHuskyCoder, null, 0L, false);
        Short[] xs = helper.random(Short.class, r -> (short) r.nextInt(256));
        assertEquals(n, helper.loadBuckets(xs));
        int spread = helper.checkBuckets();
        System.out.println(spread);
        assertTrue(spread < n / buckets);
        for (int k = 1; k < buckets; k++) {
            short max = getMax(helper.buckets[k - 1], Short.MIN_VALUE);
            short min = getMin(helper.buckets[k], Short.MAX_VALUE);
            assertTrue(max < min);
        }
    }

    @Test
    public void testLoadBuckets1() {
        int buckets = 16;
        int n = 256;
        HuskyBucketHelper<Integer> helper = new HuskyBucketHelper<>("", buckets, n, integerHuskyCoder, null, 0L, false);
        Integer[] xs = helper.random(Integer.class, Random::nextInt);
        assertEquals(n, helper.loadBuckets(xs));
        int spread = helper.checkBuckets();
        System.out.println(spread);
        assertTrue(spread < n / buckets);
        for (int k = 1; k < buckets; k++) {
            int max = getMax(helper.buckets[k - 1], Integer.MIN_VALUE);
            int min = getMin(helper.buckets[k], Integer.MAX_VALUE);
            assertTrue(max < min);
        }
    }

    @Test
    public void testLoadBuckets2() {
        int buckets = 16;
        int n = 1024;
        HuskyBucketHelper<Long> helper = new HuskyBucketHelper<>("", buckets, n, longHuskyCoder, null, 0L, false);
        Long[] xs = helper.random(Long.class, Random::nextLong);
        assertEquals(n, helper.loadBuckets(xs));
        int spread = helper.checkBuckets();
        System.out.println(spread);
        assertTrue(spread < n / buckets);
        for (int k = 1; k < buckets; k++) {
            long max = getMax(helper.buckets[k - 1], Long.MIN_VALUE);
            long min = getMin(helper.buckets[k], Long.MAX_VALUE);
            assertTrue(max < min);
        }
    }

    @Test
    public void testLoadAndUnloadBuckets0() {
        int buckets = 16;
        int n = 256;
        HuskyBucketHelper<Short> helper = new HuskyBucketHelper<>("", buckets, n, shortHuskyCoder, null, 0L, false);
        Short[] xs = helper.random(Short.class, r -> (short) r.nextInt(256));
        assertEquals(n, helper.loadBuckets(xs));
        helper.unloadBuckets(xs);
        int stride = n / buckets;
        int l = stride / 2;
        checkBucketOrder(buckets, xs, stride, l);
    }

    @Test
    public void testLoadAndUnloadBuckets1() {
        int buckets = 16;
        int n = 256;
        HuskyBucketHelper<Integer> helper = new HuskyBucketHelper<>("", buckets, n, integerHuskyCoder, null, 0L, false);
        Integer[] xs = helper.random(Integer.class, Random::nextInt);
        assertEquals(n, helper.loadBuckets(xs));
        helper.unloadBuckets(xs);
        int stride = n / buckets;
        int l = stride / 2;
        checkBucketOrder(buckets, xs, stride, l);
    }

    @Test
    public void testLoadAndUnloadBuckets2() {
        int buckets = 16;
        int n = 1024;
        HuskyBucketHelper<Long> helper = new HuskyBucketHelper<>("", buckets, n, longHuskyCoder, null, 0L, false);
        Long[] xs = helper.random(Long.class, Random::nextLong);
        assertEquals(n, helper.loadBuckets(xs));
        helper.unloadBuckets(xs);
        int stride = n / buckets;
        int l = stride / 2;
        checkBucketOrder(buckets, xs, stride, l);
    }

    @Test
    public void testStringToLong() {
        final PrivateMethodInvoker invoker = new PrivateMethodInvoker(HuskyCoderFactory.class);
        assertEquals(0x48cbb36000000000L, ((Long) invoker.invokePrivate("stringToLong", "Hell", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f0000000L, ((Long) invoker.invokePrivate("stringToLong", "Hello", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long) invoker.invokePrivate("stringToLong", "Hello, Go", 9, 7, 0x7F)).longValue());
        assertEquals(0x48cbb366f58823efL, ((Long) invoker.invokePrivate("stringToLong", "Hello, Goodbye", 9, 7, 0x7F)).longValue());
    }

    @Test
    public void testAsciiToLong() {
        String word = "a";
        assertEquals(6989586621679009792L, asciiToLong(word));
    }

    private static <X extends Comparable<X>> X getMax(Bag<X> bucket, X min) {
        X max = min;
        for (X x : bucket) if (x.compareTo(max) > 0) max = x;
        return max;
    }

    private static <X extends Comparable<X>> X getMin(Bag<X> bucket, X max) {
        X min = max;
        for (X x : bucket) if (x.compareTo(min) > 0) min = x;
        return min;
    }

    private static <X extends Comparable<X>> void checkBucketOrder(int buckets, X[] xs, int stride, int l) {
        // NOTE: we cannot expect that the buckets are uniform in size, so we have to ensure that we
        // compare two elements that are sufficiently far apart.
        for (int k = 1; k < buckets - 1; k++)
            assertTrue("k=" + k, xs[(k - 1) * stride + l].compareTo(xs[(k + 1) * stride + l]) <= 0);
    }

}
