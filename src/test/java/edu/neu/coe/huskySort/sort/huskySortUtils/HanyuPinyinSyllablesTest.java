package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HanyuPinyinSyllablesTest {

    @Test
    public void testSize() {
        assertEquals(413, HanyuPinyinSyllables.size());
        assertEquals(413, HanyuPinyinSyllables.SYLLABLES.length);
    }

    /**
     * Regression test for a real bug found 2026-08-05: the original table silently dropped the
     * entire bare "-a" final column (18 syllables, one per compatible initial). Discovered
     * indirectly, via a pinyin-aware sorter with no cleanup pass producing a visibly wrong order
     * on real corpus data. Checks every one of the 18 missing syllables is present and correctly
     * ordered relative to its "-ai"/"-an" sibling, not just that the total count is right.
     */
    @Test
    public void testPreviouslyMissingBareAFinalSyllables() {
        final String[] missingWereFound = {"a", "ba", "ca", "cha", "da", "fa", "ga", "ha", "ka", "la", "ma", "na", "pa", "sa", "sha", "ta", "za", "zha"};
        for (final String syllable : missingWereFound)
            assertTrue(syllable + " should now be a recognized syllable", HanyuPinyinSyllables.ordinalOf(syllable) >= 0);
        // Each "-a" syllable must sort immediately before its same-initial "-ai" (or "-an" for
        // "fa", which has no "fai") sibling, per pinyin alphabetical order (shorter prefix first).
        assertTrue(HanyuPinyinSyllables.ordinalOf("a") < HanyuPinyinSyllables.ordinalOf("ai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ba") < HanyuPinyinSyllables.ordinalOf("bai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ca") < HanyuPinyinSyllables.ordinalOf("cai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("cha") < HanyuPinyinSyllables.ordinalOf("chai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("da") < HanyuPinyinSyllables.ordinalOf("dai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("fa") < HanyuPinyinSyllables.ordinalOf("fan"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ga") < HanyuPinyinSyllables.ordinalOf("gai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ha") < HanyuPinyinSyllables.ordinalOf("hai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ka") < HanyuPinyinSyllables.ordinalOf("kai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("la") < HanyuPinyinSyllables.ordinalOf("lai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ma") < HanyuPinyinSyllables.ordinalOf("mai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("na") < HanyuPinyinSyllables.ordinalOf("nai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("pa") < HanyuPinyinSyllables.ordinalOf("pai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("sa") < HanyuPinyinSyllables.ordinalOf("sai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("sha") < HanyuPinyinSyllables.ordinalOf("shai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ta") < HanyuPinyinSyllables.ordinalOf("tai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("za") < HanyuPinyinSyllables.ordinalOf("zai"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("zha") < HanyuPinyinSyllables.ordinalOf("zhai"));
        // Confirmed non-existent combinations correctly remain absent (j/q/x never combine with
        // bare "a"; "ra" is not a standard Mandarin syllable).
        for (final String notASyllable : new String[]{"ja", "qa", "xa", "ra"})
            assertEquals(notASyllable + " should not be a recognized syllable", -1, HanyuPinyinSyllables.ordinalOf(notASyllable));
    }

    @Test
    public void testNoDuplicates() {
        final Set<String> distinct = new HashSet<>();
        for (final String s : HanyuPinyinSyllables.SYLLABLES) distinct.add(s);
        assertEquals(HanyuPinyinSyllables.SYLLABLES.length, distinct.size());
    }

    @Test
    public void testArrayIsSorted() {
        for (int i = 1; i < HanyuPinyinSyllables.SYLLABLES.length; i++) {
            final String prev = HanyuPinyinSyllables.SYLLABLES[i - 1];
            final String curr = HanyuPinyinSyllables.SYLLABLES[i];
            assertTrue(prev + " should sort before " + curr, HanyuPinyinSyllables.ORDER.compare(prev, curr) < 0);
        }
    }

    @Test
    public void testOrdinalMatchesArrayIndex() {
        for (int i = 0; i < HanyuPinyinSyllables.SYLLABLES.length; i++)
            assertEquals(HanyuPinyinSyllables.SYLLABLES[i], i, HanyuPinyinSyllables.ordinalOf(HanyuPinyinSyllables.SYLLABLES[i]));
    }

    @Test
    public void testFirstAndLast() {
        assertEquals(0, HanyuPinyinSyllables.ordinalOf("a"));
        assertEquals(412, HanyuPinyinSyllables.ordinalOf("zuo"));
    }

    @Test
    public void testUnknownSyllable() {
        assertEquals(-1, HanyuPinyinSyllables.ordinalOf("notasyllable"));
        assertEquals(-1, HanyuPinyinSyllables.ordinalOf(""));
    }

    /**
     * "lu" before "lü", but the whole "lu"-extension subtree (luan/lun/luo) sorts before bare
     * "lü" too -- see HanyuPinyinSyllables' class javadoc for why.
     */
    @Test
    public void testLuBeforeLuUmlaut() {
        assertTrue(HanyuPinyinSyllables.ordinalOf("lu") < HanyuPinyinSyllables.ordinalOf("lü"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("luan") < HanyuPinyinSyllables.ordinalOf("lü"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("luo") < HanyuPinyinSyllables.ordinalOf("lü"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("lü") < HanyuPinyinSyllables.ordinalOf("lüe"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("lüe") < HanyuPinyinSyllables.ordinalOf("m"));
    }

    @Test
    public void testNuBeforeNuUmlaut() {
        assertTrue(HanyuPinyinSyllables.ordinalOf("nuo") < HanyuPinyinSyllables.ordinalOf("nü"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("nü") < HanyuPinyinSyllables.ordinalOf("nüe"));
    }

    @Test
    public void testEBeforeECircumflex() {
        assertTrue(HanyuPinyinSyllables.ordinalOf("e") < HanyuPinyinSyllables.ordinalOf("ê"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("er") < HanyuPinyinSyllables.ordinalOf("ê"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("ê") < HanyuPinyinSyllables.ordinalOf("fan"));
    }

    @Test
    public void testCommonSurnameSyllablesOrdering() {
        // zhang < zhao < zhou < zhu (surnames 张/趙/周/朱), sanity-checking real names sort correctly.
        assertTrue(HanyuPinyinSyllables.ordinalOf("zhang") < HanyuPinyinSyllables.ordinalOf("zhao"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("zhao") < HanyuPinyinSyllables.ordinalOf("zhou"));
        assertTrue(HanyuPinyinSyllables.ordinalOf("zhou") < HanyuPinyinSyllables.ordinalOf("zhu"));
    }
}
