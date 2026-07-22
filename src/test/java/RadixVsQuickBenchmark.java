import java.util.*;

/**
 * Prototype comparing two strategies for the "sort the husky-coded longs" phase:
 *
 *  A) quickSortWithPayload  - introsort on longs, swapping the payload (object refs)
 *                             in lockstep at every swap. This is what Huskysort currently does.
 *
 *  B) radixSortDeferred     - LSD radix sort (8 passes of 8 bits) on the longs, carrying only
 *                             a cheap int[] index array through the passes (not the heavy payload),
 *                             then applying the final permutation to the payload array ONCE at the end.
 *                             This is Review 3's suggestion.
 */
public class RadixVsQuickBenchmark {

    static long[] genRandomLongs(int n, long seed) {
        Random r = new Random(seed);
        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = r.nextLong();
        return a;
    }

    // ---------- Approach A: Introsort on longs, swapping payload in lockstep ----------

    static void quickSortWithPayload(long[] keys, Object[] payload, int lo, int hi, int depthLimit) {
        while (hi - lo > 16) {
            if (depthLimit == 0) {
                heapSortWithPayload(keys, payload, lo, hi);
                return;
            }
            depthLimit--;
            int p = partition(keys, payload, lo, hi); // keys[lo..p] <= pivot <= keys[p+1..hi-1]
            if (p + 1 - lo < hi - (p + 1)) {
                quickSortWithPayload(keys, payload, lo, p + 1, depthLimit);
                lo = p + 1;
            } else {
                quickSortWithPayload(keys, payload, p + 1, hi, depthLimit);
                hi = p + 1;
            }
        }
        insertionSortWithPayload(keys, payload, lo, hi);
    }

    static int partition(long[] keys, Object[] payload, int lo, int hi) {
        int mid = lo + (hi - lo) / 2;
        long pivot = keys[mid];
        int i = lo, j = hi - 1;
        while (i <= j) {
            while (keys[i] < pivot) i++;
            while (keys[j] > pivot) j--;
            if (i <= j) {
                swap(keys, payload, i, j);
                i++; j--;
            }
        }
        return j;
    }

    static void insertionSortWithPayload(long[] keys, Object[] payload, int lo, int hi) {
        for (int i = lo + 1; i < hi; i++) {
            long k = keys[i];
            Object v = payload[i];
            int j = i - 1;
            while (j >= lo && keys[j] > k) {
                keys[j + 1] = keys[j];
                payload[j + 1] = payload[j];
                j--;
            }
            keys[j + 1] = k;
            payload[j + 1] = v;
        }
    }

    static void heapSortWithPayload(long[] keys, Object[] payload, int lo, int hi) {
        int n = hi - lo;
        for (int i = n / 2 - 1; i >= 0; i--) siftDown(keys, payload, i, n, lo);
        for (int i = n - 1; i > 0; i--) {
            swap(keys, payload, lo, lo + i);
            siftDown(keys, payload, 0, i, lo);
        }
    }

    static void siftDown(long[] keys, Object[] payload, int i, int n, int lo) {
        while (true) {
            int largest = i, l = 2 * i + 1, r = 2 * i + 2;
            if (l < n && keys[lo + l] > keys[lo + largest]) largest = l;
            if (r < n && keys[lo + r] > keys[lo + largest]) largest = r;
            if (largest == i) break;
            swap(keys, payload, lo + i, lo + largest);
            i = largest;
        }
    }

    static void swap(long[] keys, Object[] payload, int i, int j) {
        long tk = keys[i]; keys[i] = keys[j]; keys[j] = tk;
        Object tv = payload[i]; payload[i] = payload[j]; payload[j] = tv;
    }

    // ---------- Approach B: LSD radix sort on longs, deferred payload permutation ----------

    static Object[] radixSortDeferred(long[] keys, Object[] payload) {
        int n = keys.length;
        long[] biased = new long[n];
        for (int i = 0; i < n; i++) biased[i] = keys[i] ^ Long.MIN_VALUE; // make unsigned order == signed order

        int[] index = new int[n];
        for (int i = 0; i < n; i++) index[i] = i;

        long[] biasedBuf = new long[n];
        int[] indexBuf = new int[n];
        int[] count = new int[257];

        for (int shift = 0; shift < 64; shift += 8) {
            Arrays.fill(count, 0);
            for (int i = 0; i < n; i++) {
                int b = (int) ((biased[i] >>> shift) & 0xFF);
                count[b + 1]++;
            }
            for (int i = 0; i < 256; i++) count[i + 1] += count[i];
            for (int i = 0; i < n; i++) {
                int b = (int) ((biased[i] >>> shift) & 0xFF);
                int pos = count[b]++;
                biasedBuf[pos] = biased[i];
                indexBuf[pos] = index[i];
            }
            long[] tmpL = biased; biased = biasedBuf; biasedBuf = tmpL;
            int[] tmpI = index; index = indexBuf; indexBuf = tmpI;
        }

        Object[] result = new Object[n];
        for (int i = 0; i < n; i++) result[i] = payload[index[i]];
        return result;
    }

    // ---------- Approach C: LSD radix sort with 16-bit digits (4 passes), deferred permutation ----------

