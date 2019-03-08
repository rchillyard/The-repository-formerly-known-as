package edu.neu.coe.huskySort.util;

public interface Counter {

    public void increment(long increment);

    default void increment() {
        increment(1L);
    }

    public long get();
}
