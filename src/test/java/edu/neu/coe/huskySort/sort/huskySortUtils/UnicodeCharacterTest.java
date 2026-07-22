package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnicodeCharacterTest {

    @BeforeClass
    public static void setUp() {
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void compareTo() {
        final String x = "阿朝";
        final UnicodeCharacter unicodeCharacter1 = new ChineseCharacter(x.charAt(0));
        final UnicodeCharacter unicodeCharacter2 = new ChineseCharacter(x.charAt(1));
        final int cf = unicodeCharacter1.compareTo(unicodeCharacter2);
        assertEquals(-1, cf);
    }

}