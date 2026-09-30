/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.*;

/**
 * Tests for {@link RecordHuskyCoder}, TODO.md item 31 stage two.
 * <p>
 * The acceptance test is the same one stage one had to pass, raised a level: declare the San
 * Francisco permit as a record and check the derived coder reproduces {@code PermitCoder} -- a
 * hand-written, carefully documented, corpus-verified coder -- bit for bit over all 198,900
 * records. Stage one had to be told the fields; this stage reads them off the declaration.
 */
public class RecordHuskyCoderTest {

    /**
     * The permit, declared. Compare {@code PermitCoder}, which spends forty lines of prose and
     * three shift expressions saying the same thing, and whose widths nothing can check.
     */
    public record PermitRecord(
            @HuskyField(chars = 5, alphabet = PermitCoder.BLOCK_ALPHABET) String block,
            @HuskyField(chars = 4, alphabet = PermitCoder.LOT_ALPHABET) String lot,
            @HuskyField(epoch = "2013-01-01", days = 1879) LocalDate filed) {
    }

    private static PermitRecord[] corpus() {
        return Arrays.stream(PermitLoader.getPermits())
                .map(p -> new PermitRecord(p.getBlock(), p.getLot(), p.getFiledDate()))
                .toArray(PermitRecord[]::new);
    }

    /**
     * The acceptance test: identical codes, not merely a consistent ordering.
     */
    @Test
    public void reproducesPermitCoderFromTheDeclarationAlone() {
        final Permit[] permits = PermitLoader.getPermits();
        final CompositeHuskyCoder<PermitRecord> derived = RecordHuskyCoder.of(PermitRecord.class);
        assertEquals("the same 60 bits PermitCoder documents", 60, derived.bits());
        assertFalse(derived.truncating());
        for (final Permit p : permits) {
            final PermitRecord r = new PermitRecord(p.getBlock(), p.getLot(), p.getFiledDate());
            // XOR undoes CompositeHuskyCoder.SIGN_BIAS, which PermitCoder does not apply because
            // it knows it never fills the word. See CompositeHuskyCoderTest for the argument.
            assertEquals("codes must agree for " + p.getBlock() + "/" + p.getLot() + "/" + p.getFiledDate(),
                    PermitCoder.INSTANCE.huskyEncode(p), derived.huskyEncode(r) ^ CompositeHuskyCoder.SIGN_BIAS);
        }
    }

    @Test
    public void agreesThatTheCorpusIsExactlyEncodable() {
        assertTrue("every record must be exactly encodable, as PermitCoder.perfect() claims",
                RecordHuskyCoder.of(PermitRecord.class).huskyEncode(corpus()).perfect);
    }

    // ---------- declaration order is the packing order, which is the point of the stage ----------

    public record AB(@HuskyField(min = 0, max = 255) int a, @HuskyField(min = 0, max = 255) int b) {
    }

    public record BA(@HuskyField(min = 0, max = 255) int b, @HuskyField(min = 0, max = 255) int a) {
    }

    /**
     * The obligation {@code Tuple} states in a comment and leaves to the reader. Swapping the
     * declaration swaps which field dominates, with nothing else changed -- so the packing order
     * really is being read from the declaration rather than guessed or sorted by name.
     */
    @Test
    public void theDeclarationOrderDecidesWhichFieldDominates() {
        final CompositeHuskyCoder<AB> ab = RecordHuskyCoder.of(AB.class);
        final CompositeHuskyCoder<BA> ba = RecordHuskyCoder.of(BA.class);
        assertTrue("a dominates in AB", ab.huskyEncode(new AB(1, 0)) > ab.huskyEncode(new AB(0, 255)));
        assertTrue("b dominates in BA", ba.huskyEncode(new BA(1, 0)) > ba.huskyEncode(new BA(0, 255)));
    }

