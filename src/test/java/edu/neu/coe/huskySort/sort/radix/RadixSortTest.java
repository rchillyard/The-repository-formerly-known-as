package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.radix.RadixSort;
import edu.neu.coe.huskySort.sort.radix.RadixSortIntegral;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class RadixSortTest {

    @Test
    public void sortIntegers() {
        Integer[] input = {181, 51, 11, 33, 11, 39, 60, 2, 27, 24, 12};
        final Integer[] output = new RadixSortIntegral<Integer>(10).sort(input);
        Integer[] expected = {2, 11, 11, 12, 24, 27, 33, 39, 51, 60, 181};
        assertArrayEquals(expected, output);
    }

    //    @Test
    public void sortHex() {
        final String[] input = {"A1", "C2", "7E", "3D", "50"};
        final String[] output = new RadixSort<String>(16, p -> p + 1, RadixSort::getHexBucket).sort(input);
        final String[] expected = {"3D", "50", "7E", "A1", "C2"};
        assertArrayEquals(expected, output);
    }

    //    @Test
    public void sortEnglish() {
        final String[] input = {"Romeo", "Echo", "Xray", "Bravo", "Golf", "Alpha"};
        final String[] output = new RadixSort<String>(64, p -> p + 1, RadixSort::getEnglishBucket).sort(input);
        final String[] expected = {"Alpha", "Bravo", "Echo", "Golf", "Romeo", "Xray"};
        assertArrayEquals(expected, output);
    }
}