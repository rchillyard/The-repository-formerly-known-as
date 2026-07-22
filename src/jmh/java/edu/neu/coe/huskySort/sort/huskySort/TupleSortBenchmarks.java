package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.simple.PureDualPivotQuicksort;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks comparing RadixHuskySort against the existing sort strategies for
 * HuskySortBenchmark.Tuple (a composite birthYear/zip/name key, imperfect encoding -- needs the
 * cleanup pass), mirroring HuskySortBenchmark.sortTuples -- see
 * "doc/Radix Sort Benchmark Results.md" for the equivalent ad hoc (non-JMH) numbers this
 * replaces.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class TupleSortBenchmarks {

    @State(Scope.Thread)
    public static class TupleState {
        @Param({"20000", "100000", "500000"})
        public int n;

        HuskySortBenchmark.Tuple[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            final HuskySortBenchmark.Tuple[] tuples = new HuskySortBenchmark.Tuple[n];
            for (int i = 0; i < n; i++) tuples[i] = HuskySortBenchmark.Tuple.create();
            master = Utilities.fillRandomArray(HuskySortBenchmark.Tuple.class, new Random(42), n, r -> tuples[r.nextInt(n)]);
        }
    }

    @Benchmark
    public HuskySortBenchmark.Tuple[] systemSort(final TupleState state) {
        final HuskySortBenchmark.Tuple[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public HuskySortBenchmark.Tuple[] pureHuskySort(final TupleState state) {
        final HuskySortBenchmark.Tuple[] copy = Arrays.copyOf(state.master, state.master.length);
        new PureHuskySort<>(HuskyCoderFactory.<HuskySortBenchmark.Tuple>createGenericCoder(), false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public HuskySortBenchmark.Tuple[] dualPivotQuicksort(final TupleState state) {
        final HuskySortBenchmark.Tuple[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    @Benchmark
    public HuskySortBenchmark.Tuple[] radixHuskySort8(final TupleState state) {
        final HuskySortBenchmark.Tuple[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.<HuskySortBenchmark.Tuple>createGenericCoder(), state.config).sort(copy);
    }

    @Benchmark
    public HuskySortBenchmark.Tuple[] radixHuskySort11(final TupleState state) {
        final HuskySortBenchmark.Tuple[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.<HuskySortBenchmark.Tuple>createGenericCoder(), state.config).sort(copy);
    }

    @Benchmark
    public HuskySortBenchmark.Tuple[] radixHuskySort16(final TupleState state) {
        final HuskySortBenchmark.Tuple[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.<HuskySortBenchmark.Tuple>createGenericCoder(), state.config).sort(copy);
    }
}
