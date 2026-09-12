package edu.neu.coe.huskySort.sort.simple;

/**
 * Classic single-pivot quicksort on objects, with no helper and no instrumentation -- the
 * single-pivot counterpart of {@link edu.neu.coe.huskySort.sort.simple.PureDualPivotQuicksort}.
 * <p>
 * It exists for one comparison. Dual-pivot quicksort's advantage over single-pivot is usually
 * attributed to cache behaviour rather than to any reduction in comparisons, and the paper passes
 * that attribution along on the strength of a citation. Measuring the two against each other on
 * identical arrays, with hardware counters attached, would let it be said from measurement instead.
 * It is also the comparison Robin teaches in DSAIPG, where the cache explanation is at present
 * asserted rather than shown. See request 8 in doc/Run request for Yunlu.md.
 * <p>
 * The existing {@link QuickSort} could not be used for this. It is abstract over the instrumented
 * {@code Helper} framework, which counts comparisons and swaps as it goes; benchmarked against a
 * pure implementation it would measure the instrumentation rather than the algorithm.
 * <p>
 * Three design choices, stated because they determine what the comparison means:
 * <ul>
 *     <li>The insertion-sort cutoff is 47, matched deliberately to
 *     {@code PureDualPivotQuicksort.INSERTION_SORT_THRESHOLD}. Matching it means the difference
 *     between the two sorts is their partitioning, not where each stops partitioning.</li>
 *     <li>The pivot is the median of three (first, middle, last), which is the standard defence
 *     against sorted and reverse-sorted input. Dual-pivot's own pivots are chosen from five
 *     candidates, so this is not an identical policy -- it is the conventional single-pivot one,
 *     which is what the teaching comparison is about.</li>
 *     <li>Recursion descends into the smaller partition first and loops on the larger, bounding
 *     stack depth at lg N without a depth counter.</li>
 * </ul>
 * No introsort fallback: this is quicksort, and the point is to observe quicksort.
 */
public final class PureSinglePivotQuicksort {

    private PureSinglePivotQuicksort() {
    }

    /**
     * Matched to PureDualPivotQuicksort's own threshold so that the two sorts differ in their
     * partitioning rather than in when they stop partitioning.
     */
    private static final int INSERTION_SORT_THRESHOLD = 47;

    /**
     * Sort the given array in place.
     *
     * @param a   the array to be sorted.
     * @param <X> the element type.
     */
    public static <X extends Comparable<X>> void sort(final X[] a) {
        sort(a, 0, a.length - 1);
    }

    private static <X extends Comparable<X>> void sort(final X[] a, int lo, int hi) {
        while (lo < hi) {
            if (hi - lo + 1 <= INSERTION_SORT_THRESHOLD) {
                insertionSort(a, lo, hi);
                return;
            }
            final int p = partition(a, lo, hi);
            // Descend into the smaller side, iterate on the larger: stack depth stays within lg N.
            if (p - lo < hi - p) {
                sort(a, lo, p - 1);
                lo = p + 1;
            } else {
                sort(a, p + 1, hi);
                hi = p - 1;
            }
        }
    }

    /**
     * Hoare-style partitioning about a median-of-three pivot, which is parked at lo for the scan.
     *
     * @return the final index of the pivot.
     */
    private static <X extends Comparable<X>> int partition(final X[] a, final int lo, final int hi) {
        medianOfThreeToLo(a, lo, hi);
        final X pivot = a[lo];
        int i = lo, j = hi + 1;
        while (true) {
            while (a[++i].compareTo(pivot) < 0) if (i == hi) break;
            while (pivot.compareTo(a[--j]) < 0) if (j == lo) break;
            if (i >= j) break;
            swap(a, i, j);
        }
        swap(a, lo, j);
        return j;
    }

    private static <X extends Comparable<X>> void medianOfThreeToLo(final X[] a, final int lo, final int hi) {
        final int mid = lo + (hi - lo) / 2;
        if (a[mid].compareTo(a[lo]) < 0) swap(a, mid, lo);
        if (a[hi].compareTo(a[lo]) < 0) swap(a, hi, lo);
        if (a[hi].compareTo(a[mid]) < 0) swap(a, hi, mid);
        // lo <= mid <= hi; park the median at lo so the scan below can use it as a sentinel.
        swap(a, lo, mid);
    }

    private static <X extends Comparable<X>> void insertionSort(final X[] a, final int lo, final int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            final X x = a[i];
            int j = i - 1;
            while (j >= lo && a[j].compareTo(x) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = x;
        }
    }

    private static <X> void swap(final X[] a, final int i, final int j) {
        final X t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
