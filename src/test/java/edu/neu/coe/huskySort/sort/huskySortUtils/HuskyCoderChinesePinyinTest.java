package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import edu.neu.coe.huskySort.sort.huskySort.PureHuskySort;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
    public void testSortRealCorpusNamesMatchesComparatorOracle() {
        final String[] sample = sampleNames(new Random(1), 3000);
        assertMatchesOracle(sample);
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
            assertMatchesOracle(sampleNames(random, n));
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
        assertMatchesOracle(names);
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

    /**
     * True homonyms -- identical syllable AND tone, e.g. 郗/奚 (both "xi1", found by the corpus
     * stress test above) -- have no stroke-count data available to break the tie properly, so
     * NAME_ORDER falls back to Unicode code point. This confirms that fallback makes the
     * comparison deterministic (nonzero) rather than leaving true homonyms as a 0 (tied) result.
     */
    @Test
    public void testHomonymFallsBackToCodePointOrder() {
        assertTrue(HuskyCoderChinesePinyin.NAME_ORDER.compare("郗", "奚") != 0);
        assertEquals(Character.compare('郗', '奚'), HuskyCoderChinesePinyin.NAME_ORDER.compare("郗", "奚"));
        assertMatchesOracle(new String[]{"奚飞", "郗飞"});
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
     * NAME_ORDER now includes a Unicode-code-point fallback for true homonyms (identical
     * syllable and tone), making it a genuine strict total order over distinct strings -- so,
     * unlike an earlier version of this test, an exact permutation match against the oracle
     * (rather than merely "correctly sorted") is the right, stronger assertion, even though
     * the first sorting pass is unstable: with no remaining ties, that first pass cannot
     * produce a different final order once the deterministic cleanup pass runs.
     */
    private static void assertMatchesOracle(final String[] names) {
        final String[] expected = Arrays.copyOf(names, names.length);
        Arrays.sort(expected, HuskyCoderChinesePinyin.NAME_ORDER);

        final String[] actual = Arrays.copyOf(names, names.length);
        final PureHuskySort<String> sorter = new PureHuskySort<>(HuskyCoderFactory.chineseEncoderPinyin, false, false);
        sorter.sort(actual);

        assertArrayEquals(expected, actual);
    }
}
