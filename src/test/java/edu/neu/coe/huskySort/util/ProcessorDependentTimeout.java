package edu.neu.coe.huskySort.util;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ProcessorDependentTimeout extends org.junit.rules.Timeout {
    /**
     * Create a {@code Timeout} instance with the timeout specified
     * at the timeUnit of granularity of the provided {@code TimeUnit}.
     *
     * @param timeout  the maximum time to allow the test to run
     *                 before it should timeout
     * @param timeUnit the time unit for the {@code timeout}
     * @param config
     * @since 4.12
     */
    public ProcessorDependentTimeout(long timeout, TimeUnit timeUnit, Config config) {
        super(getFactoredMilliseconds(timeout, timeUnit, config), MILLISECONDS);
    }

    private static long getFactoredMilliseconds(long timeout, TimeUnit timeUnit, Config config) {
        Map<String, Double> processorSpeeds = getProcessorSpeeds();
        Double processorSpeed = processorSpeeds.getOrDefault(config.get("tests", "processor"), 1.0);
        long result = Math.round(MILLISECONDS.convert(timeout, timeUnit) * 1000 / processorSpeed / 1000);
        logger.info("setting timeout to " + result + " milliseconds");
        return result;
    }

    private static Map<String, Double> getProcessorSpeeds() {
        Map<String, Double> map = new HashMap<>();
        map.put("MacBookPro 2.8 GHz Quad Core Intel Core i7", 0.95);
        map.put("MacBookAir 1.6 GH Dual Core Intel Core i5", 0.68);
        return map;
    }

    private static final Logger logger = new LazyLogger(ProcessorDependentTimeout.class);
}
