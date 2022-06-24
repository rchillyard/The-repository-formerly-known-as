package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Random;

/**
 * Class to implement Most significant digit string sort (a radix sort) for UnicodeCharacters with custom collation mechanisms.
 * The custom collation is defined by the instance of CharacterMap passed in to the constructor.
 */
public final class UnicodeMSDStringSort extends BaseCountingSort<UnicodeString, UnicodeCharacter> {

    /**
     * Generic, mutating sort method which operates on a sub-array.
     *
     * @param us   sort the array xs from "from" until "to" (exclusive of to).
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(final UnicodeString[] us, final int from, final int to) {
        doRecursiveSort(us, from, to - from, 0);
    }

    /**
     * Perform the entire process of sorting the given array, including all pre- and post-processing.
     *
     * @param ws an array of Xs which will be mutated.
     */
    public void sortArray(final String[] ws) {
        sortAll(UnicodeString.class, ws, x -> new UnicodeString(characterMap, x), UnicodeString::recoverString);
    }

    /**
     * Constructor of UnicodeMSDStringSort, which requires a CharacterMap.
     *
     * @param characterMap the appropriate character map for the type of unicode strings to be sorted.
     */
    public UnicodeMSDStringSort(final CharacterMap characterMap, final CountingSortHelper<UnicodeString, UnicodeCharacter> helper) {
        super(characterMap, helper);
        this.characterMap = characterMap;
        this.helper = helper;
    }

    /**
     * Constructor of UnicodeMSDStringSort, which requires a CharacterMap.
     *
     * @param characterMap the appropriate character map for the type of unicode strings to be sorted.
     */
    public UnicodeMSDStringSort(final CharacterMap characterMap) {
        this(characterMap, new BasicCountingSortHelper<>("UnicodeMSDStringSort", 0, new Random()));
    }

    /**
     * Sort from xs[from] to xs[to] (exclusive), ignoring the first d characters of each String.
     * This method is recursive.
     *
     * @param xs   the array to be sorted.
     * @param from the low index.
     * @param to   the high index (one above the highest actually processed).
     * @param d    the number of characters in each UnicodeString to be skipped.
     */
    private void doRecursiveSort(final UnicodeString[] xs, final int from, final int to, final int d) {
        assert from >= 0 : "from " + from + " is negative";
        assert to <= xs.length : "to " + to + " is out of bounds: " + xs.length;
        final int n = to - from;
        if (logger.isTraceEnabled())
            logger.trace("UnicodeMSDStringSort.doRecursiveSort: on " + (d > 0 ? xs[from].charAt(d - 1) : "root") + " from=" + from + ", to=" + to + ", d=" + d);
        // XXX if there are fewer than two elements, we return immediately because xs is already sorted.
        if (n < 2) return;
        // XXX if there is a small number of elements, we switch to insertion sort.
        if (n < helper.getCutoff()) insertionSort(xs, from, to, d);
        else {
            // CONSIDER is this the correct place to allocate aux?
            final UnicodeString[] aux = new UnicodeString[n];
            final Counts counts = new Counts();
            counts.countCharacters(xs, from, to, d);
            final UnicodeCharacter[] keys = counts.accumulateCounts();
            for (int i = from; i < to; i++) {
                final UnicodeString xsi = xs[i];
                counts.copyAndIncrementCount(xsi, aux, d);
            }
            if (helper.instrumented())
                helper.getInstrumenter().incrementHits(3 * n); // count, copy and copy back
            // XXX Copy back.
            System.arraycopy(aux, 0, xs, from, n);
            int offset = 0;
            // XXX For each key, recursively sort the appropriate sub-array on the next character position (p).
            final int p = d + 1;
            for (final UnicodeCharacter key : keys) {
                if (key == UnicodeCharacter.NullChar)
                    continue;
                final int index = counts.get(key);
                doRecursiveSort(xs, from + offset, from + index, p);
                offset = index;
            }
        }
    }

    /**
     * Execute insertion sort on the given sub-array, but skipping the first d characters when determining the order.
     *
     * @param xs   an array.
     * @param from the first element of the array to be considered.
     * @param to   the first element following the sub-array NOT to be considered.
     * @param d    the number of characters to be ignored.
     */
    private void insertionSort(final UnicodeString[] xs, final int from, final int to, final int d) {
        if (logger.isTraceEnabled())
            logger.trace("UnicodeMSDStringSort.insertionSort: on " + (d > 0 ? xs[from].charAt(d - 1) : "root") + " from=" + from + ", to=" + to + ", d=" + d);
        for (int i = from; i < to; i++)
            for (int j = i; j > from && helper.less(xs, j, j - 1, d); j--)
                helper.swap(xs, j, j - 1);
    }

    final static LazyLogger logger = new LazyLogger(UnicodeMSDStringSort.class);

    private final CharacterMap characterMap; // NOTE this is used, despite IDEA's analysis.
    private final CountingSortHelper<UnicodeString, UnicodeCharacter> helper;
}