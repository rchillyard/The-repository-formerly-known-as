/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import org.junit.Test;

import java.io.IOException;

import static edu.neu.coe.huskySort.util.Config.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link Config#shouldRecurse}, the single place the cutoff comparison is written.
 * Added 2026-09-30, when an audit of this project and INFO6205 found the same question asked in
 * four different spellings, two of them off by one and one of them non-terminating.
 */
public class ShouldRecurseTest {

    /**
     * The convention: a cutoff of k means ranges of up to k elements stop.
     */
    @Test
    public void aCutoffOfKStopsAtK() {
        for (int cutoff = 1; cutoff <= 20; cutoff++) {
            assertFalse("a range of exactly the cutoff must stop", shouldRecurse(cutoff, cutoff));
            assertTrue("one more than the cutoff must recurse", shouldRecurse(cutoff + 1, cutoff));
            assertFalse("and anything smaller must stop", shouldRecurse(cutoff - 1, cutoff));
        }
    }

    @Test
    public void aCutoffOfOneTurnsTheMechanismOff() {
        assertFalse(shouldRecurse(0, 1));
        assertFalse(shouldRecurse(1, 1));
        assertTrue(shouldRecurse(2, 1));
        assertTrue(shouldRecurse(1000, 1));
    }

    /**
     * <b>The reason this is a method rather than an expression.</b> A cutoff of 0 or less would
     * let a range of one element recurse, and a one-element range splits into an empty half and
     * itself, so the recursion never ends. Measured before the floor existed: {@code
     * MSDStringSort.setCutoff(0)} ended every sort in {@code StackOverflowError}, and INFO6205's
     * ParSort ran forever at 0 and at 1.
     */
    @Test
    public void noCutoffValueCanProduceAnInfiniteRecursion() {
        for (final int cutoff : new int[]{0, -1, -100, Integer.MIN_VALUE}) {
            assertFalse("a single element must never recurse, whatever the cutoff", shouldRecurse(1, cutoff));
            assertFalse("nor an empty range", shouldRecurse(0, cutoff));
            assertTrue("but two elements still must", shouldRecurse(2, cutoff));
        }
    }

    /**
     * The Config overload reads {@code helper.cutoff}, with 0 or less meaning "unset". NOTE that
     * config.ini leaves the entry empty, which reads as 0, so this is the ordinary path rather
     * than an edge case.
     */
    @Test
    public void theConfigOverloadResolvesAnUnsetCutoffToTheDefault() throws IOException {
        final Config config = Config.load();
        assertEquals("config.ini leaves cutoff empty", CUTOFF_DEFAULT, getCutoff(config));
        assertEquals(CUTOFF_DEFAULT, getCutoff(config.copy(HELPER, CUTOFF, "0")));
        assertEquals(CUTOFF_DEFAULT, getCutoff(null));
        assertEquals(12, getCutoff(config.copy(HELPER, CUTOFF, "12")));
        assertFalse(shouldRecurse(12, config.copy(HELPER, CUTOFF, "12")));
        assertTrue(shouldRecurse(13, config.copy(HELPER, CUTOFF, "12")));
    }

    /**
     * The default must agree with what the helpers return when nothing is configured, or a
     * caller holding a Config would get a different answer from one holding a Helper.
     */
    @Test
    public void theDefaultAgreesWithTheHelpers() {
        assertEquals(new edu.neu.coe.huskySort.sort.ComparableSortHelper<Integer>("x").getCutoff(), CUTOFF_DEFAULT);
    }
}
