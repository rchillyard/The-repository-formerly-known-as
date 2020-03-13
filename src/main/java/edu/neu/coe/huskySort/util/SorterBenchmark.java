package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.Sort;

import java.util.function.UnaryOperator;

public class SorterBenchmark<T extends Comparable<T>> extends Benchmark<T[]> {

    public SorterBenchmark(Class<T> tClass, UnaryOperator<T[]> preProcessor, Sort<T> sorter, T[] ts, int nRuns, TimeLogger[] timeLoggers) {
        super(sorter.toString(), preProcessor, sorter::mutatingSort, sorter.getHelper()::checkSorted);
        this.sorter = sorter;
        this.tClass = tClass;
        this.ts = ts;
        this.nRuns = nRuns;
        this.timeLoggers = timeLoggers;
    }

    public SorterBenchmark(Class<T> tClass, Sort<T> sorter, T[] ts, int nRuns, TimeLogger[] timeLoggers) {
        this(tClass, null, sorter, ts, nRuns, timeLoggers);
    }

    /**
     * Run a benchmark on a sorting problem with N elements.
     *
     * @param N the number of elements.
     *          Not to be confused with nRuns, an instance field, which specifies the number of repetitions of the function.
     */
    public void run(int N) {
        logger.info(formatLocalDateTime() + ": run " + this + " with " + sorter.getHelper().getDescription());
        sorter.init(N);
        logger.info("SorterBenchmark.run: " + formatLocalDateTime() + ": starting " + sorter + " with " + N + " words");
        final double time = super.run(() -> generateRandomArray(ts), nRuns);
        for (TimeLogger timeLogger : timeLoggers) timeLogger.log(time, N);
    }

    @Override
    public String toString() {
        return "SorterBenchmark with " + ts.length + " words and " + nRuns + " runs";
    }

    private T[] generateRandomArray(T[] lookupArray) {
        return sorter.getHelper().random(tClass, (r) -> lookupArray[r.nextInt(lookupArray.length)]);
    }

    private final Sort<T> sorter;
    private final Class<T> tClass;
    private final T[] ts;
    private final int nRuns;
    private final TimeLogger[] timeLoggers;

    final static LazyLogger logger = new LazyLogger(SorterBenchmark.class);
}
