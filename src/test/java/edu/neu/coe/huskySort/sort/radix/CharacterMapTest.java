package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import org.junit.Before;
import org.junit.Test;

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

    CharacterMap characterMap;
}