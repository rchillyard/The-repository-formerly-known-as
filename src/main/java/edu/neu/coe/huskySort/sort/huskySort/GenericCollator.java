package edu.neu.coe.huskySort.sort.huskySort;

import java.nio.charset.StandardCharsets;
import java.text.CollationKey;
import java.text.Collator;
import java.util.function.Function;

/**
 * GenericCollator
 *
 * NOTE not currently used.
 *
 * @param <X> the generic type.
 */
public class GenericCollator<X> {
    public GenericCollator(final Function<X, CollationKey> xToKey, final Function<CollationKey, X> keyToX) {
        this.xToKey = xToKey;
        this.keyToX = keyToX;
    }

    /**
     * Do not use this constructor unless X is a String.
     *
     * @param collator an instance of Collator.
     */
    public GenericCollator(final Collator collator) {
        this(x -> collator.getCollationKey(x.toString()), x -> (X) x.getSourceString());
    }

    /**
     * Do not use this constructor unless X is a String.
     */
    public GenericCollator() {
        this(Collator.getInstance());
    }

    /**
     * The default implementation of this method simply forms a String from x and gets the CollationKey for that.
     *
     * @param x a value of X.
     * @return the corresponding CollationKey
     */
    public CollationKey getCollationKey(final X x) {
        return xToKey.apply(x);
    }

    /**
     * The default implementation of this method simply forms a String from x and gets the CollationKey for that.
     *
     * @param key a CollationKey.
     * @return the corresponding value of X.
     */
    public X getSourceString(final CollationKey key) {
        return keyToX.apply(key);
    }

    private final Function<X, CollationKey> xToKey;
    private final Function<CollationKey, X> keyToX;

    static class CollationKeyEnglish extends CollationKey {
        public CollationKeyEnglish(final String source) {
            super(source);
        }

        @Override
        public int compareTo(final CollationKey target) {
            return getSourceString().compareTo(target.getSourceString());
        }

        @Override
        public byte[] toByteArray() {
            return this.getSourceString().getBytes(StandardCharsets.UTF_8);
        }
    }

    public static GenericCollator<String> English = new GenericCollator<>(CollationKeyEnglish::new, CollationKey::getSourceString);
}
