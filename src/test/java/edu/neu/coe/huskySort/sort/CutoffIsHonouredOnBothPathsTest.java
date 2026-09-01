package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import edu.neu.coe.huskySort.sort.radix.BasicCountingSortHelper;
import edu.neu.coe.huskySort.sort.radix.InstrumentedCountingSortHelper;
import edu.neu.coe.huskySort.sort.radix.UnicodeString;
import edu.neu.coe.huskySort.util.Config;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Guards against the defect that produced a wrong row in the INFO6205 book's Table 8.1.
 * <p>
 * There, {@code MSDCutoff()} was overridden only on the instrumented Helper. A timing run is
 * uninstrumented, so every timed benchmark silently used the default instead of the configured
 * value — and a table captioned "256 cutoff" was in fact measured at 20.
 * <p>
 * This repository had the same shape on both of its helper hierarchies. It no longer does: the
 * uninstrumented helpers read {@code [helper] cutoff} too, and every helper a benchmark obtains is
 * given the configuration. What follows pins that down, because a cutoff that reaches only the
 * instrumented path is invisible — the counts move and the timings do not.
 */
public class CutoffIsHonouredOnBothPathsTest {

    /**
     * The comparison path, through the factory that every such sort actually uses.
     */
    @Test
    public void comparisonHelpersAgreeOnTheConfiguredCutoff() throws IOException {
        final Config config = Config.load().copy(HELPER, CUTOFF, "64");
        assertEquals("the instrumented helper reads the configuration",
                64, HelperFactory.create(DESCRIPTION, N, true, config).getCutoff());
        assertEquals("so must the uninstrumented one, which is what a timed benchmark gets",
                64, HelperFactory.create(DESCRIPTION, N, false, config).getCutoff());
    }

    /**
     * The counting path, used by UnicodeMSDStringSort.
     */
    @Test
    public void countingHelpersAgreeOnTheConfiguredCutoff() throws IOException {
        final Config config = Config.load().copy(HELPER, CUTOFF, "64");
        assertEquals("the instrumented helper reads the configuration",
                64, new InstrumentedCountingSortHelper<UnicodeString, UnicodeCharacter>(DESCRIPTION, N, config).getCutoff());
        assertEquals("so must the uninstrumented one, which is what a timed benchmark gets",
                64, new BasicCountingSortHelper<UnicodeString, UnicodeCharacter>(DESCRIPTION, N, config).getCutoff());
    }

    /**
     * With no cutoff configured, every helper falls back to the same default. A constructor given no
     * Config behaves as though the cutoff were unset, which is how the older call sites still work.
     */
    @Test
    public void allHelpersShareOneDefault() throws IOException {
        final Config config = Config.load();
        assertEquals(DEFAULT_CUTOFF, HelperFactory.create(DESCRIPTION, N, true, config).getCutoff());
        assertEquals(DEFAULT_CUTOFF, HelperFactory.create(DESCRIPTION, N, false, config).getCutoff());
        assertEquals(DEFAULT_CUTOFF, new ComparableSortHelper<Integer>(DESCRIPTION, N).getCutoff());
        assertEquals(DEFAULT_CUTOFF, new BasicCountingSortHelper<UnicodeString, UnicodeCharacter>(DESCRIPTION, N).getCutoff());
    }

    private static final String HELPER = "helper";
    private static final String CUTOFF = "cutoff";
    private static final String DESCRIPTION = "test";
    private static final int N = 100;
    private static final int DEFAULT_CUTOFF = 7;
}
