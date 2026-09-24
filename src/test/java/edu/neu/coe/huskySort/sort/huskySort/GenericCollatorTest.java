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

    // ---------- what CollationKeyEnglish actually orders by ----------

    /**
     * <b>The one to read.</b> Despite the name, {@code GenericCollator.English} does not collate the
     * way {@code Collator.getInstance(Locale.ENGLISH)} does: its key compares by
     * {@code String.compareTo}, which is UTF-16 code-point order, so every upper-case letter sorts
     * before every lower-case one. A real English collator orders "apple" before "Banana"; this one
     * puts "Banana" first.
     * <p>
     * That is a legitimate choice -- code-point order is cheap, total and stable, which is what a
     * husky code wants -- but it is not what the name suggests, and a caller who swapped this in
     * expecting locale-aware behaviour would get a silently different order rather than an error.
     * Asserted in both directions so the test fails if either side changes.
     */
    @Test
    public void englishIsNotAnEnglishCollator() {
        final Collator real = Collator.getInstance(Locale.ENGLISH);
        assertTrue("a real English collator puts apple before Banana",
                real.getCollationKey("apple").compareTo(real.getCollationKey("Banana")) < 0);
        assertTrue("GenericCollator.English puts Banana before apple, being code-point order",
                GenericCollator.English.getCollationKey("Banana")
                        .compareTo(GenericCollator.English.getCollationKey("apple")) < 0);
    }

    /**
     * The corollary: its key order is exactly {@code String.compareTo}, for every pair.
     */
    @Test
    public void englishKeyOrderIsExactlyStringOrder() {
        final String[] samples = {"", "A", "B", "a", "b", "aa", "ab", "Ab", "café", "cafe", "z", "Z", "中"};
        for (final String x : samples)
            for (final String y : samples)
                assertEquals("compare " + x + " with " + y,
                        Integer.signum(x.compareTo(y)),
                        Integer.signum(GenericCollator.English.getCollationKey(x)
                                .compareTo(GenericCollator.English.getCollationKey(y))));
    }

    @Test
    public void englishKeyBytesAreTheUtf8Encoding() {
        for (final String s : new String[]{"", "abc", "café", "中文"})
            assertArrayEquals(s, s.getBytes(StandardCharsets.UTF_8),
                    GenericCollator.English.getCollationKey(s).toByteArray());
    }

    // ---------- what that means for husky coding, which is the point of the class ----------

    /**
     * {@code HuskyCoder.huskyEncode(CollationKey[])} builds each code from the first seven bytes of
     * {@code toByteArray}. UTF-8 preserves code-point order and this key sorts by code-point order,
     * so among keys of <b>equal length</b> the codes come out in the same order as the keys. That
     * agreement is the reason the class exists.
     */
    @Test
    public void huskyCodesFromEnglishKeysAreOrderPreservingAtEqualLength() {
        final String[] sorted = {"aaa", "aab", "abz", "baa", "cat", "dog", "zzz"};
        final CollationKey[] keys = Arrays.stream(sorted)
                .map(GenericCollator.English::getCollationKey).toArray(CollationKey[]::new);
        final Coding coding = HuskyCoderFactory.asciiCoder.huskyEncode(keys);
        for (int i = 1; i < coding.longs.length; i++)
            assertTrue("codes should increase: " + sorted[i - 1] + " then " + sorted[i],
                    coding.longs[i - 1] < coding.longs[i]);
        assertTrue("seven bytes or fewer, so the coding is perfect", coding.perfect);
    }

    /**
     * <b>A latent defect, recorded rather than worked around.</b> Across keys of <i>different</i>
     * length the codes are not order-preserving, and the cause is that
     * {@code HuskyCoder.huskyEncode(byte[])} does not pad:
     * <pre>
     *     for (int i = 0; i &lt; bs.length &amp;&amp; i &lt; 7; i++) result = (result &lt;&lt; 8) | bs[i] &amp; 0xFF;
     * </pre>
     * A two-byte key therefore lands in the low sixteen bits and a seven-byte key fills the long,
     * so "b" (0x62) codes below "ab" (0x6162) although "ab" sorts first. Compare
     * {@code HuskyCoderFactory.stringToLong}, which shifts by {@code bitWidth * padding} at the end
     * for exactly this reason; the byte-array path was never given the same treatment.
     * <p>
     * It is a quality defect and not a correctness one --- an imperfect code is legal, and the
     * cleanup pass repairs the order --- but it throws away order preservation that a single shift
     * would buy. It is dormant: the only production user of this path is
     * {@code HuskyCoderFactory.chineseEncoderCollator}, which no benchmark uses. See TODO.md item 46.
     * <p>
     * This test asserts the defect, so it will fail if anyone fixes it. That is deliberate --- the
     * fix should come with a decision about the figures, not by surprise.
     */
    @Test
    public void huskyCodesAreNotOrderPreservingAcrossKeyLengths() {
        final Coding coding = HuskyCoderFactory.asciiCoder.huskyEncode(new CollationKey[]{
                GenericCollator.English.getCollationKey("ab"),
                GenericCollator.English.getCollationKey("b")});
        assertTrue("\"ab\" sorts before \"b\"",
                GenericCollator.English.getCollationKey("ab")
                        .compareTo(GenericCollator.English.getCollationKey("b")) < 0);
        assertTrue("but its code is the larger, because the shorter key is not padded",
                coding.longs[0] > coding.longs[1]);
    }

    /**
     * Beyond seven bytes the code is a prefix, so the coding reports imperfect -- which is what
     * tells the sort to run its cleanup pass. A key that is long only because it is non-ASCII
     * counts the same way, since the budget is bytes and not characters: four Chinese characters
     * are twelve UTF-8 bytes.
     */
    @Test
    public void longerKeysReportAnImperfectCoding() {
        assertFalse("eight ASCII bytes exceed the seven-byte budget",
                HuskyCoderFactory.asciiCoder.huskyEncode(
                        new CollationKey[]{GenericCollator.English.getCollationKey("abcdefgh")}).perfect);
        assertFalse("four Chinese characters are twelve UTF-8 bytes",
                HuskyCoderFactory.asciiCoder.huskyEncode(
                        new CollationKey[]{GenericCollator.English.getCollationKey("中文中文")}).perfect);
        assertTrue("seven bytes exactly is still within budget",
                HuskyCoderFactory.asciiCoder.huskyEncode(
                        new CollationKey[]{GenericCollator.English.getCollationKey("abcdefg")}).perfect);
    }

    /**
     * Two strings sharing their first seven bytes collide, which is the ordinary imperfect-coding
     * case the cleanup pass exists to repair -- recorded here so that the collision is known to be
     * a property of the encoding rather than of this collator.
     */
    @Test
    public void stringsSharingASevenByterefixCollide() {
        final CollationKey a = GenericCollator.English.getCollationKey("abcdefgh");
        final CollationKey b = GenericCollator.English.getCollationKey("abcdefgz");
        final Coding coding = HuskyCoderFactory.asciiCoder.huskyEncode(new CollationKey[]{a, b});
        assertEquals("the first seven bytes are shared, so the codes are equal",
                coding.longs[0], coding.longs[1]);
        assertNotEquals("but the keys themselves still order correctly", 0, a.compareTo(b));
    }
}
