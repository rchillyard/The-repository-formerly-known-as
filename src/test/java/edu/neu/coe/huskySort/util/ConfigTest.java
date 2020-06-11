package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.InstrumentedHelper;
import org.ini4j.Ini;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigTest {

    @Test
    public void testConfig() throws IOException {
        final Config config = Config.load();
        String name = config.get("huskysort", "version");
        System.out.println("ConfigTest: " + name);
    }

    @Test
    public void testConfigFixed() {
        final Config config = setupConfig(TRUE, "0", "10", "", "");
        assertTrue(config.isInstrumented());
        assertEquals(0L, config.getLong(Config.HELPER, SEED, -1L));
        assertEquals(10, config.getInt(INSTRUMENTING, INVERSIONS, 0));
    }

    public static Config setupConfig(final String instrumenting, final String seed, final String inversions, String cutoff, String intermissionInversions) {
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
        ini.put("huskyhelper", "countintermissioninversions", intermissionInversions);
        return new Config(ini);
    }

    public static final String TRUE = "true";
    public static final String INSTRUMENTING = "instrumenting";
    public static final String INVERSIONS = InstrumentedHelper.INVERSIONS;
    public static final String SEED = "seed";
    public static final String CUTOFF = "cutoff";
    public static final String SWAPS = "swaps";
    public static final String COMPARES = "compares";
    public static final String COPIES = "copies";
    public static final String FIXES = "fixes";

}
