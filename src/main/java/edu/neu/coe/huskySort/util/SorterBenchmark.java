package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.Sort;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class SorterBenchmark<T extends Comparable<T>> extends Benchmark<T[]> {

    public SorterBenchmark(Class<T> tClass, UnaryOperator<T[]> preProcessor, Sort<T> sorter, T[] ts, int nRuns, String normalizePrefix, Function<Double, Double> normalizeNormalizer) {
        super(sorter.toString(), preProcessor, sorter::mutatingSort, sorter.getHelper()::checkSorted);
        this.sorter = sorter;
        this.tClass = tClass;
        this.ts = ts;
        this.nRuns = nRuns;
        this.normalizePrefix = normalizePrefix;
        this.normalizeNormalizer = normalizeNormalizer;
    }

    public SorterBenchmark(Class<T> tClass, Sort<T> sorter, T[] ts, int nRuns, String normalizePrefix, Function<Double, Double> normalizeNormalizer) {
        this(tClass, null, sorter, ts, nRuns, normalizePrefix, normalizeNormalizer);
    }

    public void run(int nWords) {
        logger.info(formatLocalDateTime() + ": run " + this + " with " + sorter.getHelper().getDescription());
        sorter.init(nWords);
        logger.info("SorterBenchmark.run: " + formatLocalDateTime() + ": starting " + sorter + " with " + nWords + " words");
        final double time = super.run(() -> generateRandomArray(ts, nWords), nRuns);
        logger.info(normalizePrefix + " " + normalizeNormalizer.apply(time));
    }

    @Override
    public String toString() {
        return "SorterBenchmark with " + ts.length + " words and " + nRuns + " runs";
    }

    private T[] generateRandomArray(T[] lookupArray, int number) {
        return sorter.getHelper().random(tClass, (r) -> lookupArray[r.nextInt(lookupArray.length)]);
    }

    private final Sort<T> sorter;
    private final Class<T> tClass;
    private final T[] ts;
    private final int nRuns;
    private final String normalizePrefix;
    private final Function<Double, Double> normalizeNormalizer;

    final static LazyLogger logger = new LazyLogger(SorterBenchmark.class);
}
