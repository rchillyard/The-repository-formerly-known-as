package edu.neu.coe.huskySort.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static edu.neu.coe.huskySort.util.Utilities.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class UtilitiesTest {

    @Test
    public void testLg() {
        assertEquals(0.0, lg(1), 1E-7);
        assertEquals(1.0, lg(2), 1E-7);
        assertEquals(0.5, lg(Math.sqrt(2)), 1E-7);
    }

    @Test
    public void testAsArray() {
        List<Double> array = new ArrayList<>();
        array.add(1.0);
        array.add(2.0);
        Double[] doubles = asArray(array);
        assertArrayEquals(new Double[]{1.0, 2.0}, doubles);
    }

    @Test
    public void testFormatDecimal3Places() {
        assertEquals("3.142", formatDecimal3Places(Math.PI));
    }

    @Test
    public void testFormatWhole() {
        assertEquals("42", formatWhole(42));
    }

    @Test
    public void testAsInt() {
        assertEquals("3", asInt(Math.PI));
    }

    @Test
    public void testRound() {
        assertEquals(3, round(Math.PI));
    }

    @Test
    public void testFillRandomArray() {
        // TODO fill this in later
    }

    @Test
    public void testIsSorted() {
        // TODO fill this in later
    }

    @Test
    public void testCheckSorted() {
        // TODO fill this in later
    }
}
