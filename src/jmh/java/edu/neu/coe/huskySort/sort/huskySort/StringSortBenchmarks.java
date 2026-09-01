package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.radix.Alphabet;
import edu.neu.coe.huskySort.sort.radix.MSDStringSort;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.MultikeyQuicksort;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks comparing RadixHuskySort against the existing sort strategies on real
 * corpora (English/Chinese Leipzig corpora, common English words), mirroring
 * HuskySortBenchmark's String sorting comparisons -- see "doc/Radix Sort Benchmark Results.md"
 * for the equivalent ad hoc (non-JMH) numbers this replaces.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class StringSortBenchmarks {

    @State(Scope.Thread)
    public static class StringState {
        @Param({"32000", "200000", "1000000"})
        public int n;

        // NOTE: "commonwords" is deliberately not a default param (TODO.md item 3) -- its
        // ~3,000-word corpus sampled with replacement into 200K-1M element arrays causes
        // artificial duplicate-heavy skew, and short common words are already cheap to
        // compare, undercutting the whole point of Husky Sort. Still runnable explicitly as a
        // known-weak-case sanity check: -p corpus=commonwords.
        @Param({"english", "chinese", "chinesenames"})
        public String corpus;

        String[] master;
        HuskyCoder<String> coder;
        Config config;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            config = Config.load();
            final String[] corpusWords;
            switch (corpus) {
                case "english":
                    corpusWords = HuskySortBenchmarkHelper.getWords("eng-uk_web_2002_1M-sentences.txt", StringSortBenchmarks::getLeipzigWords);
                    coder = AbstractHuskySort.UNICODE_CODER;
                    break;
                case "chinese":
                    corpusWords = HuskySortBenchmarkHelper.getWords("zho-simp-tw_web_2014_10K-sentences.txt", StringSortBenchmarks::getLeipzigWords);
                    coder = AbstractHuskySort.UNICODE_CODER;
                    break;
                case "chinesenames":
                    // Chinese personal names, ordered by pinyin (TODO.md item 4) rather than
                    // the Unicode coder used for the Leipzig "chinese" corpus above.
                    corpusWords = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
                    coder = HuskyCoderFactory.chineseEncoderPinyin;
                    break;
                case "commonwords":
                    corpusWords = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.COMMON_WORDS_CORPUS, HuskySortBenchmark::lineAsList);
                    coder = HuskyCoderFactory.englishCoder;
                    break;
                default:
                    throw new IllegalStateException("unknown corpus: " + corpus);
            }
            final Random random = new Random(42);
            master = Utilities.fillRandomArray(String.class, random, n, r -> corpusWords[r.nextInt(corpusWords.length)]);
        }
    }

    // NOTE: reconstructs HuskySortBenchmark's private getLeipzigWords, whose building blocks
    // (splitLineIntoStrings, REGEX_STRING_SPLITTER, REGEX_LEIPZIG) are package-private/public
    // and reused as-is; only this one-line wrapper needs duplicating.
    private static List<String> getLeipzigWords(final String line) {
        return HuskySortBenchmarkHelper.splitLineIntoStrings(line, HuskySortBenchmark.REGEX_LEIPZIG, HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER);
    }

    // ---------- Encoding-only cost, isolated from any sort (paper resubmission, Phase A item
    // 7 -- a reviewer asked directly whether the encoding phase's cost was measured separately
    // from the sort). ----------

    @Benchmark
    public long[] huskyEncodeOnly(final StringState state) {
        return state.coder.huskyEncode(state.master).longs;
    }

    @Benchmark
    public String[] systemSort(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        Arrays.sort(copy);
        return copy;
    }

    // ---------- Three-way radix quicksort / multikey quicksort (Bentley and Sedgewick 1997),
    // as a real empirical baseline for the paper's classic string-sorting literature discussion,
    // replacing a purely theoretical comparison. For the chinesenames corpus, sorts by pinyin
    // (MultikeyQuicksort.sortByPinyin) rather than natural Unicode order, so that this benchmark
    // is directly comparable to RadixHuskySort/QuickHuskySort's pinyin-ordered result for that
    // corpus too, not just for english/chinese. ----------

    @Benchmark
    public String[] multikeyQuicksort(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        if (state.corpus.equals("chinesenames")) MultikeyQuicksort.sortByPinyin(copy);
        else MultikeyQuicksort.sort(copy);
        return copy;
    }

    // ---------- MSD string sort (Bentley and Sedgewick 1997), the other classic string sort named
    // in the paper's literature discussion. ENGLISH ONLY, and deliberately so: MSDStringSort's
    // Alphabet has room for 256 distinct characters beyond ASCII, and the Chinese corpora contain
    // 3,813 and 2,270 of them, so it is an extended-ASCII MSD rather than a Unicode one. It throws
    // rather than returning a misleading number for those, since a benchmark that silently does
    // nothing is worse than one that fails. Run with -p corpus=english.
    //
    // NOTE MSDStringSort's below-cutoff comparison used to allocate two Strings per comparison,
    // which made it about 1.3x slower than it should have been. Fixed before this benchmark was
    // added, so that the comparison is against MSD rather than against the allocator. ----------

    @Benchmark
    public String[] msdStringSort(final StringState state) {
        if (!state.corpus.equals("english"))
            throw new IllegalStateException("msdStringSort supports the english corpus only: "
                    + "MSDStringSort's alphabet cannot represent the Chinese corpora. Use -p corpus=english.");
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        final MSDStringSort sorter = new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE));
        sorter.reset();
        sorter.sort(copy);
        return copy;
    }

    // ---------- Insertion sort (paper resubmission, small-N crossover follow-up): Robin asked
    // how plain insertion sort compares at very small N, since System sort likely already
    // defers to something insertion-sort-like below its own internal threshold. ----------

    @Benchmark
    public String[] insertionSort(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        InsertionSort.mutatingInsertionSort(copy);
        return copy;
    }

    @Benchmark
    public String[] quickHuskySort(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        new QuickHuskySort<>(state.coder, false, false).sort(copy);
        return copy;
    }

    @Benchmark
    public String[] radixHuskySort8(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(8, state.coder, state.config).sort(copy);
    }

    @Benchmark
    public String[] radixHuskySort11(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(11, state.coder, state.config).sort(copy);
    }

    @Benchmark
    public String[] radixHuskySort16(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(16, state.coder, state.config).sort(copy);
    }

    // ---------- Finer digit-width sweep (TODO.md item 2), to locate the actual crossover
    // point between "fewer passes" and "count-array fits in cache" between 8 and 16 bits. ----------

    @Benchmark
    public String[] radixHuskySort10(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(10, state.coder, state.config).sort(copy);
    }

    @Benchmark
    public String[] radixHuskySort12(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(12, state.coder, state.config).sort(copy);
    }

    @Benchmark
    public String[] radixHuskySort13(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(13, state.coder, state.config).sort(copy);
    }

    @Benchmark
    public String[] radixHuskySort14(final StringState state) {
        final String[] copy = Arrays.copyOf(state.master, state.master.length);
        return new RadixHuskySort<>(14, state.coder, state.config).sort(copy);
    }
}
