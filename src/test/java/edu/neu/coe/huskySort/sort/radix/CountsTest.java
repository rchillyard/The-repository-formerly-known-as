package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CountsTest {


    @Before
    public void before() {
        counts = new Counts();
        characterMap = new CharacterMap(ChineseCharacter::new);
    }

    @Test
    public void get() {
        final int i = counts.get(characterMap.get('阿'));
        assertEquals(0, i);
    }

    @Test
    public void increment() {
        final int i = counts.get(characterMap.get('阿'));
        assertEquals(0, i);
        counts.increment(characterMap.get('阿'));
        final int j = counts.get(characterMap.get('阿'));
        assertEquals(1, j);
    }

    private Counts counts;
    private CharacterMap characterMap;
}