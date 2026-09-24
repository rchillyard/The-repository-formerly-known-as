package edu.neu.coe.huskySort.sort.huskySort;

import java.nio.charset.StandardCharsets;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Locale;
import java.util.function.Function;

/**
 * GenericCollator
 * <p>
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
     * <p>
     * NOTE the Collator is not shared between threads, and the reason is contention rather than
     * safety. {@link java.text.RuleBasedCollator} reuses its iterators and buffers between calls
     * instead of reallocating them, and makes that safe by declaring both {@code compare} and
     * {@code getCollationKey} {@code synchronized} -- its own source says so: "the objects persist
     * anyway to avoid wasting extra creation time. compare() and getCollationKey() are
     * synchronized to ensure thread safety with this scheme." Correct, then, but every concurrent
     * caller of a shared instance serializes on one monitor, which in a project with a parallel
     * sorter is the wrong default for a {@code public static} field such as {@link #English}.
     * Cloning per thread is the documented way out; {@code RuleBasedCollator.clone} exists for it
     * and uses a private copy constructor because, as it notes, "This is faster." Each thread gets
     * one clone, made once and reused.
     *
     * @param collator an instance of Collator.
     */
    public GenericCollator(final Collator collator) {
        //noinspection unchecked
        this(perThread(collator), key -> (X) key.getSourceString());
    }

    private static <Y> Function<Y, CollationKey> perThread(final Collator collator) {
        final ThreadLocal<Collator> mine = ThreadLocal.withInitial(() -> (Collator) collator.clone());
        return y -> mine.get().getCollationKey(y.toString());
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

    /**
     * A CollationKey which orders by Unicode code point -- that is, exactly as
     * {@link String#compareTo} does -- rather than by any locale's collation.
     * <p>
     * Its {@code toByteArray} is the UTF-8 encoding of the source, which is the point of it: UTF-8
     * preserves code-point order, so the bytes compare the same way the keys do, and a husky code
     * taken from the leading bytes is order-preserving for as far as it reaches. That makes it the
     * cheap, total, locale-free option, and the one consistent with the natural ordering the rest
     * of this project's string sorts use.
     * <p>
     * Renamed from {@code CollationKeyEnglish} on 2026-09-24: it was never English in the sense
     * {@link Collator} means, and the old name said otherwise.
     */
    static class CollationKeyCodePoint extends CollationKey {
        public CollationKeyCodePoint(final String source) {
            super(source);
        }

        public int compareTo(final CollationKey target) {
            return getSourceString().compareTo(target.getSourceString());
        }

        public byte[] toByteArray() {
            return this.getSourceString().getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * English collation, in the sense {@link java.text} means it: "apple" sorts before "Banana",
     * accents and case are tertiary differences rather than large jumps in code point, and the
     * key's bytes compare in collation order so a husky code taken from them does too.
     * <p>
     * Until 2026-09-24 this field was code-point order wearing an English name, which put "Banana"
     * first. Anyone who wants that ordering -- and for husky coding it is a reasonable thing to
     * want -- should ask for {@link #CodePointOrder}, which now says so.
     * <p>
     * NOTE collation keys are longer than their source, so a husky code built from seven of their
     * bytes runs out sooner than one built from UTF-8; expect {@code perfect} to be false for all
     * but the shortest strings. That is honest rather than unfortunate: the cleanup pass is what
     * makes the ordering exact.
     */
    public static final GenericCollator<String> English = new GenericCollator<>(Collator.getInstance(Locale.ENGLISH));

    /**
     * Unicode code-point order, which is what {@link String#compareTo} implements and what the
     * husky coders in {@code HuskyCoderFactory} produce. See {@link CollationKeyCodePoint}.
     */
    public static final GenericCollator<String> CodePointOrder =
            new GenericCollator<>(CollationKeyCodePoint::new, CollationKey::getSourceString);
}
