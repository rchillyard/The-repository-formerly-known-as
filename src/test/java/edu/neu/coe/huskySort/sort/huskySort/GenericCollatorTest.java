package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.Coding;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link GenericCollator}, which adapts an arbitrary type to {@link CollationKey} in both
 * directions so that husky coding can be driven from a collation rather than from the element.
 * <p>
 * The class is marked "not currently used", so these tests are here to pin what it does before
 * anyone picks it up -- and there is one thing about it worth pinning loudly, which is the subject
 * of {@link #englishIsNotAnEnglishCollator()}.
 */
public class GenericCollatorTest {

    // ---------- the three constructors ----------

    /**
     * The general constructor takes the two directions as functions, so X need not be a String.
     * Here it is an Integer, keyed by its zero-padded decimal form so that key order and numeric
     * order agree.
     */
    @Test
    public void theFunctionPairConstructorRoundTripsANonStringType() {
        final Collator collator = Collator.getInstance(Locale.ENGLISH);
        final GenericCollator<Integer> subject = new GenericCollator<>(
                i -> collator.getCollationKey(String.format("%08d", i)),
                key -> Integer.valueOf(key.getSourceString()));
        for (final int i : new int[]{0, 7, 42, 1_000, 99_999_999}) {
            final CollationKey key = subject.getCollationKey(i);
            assertEquals("round trip of " + i, Integer.valueOf(i), subject.getSourceString(key));
        }
    }

    @Test
    public void theCollatorConstructorRoundTripsStrings() {
        final GenericCollator<String> subject = new GenericCollator<>(Collator.getInstance(Locale.ENGLISH));
        for (final String s : new String[]{"", "a", "Willkommen", "café", "中文"})
            assertEquals(s, subject.getSourceString(subject.getCollationKey(s)));
    }

    @Test
    public void theDefaultConstructorRoundTripsStrings() {
        final GenericCollator<String> subject = new GenericCollator<>();
        for (final String s : new String[]{"", "a", "Willkommen", "café"})
            assertEquals(s, subject.getSourceString(subject.getCollationKey(s)));
    }

    @Test
    public void theEnglishInstanceRoundTripsStrings() {
        for (final String s : new String[]{"", "a", "Willkommen", "café", "中文"})
            assertEquals(s, GenericCollator.English.getSourceString(GenericCollator.English.getCollationKey(s)));
    }

    // ---------- English really is English now; CodePointOrder is the other one ----------

    /**
     * {@code GenericCollator.English} collates the way {@link Collator} does for English: "apple"
     * before "Banana", because case is a tertiary difference rather than a 31-place jump in code
     * point. Until 2026-09-24 it did the opposite, being code-point order under an English name.
     */
    @Test
    public void englishCollatesLikeAnEnglishCollator() {
        final Collator real = Collator.getInstance(Locale.ENGLISH);
        final String[] pairs = {"apple", "Banana", "a", "B", "zebra", "Zulu", "resume", "r\u00e9sum\u00e9"};
        for (int i = 0; i < pairs.length; i += 2) {
            final String x = pairs[i], y = pairs[i + 1];
            assertEquals("English should agree with Collator on " + x + " vs " + y,
                    Integer.signum(real.getCollationKey(x).compareTo(real.getCollationKey(y))),
                    Integer.signum(GenericCollator.English.getCollationKey(x)
                            .compareTo(GenericCollator.English.getCollationKey(y))));
        }
        assertTrue("apple before Banana", GenericCollator.English.getCollationKey("apple")
                .compareTo(GenericCollator.English.getCollationKey("Banana")) < 0);
    }

    /**
     * And it differs from code-point order on exactly the case that used to be wrong, so the two
     * static fields are not accidentally the same thing under two names.
     */
    @Test
    public void englishAndCodePointOrderDisagreeOnCase() {
        assertTrue("code-point order puts Banana first",
                GenericCollator.CodePointOrder.getCollationKey("Banana")
                        .compareTo(GenericCollator.CodePointOrder.getCollationKey("apple")) < 0);
        assertTrue("English puts apple first", GenericCollator.English.getCollationKey("apple")
                .compareTo(GenericCollator.English.getCollationKey("Banana")) < 0);
    }

    /** {@code CodePointOrder}'s key order is exactly {@code String.compareTo}, for every pair. */
    @Test
    public void codePointOrderKeyOrderIsExactlyStringOrder() {
        final String[] samples = {"", "A", "B", "a", "b", "aa", "ab", "Ab", "caf\u00e9", "cafe", "z", "Z", "\u4e2d"};
        for (final String x : samples)
            for (final String y : samples)
                assertEquals("compare " + x + " with " + y,
                        Integer.signum(x.compareTo(y)),
                        Integer.signum(GenericCollator.CodePointOrder.getCollationKey(x)
                                .compareTo(GenericCollator.CodePointOrder.getCollationKey(y))));
    }

    @Test
    public void codePointOrderKeyBytesAreTheUtf8Encoding() {
        for (final String s : new String[]{"", "abc", "caf\u00e9", "\u4e2d\u6587"})
            assertArrayEquals(s, s.getBytes(StandardCharsets.UTF_8),
                    GenericCollator.CodePointOrder.getCollationKey(s).toByteArray());
    }

    @Test
    public void bothStaticsRoundTripStrings() {
        for (final String s : new String[]{"", "a", "Willkommen", "caf\u00e9", "\u4e2d\u6587"}) {
            assertEquals(s, GenericCollator.English.getSourceString(GenericCollator.English.getCollationKey(s)));
            assertEquals(s, GenericCollator.CodePointOrder.getSourceString(
                    GenericCollator.CodePointOrder.getCollationKey(s)));
        }
    }

    /**
     * Every thread must get the same key for the same input.
     * <p>
     * NOTE this would have passed before the {@code ThreadLocal} was introduced too, and saying so
     * is the point: {@link java.text.RuleBasedCollator} is already thread-safe, having declared
     * {@code compare} and {@code getCollationKey} {@code synchronized} precisely because it reuses
     * its buffers between calls. The clone-per-thread is there to remove the monitor they all
     * serialize on, not to fix a race. What this test guards is the thing the change could
     * plausibly have broken -- that a cloned collator produces keys identical to the original's.
     */
    @Test
    public void everyThreadGetsTheSameKeyForTheSameInput() throws Exception {
        final String[] samples = {"apple", "Banana", "cherry", "Date", "\u00e9clair"};
        final java.util.List<CollationKey> expected = new java.util.ArrayList<>();
        for (final String s : samples) expected.add(GenericCollator.English.getCollationKey(s));
        final java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8);
        try {
            final java.util.List<java.util.concurrent.Future<Boolean>> futures = new java.util.ArrayList<>();
            for (int t = 0; t < 64; t++)
                futures.add(pool.submit(() -> {
                    for (int r = 0; r < 200; r++)
                        for (int i = 0; i < samples.length; i++)
                            if (GenericCollator.English.getCollationKey(samples[i]).compareTo(expected.get(i)) != 0)
                                return false;
                    return true;
                }));
            for (final java.util.concurrent.Future<Boolean> future : futures)
                assertTrue("keys must be identical whichever thread builds them", future.get());
        } finally {
            pool.shutdownNow();
        }
    }

    // ---------- what that means for husky coding, which is the point of the class ----------

    /**
     * {@code HuskyCoder.huskyEncode(CollationKey[])} builds each code from the first seven bytes of
     * {@code toByteArray}. UTF-8 preserves code-point order and {@code CodePointOrder} sorts by
     * code-point order, so the codes come out in the same order as the keys.
     */
    @Test
    public void huskyCodesFromCodePointKeysAreOrderPreserving() {
        final String[] sorted = {"aa", "ab", "b", "bb", "cat", "catalog", "dog", "zebra"};
        final CollationKey[] keys = Arrays.stream(sorted)
                .map(GenericCollator.CodePointOrder::getCollationKey).toArray(CollationKey[]::new);
        final Coding coding = HuskyCoderFactory.asciiCoder.huskyEncode(keys);
        for (int i = 1; i < coding.longs.length; i++)
            assertTrue("codes should increase: " + sorted[i - 1] + " then " + sorted[i],
                    coding.longs[i - 1] < coding.longs[i]);
        assertTrue("seven bytes or fewer, so the coding is perfect", coding.perfect);
    }

    /**
     * The regression test for TODO.md item 46. {@code huskyEncode(byte[])} now pads on the right,
     * so a short key no longer codes below a longer one that it ought to precede: {@code "ab"} is
     * {@code 0x61_6200_0000_0000} where it used to be {@code 0x6162}. Note the width: the field is
     * seven bytes, not eight, so the top byte stays clear of the sign bit and a two-byte key shifts
     * left by 40 rather than 48. Before the fix the key assertions passed and the code ones failed.
     */
    @Test
    public void huskyCodesAreOrderPreservingAcrossKeyLengths() {
        final String[] sorted = {"a", "ab", "abc", "b", "ba", "c"};
        final CollationKey[] keys = Arrays.stream(sorted)
                .map(GenericCollator.CodePointOrder::getCollationKey).toArray(CollationKey[]::new);
        final Coding coding = HuskyCoderFactory.asciiCoder.huskyEncode(keys);
        for (int i = 1; i < sorted.length; i++) {
            assertTrue("keys order: " + sorted[i - 1] + " before " + sorted[i],
                    keys[i - 1].compareTo(keys[i]) < 0);
            assertTrue("and so must their codes: " + sorted[i - 1] + " then " + sorted[i],
                    coding.longs[i - 1] < coding.longs[i]);
        }
        assertEquals("ab is left-aligned within the seven-byte field", 0x61620000000000L, coding.longs[1]);
    }

    /**
     * Beyond seven bytes the code is a prefix, so the coding reports imperfect -- which is what
     * tells the sort to run its cleanup pass. The budget is bytes and not characters, so four
     * Chinese characters are twelve UTF-8 bytes and exceed it.
     */
    @Test
    public void longerKeysReportAnImperfectCoding() {
        assertFalse("eight ASCII bytes exceed the seven-byte budget",
                HuskyCoderFactory.asciiCoder.huskyEncode(
                        new CollationKey[]{GenericCollator.CodePointOrder.getCollationKey("abcdefgh")}).perfect);
        assertFalse("four Chinese characters are twelve UTF-8 bytes",
                HuskyCoderFactory.asciiCoder.huskyEncode(
                        new CollationKey[]{GenericCollator.CodePointOrder.getCollationKey("\u4e2d\u6587\u4e2d\u6587")}).perfect);
        assertTrue("seven bytes exactly is still within budget",
                HuskyCoderFactory.asciiCoder.huskyEncode(
                        new CollationKey[]{GenericCollator.CodePointOrder.getCollationKey("abcdefg")}).perfect);
    }

    /**
     * Two strings sharing their first seven bytes collide, which is the ordinary imperfect-coding
     * case the cleanup pass exists to repair -- a property of the seven-byte budget rather than of
     * this collator.
     */
    @Test
    public void stringsSharingASevenBytePrefixCollide() {
        final CollationKey a = GenericCollator.CodePointOrder.getCollationKey("abcdefgh");
        final CollationKey b = GenericCollator.CodePointOrder.getCollationKey("abcdefgz");
        final Coding coding = HuskyCoderFactory.asciiCoder.huskyEncode(new CollationKey[]{a, b});
        assertEquals("the first seven bytes are shared, so the codes are equal",
                coding.longs[0], coding.longs[1]);
        assertNotEquals("but the keys themselves still order correctly", 0, a.compareTo(b));
    }
}
