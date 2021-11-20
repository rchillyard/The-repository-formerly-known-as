package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.SortException;

import java.text.CollationKey;
import java.text.Collator;

public class SequenceEncoder_Collator extends BaseHuskySequenceCoder<String> {
    /**
     * Constructor.
     *
     * @param collator the appropriate Collator
     */
    public SequenceEncoder_Collator(final Collator collator) {
        super("UTF8", Integer.MAX_VALUE);
        this.collator = collator;
    }

    @Override
    public Coding huskyEncode(final String[] xs) {
        final int length = xs.length;
        final CollationKey[] keys = new CollationKey[length];
        for (int i = 0; i < length; i++) keys[i] = this.collator.getCollationKey(xs[i]);
        return huskyEncode(keys);
    }

    /**
     * Encode x as a long.
     * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
     * If this cannot be guaranteed, then the result of imperfect(z) will be true.
     *
     * @param str the X value to encode.
     * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
     */
    public long huskyEncode(final String str) {
        throw new SortException("inappropriate call to huskyEncode(String)");
    }

    private final Collator collator;
}
