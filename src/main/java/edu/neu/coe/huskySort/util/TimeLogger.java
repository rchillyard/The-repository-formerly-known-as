package edu.neu.coe.huskySort.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiFunction;

public class TimeLogger {
    private final String prefix;
    private final BiFunction<Double, Integer, Double> normalizer;

    public TimeLogger(final String prefix, final BiFunction<Double, Integer, Double> normalizer) {
        this.prefix = prefix;
        this.normalizer = normalizer;
    }

    public void log(final Double time, final Integer N) {
        logger.info(prefix + " " + formatTime(normalizer.apply(time, N)));
    }

    public static String formatTime(final double time) {
        decimalFormat.applyPattern(timePattern);
        return decimalFormat.format(time);
    }

    final static LazyLogger logger = new LazyLogger(TimeLogger.class);

    private static final Locale locale = new Locale("en", "US");
    private static final String timePattern = "######.00";
    private static final DecimalFormat decimalFormat = (DecimalFormat)
            NumberFormat.getNumberInstance(locale);

}
