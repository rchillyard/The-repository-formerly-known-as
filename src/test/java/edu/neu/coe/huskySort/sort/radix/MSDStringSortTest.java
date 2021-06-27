package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MSDStringSortTest {

    //    @Test
    public void sort() {
        final String[] input = "she sells seashells by the seashore the shells she sells are surely seashells".split(" ");
        final String[] expected = "are by seashells seashells seashore sells sells she she shells surely the the".split(" ");

        MSDStringSort msdStringSort = new MSDStringSort(Alphabet.ASCII);
        msdStringSort.sort(input);
        assertArrayEquals(expected, input);
    }

    //    @Test
    public void sort1() {
        final Helper<String> helper = new BaseHelper<>("test", 1000, 1L);
        final String[] words = HuskySortBenchmarkHelper.getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        final String[] xs = helper.random(String.class, r -> words[r.nextInt(words.length)]);
        assertEquals(1000, xs.length);
        MSDStringSort msdStringSort = new MSDStringSort(Alphabet.ASCII);
        msdStringSort.sort(xs);
        assertEquals("African-American", xs[0]);
        assertEquals("Palestinian", xs[16]);
    }

    //    @Test
    public void sort2() {
        final Helper<String> helper = new BaseHelper<>("test", 1000, 1L);
        final String[] words = HuskySortBenchmarkHelper.getWords("3000-common-words.txt", HuskySortBenchmark::lineAsList);
        final String[] xs = helper.random(String.class, r -> words[r.nextInt(words.length)]);
        assertEquals(1000, xs.length);
        MSDStringSort msdStringSort = new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE));
        msdStringSort.sort(xs);
        assertEquals("African-American", xs[0]);
        assertEquals("Palestinian", xs[16]);
    }

    //    @Test
    public void sortWithExtendedAscii() {
        final String[] input = ("Le renard brun rapide saute par-dessus le chien paresseux chacó chacra cháchara cántara cantar caña cana canal canapé cañón día desayuno ").split(" ");
        MSDStringSort msdStringSort = new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE));
        msdStringSort.sort(input);
        Alphabet alphabet = msdStringSort.getAlphabet();
        assertTrue(new BaseHelper<String>("sortWithUnicode").sorted(input));
    }

    //    @Test
    public void sortWithUnicode() {
        final String[] input = "python.txt\t狗.txt\t\t羊.txt\t\t鸡.txt\t\t兔子.txt\t河马.txt\t猴子.txt\t豹子.txt\t眼镜蛇.txt\n熊.txt\t\t猪.txt\t\t蛇.txt\t\t鹅.txt\t\t大象.txt\t熊猫.txt\t老虎.txt\t骆驼.txt\n牛.txt\t\t猫.txt\t\t马.txt\t\t龙.txt\t\t斑马.txt\t狮子.txt\t老鼠.txt\t鳄鱼.txt".split("\\s+");
        System.out.println(Arrays.toString(input));
        MSDStringSort.setCutoff(1);
        MSDStringSort msdStringSort = new MSDStringSort(new Alphabet(Alphabet.RADIX_UNICODE));
        msdStringSort.sort(input);
        Alphabet alphabet = msdStringSort.getAlphabet();
        System.out.println(alphabet);
        System.out.println(Arrays.toString(input));
        boolean sorted = new BaseHelper<String>("sortWithUnicode").sorted(input);
        assertTrue(sorted);
    }
}