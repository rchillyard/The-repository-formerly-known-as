package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * Base Husky sequence coder.
 */
public abstract class BaseHuskySequenceCoder<X extends CharSequence> implements HuskySequenceCoder<X> {

    /**
     * Method to determine if this Husky Coder is perfect for a sequence of the given length.
     * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
     * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
     *
     * @param length the length of a particular String.
     * @return true if length <= maxLength.
     */
    public final boolean perfectForLength(final int length) {
        return length <= maxLength;
    }

    /**
     * Constructor.
     *
     * @param name      the name of this coder.
     * @param maxLength the maximum length of a sequence which can be perfectly encoded.
     */
    public BaseHuskySequenceCoder(final String name, final int maxLength) {
        this.name = name;
        this.maxLength = maxLength;
    }

    /**
     * @return the name of this coder.
     */
    final public String name() {
        return name;
    }

    /**
     * Encode an array of Xs.
     *
     * @param xs an array of X elements.
     * @return an array of longs corresponding to the the Husky codes of the X elements.
     */
    @Override
    public Coding huskyEncode(final X[] xs) {
        boolean isPerfect = true;
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            final X x = xs[i];
            if (isPerfect) isPerfect = perfectForLength(x.length());
            result[i] = huskyEncode(x);
        }
        return new Coding(result, isPerfect);
    }

    /**
     * NOTE: this implementation of perfect() is never called because perfection is
     * determined solely by the huskyEncoder(X[]) method.
     *
     * @return false.
     */
    @Override
    final public boolean perfect() {
        return false;
    }

    @Override
    final public String toString() {
        return "BaseHuskySequenceCoder{" +
                "name='" + name + '\'' +
                '}';
    }

    private final String name;
    private final int maxLength;
}
