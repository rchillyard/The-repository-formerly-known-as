package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.*;

/**
 * The rank dialect claims to be exactly order-preserving, which is a much stronger claim than any
 * other coder in this project makes, so it is tested as such: not "mostly sorted", but that sorting
 * by the code alone reproduces {@link HuskyCoderChinesePinyin#NAME_ORDER} exactly, over the whole
 * corpus. See TODO.md item 44.
 */
public class HuskyCoderChinesePinyinRankTest {

    private static final HuskyCoder<String> RANK = HuskyCoderFactory.chineseEncoderPinyinRank;
    private static final Comparator<String> ORDER = HuskyCoderChinesePinyin.NAME_ORDER;

    /**
     * The claim, on real data: sort a million names by husky code alone, and NAME_ORDER must find
     * no descent anywhere. This is what lets the coder report perfect and the cleanup pass be
     * skipped, so if it ever fails the sort is silently wrong rather than merely slow.
     */
    @Test
    public void testCodeOrderReproducesNameOrderOverTheCorpus() {
        final String[] names = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        assertTrue("corpus should be large", names.length > 1_000_000);
        final String[] byCode = Arrays.copyOf(names, names.length);
        Arrays.sort(byCode, Comparator.comparingLong(RANK::huskyEncode));
        for (int i = 1; i < byCode.length; i++)
            if (ORDER.compare(byCode[i - 1], byCode[i]) > 0)
                fail("descent at " + i + ": " + byCode[i - 1] + " then " + byCode[i]);
    }

    /**
     * Distinct names must get distinct codes -- the ordinal coder gives 1.40 names per code, which
     * is the defect this dialect exists to remove.
     */
    @Test
    public void testDistinctNamesGetDistinctCodes() {
        final String[] names = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final java.util.Set<Long> codes = new java.util.HashSet<>();
        final java.util.Set<String> distinct = new java.util.HashSet<>();
        for (final String s : names) { codes.add(RANK.huskyEncode(s)); distinct.add(s); }
        assertEquals("every distinct name should have its own code", distinct.size(), codes.size());
    }

    /**
     * True homonyms are exactly what the ordinal coder cannot separate; the rank coder must.
     */
    @Test
    public void testTrueHomonymsAreSeparated() {
        // 郗 and 奚 are both xi1; 滨 and 斌 are both bin1.
        assertNotEquals(RANK.huskyEncode("郗"), RANK.huskyEncode("奚"));
        assertNotEquals(RANK.huskyEncode("阿滨"), RANK.huskyEncode("阿斌"));
        assertEquals(HuskyCoderFactory.chineseEncoderPinyin.huskyEncode("阿滨"),
                HuskyCoderFactory.chineseEncoderPinyin.huskyEncode("阿斌"));
        assertEquals(Integer.signum(ORDER.compare("阿滨", "阿斌")),
                Integer.signum(Long.compare(RANK.huskyEncode("阿滨"), RANK.huskyEncode("阿斌"))));
    }

    /**
     * A prefix must sort before the string that extends it, which is what reserving rank 0 for
     * padding buys.
     */
    @Test
    public void testPrefixSortsFirst() {
        assertTrue(RANK.huskyEncode("阿鹭") < RANK.huskyEncode("阿鹭鹭"));
        assertEquals(Integer.signum(ORDER.compare("阿鹭", "阿露露")),
                Integer.signum(Long.compare(RANK.huskyEncode("阿鹭"), RANK.huskyEncode("阿露露"))));
    }

    /**
     * Perfection is claimed per element and must go false exactly when the encoding stops being
     * exact: a fifth character is not encoded at all, and a character outside the CJK block the
     * table covers takes rank 0 and ties with padding. Getting this wrong skips a cleanup pass that
     * was needed, so it is the most dangerous line in the class.
     */
    @Test
    public void testPerfectIsClaimedOnlyWhenItHolds() {
        assertTrue(RANK.huskyEncode(new String[]{"刘持平", "洪文胜", "樊辉辉", "王诗卉"}).perfect);
        assertTrue(RANK.huskyEncode(new String[]{"王", "王诗", "王诗卉", "王诗卉卉"}).perfect);
        assertFalse("five characters do not fit", RANK.huskyEncode(new String[]{"王诗卉卉卉"}).perfect);
        assertFalse("latin is outside the rank table", RANK.huskyEncode(new String[]{"王诗卉", "abc"}).perfect);
        assertFalse("mixed script is outside the rank table", RANK.huskyEncode(new String[]{"王诗A"}).perfect);
        // the ordinal dialect keeps its old behaviour: never perfect.
        assertFalse(HuskyCoderFactory.chineseEncoderPinyin.huskyEncode(new String[]{"刘持平"}).perfect);
    }

    /**
     * Out-of-block characters must still encode without throwing, and must still be ordered
     * consistently with each other, since the cleanup pass then runs and fixes the rest.
     */
    @Test
    public void testOutOfBlockCharactersDoNotThrow() {
        assertEquals(0L, RANK.huskyEncode(""));
        RANK.huskyEncode("abc");
        RANK.huskyEncode("Ａ１２");
        assertTrue(RANK.huskyEncode("王") > RANK.huskyEncode("abc"));
    }
}
