/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class PureDualPivotQuicksortTest {

    @Test
    public void testSort() throws Exception {
        Integer[] xs = new Integer[4];
        xs[0] = 3;
        xs[1] = 4;
        xs[2] = 2;
        xs[3] = 1;
        PureDualPivotQuicksort.sort(xs);
        assertEquals(Integer.valueOf(1), xs[0]);
        assertEquals(Integer.valueOf(2), xs[1]);
        assertEquals(Integer.valueOf(3), xs[2]);
        assertEquals(Integer.valueOf(4), xs[3]);
    }

}