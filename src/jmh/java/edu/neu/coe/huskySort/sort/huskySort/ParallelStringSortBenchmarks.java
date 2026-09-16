package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Parallel-versus-parallel benchmarks on real string corpora: {@link ParallelRadixHuskySort}
 * against {@code Arrays.parallelSort}, answering TODO.md item 33.
 * <p>
 * {@link ParallelRadixSortBenchmarks} already settles this question on {@code Long[]}, but that is
 * the case least favourable to husky coding: comparing two {@code Long}s is cheap, so the encoding
 * has almost nothing to buy back. The English and Chinese corpora are the opposite extreme -- an
 * expensive ordering, and for {@code chinesenames} a pinyin one, which no comparison sort can
 * evaluate without a per-character lookup -- and so are where a reviewer asking "how does the
 * parallel variant do against {@code Arrays.parallelSort} on real strings?" should be answered.
 * Until this class existed there was no answer:
 * {@link StringSortBenchmarks} gained a {@code systemSortParallel} on 2026-09-12 but had no
 * parallel husky sort to set against it.
 * <p>
 * This is a separate class from {@link StringSortBenchmarks}, rather than more methods added to
 * it, so that it can be run on its own -- the string corpora take a long time to sort at
 * n = 1,000,000 across three corpora, and a parallel-versus-parallel run should not require
 * re-measuring the dozen-odd serial benchmarks that class already holds.
 * <p>
 * It reuses {@code StringSortBenchmarks.StringState} rather than duplicating the corpus loading,
 * so the corpora, the seed, and the sampling semantics (including the duplicate-density confound
 * documented on that class's {@code sampling} parameter) are identical to the serial numbers these
 * are compared against.
 * <p>
 * <b>Narrowing the run.</b> The default matrix is three corpora x three sizes x seven methods,
 * which is a long run. To reduce it, pass JMH parameters, e.g.
 * <pre>
 * -p corpus=english -p n=1000000
 * </pre>
 * and/or restrict the methods with a regex, e.g. {@code ".*(Auto_p8|systemSortParallel)$"}.
 * <p>
 * <b>A measurement warning that applies to every class here, learned the hard way on 2026-09-16.</b>
 * JMH runs a class's methods in lexicographic order, so if machine load drifts upward over a run,
 * whichever method sorts last is systematically penalised -- and {@code systemSortParallel} sorts
 * last in this class, as it does in {@link PermitSortBenchmarks} and {@link StringSortBenchmarks}.
 * That is a bias, not merely noise. For a result that decides a paper claim, run the baseline and
 * the candidate as a two-method invocation so they sit adjacent in time, and record {@code uptime}
 * and the core count alongside the numbers. On a desktop, also leave several minutes between
 * building the (76 MB) benchmarks jar and measuring, or antivirus will be scanning it during the
 * first benchmark.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class ParallelStringSortBenchmarks {

    /**
     * The chunk count used by the single-configuration benchmarks below. Matches the largest thread
     * count swept by {@link ParallelRadixSortBenchmarks} on {@code Long[]}.
     */
    private static final int PARALLELISM = 8;

    /**
     * The parallel baseline, ordered the same way the husky sorts order -- which for
     * {@code chinesenames} means by pinyin, not by raw UTF-16 code point. Code-point order is a
     * cheaper task and the wrong one, and is not an ordering Chinese text is actually sorted in:
     * the real alternative to pinyin is stroke order.
     * <p>
     * Uses {@code NAME_ORDER} directly rather than the coder's Collator, which is only a thin
     * wrapper round the same comparator ({@code PinyinOrdinalCollator} delegates to it), so the
     * baseline is not charged an indirection per comparison that the husky sorts' own cleanup pass
     * would also pay. Matches {@code StringSortBenchmarks.systemSortPinyin} and
     * {@code multikeyQuicksort}.
     */
    @Benchmark
    public String[] systemSortParallel(final StringSortBenchmarks.StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        if (state.corpus.equals("chinesenames")) Arrays.parallelSort(copy, HuskyCoderChinesePinyin.NAME_ORDER);
        else Arrays.parallelSort(copy);
        return copy;
    }

    /**
     * The serial sort, as the reference for what parallelising actually buys on strings.
     * <p>
     * NOTE: at the default {@value RadixHuskySort#DEFAULT_DIGIT_BITS}-bit width, not an automatic
     * one: {@link ParallelRadixHuskySort#AUTO_DIGIT_BITS} has no counterpart in
     * {@link RadixHuskySort}, because the automatic choice budgets {@code buckets × chunks} and
     * the serial sorter has no chunks to budget against. Widening it to a serial equivalent is
     * TODO.md item 35's second bullet, which is a separate question.
     */
    @Benchmark
    public String[] serialRadixHuskySort8(final StringSortBenchmarks.StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(RadixHuskySort.DEFAULT_DIGIT_BITS, state.coder, state.config).sort(copy);
    }

    // ---------- The thread-count sweep, mirroring ParallelRadixSortBenchmarks' p1/p2/p4/p8 on
    // Long[], so that scaling on strings can be compared with scaling on a cheap ordering. ----------

    @Benchmark
    public String[] parallelRadixHuskySortAuto_p1(final StringSortBenchmarks.StringState state) {
        return sortAuto(state, 1);
    }

    @Benchmark
    public String[] parallelRadixHuskySortAuto_p2(final StringSortBenchmarks.StringState state) {
        return sortAuto(state, 2);
    }

    @Benchmark
    public String[] parallelRadixHuskySortAuto_p4(final StringSortBenchmarks.StringState state) {
        return sortAuto(state, 4);
    }

    @Benchmark
    public String[] parallelRadixHuskySortAuto_p8(final StringSortBenchmarks.StringState state) {
        return sortAuto(state, PARALLELISM);
    }

    /**
     * As many chunks as the machine has processors, rather than a hardcoded eight -- the only husky
     * row directly comparable with {@link #systemSortParallel}, which sizes itself from the machine
     * via the common {@code ForkJoinPool}. On Yunlu's sixteen-core instance that pool reported 15
     * workers against our 8, so request 9's comparison was 15 threads against 8. See the fuller
     * note on {@code PermitSortBenchmarks.parallelRadixHuskySortAuto_pAll}.
     * <p>
     * NOTE: machine-dependent by design, so record {@code availableProcessors()} with the result.
     */
    @Benchmark
    public String[] parallelRadixHuskySortAuto_pAll(final StringSortBenchmarks.StringState state) {
        return sortAuto(state, Runtime.getRuntime().availableProcessors());
    }

    /**
     * The fixed 11-bit width, for comparison with the automatic choice on strings. 11 bits is the
     * width {@link ParallelRadixSortBenchmarks} uses on {@code Long[]}; on these corpora the
     * automatic choice picks 16 bits at n = 1,000,000 and narrower at the smaller sizes.
     */
    @Benchmark
    public String[] parallelRadixHuskySort11_p8(final StringSortBenchmarks.StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>(11, state.coder, state.config, PARALLELISM).sort(copy);
    }

    /**
     * NOTE: uses the Collator-aware secondary constructor, so that the cleanup pass for
     * {@code chinesenames} -- whose encoding is deliberately imperfect, and so always runs one --
     * orders by pinyin rather than naturally. Naming a post-sorter explicitly here, as
     * {@link PermitSortBenchmarks} does with {@code Arrays::sort}, would silently produce
     * natural-order results for that corpus.
     *
     * @param state       the benchmark state holding the corpus, coder and config.
     * @param parallelism the number of chunks to split each digit pass across.
     * @return the sorted copy.
     */
    private static String[] sortAuto(final StringSortBenchmarks.StringState state, final int parallelism) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new ParallelRadixHuskySort<>(ParallelRadixHuskySort.AUTO_DIGIT_BITS, state.coder, state.config, parallelism).sort(copy);
    }
}
