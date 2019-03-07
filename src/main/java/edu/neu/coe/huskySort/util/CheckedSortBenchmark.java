package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.Sort;

import java.util.function.Consumer;

public class CheckedSortBenchmark<T extends Comparable<T>> extends Benchmark<T[]> {

    public CheckedSortBenchmark(Sort<T> sorter) {
        super((Consumer<T[]>) (sorter::sort), sorter.getHelper()::checkSorted);
    }

}
