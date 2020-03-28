package edu.neu.coe.huskySort.sort;

import java.lang.reflect.Array;
import java.util.Random;
import java.util.function.Function;

public class BaseHelper<X extends Comparable<X>> implements Helper<X> {
    public BaseHelper(String description, int n, long seed) {
        this.n = n;
        this.description = description;
        this.random = new Random(seed);
    }

    /**
     * Constructor to create a Helper with a random seed.
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     */
    public BaseHelper(String description, int n) {
        this(description, n, System.currentTimeMillis());
    }

    /**
     * Constructor to create a Helper with a random seed and an n value of 0.
     *
     * @param description the description of this Helper (for humans).
     */
    public BaseHelper(String description) {
        this(description, 0);
    }

    /**
     * @return true
     */
    public boolean instrumented() {
        return false;
    }

    /**
     * Method to determine if one X value is less than another.
     *
     * @param v the candidate element.
     * @param w the comparand element.
     * @return true only if v is less than w.
     */
    public boolean less(X v, X w) {
        return v.compareTo(w) < 0;
    }

    /**
     * Compare elements i and j of xs within the subarray lo..hi
     *
     * @param xs the array.
     * @param lo the lowest index of interest (only used for checking).
     * @param hi one more than the highest index of interest (only used for checking).
     * @param i  one of the indices.
     * @param j  the other index.
     * @return the result of comparing xs[i] to xs[j]
     */
    public int compare(X[] xs, int lo, int hi, int i, int j) {
        return xs[i].compareTo(xs[j]);
    }

    /**
     * Swap the elements of array a at indices i and j.
     *
     * @param xs the array.
     * @param lo the lowest index of interest (only used for checking).
     * @param hi one more than the highest index of interest (only used for checking).
     * @param i  one of the indices.
     * @param j  the other index.
     */
    public void swap(X[] xs, int lo, int hi, int i, int j) {
        X temp = xs[i];
        xs[i] = xs[j];
        xs[j] = temp;
    }

    public boolean sorted(X[] xs) {
        for (int i = 1; i < xs.length; i++) if (xs[i - 1].compareTo(xs[i]) > 0) return false;
        return true;
    }

    public int inversions(X[] xs, int from, int to) {
        int result = 0;
        for (int i = from; i < to; i++)
            for (int j = i + 1; j < to; j++)
                if (xs[i].compareTo(xs[j]) > 0) result++;
        return result;
    }

    /**
     * Method to post-process an array after sorting.
     *
     * In this implementation, the post-processing verifies that xs is sorted.
     *
     * @param xs the array to be post-processed.
     *
     * @throws SortException if the array xs is not sorted.
     */
    public void postProcess(X[] xs) {
        if (!sorted(xs)) throw new SortException("Array is not sorted");
    }

    // TODO this needs to be unit-tested
    public X[] random(Class<X> clazz, Function<Random, X> f) {
        if (n <= 0) throw new SortException("Helper.random: not initialized");
        return random(n, clazz, f);
    }

    @Override
    public String toString() {
        return "Helper for " + description + " with " + n + " elements";
    }

    public String getDescription() {
        return description;
    }

    public void setN(int n) {
        if (this.n == 0 || this.n == n) this.n = n;
        else throw new RuntimeException("Helper: n is already set to a different value");
    }

    public int getN() {
        return n;
    }

    public void close() {
    }

    // TODO this needs to be unit-tested
    private X[] random(int n, Class<X> clazz, Function<Random, X> f) {
        setN(n);
        @SuppressWarnings("unchecked") X[] result = (X[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) result[i] = f.apply(random);
        return result;
    }

    protected final String description;
    protected final Random random;
    protected int n;
}