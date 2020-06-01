package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Config;

public class HelperFactory {

		public static <X extends Comparable<X>> Helper<X> create(String description, int nWords, boolean instrumented, Config config) {
				return instrumented ? new InstrumentedHelper<>(description, nWords, config) : new BaseHelper<>(description, nWords);
		}

}
