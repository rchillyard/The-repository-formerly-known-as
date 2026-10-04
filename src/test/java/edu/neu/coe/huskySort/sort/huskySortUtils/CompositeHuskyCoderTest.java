/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Tests for {@link CompositeHuskyCoder} and {@link HuskyFieldCoder}, TODO.md item 31 stage one.
 * <p>
 * The test that matters is {@link #reproducesPermitCoderExactly()}: this project already contains a
 * hand-written composite coder, carefully documented and verified against a real corpus, so the
 * combinator has a ground truth to be checked against rather than merely a specification to be
 * checked for self-consistency. If the derived coder and the hand-written one disagree on any of
 * 198,900 records, one of them is wrong.
 */
public class CompositeHuskyCoderTest {

    /**
     * The same three fields {@code PermitCoder} packs, declared rather than hand-shifted.
     */
    private static CompositeHuskyCoder<Permit> permitCoder() {
        return CompositeHuskyCoder.<Permit>builder()
                .named("DerivedPermitCoder")
                .add(Permit::getBlock, HuskyFieldCoder.ofString("block", 5, PermitCoder.BLOCK_ALPHABET))
                .add(Permit::getLot, HuskyFieldCoder.ofString("lot", 4, PermitCoder.LOT_ALPHABET))
                // 1879, not 1878: the corpus runs from offset 1 (2013-01-02) to offset 1879
                // (2018-02-23), so PermitCoder's "span 1,878 days" is max minus min, and the bound
                // the field must hold is the greater offset. Both give eleven bits, but declaring
                // the span rather than the bound clamps the last day and was the first thing this
                // test caught.
                .add(Permit::getFiledDate, HuskyFieldCoder.ofDate("filed", PermitCoder.EPOCH, 1879))
                .build();
    }

    /**
     * The acceptance test. Identical codes for every record in the corpus, not merely a consistent
     * ordering: an ordering check would pass for a coder that happened to agree on this data while
     * packing differently, and the point is that the combinator derives the same encoding a careful
     * human wrote by hand.
     */
    @Test
    public void reproducesPermitCoderExactly() {
        final Permit[] permits = PermitLoader.getPermits();
        assertTrue("the corpus should be substantial", permits.length > 190_000);
        final CompositeHuskyCoder<Permit> derived = permitCoder();
        assertEquals("the same 60 bits PermitCoder documents", 60, derived.bits());
        for (final Permit p : permits) {
            final long expected = PermitCoder.INSTANCE.huskyEncode(p);
            // Undo the sign bias to compare the folds themselves. PermitCoder spends 60 of 64 bits
            // and so never sets bit 63, which is why it could omit the bias; this coder applies it
            // unconditionally because it does not know in advance how many bits its fields fill.
            // Removing it recovers bit-for-bit identity, which is the claim being made.
            assertEquals("codes must agree for " + p.getBlock() + "/" + p.getLot() + "/" + p.getFiledDate(),
                    expected, derived.huskyEncode(p) ^ CompositeHuskyCoder.SIGN_BIAS);
        }
    }

    /**
     * {@code PermitCoder.perfect()} is true and is verified against this corpus. The derived coder
     * computes the same claim per element instead of declaring it, so it must reach the same
     * answer -- and reaching it by computation is the stronger position, since it would notice a
     * corpus that grew a character outside the alphabet.
     */
    @Test
    public void agreesThatTheCorpusIsExactlyEncodable() {
        final Permit[] permits = PermitLoader.getPermits();
        assertTrue(PermitCoder.INSTANCE.perfect());
        assertTrue("every record must be exactly encodable", permitCoder().huskyEncode(permits).perfect);
    }

    /**
     * Order preservation over the real corpus, against the type's own {@code compareTo} rather than
     * against the coder's derived comparator -- which would be circular.
     */
    @Test
    public void codeOrderAgreesWithPermitOrderOverTheCorpus() {
        final Permit[] permits = Arrays.copyOf(PermitLoader.getPermits(), 40_000);
        final CompositeHuskyCoder<Permit> derived = permitCoder();
        Arrays.sort(permits, Comparator.comparingLong(derived::huskyEncode));
        for (int i = 1; i < permits.length; i++)
            assertTrue("sorted by code, but " + permits[i - 1].getBlock() + " follows " + permits[i].getBlock(),
                    permits[i - 1].compareTo(permits[i]) <= 0);
    }

    // ---------- the budget, which is item 29's point ----------

    /**
     * Over budget is imperfection, not an error. Robin's point, 2026-09-29: running out of bits is
     * the same kind of problem as a saturating field or an over-long string, and the mechanism is
     * built to tolerate all three. So the coder builds, loses the low bits of the least significant
     * fields, and says it is no longer perfect.
     */
    @Test
    public void truncatesRatherThanRefusingWhenTheFieldsDoNotFit() {
        final CompositeHuskyCoder<Permit> c = CompositeHuskyCoder.<Permit>builder()
                .named("TooWide")
                // Nine characters of block, not eight: the drop has to reach a character position
                // the corpus actually uses. Every lot in the corpus is at most four characters, so
                // a field of six positions carries twelve bits of trailing padding, and truncating
                // those twelve loses nothing whatever -- the coder is truncating and the corpus is
                // still exact, which is the per-element rule of
                // aTruncatedFieldIsStillExactForAValueWhoseLostBitsWereZero, met by accident. This
                // configuration drops five bits of the lot's fourth character instead.
                .add(Permit::getBlock, HuskyFieldCoder.ofString("block", 9, PermitCoder.BLOCK_ALPHABET))
                .add(Permit::getLot, HuskyFieldCoder.ofString("lot", 4, PermitCoder.LOT_ALPHABET))
                .build();
        assertEquals("45 + 24 = 69 declared", 69, c.declaredBits());
        assertEquals("clamped to the budget", 64, c.bits());
        assertTrue(c.truncating());
        assertTrue("toString should say what was lost: " + c, c.toString().contains("lot=24(-5)"));
        assertFalse("a corpus cannot be perfectly encoded by a truncating coder",
                c.huskyEncode(PermitLoader.getPermits()).perfect);
    }

    /**
     * <b>The property that makes truncation acceptable.</b> Dropping low bits weakens the ordering
     * to ties among values the lost bits would have separated; dropping high bits would invert it,
     * and nothing downstream could repair that. So an over-budget coder must still never place two
     * permits in the wrong order -- only fail to separate them.
     */
    @Test
    public void truncationWeakensTheOrderingButNeverInvertsIt() {
        final Permit[] permits = Arrays.copyOf(PermitLoader.getPermits(), 40_000);
        final CompositeHuskyCoder<Permit> c = CompositeHuskyCoder.<Permit>builder()
                .add(Permit::getBlock, HuskyFieldCoder.ofString("block", 9, PermitCoder.BLOCK_ALPHABET))
                .add(Permit::getLot, HuskyFieldCoder.ofString("lot", 4, PermitCoder.LOT_ALPHABET))
                .build();
        assertTrue("the drop must reach a character the corpus uses, or this proves nothing",
                c.truncating() && !c.huskyEncode(PermitLoader.getPermits()).perfect);
        int ties = 0;
        for (final Permit x : permits)
            for (int k = 0; k < 3; k++) {
                final Permit y = permits[(x.hashCode() * 31 + k) & 0x7FFF];
                final int byPermit = Integer.signum(x.compareTo(y));
                final int byCode = Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y)));
                if (byCode == 0) ties++;
                else assertEquals("the code must never invert the permits' own order", byPermit, byCode);
            }
        assertTrue("and some pairs really were only tied, or this proves nothing", ties > 0);
    }

    /**
     * A field pushed entirely past the budget contributes nothing at all, rather than wrapping.
     */
    @Test
    public void aFieldWithNoRoomLeftContributesNothing() {
        final CompositeHuskyCoder<Permit> wide = CompositeHuskyCoder.<Permit>builder()
                .add(Permit::getBlock, HuskyFieldCoder.ofString("block", 13, PermitCoder.BLOCK_ALPHABET))
                .add(Permit::getFiledDate, HuskyFieldCoder.ofDate("filed", PermitCoder.EPOCH, 1879))
                .build();
        assertEquals("13 x 5 for the block plus 11 for the date", 76, wide.declaredBits());
        assertEquals(64, wide.bits());
        assertTrue("the block alone overruns, losing one bit: " + wide, wide.toString().contains("block=65(-1)"));
        assertTrue("so the date is wholly past the boundary: " + wide, wide.toString().contains("filed=11(dropped)"));
        // With the block already filling the budget, adding the date must change nothing at all.
        final CompositeHuskyCoder<Permit> blockOnly = CompositeHuskyCoder.<Permit>builder()
                .add(Permit::getBlock, HuskyFieldCoder.ofString("block", 13, PermitCoder.BLOCK_ALPHABET))
                .build();
        for (final Permit p : Arrays.copyOf(PermitLoader.getPermits(), 5_000))
            assertEquals("a dropped field must contribute nothing, not wrap into the field above",
                    blockOnly.huskyEncode(p), wide.huskyEncode(p));
    }

    /**
     * Truncation is a property of the coder, but exactness stays a property of the element: a
     * dropped bit that happened to be zero lost nothing for that element, and reporting it inexact
     * would make a husky sort run a cleanup pass it does not need.
     */
    @Test
    public void aTruncatedFieldIsStillExactForAValueWhoseLostBitsWereZero() {
        final HuskyFieldCoder<Long> low = HuskyFieldCoder.ofRange("low", 0, 255);
        final CompositeHuskyCoder<Long> c = CompositeHuskyCoder.<Long>builder()
                .add(Function.identity(), HuskyFieldCoder.ofRange("high", 0, (1L << 58) - 1))
                .add(v -> v, low)
                .build();
        assertTrue(c.truncating());
        assertEquals("58 + 8 declared, 2 of the low field lost", 66, c.declaredBits());
        assertTrue("a value whose low 3 bits are zero loses nothing", c.exact(8L));
        assertFalse("one whose low bits are set does", c.exact(9L));
    }

    public record Pair(int high, int low) { }

    /**
     * <b>TODO.md item 49.</b> Two full-range {@code int}s declare thirty-two bits each, which is a
     * machine word exactly -- and until 2026-09-30 that was one bit too many, because the budget
     * reserved the sign bit. The commonest composite of all was truncated and reported imperfect
     * for want of the one bit the reservation cost.
     * <p>
     * Spending the sign bit and biasing the fold instead makes it exact. The bias is what earns the
     * bit: without it, a pair whose high int is negative would fold to a code with bit 63 clear and
     * sort <i>above</i> every pair whose high int is positive.
     */
    @Test
    public void twoIntsFillTheWordExactlyAndOrderAcrossTheSignBit() {
        final CompositeHuskyCoder<Pair> c = CompositeHuskyCoder.<Pair>builder()
                .named("TwoInts")
                .add(Pair::high, HuskyFieldCoder.ofRange("high", Integer.MIN_VALUE, Integer.MAX_VALUE))
                .add(Pair::low, HuskyFieldCoder.ofRange("low", Integer.MIN_VALUE, Integer.MAX_VALUE))
                .build();
        assertEquals("two ints are a machine word", 64, c.bits());
        assertFalse("and no longer a bit over budget", c.truncating());
        final Pair[] xs = {
                new Pair(Integer.MIN_VALUE, Integer.MIN_VALUE), new Pair(Integer.MIN_VALUE, Integer.MAX_VALUE),
                new Pair(-1, 0), new Pair(0, -1), new Pair(0, 0), new Pair(0, 1),
                new Pair(1, Integer.MIN_VALUE), new Pair(Integer.MAX_VALUE, Integer.MAX_VALUE)};
        final Comparator<Pair> natural = Comparator.comparingInt(Pair::high).thenComparingInt(Pair::low);
        for (final Pair x : xs)
            for (final Pair y : xs)
                assertEquals("signed comparison of the codes must give the lexicographic order of " + x + " and " + y,
                        Integer.signum(natural.compare(x, y)),
                        Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y))));
        assertTrue("and a coder that loses nothing must say it is perfect", c.huskyEncode(xs).perfect);
        assertTrue("the fold really does cross bit 63, which is the whole point",
                c.huskyEncode(new Pair(0, 0)) >= 0);
        assertTrue(c.huskyEncode(new Pair(-1, 0)) < 0);
    }

    /**
     * The other two field sets that a reserved sign bit cost a bit: four {@code char}s and eight
     * {@code byte}s. Both were a bit short of a machine word; both are now exact.
     */
    @Test
    public void theOtherWordFillingFieldSetsAreNowExactToo() {
        record FourChars(char a, char b, char c, char d) { }
        final CompositeHuskyCoder<FourChars> chars = CompositeHuskyCoder.<FourChars>builder()
                // A cast per accessor because ofRange takes a Number and a Character is not one;
                // RecordHuskyCoder normalises char for you, a hand-built composite does not.
                .add(x -> (int) x.a(), HuskyFieldCoder.ofRange("a", 0, 0xFFFF))
                .add(x -> (int) x.b(), HuskyFieldCoder.ofRange("b", 0, 0xFFFF))
                .add(x -> (int) x.c(), HuskyFieldCoder.ofRange("c", 0, 0xFFFF))
                .add(x -> (int) x.d(), HuskyFieldCoder.ofRange("d", 0, 0xFFFF))
                .build();
        assertEquals("four 16-bit characters", 64, chars.bits());
        assertFalse(chars.truncating());
        assertTrue(chars.huskyEncode(new FourChars[]{new FourChars('\uFFFF', 'a', '\u0000', 'z')}).perfect);

        record EightBytes(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h) { }
        final List<Function<EightBytes, Byte>> reads = List.of(EightBytes::a, EightBytes::b, EightBytes::c,
                EightBytes::d, EightBytes::e, EightBytes::f, EightBytes::g, EightBytes::h);
        CompositeHuskyCoder.Builder<EightBytes> builder = CompositeHuskyCoder.builder();
        for (int i = 0; i < reads.size(); i++)
            builder = builder.add(reads.get(i), HuskyFieldCoder.ofRange("b" + i, Byte.MIN_VALUE, Byte.MAX_VALUE));
        final CompositeHuskyCoder<EightBytes> bytes = builder.build();
        assertEquals("eight bytes", 64, bytes.bits());
        assertFalse(bytes.truncating());
    }

    /**
     * A {@code long} field is the case {@link HuskyFieldCoder#ofLong} exists for: its span is
     * {@code 2^64 - 1}, which {@code ofRange}'s {@code long} arithmetic cannot express at all, so
     * the full range is a coder of its own rather than a range with wide bounds. It is also the
     * case that reads oddest -- the type husky coding is <i>defined</i> in terms of was the one the
     * generic mechanism could not encode.
     * <p>
     * A composite of one such field must reproduce {@code HuskyCoderFactory.longCoder} exactly: the
     * field's signed-to-unsigned bias and the composite's unsigned-to-signed bias cancel, leaving
     * the value itself.
     */
    @Test
    public void aLongFieldIsExactAndAgreesWithLongCoder() {
        final CompositeHuskyCoder<Long> c = CompositeHuskyCoder.<Long>builder()
                .named("OneLong").add(Function.identity(), HuskyFieldCoder.ofLong("v")).build();
        assertEquals("the whole word, which is what a long is", 64, c.bits());
        assertFalse(c.truncating());
        final Long[] xs = {Long.MIN_VALUE, -1L, 0L, 1L, 42L, Long.MAX_VALUE};
        for (final Long x : xs)
            assertEquals("the two biases cancel, leaving the value", (long) x, c.huskyEncode(x));
        assertArrayEquals("so the composite is longCoder", HuskyCoderFactory.longCoder.huskyEncode(xs).longs,
                c.huskyEncode(xs).longs);
        assertTrue(c.huskyEncode(xs).perfect);
    }

    /**
     * Truncating is deliberate and silent, and what it silently costs is the cleanup pass. So it
     * warns --- <b>once, when the coder is built</b>, never per element. That distinction is the
     * whole reason this is acceptable: a coder is constructed once and then used for every
     * element of every array it codes, so a per-element log would be ruinous for a sort whose
     * premise is doing work once per element rather than once per comparison.
     */
    @Test
    public void truncatingWarnsOncePerCoderAndNeverPerElement() {
        final List<String> warnings = new ArrayList<>();
        final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CompositeHuskyCoder.class);
        final org.apache.log4j.Appender appender = new org.apache.log4j.AppenderSkeleton() {
            protected void append(final org.apache.log4j.spi.LoggingEvent event) {
                if (event.getLevel().toInt() >= org.apache.log4j.Level.WARN_INT)
                    warnings.add(String.valueOf(event.getMessage()));
            }

            public void close() {
            }

            public boolean requiresLayout() {
                return false;
            }
        };
        logger.addAppender(appender);
        try {
            CompositeHuskyCoder.<Permit>builder()
                    .add(Permit::getBlock, HuskyFieldCoder.ofString("block", 9, PermitCoder.BLOCK_ALPHABET))
                    .add(Permit::getLot, HuskyFieldCoder.ofString("lot", 4, PermitCoder.LOT_ALPHABET))
                    .build()
                    .huskyEncode(PermitLoader.getPermits());   // 198,900 elements, after the one warning
            assertEquals("exactly one warning, from build() and not from coding 198,900 permits",
                    1, warnings.size());
            final String warning = warnings.get(0);
            assertTrue("it should say how many bits were declared: " + warning, warning.contains("69"));
            assertTrue("and what the budget is: " + warning, warning.contains("64"));
            assertTrue("and what it costs, which is the part worth knowing: " + warning,
                    warning.contains("cleanup pass"));

            warnings.clear();
            permitCoder().huskyEncode(PermitLoader.getPermits());
            assertEquals("and a coder that fits says nothing at all", 0, warnings.size());
        } finally {
            logger.removeAppender(appender);
        }
    }

    @Test
    public void refusesToBuildWithNoFields() {
        try {
            CompositeHuskyCoder.<Permit>builder().build();
            fail("a coder with no fields encodes nothing");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("no fields"));
        }
    }

    // ---------- the field coders ----------

    /**
     * The field's own type is inferred, so an int accessor needs no widening lambda. Until
     * 2026-09-29 {@code ofRange} was fixed to {@code Long} and every integral field had to be
     * written {@code add(p -> (long) p.high(), ...)} for a reason the caller could not see.
     */
    @Test
    public void rangeCoderTakesTheFieldsOwnTypeWithoutAWideningLambda() {
        record Pair(int high, short low) { }
        final CompositeHuskyCoder<Pair> c = CompositeHuskyCoder.<Pair>builder()
                .add(Pair::high, HuskyFieldCoder.ofRange("high", 0, 255))
                .add(Pair::low, HuskyFieldCoder.ofRange("low", 0, 1000))
                .build();
        assertEquals(18, c.bits());
        assertTrue(c.huskyEncode(new Pair(1, (short) 0)) > c.huskyEncode(new Pair(0, (short) 1000)));
        assertTrue(c.exact(new Pair(200, (short) 999)));
        assertFalse("and the declared range is still enforced", c.exact(new Pair(300, (short) 0)));
    }

    @Test
    public void rangeCoderUsesTheNarrowestWidthAndBiasesByTheMinimum() {
        final HuskyFieldCoder<Long> zip = HuskyFieldCoder.ofRange("zip", 0, 99_999);
        assertEquals("99,999 needs seventeen bits, as Tuple assumes", 17, zip.bits());
        final HuskyFieldCoder<Long> year = HuskyFieldCoder.ofRange("year", 1850, 2020);
        assertEquals("a span of 170 needs eight, as Tuple assumes", 8, year.bits());
        assertEquals("the minimum codes as zero", 0L, (long) year.encode(1850L));
        assertEquals(170L, (long) year.encode(2020L));
        assertTrue(year.encode(1900L) > year.encode(1899L));
    }

    /**
     * Out of range must clamp and report inexact, never wrap. Wrapping is what turns a weakened
     * ordering into an inverted one, and an inverted field cannot be repaired by the cleanup pass.
     */
    @Test
    public void rangeCoderClampsRatherThanWrapping() {
        final HuskyFieldCoder<Long> f = HuskyFieldCoder.ofRange("f", 10, 20);
        assertEquals(0L, (long) f.encode(5L));
        assertEquals(10L, (long) f.encode(25L));
        assertFalse(f.exact(5L));
        assertFalse(f.exact(25L));
        assertTrue(f.exact(15L));
        assertTrue("clamping preserves the weak ordering", f.encode(5L) <= f.encode(15L));
    }

    /**
     * Padding with zero is what makes a prefix sort first, matching {@code String.compareTo}.
     */
    /**
     * A range wider than {@code Long.MAX_VALUE} wraps when the width is computed from
     * {@code max - min}, so it is refused with a message naming the coder that does handle it.
     * The full signed range is the case that reaches this in practice.
     */
    @Test
    public void rangeCoderRefusesASpanItCannotExpress() {
        for (final long[] bounds : new long[][]{{Long.MIN_VALUE, Long.MAX_VALUE}, {Long.MIN_VALUE, 100}, {-(1L << 62), 1L << 62}})
            try {
                HuskyFieldCoder.ofRange("f", bounds[0], bounds[1]);
                fail("a span of more than Long.MAX_VALUE cannot be held in long arithmetic: " + Arrays.toString(bounds));
            } catch (final IllegalArgumentException e) {
                assertTrue("it should name the coder that does handle it: " + e.getMessage(),
                        e.getMessage().contains("ofLong"));
            }
    }

    @Test
    public void stringCoderPadsSoThatAPrefixSortsFirst() {
        final HuskyFieldCoder<String> f = HuskyFieldCoder.ofString("s", 4, "ABCDE");
        assertEquals("five symbols need three bits each, four positions", 12, f.bits());
        assertTrue(f.encode("AB") < f.encode("ABA"));
        assertTrue(f.encode("A") < f.encode("B"));
        assertTrue(f.encode("ABCD") < f.encode("ABCE"));
        assertTrue("truncated beyond its width, and inexact", f.encode("ABCDE") == f.encode("ABCD"));
        assertFalse(f.exact("ABCDE"));
        assertFalse("a character outside the alphabet is not exact", f.exact("ABZ"));
        assertTrue(f.exact("ABCD"));
    }

    /**
     * An out-of-alphabet character takes the code of the largest symbol below it, so the ordering
     * weakens to a tie rather than inverting. {@code PermitCoder} relies on this and its own test
     * could not reach the branch; this one does.
     */
    @Test
    public void stringCoderWeakensRatherThanInvertsOutsideTheAlphabet() {
        final HuskyFieldCoder<String> f = HuskyFieldCoder.ofString("s", 2, "ACE");
        assertEquals("B is not in the alphabet, so it takes A's code", f.encode("A"), f.encode("B"));
        assertEquals("D likewise takes C's", f.encode("C"), f.encode("D"));
        assertEquals("below everything in the alphabet, it codes as padding", f.encode(""), f.encode("0"));
        assertTrue("and the weak ordering still holds", f.encode("B") <= f.encode("C"));
    }

    @Test
    public void stringCoderRejectsAnAlphabetOutOfOrder() {
        try {
            HuskyFieldCoder.ofString("s", 2, "ACB");
            fail("an alphabet must ascend, or the codes invert");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("ascend"));
        }
    }

    @Test
    public void dateCoderIsChronological() {
        final LocalDate epoch = LocalDate.of(2013, 1, 1);
        final HuskyFieldCoder<LocalDate> f = HuskyFieldCoder.ofDate("d", epoch, 1879);
        assertEquals("an offset of 1,879 needs eleven bits, as PermitCoder's DATE_BITS has it", 11, f.bits());
        assertEquals(0L, (long) f.encode(epoch));
        assertTrue(f.encode(LocalDate.of(2013, 1, 2)) > f.encode(epoch));
        assertTrue(f.encode(LocalDate.of(2017, 1, 1)) > f.encode(LocalDate.of(2016, 12, 31)));
        assertFalse("before the epoch is not exact", f.exact(LocalDate.of(2012, 12, 31)));
        assertTrue("the last representable offset is exact", f.exact(epoch.plusDays(1879)));
        assertFalse("beyond the window is not exact", f.exact(epoch.plusDays(1880)));
    }

    @Test
    public void booleanAndEnumCoders() {
        final HuskyFieldCoder<Boolean> b = HuskyFieldCoder.ofBoolean("b");
        assertEquals(1, b.bits());
        assertTrue("false sorts below true, as Boolean.compareTo has it", b.encode(false) < b.encode(true));
        final HuskyFieldCoder<java.time.DayOfWeek> d = HuskyFieldCoder.ofEnum("day", java.time.DayOfWeek.class);
        assertEquals("seven constants need three bits", 3, d.bits());
        assertTrue(d.encode(java.time.DayOfWeek.MONDAY) < d.encode(java.time.DayOfWeek.SUNDAY));
    }

    @Test
    public void bitsForIsTheNarrowestWidthThatHoldsTheSpan() {
        assertEquals("even zero needs a bit", 1, HuskyFieldCoder.bitsFor(0));
        assertEquals(1, HuskyFieldCoder.bitsFor(1));
        assertEquals(2, HuskyFieldCoder.bitsFor(2));
        assertEquals(2, HuskyFieldCoder.bitsFor(3));
        assertEquals(3, HuskyFieldCoder.bitsFor(4));
        assertEquals(17, HuskyFieldCoder.bitsFor(99_999));
        assertEquals(11, HuskyFieldCoder.bitsFor(1878));
    }

    // ---------- the width contract, which only a hand-written field coder can break ----------

    /** A field coder that lies: declares seven bits, returns up to 255. */
    private static HuskyFieldCoder<Long> liar(final int declared) {
        return new HuskyFieldCoder<>() {
            public int bits() {
                return declared;
            }

            public long encode(final Long v) {
                return v;
            }

            public String name() {
                return "liar";
            }
        };
    }

    /**
     * A field returning more bits than it declared corrupts the fields <b>above</b> it, and
     * corrupting a higher-priority field inverts the composite ordering rather than weakening it --
     * so unlike every other imperfection here, it cannot be left to the cleanup pass.
     * <p>
     * Measured before the check existed: a field declaring seven bits and returning eight inverted
     * <b>66,048</b> pairs of a four-by-256 grid while {@code exact()} reported true throughout. A
     * wrong ordering claimed as perfect is the worst failure this project has, which is why this
     * throws rather than masking.
     */
    @Test
    public void aFieldWiderThanItDeclaresIsRefusedRatherThanCorruptingTheFieldAboveIt() {
        final CompositeHuskyCoder<Long> c = CompositeHuskyCoder.<Long>builder()
                .add(v -> v >> 8, HuskyFieldCoder.ofRange("high", 0, 255))
                .add(v -> v & 0xFF, liar(7))
                .build();
        assertEquals("fits comfortably, so nothing is truncated", 15, c.bits());
        c.huskyEncode(0x0100L);
        try {
            c.huskyEncode(0x0080L);
            fail("a seven-bit field returning 128 should be refused");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("liar"));
            assertTrue("it should say what was declared: " + e.getMessage(), e.getMessage().contains("7 bits"));
            assertTrue("and what came back: " + e.getMessage(), e.getMessage().contains("128"));
            assertTrue("and what that needed: " + e.getMessage(), e.getMessage().contains("needs 8"));
        }
    }

    /**
     * {@code exact} reads the field coders too, so it must apply the same check -- otherwise a
     * caller asking whether an array encodes perfectly would get an answer computed from corrupt
     * codes.
     */
    @Test
    public void theWidthCheckAppliesToExactAsWellAsToEncode() {
        final CompositeHuskyCoder<Long> c = CompositeHuskyCoder.<Long>builder()
                .add(v -> v, liar(7))
                .build();
        assertTrue(c.exact(100L));
        try {
            c.exact(200L);
            fail("exact() must not compute an answer from a code that overran its field");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("liar"));
        }
    }

    /**
     * A negative code is caught by the same test, the unsigned shift of a negative long being
     * large. Worth asserting because a field coder that forgets to bias a signed value is the
     * likeliest way to produce one.
     */
    @Test
    public void aNegativeFieldCodeIsRefused() {
        final CompositeHuskyCoder<Long> c = CompositeHuskyCoder.<Long>builder().add(v -> v, liar(8)).build();
        try {
            c.huskyEncode(-1L);
            fail("a negative code cannot lie in [0, 2^bits)");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("-1"));
        }
    }

    /**
     * The coders this class ships honour their declared widths by construction. Checked over their
     * whole domains where that is finite, because the contract is the one thing a field coder must
     * not get wrong.
     */
    @Test
    public void theSuppliedFieldCodersAllHonourTheirDeclaredWidths() {
        final HuskyFieldCoder<Long> r = HuskyFieldCoder.ofRange("r", -1000, 1000);
        for (long v = -1100; v <= 1100; v++) assertWithin(r, r.encode(v), v);
        final HuskyFieldCoder<String> s = HuskyFieldCoder.ofString("s", 3, "ABCDEFG");
        for (char a = 0; a < 200; a++)
            for (char b = 0; b < 200; b += 7)
                assertWithin(s, s.encode("" + a + b), "" + (int) a + "," + (int) b);
        final HuskyFieldCoder<LocalDate> d = HuskyFieldCoder.ofDate("d", LocalDate.of(2013, 1, 1), 1879);
        for (int k = -50; k < 2000; k += 7) {
            final LocalDate date = LocalDate.of(2013, 1, 1).plusDays(k);
            assertWithin(d, d.encode(date), date);
        }
        final HuskyFieldCoder<Boolean> b = HuskyFieldCoder.ofBoolean("b");
        assertWithin(b, b.encode(true), true);
        final HuskyFieldCoder<java.time.DayOfWeek> e = HuskyFieldCoder.ofEnum("e", java.time.DayOfWeek.class);
        for (final java.time.DayOfWeek day : java.time.DayOfWeek.values()) assertWithin(e, e.encode(day), day);
    }

    private static void assertWithin(final HuskyFieldCoder<?> coder, final long code, final Object value) {
        assertTrue(coder.name() + " returned a negative code " + code + " for " + value, code >= 0);
        assertTrue(coder.name() + " declares " + coder.bits() + " bits but returned " + code + " for " + value,
                (code >>> coder.bits()) == 0);
    }

    // ---------- the derived comparator ----------

    /**
     * The obligation {@code Tuple} states in a comment and leaves to the reader: that packing order
     * and comparison order agree. Taking both from one field list makes disagreement impossible, so
     * this checks the property the derivation is supposed to guarantee.
     */
    @Test
    public void theDerivedComparatorAgreesWithTheEncodingByConstruction() {
        final Permit[] permits = Arrays.copyOf(PermitLoader.getPermits(), 20_000);
        final CompositeHuskyCoder<Permit> c = permitCoder();
        final Comparator<Permit> derived = c.comparator();
        final Random random = new Random(42);
        for (int i = 0; i < 200_000; i++) {
            final Permit x = permits[random.nextInt(permits.length)], y = permits[random.nextInt(permits.length)];
            assertEquals("comparator and code must agree on " + x.getBlock() + " vs " + y.getBlock(),
                    Integer.signum(derived.compare(x, y)),
                    Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y))));
        }
    }

    @Test
    public void toStringNamesTheFieldsAndTheirWidths() {
        final String s = permitCoder().toString();
        assertTrue(s, s.contains("60 of 64 bits"));
        assertTrue(s, s.contains("block=25"));
        assertTrue(s, s.contains("lot=24"));
        assertTrue(s, s.contains("filed=11"));
    }
}
