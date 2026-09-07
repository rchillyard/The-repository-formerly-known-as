package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.simple.PureDualPivotQuicksort;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks comparing RadixHuskySort against the existing sort strategies for
 * Integer/Double/Long/BigInteger/BigDecimal, mirroring HuskySortBenchmark.sortNumerics -- see
 * "doc/Radix Sort Benchmark Results.md" for the equivalent ad hoc (non-JMH) numbers this
 * replaces.
 * <p>
 * NOTE: the Double/BigDecimal generators here use the full signed range (not just [0,1) *
 * Long.MAX_VALUE as the old harness did), so negative values are actually exercised -- a gap
 * noted against the ad hoc results.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class NumericSortBenchmarks {

    // NOTE: "raw quicksort on primitives" baseline, shared by the two families of numeric type
    // (long-representable: Integer/Long/BigInteger; double-representable: Double/BigDecimal).
    // Returns the natural boxed primitive type rather than reconstructing the original
    // declared type (the old harness did this via reflection) -- what's being measured here is
    // the primitive-array sort + box round trip, not type fidelity.
    private static Long[] rawLongQuicksort(final Number[] xs) {
        final int n = xs.length;
        final long[] primitives = new long[n];
        for (int i = 0; i < n; i++) primitives[i] = xs[i].longValue();
        Arrays.sort(primitives);
        final Long[] result = new Long[n];
        for (int i = 0; i < n; i++) result[i] = primitives[i];
        return result;
    }

    private static Double[] rawDoubleQuicksort(final Number[] xs) {
        final int n = xs.length;
        final double[] primitives = new double[n];
        for (int i = 0; i < n; i++) primitives[i] = xs[i].doubleValue();
        Arrays.sort(primitives);
        final Double[] result = new Double[n];
        for (int i = 0; i < n; i++) result[i] = primitives[i];
        return result;
    }

    // NOTE: "raw radix sort on primitives" baseline -- the radix-sort analogue of
    // rawLongQuicksort/rawDoubleQuicksort above, and the strongest possible non-Husky baseline
    // for the radix comparison specifically. Unlike RadixHuskySort, there is no separate payload
    // array to carry in sync, no generic HuskyCoder abstraction, and no optional cleanup pass --
    // sorting the primitive key array directly *is* the whole job, so each digit pass simply
    // reorders that one array rather than needing a deferred int[] permutation applied
    // afterward. This isolates how much of RadixHuskySort's cost is its generality (working on
    // any Comparable type via an encoding function, plus the payload-permutation/cleanup-pass
    // machinery that generality requires) versus the radix algorithm itself.
    private static long[] radixSortLongsBiased(final long[] biased, final int digitBits) {
        final int n = biased.length;
        final int buckets = 1 << digitBits;
        final int mask = buckets - 1;
        long[] current = biased;
        long[] buffer = new long[n];
        final int[] count = new int[buckets + 1];
        for (int shift = 0; shift < Long.SIZE; shift += digitBits) {
            Arrays.fill(count, 0);
            for (int i = 0; i < n; i++) count[(int) ((current[i] >>> shift) & mask) + 1]++;
            for (int b = 0; b < buckets; b++) count[b + 1] += count[b];
            for (int i = 0; i < n; i++) {
                final int b = (int) ((current[i] >>> shift) & mask);
                buffer[count[b]++] = current[i];
            }
            final long[] temp = current;
            current = buffer;
            buffer = temp;
        }
        return current;
    }

    // Standard bijective bit-trick for encoding a double as a long with the same relative
    // order: flip the sign bit for non-negative values, flip every bit for negative ones. This
    // needs to be invertible, since there is no separate original-object payload to fall back
    // on here (unlike HuskyCoderFactory.doubleToLong, which only needs one-way ordering because
    // the original Double reference is what actually gets permuted); sortableSignedLongToDouble
    // below is its exact inverse. A sign-plus-magnitude-negation encoding (the first version of
    // this method) looks equivalent but is not actually bijective: it collapses +0.0 and -0.0
    // to the identical encoded value 0, silently losing the distinction Arrays.sort itself
    // preserves (Double.compare treats -0.0 as strictly less than 0.0) -- caught by comparing
    // against Arrays.sort on an explicit -0.0/+0.0 test case, not by reasoning about the bits
    // alone.
    private static long doubleToSortableSignedLong(final double value) {
        final long bits = Double.doubleToLongBits(value);
        final long mask = -(bits >>> 63) | Long.MIN_VALUE;
        return bits ^ mask;
    }

    private static double sortableSignedLongToDouble(final long sortable) {
        final long mask = ((sortable >>> 63) - 1) | Long.MIN_VALUE;
        return Double.longBitsToDouble(sortable ^ mask);
    }

    private static Long[] rawLongRadixSort(final Number[] xs) {
        final int n = xs.length;
        final long[] biased = new long[n];
        for (int i = 0; i < n; i++) biased[i] = xs[i].longValue() ^ Long.MIN_VALUE;
        final long[] sorted = radixSortLongsBiased(biased, 16);
        final Long[] result = new Long[n];
        for (int i = 0; i < n; i++) result[i] = sorted[i] ^ Long.MIN_VALUE;
        return result;
    }

    private static Double[] rawDoubleRadixSort(final Number[] xs) {
        final int n = xs.length;
        // NOTE: unlike rawLongRadixSort, no extra XOR Long.MIN_VALUE bias here --
        // doubleToSortableSignedLong's own output is already directly unsigned-comparable
        // (that is the whole point of the sign-bit/full-bit-flip trick), so radixSortLongsBiased
        // (which sorts its input in ascending unsigned order) needs no further transform. Adding
        // one anyway was a real bug caught during verification: it happened to still produce a
        // valid permutation, just not always the *correctly sorted* one.
        final long[] sortable = new long[n];
        for (int i = 0; i < n; i++) sortable[i] = doubleToSortableSignedLong(xs[i].doubleValue());
        final long[] sorted = radixSortLongsBiased(sortable, 16);
        final Double[] result = new Double[n];
        for (int i = 0; i < n; i++) result[i] = sortableSignedLongToDouble(sorted[i]);
        return result;
    }

    // ---------- Integer ----------

    @State(Scope.Thread)
    public static class IntegerState {
        @Param({"20000", "100000", "500000"})
        public int n;
        Integer[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            master = Utilities.fillRandomArray(Integer.class, new Random(42), n, Random::nextInt);
        }
    }

    @Benchmark
    public Integer[] integerSystemSort(final IntegerState state) {
        final Integer[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public Integer[] integerQuickHuskySort(final IntegerState state) {
        final Integer[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.integerCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public Integer[] integerDualPivotQuicksort(final IntegerState state) {
        final Integer[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] integerRawQuicksort(final IntegerState state) {
        return rawLongQuicksort(state.master);
    }

    @Benchmark
    public Long[] integerRawRadixSort(final IntegerState state) {
        return rawLongRadixSort(state.master);
    }

    @Benchmark
    public Integer[] integerRadixHuskySort8(final IntegerState state) {
        final Integer[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.integerCoder, state.config).sort(copy);
    }

    @Benchmark
    public Integer[] integerRadixHuskySort11(final IntegerState state) {
        final Integer[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.integerCoder, state.config).sort(copy);
    }

    @Benchmark
    public Integer[] integerRadixHuskySort16(final IntegerState state) {
        final Integer[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.integerCoder, state.config).sort(copy);
    }

    // ---------- Double ----------

    @State(Scope.Thread)
    public static class DoubleState {
        @Param({"20000", "100000", "500000"})
        public int n;
        Double[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            master = Utilities.fillRandomArray(Double.class, new Random(42), n, r -> (r.nextDouble() - 0.5) * 2 * Long.MAX_VALUE);
        }
    }

    @Benchmark
    public Double[] doubleSystemSort(final DoubleState state) {
        final Double[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public Double[] doubleQuickHuskySort(final DoubleState state) {
        final Double[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.doubleCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public Double[] doubleDualPivotQuicksort(final DoubleState state) {
        final Double[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    @Benchmark
    public Double[] doubleRawQuicksort(final DoubleState state) {
        return rawDoubleQuicksort(state.master);
    }

    @Benchmark
    public Double[] doubleRawRadixSort(final DoubleState state) {
        return rawDoubleRadixSort(state.master);
    }

    @Benchmark
    public Double[] doubleRadixHuskySort8(final DoubleState state) {
        final Double[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.doubleCoder, state.config).sort(copy);
    }

    @Benchmark
    public Double[] doubleRadixHuskySort11(final DoubleState state) {
        final Double[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.doubleCoder, state.config).sort(copy);
    }

    @Benchmark
    public Double[] doubleRadixHuskySort16(final DoubleState state) {
        final Double[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.doubleCoder, state.config).sort(copy);
    }

    // ---------- Long ----------

    @State(Scope.Thread)
    public static class LongState {
        @Param({"20000", "100000", "500000"})
        public int n;
        Long[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            master = Utilities.fillRandomArray(Long.class, new Random(42), n, Random::nextLong);
        }
    }

    @Benchmark
    public Long[] longSystemSort(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] longQuickHuskySort(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.longCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] longDualPivotQuicksort(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] longRawQuicksort(final LongState state) {
        return rawLongQuicksort(state.master);
    }

    @Benchmark
    public Long[] longRawRadixSort(final LongState state) {
        return rawLongRadixSort(state.master);
    }

    @Benchmark
    public Long[] longRadixHuskySort8(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.longCoder, state.config).sort(copy);
    }

    @Benchmark
    public Long[] longRadixHuskySort11(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.longCoder, state.config).sort(copy);
    }

    @Benchmark
    public Long[] longRadixHuskySort16(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.longCoder, state.config).sort(copy);
    }

    // ---------- BigInteger ----------

    @State(Scope.Thread)
    public static class BigIntegerState {
        @Param({"20000", "100000", "500000"})
        public int n;
        BigInteger[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            master = Utilities.fillRandomArray(BigInteger.class, new Random(42), n, r -> BigInteger.valueOf(r.nextLong()));
        }
    }

    @Benchmark
    public BigInteger[] bigIntegerSystemSort(final BigIntegerState state) {
        final BigInteger[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public BigInteger[] bigIntegerQuickHuskySort(final BigIntegerState state) {
        final BigInteger[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.bigIntegerCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public BigInteger[] bigIntegerDualPivotQuicksort(final BigIntegerState state) {
        final BigInteger[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] bigIntegerRawQuicksort(final BigIntegerState state) {
        return rawLongQuicksort(state.master);
    }

    @Benchmark
    public Long[] bigIntegerRawRadixSort(final BigIntegerState state) {
        return rawLongRadixSort(state.master);
    }

    @Benchmark
    public BigInteger[] bigIntegerRadixHuskySort8(final BigIntegerState state) {
        final BigInteger[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.bigIntegerCoder, state.config).sort(copy);
    }

    @Benchmark
    public BigInteger[] bigIntegerRadixHuskySort11(final BigIntegerState state) {
        final BigInteger[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.bigIntegerCoder, state.config).sort(copy);
    }

    @Benchmark
    public BigInteger[] bigIntegerRadixHuskySort16(final BigIntegerState state) {
        final BigInteger[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.bigIntegerCoder, state.config).sort(copy);
    }

    // ---------- BigDecimal ----------

    @State(Scope.Thread)
    public static class BigDecimalState {
        @Param({"20000", "100000", "500000"})
        public int n;
        BigDecimal[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            master = Utilities.fillRandomArray(BigDecimal.class, new Random(42), n, r -> BigDecimal.valueOf((r.nextDouble() - 0.5) * 2 * Long.MAX_VALUE));
        }
    }

    @Benchmark
    public BigDecimal[] bigDecimalSystemSort(final BigDecimalState state) {
        final BigDecimal[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public BigDecimal[] bigDecimalQuickHuskySort(final BigDecimalState state) {
        final BigDecimal[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.bigDecimalCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public BigDecimal[] bigDecimalDualPivotQuicksort(final BigDecimalState state) {
        final BigDecimal[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    @Benchmark
    public Double[] bigDecimalRawQuicksort(final BigDecimalState state) {
        return rawDoubleQuicksort(state.master);
    }

    @Benchmark
    public Double[] bigDecimalRawRadixSort(final BigDecimalState state) {
        return rawDoubleRadixSort(state.master);
    }

    @Benchmark
    public BigDecimal[] bigDecimalRadixHuskySort8(final BigDecimalState state) {
        final BigDecimal[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.bigDecimalCoder, state.config).sort(copy);
    }

    @Benchmark
    public BigDecimal[] bigDecimalRadixHuskySort11(final BigDecimalState state) {
        final BigDecimal[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.bigDecimalCoder, state.config).sort(copy);
    }

    @Benchmark
    public BigDecimal[] bigDecimalRadixHuskySort16(final BigDecimalState state) {
        final BigDecimal[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.bigDecimalCoder, state.config).sort(copy);
    }
}
