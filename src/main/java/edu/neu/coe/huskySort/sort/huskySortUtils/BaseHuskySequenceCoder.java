package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * Base Husky sequence coder.
 */
public abstract class BaseHuskySequenceCoder<X extends CharSequence> implements HuskySequenceCoder<X> {

    /**
     * Constructor.
     *
     * @param name the name of this coder.
     */
    public BaseHuskySequenceCoder(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Encode an array of Xs.
     *
     * @param xs an array of X elements.
     * @return an array of longs corresponding to the the Husky codes of the X elements.
     */
    @Override
    public Coding huskyEncode(X[] xs) {
        boolean isPerfect = true;
        long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            X x = xs[i];
            if (isPerfect) isPerfect = perfectForLength(x.length());
            result[i] = huskyEncode(x);
        }
        return new Coding(result, isPerfect);
    }

    /**
     * NOTE: this implementation of perfect() is never called because perfection is
     * determined solely by the huskyEncoder(X[]) method.
     * @return false.
     */
    @Override
    public boolean perfect() {
        return false;
    }

    @Override
    public String toString() {
        return "BaseHuskySequenceCoder{" +
                "name='" + name + '\'' +
                '}';
    }

    private final String name;
}