    static Object[] radixSortDeferred16(long[] keys, Object[] payload) {
        int n = keys.length;
        long[] biased = new long[n];
        for (int i = 0; i < n; i++) biased[i] = keys[i] ^ Long.MIN_VALUE;

        int[] index = new int[n];
        for (int i = 0; i < n; i++) index[i] = i;

        long[] biasedBuf = new long[n];
        int[] indexBuf = new int[n];
        int[] count = new int[65537];

        for (int shift = 0; shift < 64; shift += 16) {
            Arrays.fill(count, 0);
            for (int i = 0; i < n; i++) {
                int b = (int) ((biased[i] >>> shift) & 0xFFFF);
                count[b + 1]++;
            }
            for (int i = 0; i < 65536; i++) count[i + 1] += count[i];
            for (int i = 0; i < n; i++) {
                int b = (int) ((biased[i] >>> shift) & 0xFFFF);
                int pos = count[b]++;
                biasedBuf[pos] = biased[i];
                indexBuf[pos] = index[i];
            }
            long[] tmpL = biased; biased = biasedBuf; biasedBuf = tmpL;
            int[] tmpI = index; index = indexBuf; indexBuf = tmpI;
        }

        Object[] result = new Object[n];
        for (int i = 0; i < n; i++) result[i] = payload[index[i]];
        return result;
    }

    // ---------- correctness check ----------

    static boolean isSorted(long[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }

    static long time(Runnable r) {
        long t0 = System.nanoTime();
        r.run();
        return (System.nanoTime() - t0) / 1_000_000; // ms
    }

    public static void main(String[] args) {
        // --- stress test on small arrays to find any bug ---
        {
            Random r = new Random(7);
            boolean allPassed = true;
            for (int trial = 0; trial < 5000; trial++) {
                int n = 1 + r.nextInt(200);
                long[] keys = new long[n];
                for (int i = 0; i < n; i++) keys[i] = r.nextLong();
                Object[] payload = new Object[n];
                for (int i = 0; i < n; i++) payload[i] = keys[i];
                long[] k = keys.clone();
                Object[] p = payload.clone();
                int depthLimit = 2 * (63 - Integer.numberOfLeadingZeros(Math.max(1, n)));
                quickSortWithPayload(k, p, 0, n, depthLimit);
                if (!isSorted(k)) {
                    allPassed = false;
                    System.out.println("QUICKSORT STRESS FAIL n=" + n + " depthLimit=" + depthLimit);
                    System.out.println(Arrays.toString(keys));
                    System.out.println(Arrays.toString(k));
                    break;
                }
            }
            System.out.println("Quicksort stress test: " + (allPassed ? "all passed" : "FAILED (see above)"));
            System.out.println();
        }

        // --- correctness check first ---
        {
            int n = 50_000;
            long[] keys = genRandomLongs(n, 1);
            Object[] payload = new Object[n];
            for (int i = 0; i < n; i++) payload[i] = keys[i]; // payload == key so we can verify permutation correctness

            long[] k1 = keys.clone();
            Object[] p1 = payload.clone();
            quickSortWithPayload(k1, p1, 0, n, 2 * (63 - Integer.numberOfLeadingZeros(n)));
            boolean quickOk = isSorted(k1);
            boolean quickPayloadMatches = true;
            for (int i = 0; i < n; i++) if (!p1[i].equals(k1[i])) quickPayloadMatches = false;

            Object[] p2 = radixSortDeferred(keys, payload);
            long[] k2 = new long[n];
            for (int i = 0; i < n; i++) k2[i] = (Long) p2[i];
            boolean radixOk = isSorted(k2);

            System.out.println("Correctness: quicksort sorted=" + quickOk + ", payload matches keys=" + quickPayloadMatches
                    + ", radix(deferred) sorted=" + radixOk);
            System.out.println();
        }

        int[] sizes = {10_000, 100_000, 1_000_000, 5_000_000};
        int warmup = 3, trials = 5;

        System.out.println("Benchmark: sort longs + carry payload (String) along, JVM: " + System.getProperty("java.version")
                + ", maxHeap=" + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
        System.out.printf("%-12s %-14s %-18s %-18s %-10s %-10s%n",
                "N", "quicksort(ms)", "radix8(ms)", "radix16(ms)", "spd(r8)", "spd(r16)");

        for (int n : sizes) {
            long[] baseKeys = genRandomLongs(n, 42);
            Object[] basePayload = new Object[n];
            for (int i = 0; i < n; i++) basePayload[i] = "item-" + i;

            List<Long> quickTimes = new ArrayList<>();
            List<Long> radix8Times = new ArrayList<>();
            List<Long> radix16Times = new ArrayList<>();

            for (int t = 0; t < warmup + trials; t++) {
                System.gc();
                long[] k1 = baseKeys.clone();
                Object[] p1 = basePayload.clone();
                int depthLimit = 2 * (63 - Integer.numberOfLeadingZeros(Math.max(1, k1.length)));
                long tq = time(() -> quickSortWithPayload(k1, p1, 0, k1.length, depthLimit));
                if (t >= warmup) quickTimes.add(tq);

                System.gc();
                long[] k2 = baseKeys.clone();
                Object[] p2 = basePayload.clone();
                long tr8 = time(() -> radixSortDeferred(k2, p2));
                if (t >= warmup) radix8Times.add(tr8);

                System.gc();
                long[] k3 = baseKeys.clone();
                Object[] p3 = basePayload.clone();
                long tr16 = time(() -> radixSortDeferred16(k3, p3));
                if (t >= warmup) radix16Times.add(tr16);
            }

            double avgQ = quickTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            double avgR8 = radix8Times.stream().mapToLong(Long::longValue).average().orElse(0);
            double avgR16 = radix16Times.stream().mapToLong(Long::longValue).average().orElse(0);
            System.out.printf("%-12s %-14.2f %-18.2f %-18.2f %-10.2fx %-10.2fx%n",
                    String.format("%,d", n), avgQ, avgR8, avgR16, avgQ / avgR8, avgQ / avgR16);
        }
    }
}