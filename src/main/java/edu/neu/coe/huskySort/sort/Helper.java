package edu.neu.coe.huskySort.sort;

import java.lang.reflect.Array;
import java.util.Random;
import java.util.function.Function;

/**
 * Helper class for sorting methods.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class Helper<X extends Comparable<X>> {

    /**
     * Constructor to create a Helper
     *
     * @param description the description of this Helper (for humans).
     * @param n           the number of elements expected to be sorted. The field n is mutable so can be set after the constructor.
     * @param seed        the seed for the random number generator
     */
    public Helper(String description, int n, long seed) {
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
    public Helper(String description, int n) {
        this(description, n, System.currentTimeMillis());
    }

    /**
     * Constructor to create a Helper with a random seed and an n value of 0.
     *
     * @param description the description of this Helper (for humans).
     */
    public Helper(String description) {
        this(description, 0);
    }

    /**
     * Method to determine if one X value is less than another.
     *
     * @param v the candidate element.
     * @param w the comparand element.
     * @return true only if v is less than w.
     */
    public boolean less(X v, X w) {
        compares++;
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
    public void swap(X[] a, int lo, int hi, int i, int j) {
        swaps++;
        if (i < lo) throw new RuntimeException("i is out of range: i; " + i + "; lo=" + lo);
        if (j > hi) throw new RuntimeException("j is out of range: j; " + j + "; hi=" + hi);
        X temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public boolean sorted(X[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1].compareTo(a[i]) > 0) return false;
        return true;
    }

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
    public void checkSorted(X[] xs) {
        if (!sorted(xs)) System.err.println("array is not sorted");
    }

    // TODO this needs to be unit-tested
    public X[] random(Class<X> clazz, Function<Random, X> f) {
        return random(n, clazz, f);
    }

    @Override
    public String toString() {
        return "Helper for " + description + " with " + n + " elements: compares=" + compares + ", swaps=" + swaps;
    }

    public void setN(int n) {
        if (this.n == 0 || this.n == n) this.n = n;
        else throw new RuntimeException("Helper: n is already set to a different value");
    }

    // TODO this needs to be unit-tested
    private X[] random(int n, Class<X> clazz, Function<Random, X> f) {
        setN(n);
        @SuppressWarnings("unchecked") X[] result = (X[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) result[i] = f.apply(random);
        return result;
    }

    private int compares = 0;
    private int swaps = 0;

    protected int n;
    private final String description;
    private final Random random;
}
