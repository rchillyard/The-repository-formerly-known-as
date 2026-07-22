package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.util.Config;
import org.openjdk.jmh.annotations.*;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks comparing RadixHuskySort against the existing sort strategies for
 * ChronoLocalDateTime, mirroring HuskySortBenchmark.sortLocalDateTimes -- see
 * "doc/Radix Sort Benchmark Results.md" for the equivalent ad hoc (non-JMH) numbers this
 * replaces. The date coder is "perfect" (a single epoch-second long, no collisions), so this
 * is a simple case: no cleanup pass is ever needed.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class DateSortBenchmarks {

    @State(Scope.Thread)
    public static class DateState {
        @Param({"20000"})
        public int n;

        LocalDateTime[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            master = HuskySortHelper.generateRandomLocalDateTimeArray(n);
        }
    }

    @Benchmark
    public LocalDateTime[] systemSort(final DateState state) {
        final LocalDateTime[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public ChronoLocalDateTime<?>[] quickHuskySort(final DateState state) {
        final ChronoLocalDateTime<?>[] copy = Arrays.copyOf(state.master, state.master.length);
        return new QuickHuskySort<>(HuskyCoderFactory.chronoLocalDateTimeCoder, state.config).sort(copy);
    }

    @Benchmark
    public ChronoLocalDateTime<?>[] quickHuskySortWithInsertion(final DateState state) {
        final ChronoLocalDateTime<?>[] copy = Arrays.copyOf(state.master, state.master.length);
        return new QuickHuskySort<>("QuickHuskySort/Insertion", HuskyCoderFactory.chronoLocalDateTimeCoder, new InsertionSort<ChronoLocalDateTime<?>>()::mutatingSort, state.config).sort(copy);
    }

    @Benchmark
    public ChronoLocalDateTime<?>[] radixHuskySort8(final DateState state) {
        final ChronoLocalDateTime<?>[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.chronoLocalDateTimeCoder, state.config).sort(copy);
    }

    @Benchmark
    public ChronoLocalDateTime<?>[] radixHuskySort11(final DateState state) {
        final ChronoLocalDateTime<?>[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.chronoLocalDateTimeCoder, state.config).sort(copy);
    }

    @Benchmark
    public ChronoLocalDateTime<?>[] radixHuskySort16(final DateState state) {
        final ChronoLocalDateTime<?>[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.chronoLocalDateTimeCoder, state.config).sort(copy);
    }
}
