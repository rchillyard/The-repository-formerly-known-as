package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.InstrumentedComparisonSortHelper;
import org.ini4j.Ini;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void testConfigFromString1() throws IOException {
        final String s = "[x]\nx1=\nx2= \nx3=Hello\nx4= Hello\nx5=Hello World!\nx6= Hello World!\nx7= \"Hello\"";
        final Config config = new Config(new ByteArrayInputStream(s.getBytes()));
        assertNull(config.get("x", "x1"));
        assertNull(config.get("x", "x2"));
        assertEquals("Hello", config.get("x", "x3"));
        assertEquals("Hello", config.get("x", "x4"));
        assertEquals("Hello World!", config.get("x", "x5"));
        assertEquals("Hello World!", config.get("x", "x6"));
        assertEquals("\"Hello\"", config.get("x", "x7"));
    }

    @Test
    public void testConfigFromString2() throws IOException {
        final String s = "[x]\nx1=\nx2=\t\nx3=Hello\nx4=\tHello\nx5=Hello World!\nx6=\tHello World!\nx7=\t\"Hello\"";
        final Config config = new Config(new ByteArrayInputStream(s.getBytes()));
        assertNull(config.get("x", "x1"));
        assertNull(config.get("x", "x2"));
        assertEquals("Hello", config.get("x", "x3"));
        assertEquals("Hello", config.get("x", "x4"));
        assertEquals("Hello World!", config.get("x", "x5"));
        assertEquals("Hello World!", config.get("x", "x6"));
        assertEquals("\"Hello\"", config.get("x", "x7"));
    }

    @Test
    public void testConfig() throws IOException {
        final Config config = Config.load();
        final String name = config.get("huskysort", "version");
        System.out.println("ConfigTest: " + name);
    }

    @Test
    public void testConfigFixed() {
        final Config config = setupConfig(TRUE, "0", "10", "", "");
        assertTrue(config.isInstrumented());
        assertEquals(0L, config.getLong(Config.HELPER, SEED, -1L));
        assertEquals(10, config.getInt(INSTRUMENTING, INVERSIONS, 0));
    }

    @Test
    public void testCopy() {
        final Config config = setupConfig(FALSE, "", "", "", "");
        final int originalSeed = config.getInt(Config.HELPER, SEED, -1);
        final Config config1 = config.copy(Config.HELPER, SEED, "1");
        assertEquals(originalSeed, config.getInt(Config.HELPER, SEED, -1));
        assertEquals(1, config1.getInt(Config.HELPER, SEED, -1));
    }

    // NOTE: we ignore this for now, because this would need to run before any other tests in order to work as originally designed.
    @Ignore
    public void testUnLogged() throws IOException {
        final Config config = Config.load();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(config);
        assertTrue((Boolean) privateMethodInvoker.invokePrivate("unLogged", Config.HELPER + "." + SEED));
        assertFalse((Boolean) privateMethodInvoker.invokePrivate("unLogged", Config.HELPER + "." + SEED));
    }

    public static Config setupConfig(final String instrumenting, final String seed, final String inversions, final String cutoff, final String interimInversions) {
        final Ini ini = new Ini();
        final String sInstrumenting = INSTRUMENTING;
        ini.put(Config.HELPER, Config.INSTRUMENT, instrumenting);
        ini.put(Config.HELPER, SEED, seed);
        ini.put(Config.HELPER, CUTOFF, cutoff);
        ini.put(sInstrumenting, INVERSIONS, inversions);
        ini.put(sInstrumenting, SWAPS, instrumenting);
        ini.put(sInstrumenting, COMPARES, instrumenting);
        ini.put(sInstrumenting, COPIES, instrumenting);
        ini.put(sInstrumenting, FIXES, instrumenting);
        ini.put("huskyhelper", "countinteriminversions", interimInversions);
        return new Config(ini);
    }

    public static final String TRUE = "true";
    public static final String FALSE = "";
    public static final String INSTRUMENTING = InstrumentedComparisonSortHelper.INSTRUMENTING;
    public static final String INVERSIONS = InstrumentedComparisonSortHelper.INVERSIONS;
    public static final String SEED = "seed";
    public static final String CUTOFF = "cutoff";
    public static final String SWAPS = InstrumentedComparisonSortHelper.SWAPS;
    public static final String COMPARES = InstrumentedComparisonSortHelper.COMPARES;
    public static final String COPIES = InstrumentedComparisonSortHelper.COPIES;
    public static final String FIXES = InstrumentedComparisonSortHelper.FIXES;

}
