package edu.neu.coe.huskySort.util;

public class SimpleCounter implements Counter {
    private long count = 0;

    public void increment(long increment) {
        count += increment;
    }

    @Override
    public long get() {
        return count;
    }
}
