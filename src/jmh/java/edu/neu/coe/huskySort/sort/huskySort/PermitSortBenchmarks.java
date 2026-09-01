package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.Permit;
import edu.neu.coe.huskySort.sort.huskySortUtils.PermitCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.PermitLoader;
import edu.neu.coe.huskySort.sort.simple.PureDualPivotQuicksort;
import edu.neu.coe.huskySort.util.Config;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks over real data: San Francisco's published building permit record, sorted by the
 * ordering the records are actually browsed in -- Assessor's block, then lot, then filing date.
 * <p>
 * This is the case the mechanism is best suited to, and it is here because the paper's other
 * favourable cases are synthetic. Three properties hold at once, and each is one of the factors that
 * decides husky encoding's advantage. The native comparison is composite and therefore expensive: two
 * Strings and a date, with short-circuiting. The encoding is exact, packing the whole ordering into 60
 * of 64 bits, so no cleanup pass is needed at all -- see {@link PermitCoder}, and
 * {@code PermitCoderTest}, which verifies that against every record rather than arguing it from the
 * bit budget. And no sort specialised to municipal permit records exists, so the alternative is a
 * comparator-driven sort or a radix sort hand-written for this one type.
 * <p>
 * NOTE the two {@code quickHuskySort} benchmarks differ in one respect only: which coder they are
 * given. Both compute identical codes. {@link PermitCoder} declares itself perfect, so
 * {@code Coding.perfect} is true and the sort skips its cleanup pass;
 * {@code HuskyCoderFactory.createGenericCoder} is a lambda over the same method whose {@code perfect()}
 * takes the interface default of false, so the pass runs and finds nothing to do. The difference
 * between them is therefore the cost of the cleanup pass alone, on an input where it is provably
 * unnecessary -- which is the quantity the discussion of $p_{crit}$ turns on.
 * <p>
 * Sizes below the corpus are drawn from it <em>without</em> replacement, so every element remains a
 * distinct record. Sampling with replacement would raise duplicate density with n and confound any
 * comparison between a bucketing sort and a comparison sort, which is a trap the string benchmarks in
 * this suite had to be rescued from.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class PermitSortBenchmarks {

    @State(Scope.Thread)
    public static class PermitState {
        /**
         * 198,900 is the whole corpus; the smaller sizes are subsets of it.
         */
        @Param({"32000", "100000", "198900"})
        public int n;

        Permit[] master;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            final Permit[] corpus = PermitLoader.getPermits();
            if (n > corpus.length)
                throw new IllegalStateException("the corpus holds " + corpus.length + " permits, fewer than " + n);
            final Random random = new Random(42);
            // shuffle, then take a prefix: a subset without replacement, and one that does not inherit
            // the published order (which is chronological in part, and so partially sorted).
            final Permit[] pool = Arrays.copyOf(corpus, corpus.length);
            for (int i = pool.length - 1; i > 0; i--) {
                final int j = random.nextInt(i + 1);
                final Permit swap = pool[i];
                pool[i] = pool[j];
                pool[j] = swap;
            }
            master = Arrays.copyOf(pool, n);
        }
    }

    @Benchmark
    public Permit[] systemSort(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    @Benchmark
    public Permit[] dualPivotQuicksort(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        PureDualPivotQuicksort.sort(copy);
        return copy;
    }

    /**
     * The exact coder: no cleanup pass.
     */
    @Benchmark
    public Permit[] quickHuskySort(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(PermitCoder.INSTANCE, false, false).sort(copy);
        return copy;
    }

    /**
     * The same codes, but declared imperfect, so the cleanup pass runs. Paired with the benchmark
     * above, this isolates what that pass costs when it has nothing to fix.
     */
    @Benchmark
    public Permit[] quickHuskySortWithCleanup(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(HuskyCoderFactory.<Permit>createGenericCoder(), false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public Permit[] radixHuskySort8(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, PermitCoder.INSTANCE, state.config).sort(copy);
    }

    @Benchmark
    public Permit[] radixHuskySort11(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, PermitCoder.INSTANCE, state.config).sort(copy);
    }

    @Benchmark
    public Permit[] radixHuskySort16(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, PermitCoder.INSTANCE, state.config).sort(copy);
    }
}
