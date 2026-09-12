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
     * Primary constructor.
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
        super(name, n, huskyCoder, postSorter, config);
        if (digitBits < 1 || digitBits > 20) throw new IllegalArgumentException("digitBits must be between 1 and 20: " + digitBits);
        if (parallelism < 1) throw new IllegalArgumentException("parallelism must be at least 1: " + parallelism);
        this.digitBits = digitBits;
        this.parallelism = parallelism;
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
        this("ParallelRadixHuskySort/" + digitBits, 0, digitBits, huskyCoder, defaultPostSorter(huskyCoder), config, Runtime.getRuntime().availableProcessors());
    }

    private static <Y extends Comparable<Y>> Consumer<Y[]> defaultPostSorter(final HuskyCoder<Y> huskyCoder) {
        final Collator collator = huskyCoder.getCollator();
        return collator == null ? Arrays::sort : xs -> Arrays.sort(xs, collator);
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

    @Override
    public void sort(final X[] xs, final int from, final int to) {
        final int n = to - from;
        if (n < 2) return;
        final long[] longs = getHelper().getLongs();
        final int chunks = Math.max(1, Math.min(parallelism, n / MIN_CHUNK_SIZE));
        final ExecutorService executor = Executors.newFixedThreadPool(chunks);
        try {
            final int[] permutation = radixSortIndices(longs, from, n, digitBits, chunks, executor);
            applyPermutation(xs, longs, from, n, permutation);
        } finally {
            executor.shutdown();
        }
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

        final PassState state = new PassState(n);
        for (int i = 0; i < n; i++) {
            state.biased[i] = longs[from + i] ^ Long.MIN_VALUE;
            state.index[i] = i;
        }

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
                    final long[] biasedIn = state.biased;
                    final int[] localCount = localCounts[chunkIndex];
                    Arrays.fill(localCount, 0);
                    for (int i = start; i < end; i++) localCount[(int) ((biasedIn[i] >>> shift) & mask)]++;
                    awaitUninterruptibly(afterHistogram);

                    final long[] biasedOut = state.biasedBuffer;
                    final int[] indexIn = state.index;
                    final int[] indexOut = state.indexBuffer;
                    final int[] chunkCursor = chunkBucketOffset[chunkIndex].clone();
                    for (int i = start; i < end; i++) {
                        final int b = (int) ((biasedIn[i] >>> shift) & mask);
                        final int pos = chunkCursor[b]++;
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
     * Method to apply the given permutation to xs[from..from+n) (and, for consistency, to the
     * corresponding range of longs) in a single O(N) pass. Not parallelized: this single pass is
     * cheap relative to the digit passes above, and payload types are arbitrary objects, so a
     * parallel version would need to reason about safe concurrent writes to an Object[] -- not
     * worth the complexity for an O(N) pass that already runs once, not once per digit.
     *
     * @param xs          the payload array to be permuted in place.
     * @param longs       the array of longs corresponding to xs (kept in sync for consistency).
     * @param from        the index of the first element to permute.
     * @param n           the number of elements to permute.
     * @param permutation an array of n indices (each relative to "from") such that, for each i,
     *                    the element currently at from + permutation[i] should end up at from + i.
     */
    private static <Y> void applyPermutation(final Y[] xs, final long[] longs, final int from, final int n, final int[] permutation) {
        final Y[] sourceObjects = Arrays.copyOfRange(xs, from, from + n);
        final long[] sourceLongs = Arrays.copyOfRange(longs, from, from + n);
        for (int i = 0; i < n; i++) {
            final int sourceIndex = permutation[i];
            xs[from + i] = sourceObjects[sourceIndex];
            longs[from + i] = sourceLongs[sourceIndex];
        }
    }

    private final int digitBits;
    private final int parallelism;
}
