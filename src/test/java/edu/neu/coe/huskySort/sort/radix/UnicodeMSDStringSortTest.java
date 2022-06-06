package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class UnicodeMSDStringSortTest {

    static CharacterMap characterMap = new CharacterMap(ChineseCharacter::new, '阿');

    @BeforeClass
    public static void beforeClass() {
        characterMap = new CharacterMap(ChineseCharacter::new, '阿'); // NOTE: this is an attempt to ensure that the pinyin4j library gets initialized
    }

    @Test
    public void sort0() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        sorter.sort(new String[0]);
    }

    @Test
    public void sort1() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings = {"阿"};
        sorter.sort(strings);
        assertArrayEquals(new String[]{"阿"}, strings);
    }

    @Test
    public void sort2() {
        final UnicodeMSDStringSort sorter = new UnicodeMSDStringSort(characterMap);
        final String[] strings1 = {"阿", "朝"};
        sorter.sort(strings1);
        assertArrayEquals(new String[]{"阿", "朝"}, strings1);
        final String[] strings2 = {"朝", "阿"};
        sorter.sort(strings2);
        assertArrayEquals(new String[]{"阿", "朝"}, strings2);
    }

    @Test
    public void reset() {
    }

    @Test
    public void getCharacterMap() {
    }

    @Test
    public void setCutoff() {
    }


}