    /**
     * And the comparator comes from the same list, so it cannot disagree with the encoding.
     */
    @Test
    public void theDerivedComparatorAgreesWithTheDerivedEncoding() {
        final CompositeHuskyCoder<AB> c = RecordHuskyCoder.of(AB.class);
        final Comparator<AB> cmp = c.comparator();
        for (int a = 0; a < 16; a++)
            for (int b = 0; b < 16; b++)
                for (int a2 = 0; a2 < 16; a2++) {
                    final AB x = new AB(a, b), y = new AB(a2, 15 - b);
                    assertEquals(Integer.signum(cmp.compare(x, y)),
                            Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y))));
                }
    }

    // ---------- annotation is optional; it buys exactness, not correctness ----------

    public record TwoInts(int high, int low) {
    }

    /**
     * <b>TODO.md item 49.</b> Two unannotated {@code int}s are a machine word exactly, and since
     * 2026-09-30 the budget is a machine word, so the commonest composite there is needs no
     * annotation at all to be exact. Under the old 63-bit budget this same record was truncated
     * and reported imperfect.
     */
    @Test
    public void twoUnannotatedIntsAreExactBecauseTheBudgetIsAWholeWord() {
        final CompositeHuskyCoder<TwoInts> c = RecordHuskyCoder.of(TwoInts.class);
        assertEquals("two ints declare 64", 64, c.declaredBits());
        assertEquals(64, c.bits());
        assertFalse(c.truncating());
        final TwoInts[] xs = {
                new TwoInts(-5, 7), new TwoInts(-5, 6), new TwoInts(0, 0),
                new TwoInts(3, Integer.MIN_VALUE), new TwoInts(3, Integer.MAX_VALUE), new TwoInts(Integer.MAX_VALUE, 0)};
        final Comparator<TwoInts> natural = Comparator.comparingInt(TwoInts::high).thenComparingInt(TwoInts::low);
        for (final TwoInts x : xs)
            for (final TwoInts y : xs)
                assertEquals("the code order must be the declaration order",
                        Integer.signum(natural.compare(x, y)),
                        Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y))));
        assertTrue("and nothing was lost, so it is perfect", c.huskyEncode(xs).perfect);
    }

    public record ThreeInts(int high, int mid, int low) {
    }

    /**
     * Three, though, declare 96 bits, so the lowest loses everything and the middle one all but
     * its top bits. The record still sorts correctly -- the loss is at the bottom -- and simply
     * reports itself imperfect. That is the bargain the whole mechanism offers, applied to the
     * budget.
     */
    @Test
    public void anUnannotatedRecordStillSortsButIsNotPerfect() {
        final CompositeHuskyCoder<ThreeInts> c = RecordHuskyCoder.of(ThreeInts.class);
        assertEquals("three ints declare 96", 96, c.declaredBits());
        assertEquals(64, c.bits());
        assertTrue(c.truncating());
        final ThreeInts[] xs = {
                new ThreeInts(-5, 7, 0), new ThreeInts(-5, 6, 1), new ThreeInts(0, 0, 0),
                new ThreeInts(3, Integer.MIN_VALUE, 9), new ThreeInts(3, Integer.MAX_VALUE, -9),
                new ThreeInts(Integer.MAX_VALUE, 0, 0)};
        final Comparator<ThreeInts> natural = Comparator.comparingInt(ThreeInts::high)
                .thenComparingInt(ThreeInts::mid).thenComparingInt(ThreeInts::low);
        for (final ThreeInts x : xs)
            for (final ThreeInts y : xs) {
                final int byCode = Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y)));
                if (byCode != 0)
                    assertEquals("truncation may tie, but must never invert", Integer.signum(natural.compare(x, y)), byCode);
            }
        assertFalse("and it must say it is not perfect", c.huskyEncode(xs).perfect);
    }

    public record OneLong(long v) {
    }

    /**
     * An unannotated {@code long} takes the whole budget through {@link HuskyFieldCoder#ofLong},
     * rather than being narrowed to whatever range would fit. Before item 49 it could not be
     * exact at all, which was the oddest consequence of reserving the sign bit: the type husky
     * coding is defined in terms of was the one type the generic coder could not encode.
     */
    @Test
    public void anUnannotatedLongTakesTheWholeWordAndIsExact() {
        final CompositeHuskyCoder<OneLong> c = RecordHuskyCoder.of(OneLong.class);
        assertEquals(64, c.bits());
        assertFalse(c.truncating());
        final OneLong[] xs = {new OneLong(Long.MIN_VALUE), new OneLong(-1), new OneLong(0),
                new OneLong(1), new OneLong(Long.MAX_VALUE)};
        for (final OneLong x : xs)
            assertEquals("the field's bias and the composite's cancel", x.v(), c.huskyEncode(x));
        assertTrue(c.huskyEncode(xs).perfect);
    }

    public record NarrowLong(@HuskyField(min = 0, max = 1_000_000) long n) {
    }

    /**
     * Declaring a range still narrows a long, which is the whole purpose of the annotation: a
     * million needs twenty bits, leaving forty-four for the fields beside it.
     */
    @Test
    public void aDeclaredRangeStillNarrowsALong() {
        final CompositeHuskyCoder<NarrowLong> c = RecordHuskyCoder.of(NarrowLong.class);
        assertEquals("a million needs twenty bits", 20, c.bits());
        assertTrue(c.huskyEncode(new NarrowLong(999_999)) > c.huskyEncode(new NarrowLong(1)));
        assertFalse("and the range is enforced", c.huskyEncode(new NarrowLong[]{new NarrowLong(-1)}).perfect);
    }

    /**
     * Saying what the range really is turns the same record exact -- which is what the annotation
     * is for.
     */
    @Test
    public void declaringTheRangeBuysExactness() {
        final CompositeHuskyCoder<Narrowed> c = RecordHuskyCoder.of(Narrowed.class);
        assertEquals("17 for the zip and 8 for the year, as Tuple assumes", 25, c.bits());
        assertFalse(c.truncating());
        assertTrue(c.huskyEncode(new Narrowed[]{new Narrowed(94110, 1968), new Narrowed(0, 1850)}).perfect);
        assertFalse("and a value outside the declared range is not exact",
                c.huskyEncode(new Narrowed[]{new Narrowed(100_000, 1968)}).perfect);
    }

    public record Narrowed(@HuskyField(min = 0, max = 99_999) int zip, @HuskyField(min = 1850, max = 2020) int year) {
    }

    public record HalfDeclaredLong(@HuskyField(max = 100) long n) {
    }

    /**
     * One end of a long's range is not enough: completing it with the type's own extreme gives a
     * span wider than {@code Long.MAX_VALUE}, and guessing anything narrower would be a width
     * declaration the caller never made. Before item 49 this quietly took {@code [-2^62, 2^62-1]}
     * as the default range, which is precisely the silent guess this annotation exists to replace.
     */
    @Test
    public void refusesALongWithOnlyOneEndOfItsRangeDeclared() {
        try {
            RecordHuskyCoder.of(HalfDeclaredLong.class);
            fail("one end alone leaves a range wider than Long.MAX_VALUE");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("both min and max or neither"));
        }
    }

    // ---------- the types the declaration fixes on its own ----------

    public record Mixed(boolean flag, DayOfWeek day, @HuskyField(min = 0, max = 1000) short n) {
    }

    @Test
    public void booleanAndEnumTakeTheirWidthFromTheType() {
        final CompositeHuskyCoder<Mixed> c = RecordHuskyCoder.of(Mixed.class);
        assertEquals("1 for the boolean, 3 for seven days, 10 for 0..1000", 14, c.bits());
        assertTrue(c.huskyEncode(new Mixed(true, DayOfWeek.MONDAY, (short) 0))
                > c.huskyEncode(new Mixed(false, DayOfWeek.SUNDAY, (short) 1000)));
        assertTrue(c.huskyEncode(new Mixed(false, DayOfWeek.TUESDAY, (short) 0))
                > c.huskyEncode(new Mixed(false, DayOfWeek.MONDAY, (short) 1000)));
    }

    // ---------- nested records flatten in declaration order ----------

    public record Inner(@HuskyField(min = 0, max = 255) int p, @HuskyField(min = 0, max = 255) int q) {
    }

    public record Outer(Inner inner, @HuskyField(min = 0, max = 255) int r) {
    }

    /**
     * The fields of the composite are the leaves of the declaration tree, read left to right --
     * which is the order a lexicographic comparison of the nested type would visit them in.
     */
    @Test
    public void nestedRecordsFlattenIntoTheirLeavesInOrder() {
        final CompositeHuskyCoder<Outer> c = RecordHuskyCoder.of(Outer.class);
        assertEquals("three eight-bit leaves", 24, c.bits());
        assertTrue(c.toString(), c.toString().contains("inner.p=8"));
        assertTrue(c.toString(), c.toString().contains("inner.q=8"));
        assertTrue(c.toString(), c.toString().contains("r=8"));
        assertTrue("the first leaf dominates",
                c.huskyEncode(new Outer(new Inner(1, 0), 0)) > c.huskyEncode(new Outer(new Inner(0, 255), 255)));
        assertTrue("and the last is least significant",
                c.huskyEncode(new Outer(new Inner(0, 0), 1)) > c.huskyEncode(new Outer(new Inner(0, 0), 0)));
    }

    // ---------- refusals, which should say what to do instead ----------

    public record Unsupported(double d) {
    }

    @Test
    public void refusesATypeItCannotEncodeRatherThanIgnoringTheField() {
        try {
            RecordHuskyCoder.of(Unsupported.class);
            fail("silently omitting a field would produce a coder that ignores part of the key");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("double"));
            assertTrue("it should say what to do instead: " + e.getMessage(),
                    e.getMessage().contains("CompositeHuskyCoder.builder()"));
        }
    }

    public record MisAnnotated(@HuskyField(chars = 4) boolean flag) {
    }

    public record WrongAttribute(@HuskyField(min = 0, max = 9) String s) {
    }

    /**
     * An attribute that does not apply is an error, not something to ignore. A width declaration
     * that silently does nothing is precisely the failure this item exists to prevent.
     */
    @Test
    public void refusesAnAttributeThatDoesNotApplyToTheComponentType() {
        try {
            RecordHuskyCoder.of(MisAnnotated.class);
            fail("a boolean is one bit whatever the annotation says");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("flag"));
        }
        try {
            RecordHuskyCoder.of(WrongAttribute.class);
            fail("min and max mean nothing for a String");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("min and max do not apply"));
        }
    }

    @Test
    public void refusesSomethingThatIsNotARecord() {
        try {
            RecordHuskyCoder.of((Class) String.class);
            fail("String is not a record");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("not a record"));
        }
    }

    // ---------- reflection happens once ----------

    /**
     * Husky coding exists to extract a key once per element rather than once per comparison, so a
     * coder that reflected per element would spend more than it saves. Accessors are unreflected
     * into MethodHandles when the coder is built; this checks the cost of encoding stays within
     * sight of the hand-written coder rather than the hundredfold a reflective call would bring.
     */
    @Test
    public void encodingDoesNotPayReflectionPerElement() {
        final Permit[] permits = PermitLoader.getPermits();
        final PermitRecord[] records = corpus();
        final CompositeHuskyCoder<PermitRecord> derived = RecordHuskyCoder.of(PermitRecord.class);
        double best = Double.MAX_VALUE, hand = Double.MAX_VALUE;
        for (int i = 0; i < 7; i++) {
            long t0 = System.nanoTime();
            for (final PermitRecord r : records) derived.huskyEncode(r);
            if (i >= 2) best = Math.min(best, (System.nanoTime() - t0) / 1e6);
            t0 = System.nanoTime();
            for (final Permit p : permits) PermitCoder.INSTANCE.huskyEncode(p);
            if (i >= 2) hand = Math.min(hand, (System.nanoTime() - t0) / 1e6);
        }
        assertTrue("derived " + best + " ms against hand-written " + hand + " ms for " + permits.length
                + " permits: a reflective read per element would be far worse than this", best < hand * 8);
    }
}
