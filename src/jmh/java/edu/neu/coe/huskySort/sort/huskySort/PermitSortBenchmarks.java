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

    /**
     * The parallel bakeoff, added 2026-09-12. The permits are the strongest case in the paper for
     * husky coding -- an expensive three-field composite ordering paired with a perfect encoding,
     * so step 3 never runs -- which makes them the case where a parallel comparison is most worth
     * having. The Long[] comparison of ParallelRadixSortBenchmarks is the opposite extreme, a cheap
     * ordering where the encoding has least to offer. See request 9 in doc/Run request for Yunlu.md.
     * <p>
     * N is capped at the corpus's own 198,900, which cannot be extended on real data -- an order of
     * magnitude below the Long[] parallel sizes. Parallel speedup follows total work rather than
     * element count, though, and a three-field comparator over 198,900 records is a great deal of
     * work, so the comparison is still meaningful at that size.
     */
    @Benchmark
    public Permit[] systemSortParallel(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.parallelSort(copy);
        return copy;
    }

    @Benchmark
    public Permit[] parallelRadixHuskySort16_p4(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("p4", 0, 16, PermitCoder.INSTANCE, Arrays::sort, state.config, 4).sort(copy);
    }

    @Benchmark
    public Permit[] parallelRadixHuskySort16_p8(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("p8", 0, 16, PermitCoder.INSTANCE, Arrays::sort, state.config, 8).sort(copy);
    }

    /**
     * The same sort as parallelRadixHuskySort16_p8, differing only in that the digit width is
     * derived from n and the chunk count rather than fixed at 16 (see TODO.md items 34 and 35). At
     * these sizes the derivation picks 12 bits at n = 198,900 and 11 at n = 100,000, trading more
     * passes over the keys for far less per-pass bucket bookkeeping. Kept alongside the fixed-16
     * benchmark rather than replacing it, so that the two are measured in the same JMH invocation
     * under the same machine conditions -- desktop drift between separate runs has been observed
     * to exceed the effect being measured.
     */
    @Benchmark
    public Permit[] parallelRadixHuskySortAuto_p8(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("auto", 0, ParallelRadixHuskySort.AUTO_DIGIT_BITS, PermitCoder.INSTANCE, Arrays::sort, state.config, 8).sort(copy);
    }

    /**
     * As parallelRadixHuskySortAuto_p8, but allowing chunks as small as 4,096 elements instead of
     * the default 16,384. The default leaves the middle size short of the threads it asked for --
     * at n = 100,000 it permits only 6 chunks of the 8 requested, and at n = 32,000 only 1, so
     * that size runs entirely serially -- and n = 100,000 is the one size where the husky sort does
     * not beat Arrays.parallelSort. This benchmark is what decides whether that is the reason. See
     * TODO.md item 34's last paragraph.
     */
    /**
     * As parallelRadixHuskySortAuto_p8, but with as many chunks as the machine has processors,
     * rather than a hardcoded eight.
     * <p>
     * This is the only husky row directly comparable with systemSortParallel, because
     * Arrays.parallelSort sizes itself from the machine too -- via the common ForkJoinPool, whose
     * parallelism is availableProcessors() - 1 (or the cgroup CPU quota, where one applies). On
     * eight cores p8 and the system sort get comparable resources and the comparison is fair by
     * accident; on Yunlu's sixteen-core instance the common pool reported **15 workers** while our
     * rows asked for 8, so request 9's permits result -- Arrays.parallelSort 2.77x faster than p8 --
     * compared a 15-thread sort against an 8-thread one. His notes say as much: "no p15/p16 husky
     * row was requested or run". Added 2026-09-16 so that request 10 does not repeat that.
     * <p>
     * NOTE: deliberately machine-dependent, so this row's thread count is not reproducible from the
     * label alone. Record availableProcessors() alongside the result. That is the price of
     * comparing against a baseline which is itself machine-sized; the fixed p4/p8 rows remain for
     * the scaling sweep, where a fixed count is the point.
     */
    @Benchmark
    public Permit[] parallelRadixHuskySortAuto_pAll(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("autoAll", 0, ParallelRadixHuskySort.AUTO_DIGIT_BITS, PermitCoder.INSTANCE, Arrays::sort, state.config, Runtime.getRuntime().availableProcessors()).sort(copy);
    }

    @Benchmark
    public Permit[] parallelRadixHuskySortAuto_p8_chunk4k(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>("auto4k", 0, ParallelRadixHuskySort.AUTO_DIGIT_BITS, 1 << 12, PermitCoder.INSTANCE, Arrays::sort, state.config, 8).sort(copy);
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

    /**
     * The serial sorter with the digit width derived from n rather than fixed (TODO.md item 35,
     * second bullet). The permits table has /11 ahead of /16 at n = 32,000 and behind it at 198,900;
     * the rule picks 12 and 15 respectively, so this row is what tests whether choosing per size
     * beats any single fixed width. Kept alongside the fixed widths, which still run at exactly the
     * width they name.
     */
    @Benchmark
    public Permit[] radixHuskySortAuto(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(RadixHuskySort.AUTO_DIGIT_BITS, PermitCoder.INSTANCE, state.config).sort(copy);
    }

    @Benchmark
    public Permit[] radixHuskySort16(final PermitState state) {
        final Permit[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, PermitCoder.INSTANCE, state.config).sort(copy);
    }
}
