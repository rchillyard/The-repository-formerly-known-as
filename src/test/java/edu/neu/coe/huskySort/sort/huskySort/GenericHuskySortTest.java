package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortable;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link GenericHuskySort}, the entry point for a caller whose own type knows how to
 * encode itself -- {@link HuskySortable} rather than a coder supplied from outside.
 * <p>
 * The interesting property to test is not that a good husky code sorts correctly; it is that a
 * <i>bad</i> one does. The whole premise of the method is that step 2 orders by an approximation
 * and step 3 repairs whatever is left, so a coder that is merely quasi-order-preserving -- or, at
 * the limit, carries no information at all -- must still produce an exactly sorted array. The
 * interface's own javadoc is candid about this, promising a long that is only "approximately 90%
 * accurate". These tests therefore run the same assertion over a faithful code, a constant code
 * and a code that is deliberately <i>reversed</i>, since a caller writing a {@code huskyCode} by
 * hand is much more likely to get it partly wrong than to get it exactly right.
 */
public class GenericHuskySortTest {

    /**
     * A sortable whose natural order is by name, and whose husky code is whatever the test says it
     * is. Keeping the two independent is what lets a test feed the sorter a deliberately useless
     * code while still knowing the answer.
     */
    private static class Word implements HuskySortable<Word> {
        private final String name;
        private final long code;

        Word(final String name, final long code) {
            this.name = name;
            this.code = code;
        }

        public long huskyCode() {
            return code;
        }

