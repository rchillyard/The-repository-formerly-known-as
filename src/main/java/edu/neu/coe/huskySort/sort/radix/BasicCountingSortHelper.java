package edu.neu.coe.huskySort.sort.radix;

import java.util.Random;
import java.util.function.Function;

public class BasicCountingSortHelper<T> implements CountingSortHelper<CharacterMap.UnicodeString> {
    /**
     * @return true if this is an instrumented ComparisonSortHelper.
     */
    @Override
    public boolean instrumented() {
        return false;
    }

    /**
     * Compare values v and w and return true if v is less than w.
     *
     * @param v the first value.
     * @param w the second value.
     * @param d the position of interest.
     * @return true if v is less than w.
     */
    @Override
    public boolean less(final CharacterMap.UnicodeString v, final CharacterMap.UnicodeString w, final int d) {
        return false;
    }

    /**
     * Compare value v with value w.
     *
     * @param v the first value.
     * @param w the second value.
     * @param d the position of interest.
     * @return -1 if v is less than w; 1 if v is greater than w; otherwise 0.
     */
    @Override
    public int compare(final CharacterMap.UnicodeString v, final CharacterMap.UnicodeString w, final int d) {
        return 0;
    }

    /**
     * Copy the element at source[j] into target[i]
     *
     * @param source the source array.
     * @param i      the target index.
     * @param target the target array.
     * @param j      the source index.
     */
    @Override
    public void copy(final CharacterMap.UnicodeString[] source, final int i, final CharacterMap.UnicodeString[] target, final int j) {

    }

    /**
     * Return true if xs is sorted, i.e. has no inversions.
     *
     * @param xs an array of Xs.
     * @return true if there are no inversions, else false.
     */
    @Override
    public boolean sorted(final CharacterMap.UnicodeString[] xs) {
        return false;
    }

    /**
     * Count the number of inversions of this array.
     *
     * @param xs an array of Xs.
     * @return the number of inversions.
     */
    @Override
    public int inversions(final CharacterMap.UnicodeString[] xs) {
        return 0;
    }

    /**
     * Method to post-process the array xs after sorting.
     *
     * @param xs the array that has been sorted.
     * @return whether the postProcessing was successful.
     */
    @Override
    public boolean postProcess(final CharacterMap.UnicodeString[] xs) {
        return false;
    }

    /**
     * Method to generate an array of randomly chosen X elements.
     *
     * @param clazz the class of X.
     * @param f     a function which takes a Random and generates a random value of X.
     * @return an array of X of length determined by the current value according to setN.
     */
    @Override
    public CharacterMap.UnicodeString[] random(final Class<CharacterMap.UnicodeString> clazz, final Function<Random, CharacterMap.UnicodeString> f) {
        return new CharacterMap.UnicodeString[0];
    }

    /**
     * @return the description of this ComparisonSortHelper.
     */
    @Override
    public String getDescription() {
        return null;
    }

    /**
     * Initialize this ComparisonSortHelper with the size of the array to be managed.
     *
     * @param n the size to be managed.
     */
    @Override
    public void init(final int n) {

    }

    /**
     * Get the current value of N.
     *
     * @return the value of N.
     */
    @Override
    public int getN() {
        return 0;
    }

    /**
     * Close this ComparisonSortHelper, freeing up any resources used.
     */
    @Override
    public void close() {

    }
}
