package edu.neu.coe.huskySort.util;

import org.ini4j.Ini;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigTest {

		@Test
		public void testConfig() {
				final Config config = setupConfig(TRUE, "0", "10", "");
				assertTrue(config.getBoolean(HELPER, INSTRUMENT));
				assertEquals(0L, config.getLong(HELPER, SEED, -1L));
				assertEquals(10, config.getInt(INSTRUMENTING, INVERSIONS, 0));
		}

		// TODO add cutoff value
		public static Config setupConfig(final String instrumenting, final String seed, final String inversions, String cutoff) {
				final Ini ini = new Ini();
				final String sInstrumenting = INSTRUMENTING;
				ini.put(HELPER, INSTRUMENT, instrumenting);
				ini.put(HELPER, SEED, seed);
				ini.put(HELPER, CUTOFF, cutoff);
				ini.put(sInstrumenting, INVERSIONS, inversions);
				ini.put(sInstrumenting, SWAPS, instrumenting);
				ini.put(sInstrumenting, COMPARES, instrumenting);
				ini.put(sInstrumenting, COPIES, instrumenting);
				ini.put(sInstrumenting, FIXES, instrumenting);
				return new Config(ini);
		}

		public static final String HELPER = "helper";
		public static final String TRUE = "true";
		public static final String INSTRUMENTING = "instrumenting";
		public static final String INVERSIONS = "inversions";
		public static final String INSTRUMENT = "instrument";
		public static final String SEED = "seed";
		public static final String CUTOFF = "cutoff";
		public static final String SWAPS = "swaps";
		public static final String COMPARES = "compares";
		public static final String COPIES = "copies";
		public static final String FIXES = "fixes";

}
