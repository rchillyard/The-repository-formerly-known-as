package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.simple.PureDualPivotQuicksort;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks answering Reviewer 4's adversarial-input question (TODO.md item 7): the
 * original paper never showed what happens when the husky encoding has poor entropy in its
 * high-order bits (i.e. many keys collide there), and it would be "trivial to construct inputs
 * that cause the proposed scheme to perform poorly". This class benchmarks (not just tests for
 * correctness -- see RadixHuskySortTest for that) how RadixHuskySort's performance actually
 * degrades relative to the existing quicksort-based approach (QuickHuskySort) as entropy in the
 * high-order bits is progressively removed, via two scenarios:
 * <ul>
 *     <li>{@link CollapsedBitsState}: synthetic longs with a swept number of high-order bits
 *     held fixed (identical across every element) -- a direct, parameterized version of the
 *     concern.</li>
 *     <li>{@link SharedPrefixState}: real English words with a swept-length shared prefix
 *     prepended -- a realistic version, since string husky coders pack characters left-to-right
 *     into the long (most significant first), so a shared prefix means the leading many bits of
 *     every element's husky code collide.</li>
 * </ul>
 * Radix sort's digit-by-digit passes always do the same fixed amount of work per pass,
 * regardless of the data's actual distribution (a pass over collapsed/identical digits is just
 * "everything goes in one bucket", no more expensive than any other pass) -- so the expectation
 * is that RadixHuskySort's timing should stay flat as entropy is removed, while the
 * comparison-based approach's behavior is less obviously predictable in advance (a 3-way
 * partition can also handle heavy duplication well, but not by the same mechanism).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class AdversarialSortBenchmarks {

    // ---------- Scenario A: synthetic longs with swept high-bit collapse ----------

    @State(Scope.Thread)
    public static class CollapsedBitsState {
        @Param({"200000", "1000000"})
        public int n;

        // Number of high-order bits held fixed (identical across all elements); 0 = fully
        // random (baseline), 63 = only the sign bit varies.
        @Param({"0", "16", "32", "48", "56", "60", "63"})
        public int fixedHighBits;

        Long[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            final Random random = new Random(43);
            final long mask = fixedHighBits == 0 ? 0L : (-1L << (64 - fixedHighBits));
            final long fixedBits = 0x5A5A_5A5A_5A5A_5A5AL & mask;
            master = Utilities.fillRandomArray(Long.class, random, n, r -> (r.nextLong() & ~mask) | fixedBits);
        }
    }

    @Benchmark
    public Long[] collapsedBitsSystemSort(final CollapsedBitsState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] collapsedBitsQuickHuskySort(final CollapsedBitsState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.longCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] collapsedBitsDualPivotQuicksort(final CollapsedBitsState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    @Benchmark
    public Long[] collapsedBitsRadixHuskySort8(final CollapsedBitsState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.longCoder, state.config).sort(copy);
    }

    @Benchmark
    public Long[] collapsedBitsRadixHuskySort11(final CollapsedBitsState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.longCoder, state.config).sort(copy);
    }

    @Benchmark
    public Long[] collapsedBitsRadixHuskySort16(final CollapsedBitsState state) {
        final Long[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.longCoder, state.config).sort(copy);
    }

    // ---------- Scenario B: strings sharing a long common prefix ----------

    @State(Scope.Thread)
    public static class SharedPrefixState {
        @Param({"200000", "1000000"})
        public int n;

        // Length of the fixed prefix prepended to every word; 0 = baseline (no shared prefix).
        @Param({"0", "10", "20", "40"})
        public int prefixLength;

        String[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            final String[] corpusWords = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_1M-sentences.txt", AdversarialSortBenchmarks::getLeipzigWords);
            final String prefix = "a".repeat(prefixLength);
            final Random random = new Random(42);
            master = Utilities.fillRandomArray(String.class, random, n, r -> prefix + corpusWords[r.nextInt(corpusWords.length)]);
        }
    }

    // NOTE: same reconstruction of HuskySortBenchmark's private getLeipzigWords used in
    // StringSortBenchmarks -- see that class for why this one-line wrapper needs duplicating.
    private static List<String> getLeipzigWords(final String line) {
        return HuskySortBenchmarkHelper.splitLineIntoStrings(line, HuskySortBenchmark.REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER);
    }

    @Benchmark
    public String[] sharedPrefixSystemSort(final SharedPrefixState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public String[] sharedPrefixQuickHuskySort(final SharedPrefixState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.englishCoder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public String[] sharedPrefixRadixHuskySort8(final SharedPrefixState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, HuskyCoderFactory.englishCoder, state.config).sort(copy);
    }

    @Benchmark
    public String[] sharedPrefixRadixHuskySort11(final SharedPrefixState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, HuskyCoderFactory.englishCoder, state.config).sort(copy);
    }

    @Benchmark
    public String[] sharedPrefixRadixHuskySort16(final SharedPrefixState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, HuskyCoderFactory.englishCoder, state.config).sort(copy);
    }
}
