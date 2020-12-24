package edu.neu.coe.huskySort.sort.simple;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class RadixSortTest {

    @Test
    public void sort() {
        Integer[] input = {181, 51, 11, 33, 11, 39, 60, 2, 27, 24, 12};
        Integer[] output = new RadixSort<Integer>(10, (x, p) -> x / p).sort(input);
        Integer[] expected = {2, 11, 11, 12, 24, 27, 33, 39, 51, 60, 181};
        assertArrayEquals(expected, output);
    }
}