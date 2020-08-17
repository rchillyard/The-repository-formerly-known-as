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
     * @param config   the configuration (containing the processor details).
     * @since 4.12
     */
    public ProcessorDependentTimeout(long timeout, TimeUnit timeUnit, Config config) {
        super(getFactoredTimeout(timeout, timeUnit, config, MILLISECONDS), MILLISECONDS);
    }

    public static long getFactoredTimeout(long timeoutGiven, TimeUnit timeUnit, Config config, TimeUnit timeoutRequired) {
        Map<String, Double> processorSpeeds = getProcessorSpeeds();
        Double processorSpeed = processorSpeeds.getOrDefault(config.get("tests", "processor"), 1.0);
        long result = Math.round(timeoutRequired.convert(timeoutGiven, timeUnit) * GRANULARITY / processorSpeed / GRANULARITY);
        logger.info("setting timeout to " + result + " " + timeoutRequired);
        return result;
    }

    private static Map<String, Double> getProcessorSpeeds() {
        Map<String, Double> map = new HashMap<>();
        map.put("MacBookPro 2.8 GHz Quad Core Intel Core i7", 0.92);
        map.put("MacBookAir 1.6 GH Dual Core Intel Core i5", 0.68);
        map.put("Intel Core i7-8700K", 1.55);
        map.put("Intel Core i7-8550U", 0.71);
        return map;
    }

    private static final Logger logger = new LazyLogger(ProcessorDependentTimeout.class);
    private static final int GRANULARITY = 10000;

}
