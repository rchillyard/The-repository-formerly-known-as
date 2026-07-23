package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HanyuPinyinSyllablesTest {

    @Test
    public void testSize() {
        assertEquals(395, HanyuPinyinSyllables.size());
        assertEquals(395, HanyuPinyinSyllables.SYLLABLES.length);
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
        assertEquals(0, HanyuPinyinSyllables.ordinalOf("ai"));
        assertEquals(394, HanyuPinyinSyllables.ordinalOf("zuo"));
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
