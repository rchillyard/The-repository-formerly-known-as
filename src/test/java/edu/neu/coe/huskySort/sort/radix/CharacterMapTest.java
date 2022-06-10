package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

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
        assertEquals("", 何昕.charAt(2).alt());
        assertEquals(codeNull, 何昕.charAt(2).encode());
        assertEquals(1, Long.compare(codeXin1, codeNull));
        assertEquals(0, 何欣蔚.compare(何昕, 0));
        assertEquals(0, 何欣蔚.compare(何昕, 1));
        assertEquals(1, 何欣蔚.compare(何昕, 2));
        final Comparator<String> stringComparator = characterMap.stringComparator;
        assertEquals(1, stringComparator.compare("何欣蔚", "何昕"));
    }

    CharacterMap characterMap;
}