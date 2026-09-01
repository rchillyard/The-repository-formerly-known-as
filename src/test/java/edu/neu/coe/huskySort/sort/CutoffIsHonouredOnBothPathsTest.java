package edu.neu.coe.huskySort.sort;

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
 * This repository has the same shape. {@code ComparisonSortHelper.getCutoff()} is an interface
 * default returning a hardcoded 7; only {@link InstrumentedComparisonSortHelper} overrides it to read
 * {@code [helper] cutoff} from the configuration, and {@link ComparableSortHelper} has no constructor
 * that takes a {@code Config} at all, so the uninstrumented path cannot see one.
 * <p>
 * As shipped the two agree, because {@code cutoff} is empty in every config file and the instrumented
 * override falls through to the same 7. So this is a trap rather than a live defect: set
 * {@code cutoff = 32} to explore the parameter and the instrumented counts would move while the
 * timings would not, which is exactly how Table 8.1 came to be mislabelled.
 * <p>
 * This test fails the moment that divergence is introduced. If it fails, either revert the config
 * change or thread a {@code Config} through {@code ComparableSortHelper} so both paths read the same
 * value — do not simply delete the test.
 */
public class CutoffIsHonouredOnBothPathsTest {

    @Test
    public void bothPathsAgreeUnderTheShippedConfiguration() throws IOException {
        Config config = Config.load();
        int instrumented = new InstrumentedComparisonSortHelper<Integer>("instrumented", N, config).getCutoff();
        int plain = new ComparableSortHelper<Integer>("uninstrumented", N).getCutoff();
        assertEquals("The configured cutoff applies only when instrumented, so a timed benchmark would"
                + " not use it. Either leave [helper] cutoff unset, or give ComparableSortHelper a Config"
                + " so that both paths read the same value. See Table 8.1 in the INFO6205 book for what"
                + " happens otherwise.", instrumented, plain);
    }

    /**
     * The mechanism itself, recorded so that the guard above is understood rather than merely obeyed.
     * With a cutoff configured, the two paths part company — that is the trap, and it is why the test
     * above exists.
     */
    @Test
    public void theTwoPathsDivergeOnceACutoffIsConfigured() throws IOException {
        Config config = Config.load().copy("helper", "cutoff", "64");
        assertEquals("the instrumented helper reads the configuration",
                64, new InstrumentedComparisonSortHelper<Integer>("instrumented", N, config).getCutoff());
        assertEquals("the uninstrumented helper cannot see it, and returns the interface default",
                7, new ComparableSortHelper<Integer>("uninstrumented", N).getCutoff());
    }

    private static final int N = 100;
}