        public int compareTo(final Word other) {
            return name.compareTo(other.name);
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof Word && name.equals(((Word) o).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final String[] NAMES = {
            "Willkommen", "Hello", "Ciao", "Goodbye", "Bonjour", "Hola", "Ahoj", "Zdravo", "Salve", "Namaste"};

    /** The faithful case: the code orders exactly as the elements do. */
    private static Word faithful(final String s) {
        return new Word(s, HuskyCoderFactory.asciiToLong(s));
    }

    /** The degenerate case: every element has the same code, so step 2 learns nothing. */
    private static Word constant(final String s) {
        return new Word(s, 42L);
    }

    /** The adversarial case: the code orders exactly backwards. */
    private static Word reversed(final String s) {
        return new Word(s, -HuskyCoderFactory.asciiToLong(s));
    }

    private static Word[] words(final java.util.function.Function<String, Word> f) {
        return Arrays.stream(NAMES).map(f).toArray(Word[]::new);
    }

    private static Word[] expected() {
        final Word[] xs = words(GenericHuskySortTest::faithful);
        Arrays.sort(xs);
        return xs;
    }

    @Test
    public void sortsCorrectlyWithAFaithfulHuskyCode() {
        assertSorts(words(GenericHuskySortTest::faithful));
    }

    /**
     * The code carries no information whatever, so every element collides and step 2 can do nothing
     * useful. The result must still be exactly sorted, by step 3 alone. If this fails, the cleanup
     * pass is not being run, or not being run over the whole array.
     */
    @Test
    public void sortsCorrectlyWithAConstantHuskyCode() {
        assertSorts(words(GenericHuskySortTest::constant));
    }

    /**
     * Worse than useless: the code is exactly anti-correlated with the ordering, so step 2 leaves
     * the array in reverse order and step 3 has the maximum possible number of inversions to
     * remove. Correctness must not depend on the coder being any good.
     */
    @Test
    public void sortsCorrectlyWithAReversedHuskyCode() {
        assertSorts(words(GenericHuskySortTest::reversed));
    }

    /** The same three cases at a size that exercises the recursion rather than a base case. */
    @Test
    public void sortsALargeArrayWhateverTheCodeIsWorth() {
        final Random random = new Random(42L);
        final int n = 5_000;
        final Word[] faithfulWords = new Word[n], constantWords = new Word[n], reversedWords = new Word[n];
        for (int i = 0; i < n; i++) {
            final String s = randomName(random);
            faithfulWords[i] = faithful(s);
            constantWords[i] = constant(s);
            reversedWords[i] = reversed(s);
        }
        final Word[] truth = Arrays.copyOf(faithfulWords, n);
        Arrays.sort(truth);
        assertArrayEquals("faithful code", truth, sorted(faithfulWords));
        assertArrayEquals("constant code", truth, sorted(constantWords));
        assertArrayEquals("reversed code", truth, sorted(reversedWords));
    }

    /**
     * The one-argument constructor documents its post-sorter as {@code Arrays::sort}. Since the
     * coder is whatever the element supplies and can be arbitrarily bad, that default is the only
     * thing standing between a caller and a wrong answer, so it is worth pinning that it is
     * actually wired up rather than left null.
     */
    @Test
    public void theDefaultPostSorterIsTheSystemSort() {
        assertSorts(words(GenericHuskySortTest::constant));
    }

    /**
     * The two-argument constructor's post-sorter must actually be invoked, and must be what fixes
     * the array. A custom one is used here both to count the invocation and to do the work.
     */
    @Test
    public void theSuppliedPostSorterIsUsed() {
        final AtomicInteger calls = new AtomicInteger();
        final Word[] xs = words(GenericHuskySortTest::constant);
        final Word[] result;
        try (final GenericHuskySort<Word> sorter = new GenericHuskySort<>(a -> {
            calls.incrementAndGet();
            Arrays.sort(a);
        }, config)) {
            result = sorter.sort(xs);
        }
        assertEquals("the post-sorter should be invoked exactly once", 1, calls.get());
        assertArrayEquals(expected(), result);
    }

    /**
     * A perfect coder is entitled to skip step 3 -- but {@code createGenericCoder} never claims
     * perfection (it cannot: it knows nothing about X beyond the long it is handed), so the pass
     * must run even when the code happens to be faithful. Otherwise a caller whose code is 99%
     * right would get a 99% sorted array.
     */
    @Test
    public void theCleanupPassRunsEvenWhenTheCodeIsFaithful() {
        final AtomicInteger calls = new AtomicInteger();
        try (final GenericHuskySort<Word> sorter = new GenericHuskySort<>(a -> {
            calls.incrementAndGet();
            Arrays.sort(a);
        }, config)) {
            sorter.sort(words(GenericHuskySortTest::faithful));
        }
        assertEquals("a generic coder cannot claim perfection, so step 3 must run", 1, calls.get());
    }

    /**
     * The control for the three tests above: with the cleanup replaced by a no-op, a constant code
     * must leave the array <i>un</i>sorted. Without this, those three would still pass if step 2
     * happened to order the array by itself, and they would be asserting nothing.
     */
    @Test
    public void stepTwoAloneIsNotSufficientSoTheCleanupIsLoadBearing() {
        final Word[] result;
        try (final GenericHuskySort<Word> sorter = new GenericHuskySort<>(a -> { }, config)) {
            result = sorter.sort(words(GenericHuskySortTest::constant));
        }
        boolean ordered = true;
        for (int i = 1; i < result.length; i++)
            if (result[i - 1].compareTo(result[i]) > 0) ordered = false;
        assertFalse("with a constant code and no cleanup the array should NOT come out sorted;"
                + " if it does, the other tests here prove nothing", ordered);
    }

    @Test
    public void sortsEmptyAndSingletonArrays() {
        try (final GenericHuskySort<Word> sorter = new GenericHuskySort<>(config)) {
            assertArrayEquals(new Word[0], sorter.sort(new Word[0]));
            final Word[] one = {faithful("only")};
            assertArrayEquals(one, sorter.sort(one));
        }
    }

    @Test
    public void sortsAnAlreadySortedAndAReversedArray() {
        final Word[] truth = expected();
        try (final GenericHuskySort<Word> sorter = new GenericHuskySort<>(config)) {
            assertArrayEquals(truth, sorter.sort(Arrays.copyOf(truth, truth.length)));
            final Word[] backwards = Arrays.copyOf(truth, truth.length);
            reverse(backwards);
            assertArrayEquals(truth, sorter.sort(backwards));
        }
    }

    @Test
    public void isNamedForTheBenefitOfTheHelperAndAnyLogging() {
        try (final GenericHuskySort<Word> sorter = new GenericHuskySort<>(config)) {
            assertEquals("Generic HuskySort", sorter.toString());
        }
    }

    // ---------- helpers ----------

    private void assertSorts(final Word[] xs) {
        assertArrayEquals(expected(), sorted(xs));
    }

    private Word[] sorted(final Word[] xs) {
        try (final GenericHuskySort<Word> sorter = new GenericHuskySort<>(config)) {
            final Word[] result = sorter.sort(Arrays.copyOf(xs, xs.length));
            for (int i = 1; i < result.length; i++)
                assertTrue("out of order at " + i + ": " + result[i - 1] + " then " + result[i],
                        result[i - 1].compareTo(result[i]) <= 0);
            return result;
        }
    }

    private static String randomName(final Random random) {
        final int length = 1 + random.nextInt(8);
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append((char) ('a' + random.nextInt(26)));
        return sb.toString();
    }

    private static void reverse(final Word[] xs) {
        for (int i = 0, j = xs.length - 1; i < j; i++, j--) {
            final Word t = xs[i];
            xs[i] = xs[j];
            xs[j] = t;
        }
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(GenericHuskySortTest.class);
    }

    private static Config config;
}
