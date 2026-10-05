package edu.neu.coe.huskySort.sort.huskySortUtils;

/**
 * Base Husky sequence coder.
 */
public abstract class BaseHuskySequenceCoder<X extends CharSequence> implements HuskySequenceCoder<X> {

    /**
     * {@inheritDoc}
     * <p>
     * Here that is simply whether the sequence fits the coder's window. NOTE the result being
     * true does not make a value exact --- see {@link #exactlyEncodable}, which calls this and
     * then goes on to ask about the characters themselves.
     *
     * @param length the length of a particular String.
     * @return true if {@code length <= maxLength}.
     */
    public final boolean couldBeExactAtLength(final int length) {
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
     * @return an array of longs corresponding to the Husky codes of the X elements.
     */
    @Override
    public Coding huskyEncode(final X[] xs) {
        boolean isPerfect = true;
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            final X x = xs[i];
            if (isPerfect) isPerfect = exactlyEncodable(x);
            result[i] = huskyEncode(x);
        }
        return new Coding(result, isPerfect);
    }

    /**
     * Whether this coder encodes x exactly, so that comparing codes orders x correctly against
     * anything else this coder also encodes exactly.
     * <p>
     * <b>Length is necessary but not sufficient, and assuming otherwise returned wrong answers.</b>
     * Until 2026-09-28 {@link #huskyEncode(CharSequence[])} tested only {@link #couldBeExactAtLength},
     * so a coder that narrows each character -- the ASCII pair to 7 bits, the English pair to 6 --
     * reported a whole array perfect whenever every word was short, even when some word held a
     * character outside the window it can represent. The sort then skipped its cleanup pass and
     * returned an array that was not sorted:
     * <pre>
     *     englishSaturatingCoder on {"caf\u00ff", "caf\u00e9", "cafa", "cafz"}
     *         -> perfect = true, result [cafa, cafz, caf\u00ff, caf\u00e9]
     * </pre>
     * because {@code \u00e9} and {@code \u00ff} both saturate to 63 and the codes tie. Yunlu found
     * it in request 11c/11d (TODO.md item 48); no benchmark array is short enough throughout to
     * have been affected, which is why it survived so long.
     * <p>
     * The default remains the length test, which is right for a coder that stores each character
     * faithfully -- {@code unicodeCoder} keeps a whole 16-bit char per slot, so for it length really
     * is the only limit. A narrowing coder must override this and say what its window is.
     *
     * @param x an element about to be encoded.
     * @return true if the code for x preserves order exactly.
     */
    protected boolean exactlyEncodable(final X x) {
        return couldBeExactAtLength(x.length());
    }

    /**
     * @param x  a sequence.
     * @param lo the lowest character this coder represents faithfully.
     * @param hi the highest.
     * @return true if every character of x lies in [lo, hi].
     */
    protected static boolean charactersWithin(final CharSequence x, final char lo, final char hi) {
        for (int i = 0; i < x.length(); i++) {
            final char c = x.charAt(i);
            if (c < lo || c > hi) return false;
        }
        return true;
    }

    private final String name;
    private final int maxLength;
}
