package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChineseCharacterTest {

    @Test
    public void testEncode() {
        assertEquals(0x8A986E834000000L, new ChineseCharacter('卞').longCode);
        assertEquals(0x8A9834000000000L, new ChineseCharacter('毕').longCode);
    }

    @Test
    public void testAlt() {
        assertEquals("bian 4", new ChineseCharacter('卞').alt);
        assertEquals("bi 4", new ChineseCharacter('毕').alt);
    }

    @Test
    public void testCompare1() {
        final ChineseCharacter 鹿 = new ChineseCharacter('鹿'); // lu4
        final ChineseCharacter 绿 = new ChineseCharacter('绿'); // lu:4
        assertEquals(-1, 鹿.compareTo(绿));
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testConvertToPinyin() {
        final String 何欣蔚 = ChineseCharacter.convertToPinyin("何欣蔚");
        assertEquals("he 2xin 1yu 4", 何欣蔚);
    }

    @Test
    public void testParsePinyin0() {
        final String[] strings = ChineseCharacter.parsePinyin("bo 1");
        assertEquals(1, strings.length);
        assertEquals("b-o-1", strings[0]);
    }

    @Test
    public void testParsePinyin1() {
        final String[] strings = ChineseCharacter.parsePinyin("xin 1");
        assertEquals(1, strings.length);
        assertEquals("x-in-1", strings[0]);
    }

    @Test
    public void testParsePinyin3() {
        final String[] strings = ChineseCharacter.parsePinyin("he 2xin 1yu 4");
        assertEquals(3, strings.length);
        assertEquals("h-e-2", strings[0]);
        assertEquals("x-in-1", strings[1]);
        assertEquals("y-u-4", strings[2]);
    }
}