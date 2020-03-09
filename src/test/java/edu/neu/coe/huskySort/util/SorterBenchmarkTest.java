package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SorterBenchmarkTest {

    @Before
    public void setUp() {
        String[] strings = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        benchmark = new SorterBenchmark<>(String.class, new InsertionSort<>(), strings, 100, "test", x -> x);
    }

    @SuppressWarnings("EmptyMethod")
    @After
    public void tearDown() {
        // Nothing to do.
    }

    @Test
    public void run() {
        benchmark.run(4);
    }

    SorterBenchmark<String> benchmark = null;

}