package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.SortWithHelper;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * Class to extend Benchmark for sorting an array of T values.
 * The default implementation of run in this class randomly selects a subset of the array to be sorted.
 * Each sort is preceded (optionally) by a preProcessor and succeeded (optionally) by a postProcessor.
 *
 * @param <T> the underlying type to be sorted.
 */
public final class SorterBenchmark<T extends Comparable<T>> extends Benchmark<T[]> {

    /**
     * Run a benchmark on a sorting problem with N elements.
     *
     * @param N the number of elements.
     *          Not to be confused with nRuns, an instance field, which specifies the number of repetitions of the function.
     */
    public void run(final int N) {
        logger.info("run: sort " + formatWhole(N) + " elements using " + this);
        sorter.init(N);
        final double time = super.run(() -> generateRandomArray(ts), nRuns);
        for (final TimeLogger timeLogger : timeLoggers) timeLogger.log(time, N);
    }

    @Override
    public String toString() {
        return "SorterBenchmark on " + tClass + " from " + formatWhole(ts.length) + " total elements and " + formatWhole(nRuns) + " runs using sorter: " + sorter.getHelper().getDescription();
    }

    /**
     * Constructor for a SorterBenchmark where we provide the following parameters:
     *
     * @param tClass        the class of T.
     * @param preProcessor  an optional pre-processor which is applied before each sort.
     * @param sorter        the sorter.
     * @param postProcessor an optional pre-processor which is applied before each sort.
     * @param ts            the array of Ts.
     * @param nRuns         the number of runs to perform in this benchmark.
     * @param timeLoggers   the time-loggers.
     */
    public SorterBenchmark(final Class<T> tClass, final UnaryOperator<T[]> preProcessor, final SortWithHelper<T> sorter, final Consumer<T[]> postProcessor, final T[] ts, final int nRuns, final TimeLogger[] timeLoggers) {
        super(sorter.toString(), preProcessor, sorter::mutatingSort, postProcessor);
        this.sorter = sorter;
        this.tClass = tClass;
        this.ts = ts;
        this.nRuns = nRuns;
        this.timeLoggers = timeLoggers;
    }

    /**
     * Constructor for a SorterBenchmark where we provide the following parameters:
     * For this form of the constructor, the post-processor always checks that the sort was successful.
     *
     * @param tClass       the class of T.
     * @param preProcessor an optional pre-processor which is applied before each sort.
     * @param sorter       the sorter.
     * @param ts           the array of Ts.
     * @param nRuns        the number of runs to perform in this benchmark.
     * @param timeLoggers  the time-loggers.
     */
    public SorterBenchmark(final Class<T> tClass, final UnaryOperator<T[]> preProcessor, final SortWithHelper<T> sorter, final T[] ts, final int nRuns, final TimeLogger[] timeLoggers) {
        this(tClass, preProcessor, sorter, sorter::postProcess, ts, nRuns, timeLoggers);
    }

    /**
     * Constructor for a SorterBenchmark where we provide the following parameters:
     * For this form of the constructor, the post-processor always checks that the sort was successful.
     * For this form of the constructor, there is no pre-processor.
     *
     * @param tClass      the class of T.
     * @param sorter      the sorter.
     * @param ts          the array of Ts.
     * @param nRuns       the number of runs to perform in this benchmark.
     * @param timeLoggers the time-loggers.
     */
    public SorterBenchmark(final Class<T> tClass, final SortWithHelper<T> sorter, final T[] ts, final int nRuns, final TimeLogger[] timeLoggers) {
        this(tClass, null, sorter, ts, nRuns, timeLoggers);
    }

    private T[] generateRandomArray(final T[] lookupArray) {
        return sorter.getHelper().random(tClass, (r) -> lookupArray[r.nextInt(lookupArray.length)]);
    }

    private final SortWithHelper<T> sorter;
    private final T[] ts;
    private final int nRuns;
    private final TimeLogger[] timeLoggers;
    private final static LazyLogger logger = new LazyLogger(SorterBenchmark.class);
    private final Class<T> tClass;

}
