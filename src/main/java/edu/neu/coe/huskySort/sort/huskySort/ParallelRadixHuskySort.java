/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.util.Config;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Parallel variant of {@link RadixHuskySort}: the same LSD radix sort with a deferred
 * permutation, but each digit pass is itself parallelized across a configurable number of
 * threads, rather than run single-threaded.
 * <p>
 * Radix sort's digit passes are natural candidates for parallelization, unlike the
 * comparison-based Introsort/Timsort steps this repo also uses (see the paper's discussion of
 * why no equivalent claim is made for those). Each pass here is split into contiguous chunks,
 * one per thread: every chunk first computes its own local per-bucket histogram independently
 * (no synchronization needed), then a short sequential step combines those histograms into an
 * exact starting offset, in the output array, for every (chunk, bucket) pair -- preserving LSD
 * radix sort's required stability, since a chunk's elements always land after all
 * lower-numbered chunks' elements that share the same bucket -- and finally every chunk scatters
 * its own elements into the output arrays independently, using only its own precomputed offsets,
 * so no chunk ever writes to a location another chunk might also write to.
 * <p>
 * Synchronization is via two reused {@link CyclicBarrier}s (one per phase transition) rather
 * than resubmitting fresh tasks to an executor at every phase of every pass: worker threads are
 * started once, each running its own loop over every pass, and a barrier's own "action" (which
 * runs exactly once per trip, in whichever thread arrives last, before any thread is released)
 * does the sequential histogram-combine step and the buffer-swap/shift-advance step. This
 * replaces what was originally two {@code ExecutorService.invokeAll} round-trips per pass (twelve
 * for an 11-bit sort) with a single {@code invokeAll} for the whole sort; ongoing
 * phase-to-phase and pass-to-pass coordination is then just a barrier wait, which is
 * substantially cheaper than re-dispatching tasks to a thread pool. An earlier version of this
 * class did exactly that per-phase re-dispatch, and JMH measurement showed real per-pass
 * overhead eating a meaningful share of the theoretical parallel benefit -- see
 * doc/Radix Sort Benchmark Results.md for the numbers that motivated this redesign.
 *
 * @param <X> the underlying type of the elements to be sorted.
 */
