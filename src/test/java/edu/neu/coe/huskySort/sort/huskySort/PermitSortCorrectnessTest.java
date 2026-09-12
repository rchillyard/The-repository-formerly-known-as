package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.Permit;
import edu.neu.coe.huskySort.sort.huskySortUtils.PermitCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.PermitLoader;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Every sorter benchmarked in {@code PermitSortBenchmarks} must actually sort.
 * <p>
 * JMH does not check its subject's output, and this repository has already shipped a benchmarked sort
 * which produced the wrong order at its largest size without anything noticing -- see
 * {@code AlphabetOrderingTest}. A sort that returns early, or that mis-orders a fraction of the array,
 * is faster than one that does not, so an unverified benchmark can reward a defect. These tests are
 * the guard against publishing such a number.
 * <p>
 * The check is against {@code Arrays.sort} on the same array, comparing the resulting sequences by
 * {@code compareTo} rather than by identity, since equal permits may legitimately be interchanged.
 */
public class PermitSortCorrectnessTest {

    @BeforeClass
    public static void loadCorpus() {
        corpus = PermitLoader.getPermits();
    }

    @Test
    public void quickHuskySortWithTheExactCoderSortsCorrectly() {
        assertSortsCorrectly(xs -> new QuickHuskySort<>(PermitCoder.INSTANCE, false, false).sort(xs));
    }

    /**
     * The same codes, declared imperfect, so the cleanup pass runs. It must reach the same answer --
     * otherwise the pair of benchmarks would not be measuring the same task.
     */
    @Test
    public void quickHuskySortWithTheCleanupPassSortsCorrectly() {
        assertSortsCorrectly(xs -> new QuickHuskySort<>(HuskyCoderFactory.<Permit>createGenericCoder(), false, false).sort(xs));
    }

    @Test
    public void radixHuskySortSortsCorrectlyAtEveryDigitWidth() throws IOException {
        final Config config = Config.load();
        for (final int bits : new int[]{8, 11, 16})
            assertSortsCorrectly(xs -> new RadixHuskySort<Permit>(bits, PermitCoder.INSTANCE, config).sort(xs),
                    "radix/" + bits);
    }

    /**
     * The whole corpus, not a sample. The defect this guards against appeared only at the largest size
     * tried, because that was where the input finally contained the shape that triggered it.
     */
    @Test
    public void theWholeCorpusSortsCorrectly() throws IOException {
        final Permit[] expected = corpus.clone();
        Arrays.sort(expected);
        final Permit[] actual = corpus.clone();
        new RadixHuskySort<Permit>(11, PermitCoder.INSTANCE, Config.load()).sort(actual);
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertEquals("position " + i, 0, expected[i].compareTo(actual[i]));
    }

    private void assertSortsCorrectly(final java.util.function.Consumer<Permit[]> sorter) {
        assertSortsCorrectly(sorter, "sorter");
    }

    private void assertSortsCorrectly(final java.util.function.Consumer<Permit[]> sorter, final String name) {
        final Permit[] sample = shuffledSample(SAMPLE);
        final Permit[] expected = sample.clone();
        Arrays.sort(expected);
        final Permit[] actual = sample.clone();
        sorter.accept(actual);
        assertEquals(name + ": length", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertEquals(name + " at position " + i, 0, expected[i].compareTo(actual[i]));
    }

    /**
     * A subset without replacement, so the sample stays all-distinct and is not in the published
     * order, which is partly chronological and so partly sorted.
     */
    private static Permit[] shuffledSample(final int n) {
        final Random random = new Random(11);
        final Permit[] pool = corpus.clone();
        for (int i = pool.length - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final Permit swap = pool[i];
            pool[i] = pool[j];
            pool[j] = swap;
        }
        return Arrays.copyOf(pool, n);
    }

    private static final int SAMPLE = 40000;
    private static Permit[] corpus;
}
