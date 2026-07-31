package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for {@link ParallelRadixHuskySort} against the existing serial
 * {@link RadixHuskySort} and {@link PureHuskySort}, answering the paper's own claim (previously
 * "left as future work") that RadixHuskySort's per-digit passes are a natural candidate for
 * parallelization. Uses large N throughout, since parallel overhead (thread-pool creation,
 * per-chunk task submission) only pays for itself once the per-pass work is substantial.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class ParallelRadixSortBenchmarks {

    @State(Scope.Thread)
    public static class LongState {
        @Param({"2000000", "10000000"})
        public int n;

        Long[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            final Random random = new Random(43);
            master = Utilities.fillRandomArray(Long.class, random, n, Random::nextLong);
        }
    }

    @Benchmark
    public Long[] pureHuskySort(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        new PureHuskySort<>(HuskyCoderFactory.longCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] serialRadixHuskySort11(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.longCoder, state.config).sort(copy);
    }

    @Benchmark
    public Long[] parallelRadixHuskySort11_p1(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("p1", 0, 11, HuskyCoderFactory.longCoder, Arrays::sort, state.config, 1).sort(copy);
    }

    @Benchmark
    public Long[] parallelRadixHuskySort11_p2(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("p2", 0, 11, HuskyCoderFactory.longCoder, Arrays::sort, state.config, 2).sort(copy);
    }

    @Benchmark
    public Long[] parallelRadixHuskySort11_p4(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("p4", 0, 11, HuskyCoderFactory.longCoder, Arrays::sort, state.config, 4).sort(copy);
    }

    @Benchmark
    public Long[] parallelRadixHuskySort11_p8(final LongState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("p8", 0, 11, HuskyCoderFactory.longCoder, Arrays::sort, state.config, 8).sort(copy);
    }
}
