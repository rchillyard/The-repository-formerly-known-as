package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.Sort;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class SorterBenchmark<T extends Comparable<T>> extends Benchmark<T[]> {

    public SorterBenchmark(Class<T> tClass, UnaryOperator<T[]> preProcessor, Sort<T> sorter, T[] ts, int nRuns, String timePrefix, BiFunction<Double, Integer, Double> timeNormalizer) {
        super(sorter.toString(), preProcessor, sorter::mutatingSort, sorter.getHelper()::checkSorted);
        this.sorter = sorter;
        this.tClass = tClass;
        this.ts = ts;
        this.nRuns = nRuns;
        this.timePrefix = timePrefix;
        this.timeNormalizer = timeNormalizer;
    }

    public SorterBenchmark(Class<T> tClass, Sort<T> sorter, T[] ts, int nRuns, String timePrefix, BiFunction<Double, Integer, Double> timeNormalizer) {
        this(tClass, null, sorter, ts, nRuns, timePrefix, timeNormalizer);
    }

    public void run(int nWords) {
        logger.info(formatLocalDateTime() + ": run " + this + " with " + sorter.getHelper().getDescription());
        sorter.init(nWords);
        logger.info("SorterBenchmark.run: " + formatLocalDateTime() + ": starting " + sorter + " with " + nWords + " words");
        final double time = super.run(() -> generateRandomArray(ts, nWords), nRuns);
        logger.info(timePrefix + " " + formatTime(timeNormalizer.apply(time, nWords)));
    }

    @Override
    public String toString() {
        return "SorterBenchmark with " + ts.length + " words and " + nRuns + " runs";
    }

    private T[] generateRandomArray(T[] lookupArray, int number) {
        return sorter.getHelper().random(tClass, (r) -> lookupArray[r.nextInt(lookupArray.length)]);
    }

    private static String formatTime(double time) {
        decimalFormat.applyPattern(timePattern);
        return decimalFormat.format(time);
    }

    private final Sort<T> sorter;
    private final Class<T> tClass;
    private final T[] ts;
    private final int nRuns;
    private final String timePrefix;
    private final BiFunction<Double, Integer, Double> timeNormalizer;

    private static final Locale locale = new Locale("en", "US");
    private static final String timePattern = "######.##";
    private static final DecimalFormat decimalFormat = (DecimalFormat)
            NumberFormat.getNumberInstance(locale);

    final static LazyLogger logger = new LazyLogger(SorterBenchmark.class);
}
