package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import edu.neu.coe.huskySort.sort.huskySort.PureHuskySort;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Correctness tests for HuskyCoderChinesePinyin's Hanyu (per-syllable-ordinal) encoding,
 * cross-validated against the trusted NAME_ORDER comparator on real corpus data -- the same
 * discipline the original radix-sort task brief called out (stress-test many random samples
 * against a trusted reference before trusting anything about performance).
 */
public class HuskyCoderChinesePinyinTest {

    private static String[] corpusNames;

    @BeforeClass
    public static void loadCorpus() {
        corpusNames = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
    }

    @Test
    public void testCorpusLoaded() {
        assertTrue(corpusNames.length > 100000);
    }

    @Test
    public void testSortRealCorpusNamesIsCorrectlyOrdered() {
        final String[] sample = sampleNames(new Random(1), 3000);
        assertSorted(sample);
    }

    /**
     * Many small random samples, per the stress-testing discipline noted in the task brief
     * (this is what caught a real off-by-one bug in an early radix-sort prototype).
     */
    @Test
    public void testStressManySmallSamples() {
        final Random random = new Random(7);
        for (int trial = 0; trial < 200; trial++) {
            final int n = 1 + random.nextInt(50);
            assertSorted(sampleNames(random, n));
        }
    }

    /**
     * A synthetic name longer than the 7-character encoding capacity: the first pass cannot
     * fully distinguish characters beyond the 7th, so this specifically exercises the cleanup
     * pass (via getCollator()) rather than relying on the first pass alone.
     */
    @Test
    public void testLongSyntheticNamesBeyondCapacity() {
        final String[] names = {
                "刘持平洪文胜樊辉辉苏会敏", // 12 characters
                "刘持平洪文胜樊辉辉苏会敏高", // 13 characters, shares the first 12 with the above
                "曹玉德袁继鹏舒冬梅杨腊香许", // 13 characters, unrelated
                "刘持平",
                "刘持平洪",
        };
        assertSorted(names);
    }

    /**
     * Four characters sharing the identical pinyin syllable spelling ("ma") but differing
     * only in tone: 妈(ma1) 麻(ma2) 马(ma3) 骂(ma4). The Hanyu encoding deliberately drops tone
     * (see HuskyCoderChinesePinyin's class javadoc), so these are guaranteed to collide in the
     * first pass; this verifies the cleanup pass correctly breaks the tie by tone.
     */
    @Test
    public void testToneOnlyCollision() {
        final String[] names = {"骂", "马", "麻", "妈"}; // deliberately out of order
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.chineseEncoderPinyin, false, false);
        sorter.sort(names);
        assertArrayEquals(new String[]{"妈", "麻", "马", "骂"}, names);
    }

    @Test
    public void testEmptyAndSingleton() {
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.chineseEncoderPinyin, false, false);
        final String[] empty = new String[0];
        sorter.sort(empty);
        assertArrayEquals(new String[0], empty);
        final String[] singleton = new String[]{"刘持平"};
        sorter.sort(singleton);
        assertArrayEquals(new String[]{"刘持平"}, singleton);
    }

    private static String[] sampleNames(final Random random, final int n) {
        final String[] sample = new String[n];
        for (int i = 0; i < n; i++) sample[i] = corpusNames[random.nextInt(corpusNames.length)];
        return sample;
    }

    /**
     * NOTE: this checks that the result is non-decreasing under NAME_ORDER, not that it
     * exactly matches Arrays.sort(names, NAME_ORDER) permutation-for-permutation. Real corpus
     * data contains genuine homonyms -- identical syllable AND tone, e.g. 郗/契 (both
     * "xi1") -- which Wikipedia's pinyin-alphabetical-order page says need stroke-count as a
     * final tiebreak; neither the old nor the new encoding implements that, so NAME_ORDER
     * legitimately cannot distinguish such pairs, and the (unstable) first sorting pass is
     * free to leave them in either relative order. A weaker "correctly sorted" check is the
     * right level of assertion here; the exact-match tests above use hand-picked strings with
     * no such ties.
     */
    private static void assertSorted(final String[] names) {
        final String[] actual = Arrays.copyOf(names, names.length);
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.chineseEncoderPinyin, false, false);
        sorter.sort(actual);

        for (int i = 1; i < actual.length; i++)
            assertTrue(actual[i - 1] + " should not come after " + actual[i],
                    HuskyCoderChinesePinyin.NAME_ORDER.compare(actual[i - 1], actual[i]) <= 0);
    }
}
