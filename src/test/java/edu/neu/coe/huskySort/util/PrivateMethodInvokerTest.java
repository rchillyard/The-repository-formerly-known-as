/*
 * Copyright (c) 2018. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrivateMethodInvokerTest {

    @SuppressWarnings("SameParameterValue")
    static
    class Mock {
        private final int x;
        private final double y;
        private final String z;

        Mock(int x, double y, String z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private double xTimesY() {
            return x * y;
        }

        private double xTimesYTimesW(Double w) {
            return x * y * w;
        }

        private double xTimesYTimesWPlusZ(double w, int z) {
            return x * y * w + z;
        }

        private double xTimesYTimesWPlusZ(Integer z, double w) {
            return x * y * w + z;
        }

    }

    @SuppressWarnings("SameReturnValue")
    private static int one() {
        return 1;
    }

    @Test
    public void testStatic() {
        final PrivateMethodInvoker invoker = new PrivateMethodInvoker(PrivateMethodInvokerTest.class);
        assertEquals(1, invoker.invokePrivate("one"));
    }

    /**
     * Test method for Mock
     */
    @Test
    public void testMock() {
        final Mock hello = new Mock(10, Math.PI, "Hello");
        final PrivateMethodInvoker invoker = new PrivateMethodInvoker(hello);
        assertEquals(10 * Math.PI, invoker.invokePrivate("xTimesY"));
        assertEquals(20 * Math.PI + 1, invoker.invokePrivate("xTimesYTimesWPlusZ", 2.0, 1));
        assertEquals(20 * Math.PI + 1, invoker.invokePrivate("xTimesYTimesWPlusZ", 1, 2.0));
        assertEquals(20 * Math.PI, invoker.invokePrivate("xTimesYTimesW", 2.0));
    }

    /**
     * Test method for Mock
     */
    @Test
    public void testMockExplicit() {
        final Mock hello = new Mock(10, Math.PI, "Hello");
        final PrivateMethodInvoker invoker = new PrivateMethodInvoker(hello);
        Class<?>[] xTimesYTimesWClasses = new Class<?>[]{Double.class};
        Class<?>[] xTimesYTimesWPlusZClasses1 = new Class<?>[]{double.class, int.class};
        Class<?>[] xTimesYTimesWPlusZClasses2 = new Class<?>[]{Integer.class, double.class};
        assertEquals(20 * Math.PI + 1, invoker.invokePrivateExplicit("xTimesYTimesWPlusZ", xTimesYTimesWPlusZClasses1, 2.0, 1));
        assertEquals(20 * Math.PI + 1, invoker.invokePrivateExplicit("xTimesYTimesWPlusZ", xTimesYTimesWPlusZClasses2, 1, 2.0));
        assertEquals(20 * Math.PI, invoker.invokePrivateExplicit("xTimesYTimesW", xTimesYTimesWClasses, 2.0));
    }
}
