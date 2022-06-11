package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CharacterMapTest {

    @Before
    public void before() {
        characterMap = new CharacterMap(ChineseCharacter::new);
    }

    @Test
    public void get() {
        final String x = "阿朝";
        final UnicodeCharacter unicodeCharacter0 = characterMap.get(x.charAt(0));
        final UnicodeCharacter unicodeCharacter1 = characterMap.get(x.charAt(1));
        final int size = characterMap.size();
        assertEquals(2, size);
        assertEquals(unicodeCharacter0, characterMap.get(x.charAt(0)));
        assertEquals(unicodeCharacter1, characterMap.get(x.charAt(1)));
    }

    @Test
    public void keySet() {
        final String x = "阿朝";
        final UnicodeCharacter unicodeCharacter0 = characterMap.get(x.charAt(0));
        final UnicodeCharacter unicodeCharacter1 = characterMap.get(x.charAt(1));
        final Set<Character> characters = characterMap.keySet();
        final int size = characters.size();
        assertEquals(2, size);
        for (final Character character : characters) {
            System.out.println(character + ": " + characterMap.get(character));
        }
    }


    @Test
    public void testComparison1() {
        final CharacterMap.UnicodeString 卞燕燕 = characterMap.getUnicodeString("卞燕燕");// XXX bian4 yan4 yan4
        final CharacterMap.UnicodeString 卞艳红 = characterMap.getUnicodeString("卞艳红");// XXX bian4 yan4 hong2
        final long codeYan4 = 0xE61BA0D00000000L;
        final long codeHong2 = 0xA2FBA7832000000L;
        final String bian4 = "bian 4";
        final String yan4 = "yan 4";
        assertEquals(bian4, 卞燕燕.charAt(0).alt());
        assertEquals(0x8A986E834000000L, 卞燕燕.charAt(0).encode());
        assertEquals(yan4, 卞燕燕.charAt(1).alt());
        assertEquals(codeYan4, 卞燕燕.charAt(1).encode());
        assertEquals(bian4, 卞艳红.charAt(0).alt());
        assertEquals(0x8A986E834000000L, 卞艳红.charAt(0).encode());
        assertEquals(yan4, 卞艳红.charAt(1).alt());
        assertEquals(codeYan4, 卞艳红.charAt(1).encode());
        assertEquals("hong 2", 卞艳红.charAt(2).alt());
        assertEquals(codeHong2, 卞艳红.charAt(2).encode());
        assertEquals(1, Long.compare(codeYan4, codeHong2));
        assertEquals(0, 卞燕燕.compare(卞艳红, 0));
        assertEquals(0, 卞燕燕.compare(卞艳红, 1));
        assertEquals(1, 卞燕燕.compare(卞艳红, 2));
        final Comparator<String> stringComparator = characterMap.stringComparator;
        assertEquals(1, stringComparator.compare("卞燕燕", "卞艳红"));
    }

    @Test
    public void testComparison2() {
        final CharacterMap.UnicodeString 何欣蔚 = characterMap.getUnicodeString("何欣蔚"); // XXX he2 xin1 yu4
        final CharacterMap.UnicodeString 何昕 = characterMap.getUnicodeString("何昕"); // XXX he2 xin1
        final long codeXin1 = 0xE29BA0C40000000L;
        final long codeNull = 0L;
        final String he2 = "he 2";
        final String xin1 = "xin 1";
        assertEquals(he2, 何欣蔚.charAt(0).alt());
        assertEquals(0xA25832000000000L, 何欣蔚.charAt(0).encode());
        assertEquals(xin1, 何欣蔚.charAt(1).alt());
        assertEquals(codeXin1, 何欣蔚.charAt(1).encode());
        assertEquals(he2, 何昕.charAt(0).alt());
        assertEquals(0xA25832000000000L, 何昕.charAt(0).encode());
        assertEquals(xin1, 何昕.charAt(1).alt());
        assertEquals(codeXin1, 何昕.charAt(1).encode());
        assertEquals(" ", 何昕.charAt(2).alt());
        assertEquals(codeNull, 何昕.charAt(2).encode());
        assertEquals(1, Long.compare(codeXin1, codeNull));
        assertEquals(0, 何欣蔚.compare(何昕, 0));
        assertEquals(0, 何欣蔚.compare(何昕, 1));
        assertEquals(1, 何欣蔚.compare(何昕, 2));
        final Comparator<String> stringComparator = characterMap.stringComparator;
        assertEquals(1, stringComparator.compare("何欣蔚", "何昕"));
    }

    @Test
    public void testComparison3() {
        final String 卞佳丽 = "卞佳丽";
        final String 卞佳 = "卞佳";
        final CharacterMap.UnicodeString u卞佳丽 = characterMap.getUnicodeString(卞佳丽);// XXX bian4 jia1 li4
        final CharacterMap.UnicodeString u卞佳 = characterMap.getUnicodeString(卞佳);// XXX bian4 jia1
        final long codeJia1 = 0xAA9860C40000000L;
        final long codeHong2 = 0xA2FBA7832000000L;
        final String bian4 = "bian 4";
        final String jia1 = "jia 1";
        final long codeNull = 0L;
        assertEquals(bian4, u卞佳丽.charAt(0).alt());
        assertEquals(0x8A986E834000000L, u卞佳丽.charAt(0).encode());
        assertEquals(jia1, u卞佳丽.charAt(1).alt());
        assertEquals(codeJia1, u卞佳丽.charAt(1).encode());
        assertEquals(bian4, u卞佳.charAt(0).alt());
        assertEquals(0x8A986E834000000L, u卞佳.charAt(0).encode());
        assertEquals(jia1, u卞佳.charAt(1).alt());
        assertEquals(codeJia1, u卞佳.charAt(1).encode());
        assertEquals(" ", u卞佳.charAt(2).alt());
        assertEquals(codeNull, u卞佳.charAt(2).encode());
        assertEquals(1, Long.compare(codeJia1, codeHong2));
        assertEquals(0, u卞佳丽.compare(u卞佳, 0));
        assertEquals(0, u卞佳丽.compare(u卞佳, 1));
        assertEquals(1, u卞佳丽.compare(u卞佳, 2));
        assertEquals(1, characterMap.stringComparator.compare(卞佳丽, 卞佳));
        assertEquals(1, characterMap.stringComparatorPinyin.compare(卞佳丽, 卞佳));
    }

    @Test
    public void testComparison4() {
        final String 王略 = "王略";
        final String 王卢城 = "王卢城";
        final CharacterMap.UnicodeString u王略 = characterMap.getUnicodeString(王略);// XXX wang2 lu: e4
        final CharacterMap.UnicodeString u王卢城 = characterMap.getUnicodeString(王卢城);// XXX wang2 lu2 cheng2
        final long codeLue4 = 0xB35FA5834000000L;
        final long codeLu2 = 0xA2FBA7832000000L;
        final String wang2 = "wang 2";
        final String lue4 = "lu~e 4";
        final long codeNull = 0L;
        assertEquals(wang2, u王略.charAt(0).alt());
        assertEquals(0xDE1BA7832000000L, u王略.charAt(0).encode());
        assertEquals(lue4, u王略.charAt(1).alt());
        final long encode = u王略.charAt(1).encode();
        assertEquals(codeLue4, encode);
        assertEquals(wang2, u王卢城.charAt(0).alt());
        assertEquals(0xDE1BA7832000000L, u王卢城.charAt(0).encode());
        assertEquals("lu 2", u王卢城.charAt(1).alt());
        assertEquals(0xB35832000000000L, u王卢城.charAt(1).encode());
        assertEquals("cheng 2", u王卢城.charAt(2).alt());
        assertEquals(0x8E896E9E0C80000L, u王卢城.charAt(2).encode());
        assertEquals(1, Long.compare(codeLue4, codeLu2));
        assertEquals(0, u王略.compare(u王卢城, 0));
        assertEquals(1, u王略.compare(u王卢城, 1));
        assertEquals(-1, u王略.compare(u王卢城, 2));
        assertEquals(1, characterMap.stringComparator.compare(王略, 王卢城));
        assertEquals(94, characterMap.stringComparatorPinyin.compare(王略, 王卢城));
    }

    @Test
    public void testComparisonP1() {
        final Comparator<String> stringComparator = characterMap.stringComparatorPinyin;
        assertTrue(stringComparator.compare("卞燕燕", "卞艳红") > 0);
    }

    @Test
    public void testComparisonP2() {
        final Comparator<String> stringComparator = characterMap.stringComparatorPinyin;
        assertTrue(stringComparator.compare("何欣蔚", "何昕") > 0);
    }

    CharacterMap characterMap;
}