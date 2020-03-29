package edu.neu.coe.huskySort.sort;

public class HelperFactory {

    public static <X extends Comparable<X>> Helper<X> create(String description, int nWords, boolean instrumented) {
        return instrumented ? new InstrumentedHelper<>(description, nWords) : new BaseHelper<>(description, nWords);
    }

}
