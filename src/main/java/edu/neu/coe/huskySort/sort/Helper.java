package edu.neu.coe.huskySort.sort;

import java.util.Random;
import java.util.function.Function;

public interface Helper<X extends Comparable<X>> {
    boolean less(X v, X w);

    void swap(X[] a, int lo, int hi, int i, int j);

    boolean sorted(X[] a);

    int inversions(X[] a, int from, int to);

    void postProcess(X[] xs);

    X[] random(Class<X> clazz, Function<Random, X> f);

    String getDescription();

    void setN(int n);

    int getN();

    void close();
}
