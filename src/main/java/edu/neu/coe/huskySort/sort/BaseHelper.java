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
     * Method to determine if one X value is less than another.
     *
     * @param v the candidate element.
     * @param w the comparand element.
     * @return true only if v is less than w.
     */
    @Override
    public boolean less(X v, X w) {
        return v.compareTo(w) < 0;
    }

    /**
     * Swap the elements of array a at indices i and j.
     *
     * @param a  the array.
     * @param lo the lowest index of interest (only used for checking).
     * @param hi one more than the highest index of interest (only used for checking).
     * @param i  one of the indices.
     * @param j  the other index.
     */
    @Override
    public void swap(X[] a, int lo, int hi, int i, int j) {
        X temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    @Override
    public boolean sorted(X[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1].compareTo(a[i]) > 0) return false;
        return true;
    }

    @Override
    public int inversions(X[] a, int from, int to) {
        int result = 0;
        for (int i = from; i < to; i++)
            for (int j = i + 1; j < to; j++)
                if (a[i].compareTo(a[j]) > 0) result++;
        return result;
    }

    /**
     * Method to check that an array is sorted.
     *
     * @param xs the array to be tested.
     *           TODO log the message
     *           TODO show the number of inversions
     */
    // TODO this needs to be unit-tested
    @Override
    public void postProcess(X[] xs) {
        if (!sorted(xs)) System.err.println("array is not sorted");
    }

    // TODO this needs to be unit-tested
    @Override
    public X[] random(Class<X> clazz, Function<Random, X> f) {
        if (n <= 0) throw new SortException("Helper.random: not initialized");
        return random(n, clazz, f);
    }

    @Override
    public String toString() {
        return "Helper for " + description + " with " + n + " elements";
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setN(int n) {
        if (this.n == 0 || this.n == n) this.n = n;
        else throw new RuntimeException("Helper: n is already set to a different value");
    }

    @Override
    public int getN() {
        return n;
    }

    @Override
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
