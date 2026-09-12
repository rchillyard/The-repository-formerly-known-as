package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.sort.simple.IntroSort;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.MergeSortBasic;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.sort.simple.QuickSort_DualPivot;
import edu.neu.coe.huskySort.sort.simple.TimSort;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Instrumenter;
import edu.neu.coe.huskySort.util.ConfigTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A sort's reported comparison count must equal the number of comparisons it
 * actually makes.
 * <p>
 * This is the ground-truth check. The Helper counts what it is *asked* to count,
 * so a sort which compares without going through the Helper is undercounted and
 * nothing notices — the figure simply comes out low, looks plausible, and gets
 * published. In the sibling INFO6205 repository exactly that had happened: its
 * TimSort was reimplemented from the JDK so it could be instrumented, only
 * {@code binarySort} was ever wired up, and on 1,000 random ints it reported
 * 3,677 comparisons out of 8,702 actually made — and **zero** out of 999 on
 * already-sorted input, because the one instrumented method is never reached when
 * the run detector finds the array already in order.
 * <p>
 * The trick is to count comparisons at the only place that cannot be bypassed:
 * inside the element type. {@link Counted#compareTo} increments a counter, so
 * whatever route a sort takes to compare two elements — Helper, raw
 * {@code compareTo}, or a JDK sort it delegates to — the comparison is seen.
 * <p>
 * NOTE this repository is in a much better position than INFO6205 was, and for a
 * structural reason: {@code InstrumentedComparisonSortHelper.compare} is itself
 * {@code v.compareTo(w)}, and its TimSort is a thin wrapper around
 * {@code Arrays.sort} rather than a reimplementation. This test exists to keep it
 * that way, and because the paper reports these counts.
 */
public class InstrumentationIsCompleteTest {

    private static final int N = 500;

    /**
     * An Integer which counts every comparison anyone makes on it, by whatever
     * route.
     */
    private static class Counted implements Comparable<Counted> {
        private static long comparisons = 0;
        private final int value;

        Counted(final int value) {
            this.value = value;
        }

        public int compareTo(final Counted other) {
            comparisons++;
            return Integer.compare(value, other.value);
        }

        static void reset() {
            comparisons = 0;
        }
    }

    private static Counted[] randomArray() {
        final Random random = new Random(0L);
        final Counted[] xs = new Counted[N];
        for (int i = 0; i < N; i++) xs[i] = new Counted(random.nextInt(1000));
        return xs;
    }

    private static Counted[] sortedArray() {
        final Counted[] xs = new Counted[N];
        for (int i = 0; i < N; i++) xs[i] = new Counted(i);
        return xs;
    }

    /**
     * Run a sort and report how many comparisons it made without telling the
     * Helper.
     *
     * @param make how to build the sort from an instrumented Helper.
     * @param xs   the array to sort.
     * @return actual comparisons minus reported comparisons.
     */
    private long uncounted(final Function<ComparisonSortHelper<Counted>, SortWithHelper<Counted>> make,
                           final Counted[] xs) {
        final InstrumentedComparisonSortHelper<Counted> helper =
                new InstrumentedComparisonSortHelper<>("test", xs.length, config);
        helper.init(xs.length);
        final SortWithHelper<Counted> sorter = make.apply(helper);
        // NOTE preProcess, not just init. MergeSortBasic allocates its auxiliary
        // array in preSort, and its sort(xs, from, to) reads that array without
        // checking -- so calling the sub-array sort on a sorter which has not been
        // pre-sorted is a NullPointerException. The same was true of INFO6205's
        // MergeSortBasic, where it has been fixed.
        sorter.init(xs.length);
        sorter.preSort(xs, false);
        Counted.reset();
        sorter.sort(xs, 0, xs.length);
        // NOTE captured BEFORE postProcess, which checks sortedness and counts
        // inversions -- both of which compare, and neither of which is part of the
        // sort's own work.
        final long actual = Counted.comparisons;
        // NOTE the raw counter is private, so the figure has to come through the
        // StatPack -- which is also the figure that gets reported and published.
        helper.postProcess(xs);
        final long reported = Math.round(helper.getStatPack().mean(Instrumenter.COMPARES));
        return actual - reported;
    }

    /**
     * Assert that a sort reports every comparison it makes, on both random and
     * already-sorted input. The sorted case is the one that catches a sort whose
     * instrumented path is skipped when the input is already in order.
     *
     * @param name the sort's name, for the failure message.
     * @param make how to build it.
     */
    private void check(final String name, final Function<ComparisonSortHelper<Counted>, SortWithHelper<Counted>> make) {
        final long onRandom = uncounted(make, randomArray());
        assertEquals(name + ": comparisons made but not counted, on random input", 0, onRandom);
        final long onSorted = uncounted(make, sortedArray());
        assertEquals(name + ": comparisons made but not counted, on sorted input", 0, onSorted);
    }

    @Test
    public void insertionSortCountsEveryComparison() {
        check("InsertionSort", InsertionSort::new);
    }

    @Test
    public void mergeSortBasicCountsEveryComparison() {
        check("MergeSortBasic", MergeSortBasic::new);
    }

    @Test
    public void quickSort3wayCountsEveryComparison() {
        check("QuickSort_3way", QuickSort_3way::new);
    }

    @Test
    public void quickSortDualPivotCountsEveryComparison() {
        check("QuickSort_DualPivot", QuickSort_DualPivot::new);
    }

    @Test
    public void introSortCountsEveryComparison() {
        check("IntroSort", IntroSort::new);
    }

    /**
     * TimSort here delegates to {@code Arrays.sort} and goes nowhere near the
     * Helper, so it reports no comparisons at all. That is honest — it reports
     * nothing rather than reporting a wrong number — but it does mean any figure
     * for it must be a timing, never a count.
     * <p>
     * Recorded rather than asserted as complete, so that if anyone ever
     * instruments it, this test tells them the recording is out of date.
     */
    @Test
    public void timSortReportsNoComparisonsAtAll() {
        final Counted[] xs = randomArray();
        final InstrumentedComparisonSortHelper<Counted> helper =
                new InstrumentedComparisonSortHelper<>("test", xs.length, config);
        helper.init(xs.length);
        final SortWithHelper<Counted> sorter = new TimSort<>(helper);
        sorter.init(xs.length);
        sorter.preSort(xs, false);
        Counted.reset();
        sorter.sort(xs, 0, xs.length);
        assertTrue("TimSort really does compare", Counted.comparisons > 0);
        helper.postProcess(xs);
        assertEquals("TimSort delegates to Arrays.sort, so the Helper sees nothing",
                0, Math.round(helper.getStatPack().mean(Instrumenter.COMPARES)));
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(ConfigTest.class).copy("helper", "instrument", "true")
                .copy("instrumenting", "compares", "true")
                .copy("instrumenting", "swaps", "true")
                .copy("instrumenting", "hits", "true");
    }

    private static Config config;
}
