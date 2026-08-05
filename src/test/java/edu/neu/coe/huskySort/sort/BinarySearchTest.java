package edu.neu.coe.huskySort.sort;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BinarySearchTest {

    @Test
    public void testSequence() {
        Integer[] xs = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        assertEquals(2, BinarySearch.binarySearch(xs, 0, xs.length, 3, Integer::compare));
    }

    @Test
    public void testMissingTooLarge() {
        Integer[] xs = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        // 11 is not present; insertion point is xs.length (9), so the result is -(9) - 1 = -10.
        assertEquals(-10, BinarySearch.binarySearch(xs, 0, xs.length, 11, Integer::compare));
    }

    @Test
    public void testSingletonArray() {
        Integer[] xs = {1};
        assertEquals(0, BinarySearch.binarySearch(xs, 0, xs.length, 1, Integer::compare));
    }

    @Test
    public void testEmptyArray() {
        Integer[] xs = {};
        // insertion point is 0, so the result is -(0) - 1 = -1.
        assertEquals(-1, BinarySearch.binarySearch(xs, 0, xs.length, 1, Integer::compare));
    }

    @Test
    public void testIgnoreFirstHalf() {
        Integer[] ar = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        // Searching only within [4, 9) for 3 (which is outside that range): insertion point is
        // 4 (the first index of interest), so the result is -(4) - 1 = -5.
        assertEquals(-5, BinarySearch.binarySearch(ar, 4, ar.length, 3, Integer::compare));
        assertEquals(6, BinarySearch.binarySearch(ar, 4, ar.length, 7, Integer::compare));
    }

}