public final class ParallelRadixHuskySort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    /**
     * The default digit width, in bits, of each radix-sort pass.
     */
    public static final int DEFAULT_DIGIT_BITS = 8;

    /**
     * Below this many elements per chunk, parallelizing is not worth the thread-coordination
     * overhead, so fewer (possibly just one) chunks are used instead.
     */
    public static final int MIN_CHUNK_SIZE = 1 << 14;

    /**
     * Pass this as {@code digitBits} to have the digit width derived from n and the chunk count
     * rather than fixed in advance -- see {@link #chooseDigitBits}. An explicitly-given width is
     * always honoured exactly, so that a sorter named /16 really does run at 16 bits.
     */
    public static final int AUTO_DIGIT_BITS = 0;

    /**
     * The narrowest digit the automatic choice will pick. Below this the pass count grows faster
     * than the bookkeeping shrinks: 8 bits is 8 passes, and 4 bits would be 16.
     */
    static final int MIN_AUTO_DIGIT_BITS = 8;

    /**
     * The widest digit the automatic choice will pick. 16 bits already reaches the minimum useful
     * pass count of four; going wider (20 bits is still ceil(64/20) = 4 passes) buys no passes and
     * costs sixteen times the buckets.
     */
    static final int MAX_AUTO_DIGIT_BITS = 16;

    /**
     * Method to choose a digit width for which the per-pass bucket bookkeeping stays small
     * relative to the data it is bookkeeping for.
     * <p>
     * Three of this class's per-pass costs -- clearing each chunk's histogram, the sequential
     * histogram-combine in the afterHistogram action, and the per-chunk cursor row -- are sized by
     * {@code buckets × chunks}, not by n. The design is sound while that product is much smaller
     * than n and collapses when it is not: at 16 bits with 8 chunks the product is 524,288, which
     * is more than twice the 198,900-record permits corpus, so the sort spends more traffic on
     * bookkeeping than on keys. This budgets that product at n/4 and takes the widest digit
     * fitting inside it, since wider digits mean fewer passes over the keys.
     *
     * @param n      the number of elements to be sorted.
     * @param chunks the number of chunks each pass will be split across.
     * @return a digit width in [MIN_AUTO_DIGIT_BITS, MAX_AUTO_DIGIT_BITS].
     */
    static int chooseDigitBits(final int n, final int chunks) {
        final int bucketBudget = n / (4 * chunks);
        // highestOneBit(0) is 0, whose numberOfTrailingZeros is 32, so guard the small-n case
        // rather than letting it wrap round to an absurdly wide digit.
        final int widest = bucketBudget < 2 ? MIN_AUTO_DIGIT_BITS : Integer.numberOfTrailingZeros(Integer.highestOneBit(bucketBudget));
        return Math.max(MIN_AUTO_DIGIT_BITS, Math.min(MAX_AUTO_DIGIT_BITS, widest));
    }

    /**
     * Primary constructor.
     *
     * @param name         the name of the sorter (used by the helper).
     * @param n            the number of elements to be sorted (may be 0 if unknown).
     * @param digitBits    the width, in bits, of each radix-sort digit/pass (e.g. 8, 11, 16), or
     *                     {@link #AUTO_DIGIT_BITS} to have it derived from n and the chunk count.
     * @param minChunkSize the smallest number of elements a chunk may be given; the chunk count is
     *                     reduced below "parallelism" as far as necessary to respect it. See
     *                     {@link #MIN_CHUNK_SIZE} for what it trades off.
     * @param huskyCoder   the Husky coder.
     * @param postSorter   the post-sorter which will fix any remaining inversions.
     * @param config       the configuration.
     * @param parallelism  the number of chunks (and worker threads) to use for each digit pass.
     */
    public ParallelRadixHuskySort(final String name, final int n, final int digitBits, final int minChunkSize, final HuskyCoder<X> huskyCoder, final Consumer<X[]> postSorter, final Config config, final int parallelism) {
        super(name, n, huskyCoder, postSorter, config);
        if (digitBits != AUTO_DIGIT_BITS && (digitBits < 1 || digitBits > 20)) throw new IllegalArgumentException("digitBits must be between 1 and 20, or AUTO_DIGIT_BITS: " + digitBits);
        if (parallelism < 1) throw new IllegalArgumentException("parallelism must be at least 1: " + parallelism);
        if (minChunkSize < 1) throw new IllegalArgumentException("minChunkSize must be at least 1: " + minChunkSize);
        this.digitBits = digitBits;
        this.minChunkSize = minChunkSize;
        this.parallelism = parallelism;
    }

    /**
     * Secondary constructor which takes the default minimum chunk size
     * ({@value #MIN_CHUNK_SIZE} elements).
     *
     * @param name        the name of the sorter (used by the helper).
     * @param n           the number of elements to be sorted (may be 0 if unknown).
     * @param digitBits   the width, in bits, of each radix-sort digit/pass (e.g. 8, 11, 16).
     * @param huskyCoder  the Husky coder.
     * @param postSorter  the post-sorter which will fix any remaining inversions.
     * @param config      the configuration.
     * @param parallelism the number of chunks (and worker threads) to use for each digit pass.
     */
    public ParallelRadixHuskySort(final String name, final int n, final int digitBits, final HuskyCoder<X> huskyCoder, final Consumer<X[]> postSorter, final Config config, final int parallelism) {
        this(name, n, digitBits, MIN_CHUNK_SIZE, huskyCoder, postSorter, config, parallelism);
    }

    /**
     * Secondary constructor: the number of elements is unknown, parallelism defaults to the
     * number of available processors, and the post-sorter is the System sort -- using
     * huskyCoder's Collator if it supplies one, falling back to natural ordering otherwise (see
     * RadixHuskySort's equivalent constructor for why this check matters).
     *
     * @param digitBits  the width, in bits, of each radix-sort digit/pass.
     * @param huskyCoder the Husky coder.
     * @param config     the configuration.
     */
    public ParallelRadixHuskySort(final int digitBits, final HuskyCoder<X> huskyCoder, final Config config) {
        this("ParallelRadixHuskySort/" + (digitBits == AUTO_DIGIT_BITS ? "auto" : digitBits), 0, digitBits, huskyCoder, defaultPostSorter(huskyCoder), config, Runtime.getRuntime().availableProcessors());
    }

    private static <Y extends Comparable<Y>> Consumer<Y[]> defaultPostSorter(final HuskyCoder<Y> huskyCoder) {
        final Collator collator = huskyCoder.getCollator();
        return collator == null ? Arrays::sort : xs -> Arrays.sort(xs, collator);
    }

    /**
     * Secondary constructor taking an explicit chunk count while still deriving the post-sorter
     * from the coder, as the two-argument constructor does -- so that a coder supplying a Collator
     * (e.g. HuskyCoderChinesePinyin) gets a cleanup pass in Collator order rather than natural
     * order. Without this, a caller wanting a specific thread count had to reach for the primary
     * constructor and name a post-sorter itself, which is how a pinyin-ordered sort acquires a
     * natural-order cleanup pass and quietly produces the wrong answer.
     *
     * @param digitBits   the width, in bits, of each radix-sort digit/pass, or
     *                    {@link #AUTO_DIGIT_BITS}.
     * @param huskyCoder  the Husky coder.
     * @param config      the configuration.
     * @param parallelism the number of chunks (and worker threads) to use for each digit pass.
     */
    public ParallelRadixHuskySort(final int digitBits, final HuskyCoder<X> huskyCoder, final Config config, final int parallelism) {
        this("ParallelRadixHuskySort/" + (digitBits == AUTO_DIGIT_BITS ? "auto" : digitBits) + "/p" + parallelism, 0, digitBits, huskyCoder, defaultPostSorter(huskyCoder), config, parallelism);
    }

    /**
     * Secondary constructor which uses the default digit width ({@value #DEFAULT_DIGIT_BITS} bits).
     *
     * @param huskyCoder the Husky coder.
     * @param config     the configuration.
     */
    public ParallelRadixHuskySort(final HuskyCoder<X> huskyCoder, final Config config) {
        this(DEFAULT_DIGIT_BITS, huskyCoder, config);
    }

    /**
     * Worker threads, shared across every instance and every sort: created on demand, reused from
     * one sort to the next, and retired after the pool's own idle timeout. This replaces a
     * per-sort {@code Executors.newFixedThreadPool}, whose construction was charged to every
     * measurement -- where {@code Arrays.parallelSort}, the baseline we are measured against, gets
     * the already-running common {@link java.util.concurrent.ForkJoinPool} for free.
     * <p>
     * NOTE: deliberately NOT {@code ForkJoinPool.commonPool()}. These workers block on a
     * {@link CyclicBarrier} until every chunk arrives, and the common pool runs only
     * {@code availableProcessors - 1} threads; a sort asking for more chunks than that would
     * deadlock, with the unscheduled chunks never reaching the barrier. A cached pool is
     * unbounded, which also keeps working the deliberately-oversized chunk counts that
     * ParallelRadixHuskySortTest sweeps.
     * <p>
     * The threads are daemons, so holding the pool open for the life of the JVM never delays exit.
     */
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        final Thread thread = new Thread(runnable, "ParallelRadixHuskySort-worker");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void sort(final X[] xs, final int from, final int to) {
        final int n = to - from;
        if (n < 2) return;
        final long[] longs = getHelper().getLongs();
        final int chunks = Math.max(1, Math.min(parallelism, n / minChunkSize));
        // The automatic width depends on the chunk count, so it can only be settled here.
        final int passDigitBits = digitBits == AUTO_DIGIT_BITS ? chooseDigitBits(n, chunks) : digitBits;
        final int[] permutation = radixSortIndices(longs, from, n, passDigitBits, chunks, EXECUTOR);
        applyPermutation(xs, from, n, permutation);
    }

    /**
     * As {@link RadixHuskySort}'s equivalent method, but each pass is split into "chunks"
     * contiguous chunks, processed by "chunks" worker threads (submitted once, via "executor",
     * for the whole sort) that synchronize between phases via two reused {@link CyclicBarrier}s
     * rather than being resubmitted at every phase of every pass.
     *
     * @param longs     the array of longs to consider (only the range [from, from+n) is read).
     * @param from      the index of the first long to consider.
     * @param n         the number of longs to consider.
     * @param digitBits the width, in bits, of each digit/pass.
     * @param chunks    the number of chunks (and worker threads) to split each pass across.
     * @param executor  the executor service used to run the (single, whole-sort) worker tasks.
     * @return an array of n indices (each relative to "from") giving the order in which the
     * original elements should appear so that the corresponding longs are sorted ascending.
     */
    private static int[] radixSortIndices(final long[] longs, final int from, final int n, final int digitBits, final int chunks, final ExecutorService executor) {
        final int buckets = 1 << digitBits;
        final int mask = buckets - 1;
        final int numPasses = (Long.SIZE + digitBits - 1) / digitBits;

        final int[] chunkStart = new int[chunks];
        final int[] chunkEnd = new int[chunks];
        final int baseChunkSize = n / chunks;
        final int remainder = n % chunks;
        int cursor = 0;
        for (int c = 0; c < chunks; c++) {
            chunkStart[c] = cursor;
            cursor += baseChunkSize + (c < remainder ? 1 : 0);
            chunkEnd[c] = cursor;
        }

        // NOTE: neither "biased" nor "index" is pre-filled. Pass 0 below reads its keys straight
        // from "longs", applying the sign bias as it goes, and writes the identity index as it
        // scatters -- so the two setup passes over n that used to stand here (n reads and n writes
        // each) are absorbed into a pass that was already reading and writing every element.
        final PassState state = new PassState(n);

        // Reused across every pass: one histogram row per chunk, and the corresponding
        // per-(chunk, bucket) starting offsets, both sized once, refilled every pass.
        final int[][] localCounts = new int[chunks][buckets];
        final int[][] chunkBucketOffset = new int[chunks][buckets];

        // Barrier 1's action runs once per pass, after every chunk's local histogram is ready:
        // combines them into exact starting offsets, in (bucket, chunk) order, so that a bucket's
        // elements from an earlier chunk always precede the same bucket's elements from a later
        // chunk -- the stability property this design must preserve.
        final CyclicBarrier afterHistogram = new CyclicBarrier(chunks, () -> {
            int globalOffset = 0;
            for (int b = 0; b < buckets; b++) {
                for (int c = 0; c < chunks; c++) {
                    chunkBucketOffset[c][b] = globalOffset;
                    globalOffset += localCounts[c][b];
                }
            }
        });

        // Barrier 2's action runs once per pass, after every chunk has scattered its elements:
        // swaps the double-buffered arrays and advances the shift, so the next pass (if any)
        // reads the just-written buffer as its input.
        final CyclicBarrier afterScatter = new CyclicBarrier(chunks, () -> {
            state.swapAndAdvance(digitBits);
        });

        final List<Callable<Void>> workers = new ArrayList<>(chunks);
        for (int c = 0; c < chunks; c++) {
            final int chunkIndex = c;
            final int start = chunkStart[c];
            final int end = chunkEnd[c];
            workers.add(() -> {
                for (int pass = 0; pass < numPasses; pass++) {
                    final int shift = state.shift;
                    // Pass 0 has no biased array to read yet: it takes its keys from "longs",
                    // biasing each one as it is read, and supplies the identity index itself.
                    final boolean fromSource = pass == 0;
                    // The keys written by the last pass would never be read: only the index is
                    // returned. digitBits is capped at 20, so numPasses is always at least four,
                    // which is why the last-pass branch below can assume it is not also the first.
                    final boolean lastPass = pass == numPasses - 1;
                    final long[] biasedIn = state.biased;
                    final int[] localCount = localCounts[chunkIndex];
                    Arrays.fill(localCount, 0);
                    if (fromSource)
                        for (int i = start; i < end; i++) localCount[(int) (((longs[from + i] ^ Long.MIN_VALUE) >>> shift) & mask)]++;
                    else
                        for (int i = start; i < end; i++) localCount[(int) ((biasedIn[i] >>> shift) & mask)]++;
                    awaitUninterruptibly(afterHistogram);

                    final long[] biasedOut = state.biasedBuffer;
                    final int[] indexIn = state.index;
                    final int[] indexOut = state.indexBuffer;
                    // NOTE: mutated in place rather than cloned. The afterHistogram action above
                    // rewrites every element of every row before the next scatter reads it, so a
                    // chunk advancing its own cursors here destroys nothing that is read again.
                    // Cloning cost an allocation and a buckets-sized copy per chunk per pass.
                    final int[] chunkCursor = chunkBucketOffset[chunkIndex];
                    if (lastPass)
                        for (int i = start; i < end; i++) indexOut[chunkCursor[(int) ((biasedIn[i] >>> shift) & mask)]++] = indexIn[i];
                    else if (fromSource)
                        for (int i = start; i < end; i++) {
                            final long biased = longs[from + i] ^ Long.MIN_VALUE;
                            final int pos = chunkCursor[(int) ((biased >>> shift) & mask)]++;
                            biasedOut[pos] = biased;
                            indexOut[pos] = i;
                        }
                    else
                        for (int i = start; i < end; i++) {
                            final int pos = chunkCursor[(int) ((biasedIn[i] >>> shift) & mask)]++;
                            biasedOut[pos] = biasedIn[i];
                            indexOut[pos] = indexIn[i];
                        }
                    awaitUninterruptibly(afterScatter);
                }
                return null;
            });
        }

        try {
            final List<java.util.concurrent.Future<Void>> futures = executor.invokeAll(workers);
            for (final java.util.concurrent.Future<Void> f : futures) f.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("ParallelRadixHuskySort: interrupted during sort", e);
        } catch (final ExecutionException e) {
            throw new RuntimeException("ParallelRadixHuskySort: chunk task failed", e.getCause() != null ? e.getCause() : e);
        }
        return state.index;
    }

    /**
     * CyclicBarrier.await() declares checked InterruptedException/BrokenBarrierException; since
     * these worker tasks have no meaningful per-thread recovery from either (a broken barrier or
     * an interrupt here means the whole sort has failed), both are wrapped as unchecked and
     * rethrown, to be caught once at the top level in radixSortIndices via the Callable's
     * ExecutionException.
     *
     * @param barrier the barrier to await.
     */
    private static void awaitUninterruptibly(final CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("ParallelRadixHuskySort: interrupted awaiting barrier", e);
        } catch (final java.util.concurrent.BrokenBarrierException e) {
            throw new RuntimeException("ParallelRadixHuskySort: barrier broken (another chunk failed)", e);
        }
    }

    /**
     * Mutable, shared-across-threads holder for the double-buffered biased-longs/index arrays
     * and the current pass's shift amount. Fields are written only inside a CyclicBarrier action
     * (by whichever single thread triggers that barrier's trip) and read by all worker threads
     * only after their own call to that barrier's await() has returned -- CyclicBarrier
     * guarantees a happens-before edge from the action to every returning await(), so these
     * plain (non-volatile) fields are safely visible across threads without further
     * synchronization.
     */
    private static final class PassState {
        long[] biased;
        long[] biasedBuffer;
        int[] index;
        int[] indexBuffer;
        int shift;

        PassState(final int n) {
            biased = new long[n];
            biasedBuffer = new long[n];
            index = new int[n];
            indexBuffer = new int[n];
            shift = 0;
        }

        void swapAndAdvance(final int digitBits) {
            final long[] tempBiased = biased;
            biased = biasedBuffer;
            biasedBuffer = tempBiased;
            final int[] tempIndex = index;
            index = indexBuffer;
            indexBuffer = tempIndex;
            shift += digitBits;
        }
    }

    /**
     * Method to apply the given permutation to xs[from..from+n) in a single O(N) pass. Not
     * parallelized: this single pass is cheap relative to the digit passes above, and payload types
     * are arbitrary objects, so a parallel version would need to reason about safe concurrent
     * writes to an Object[] -- not worth the complexity for an O(N) pass that already runs once,
     * not once per digit.
     * <p>
     * NOTE: the helper's long array is deliberately not permuted to match -- see the fuller note on
     * {@link RadixHuskySort}'s equivalent method. After this sort, getLongs() holds the codes in
     * their original input order and is not meaningful.
     *
     * @param xs          the payload array to be permuted in place.
     * @param from        the index of the first element to permute.
     * @param n           the number of elements to permute.
     * @param permutation an array of n indices (each relative to "from") such that, for each i,
     *                    the element currently at from + permutation[i] should end up at from + i.
     */
    private static <Y> void applyPermutation(final Y[] xs, final int from, final int n, final int[] permutation) {
        final Y[] sourceObjects = Arrays.copyOfRange(xs, from, from + n);
        for (int i = 0; i < n; i++) xs[from + i] = sourceObjects[permutation[i]];
    }

    private final int digitBits;
    private final int minChunkSize;
    private final int parallelism;
}
