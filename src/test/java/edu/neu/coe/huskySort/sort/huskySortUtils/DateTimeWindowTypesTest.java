/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests for {@link IsoDate}, {@link TimeOfDay}, {@link SecondOfDay} and {@link IsoTimestamp}:
 * date and time types that carry their own width, TODO.md item 54.
 * <p>
 * Dates differ from characters in that they have no canonical window --- an epoch is a property of
 * the data, which is why {@code @HuskyField(epoch, days)} asks for one. The claim tested here is
 * that the four-digit ISO year is the exception: a window natural enough to be a type, cheap
 * enough to be worth it, and wide enough that no realistic date falls outside.
 */
public class DateTimeWindowTypesTest {

    // ---------- the widths, and the arithmetic behind them ----------

    @Test
    public void eachTypeDeclaresTheWidthItsWindowNeeds() {
        assertEquals(IsoDate.BITS, IsoDate.HUSKY_CODER.bits());
        assertEquals(TimeOfDay.BITS, TimeOfDay.HUSKY_CODER.bits());
        assertEquals(SecondOfDay.BITS, SecondOfDay.HUSKY_CODER.bits());
        assertEquals(IsoTimestamp.BITS, IsoTimestamp.HUSKY_CODER.bits());
        assertEquals("3,652,058 days of four-digit ISO years", 22, IsoDate.BITS);
        assertEquals("a whole day in nanoseconds", 47, TimeOfDay.BITS);
        assertEquals("86,399 seconds", 17, SecondOfDay.BITS);
        assertEquals("date and second together", 39, IsoTimestamp.BITS);
    }

    /**
     * The widths come from the domains, not from a preference, so the arithmetic should say so.
     */
    @Test
    public void theWidthsFollowFromTheDomains() {
        assertEquals(3_652_058L, IsoDate.HIGHEST.toEpochDay() - IsoDate.LOWEST.toEpochDay());
        assertEquals(22, HuskyFieldCoder.bitsFor(IsoDate.HIGHEST.toEpochDay() - IsoDate.LOWEST.toEpochDay()));
        assertEquals(86_399_999_999_999L, LocalTime.MAX.toNanoOfDay());
        assertEquals(47, HuskyFieldCoder.bitsFor(LocalTime.MAX.toNanoOfDay()));
        assertEquals(86_399, LocalTime.MAX.toSecondOfDay());
        assertEquals(17, HuskyFieldCoder.bitsFor(LocalTime.MAX.toSecondOfDay()));
        assertEquals("a timestamp is a date and a second, however it is spelled",
                IsoDate.BITS + SecondOfDay.BITS, IsoTimestamp.BITS);
    }

    // ---------- IsoDate ----------

    public record Dated(IsoDate on) { }

    @Test
    public void isoDateOrdersAsTheDatesDoAcrossTheWholeWindow() {
        final CompositeHuskyCoder<Dated> c = RecordHuskyCoder.of(Dated.class);
        final Random random = new Random(20261004);
        final long span = IsoDate.HIGHEST.toEpochDay() - IsoDate.LOWEST.toEpochDay();
        for (int trial = 0; trial < 20_000; trial++) {
            final LocalDate a = LocalDate.ofEpochDay(IsoDate.LOWEST.toEpochDay() + (long) (random.nextDouble() * span));
            final LocalDate b = LocalDate.ofEpochDay(IsoDate.LOWEST.toEpochDay() + (long) (random.nextDouble() * span));
            assertEquals("code order must be date order for " + a + " and " + b,
                    Integer.signum(a.compareTo(b)),
                    Integer.signum(Long.compare(c.huskyEncode(new Dated(new IsoDate(a))),
                            c.huskyEncode(new Dated(new IsoDate(b))))));
        }
    }

    @Test
    public void isoDateIsExactAtBothEndsAndAlwaysPerfect() {
        final CompositeHuskyCoder<Dated> c = RecordHuskyCoder.of(Dated.class);
        assertEquals("the window's first day codes as zero", 0L, IsoDate.HUSKY_CODER.encode(new IsoDate(IsoDate.LOWEST)));
        assertEquals(3_652_058L, IsoDate.HUSKY_CODER.encode(new IsoDate(IsoDate.HIGHEST)));
        assertTrue(c.huskyEncode(new Dated[]{new Dated(new IsoDate(IsoDate.LOWEST)),
                new Dated(new IsoDate(IsoDate.HIGHEST)), new Dated(new IsoDate(LocalDate.of(2026, 10, 4)))}).perfect);
    }

    @Test
    public void isoDateRefusesYearsOutsideTheWindowAndClampSaturates() {
        for (final LocalDate d : new LocalDate[]{LocalDate.of(10_000, 1, 1), LocalDate.of(0, 12, 31),
                LocalDate.of(-1, 1, 1), LocalDate.MAX, LocalDate.MIN})
            try {
                new IsoDate(d);
                fail("IsoDate must refuse " + d);
            } catch (final IllegalArgumentException e) {
                assertTrue("the message should offer the alternatives: " + e.getMessage(),
                        e.getMessage().contains("clamp") && e.getMessage().contains("@HuskyField"));
            }
        assertEquals(IsoDate.LOWEST, IsoDate.clamp(LocalDate.MIN).value());
        assertEquals(IsoDate.HIGHEST, IsoDate.clamp(LocalDate.MAX).value());
        assertEquals(LocalDate.of(2026, 10, 4), IsoDate.clamp(LocalDate.of(2026, 10, 4)).value());
        assertFalse(IsoDate.admits(LocalDate.MAX));
        assertTrue(IsoDate.admits(LocalDate.of(2026, 10, 4)));
    }

    // ---------- TimeOfDay: the type that needs no invariant ----------

    public record Timed(TimeOfDay at) { }

    /**
     * {@link LocalTime}'s whole domain fits in 47 bits, so there is nothing for the constructor
     * to refuse and nothing that can ever be inexact. The only one of these types of which that
     * is true.
     */
    @Test
    public void everyLocalTimeIsExactInFortySevenBits() {
        final CompositeHuskyCoder<Timed> c = RecordHuskyCoder.of(Timed.class);
        final Random random = new Random(11);
        final Timed[] xs = new Timed[5000];
        xs[0] = new Timed(new TimeOfDay(LocalTime.MIN));
        xs[1] = new Timed(new TimeOfDay(LocalTime.MAX));
        for (int i = 2; i < xs.length; i++)
            xs[i] = new Timed(new TimeOfDay(LocalTime.ofNanoOfDay((long) (random.nextDouble() * LocalTime.MAX.toNanoOfDay()))));
        assertTrue("no LocalTime can be outside the window, there being no window", c.huskyEncode(xs).perfect);
        for (final Timed x : xs) assertTrue(TimeOfDay.HUSKY_CODER.exact(x.at()));
        assertEquals(0L, TimeOfDay.HUSKY_CODER.encode(new TimeOfDay(LocalTime.MIN)));
        assertEquals(86_399_999_999_999L, TimeOfDay.HUSKY_CODER.encode(new TimeOfDay(LocalTime.MAX)));
    }

    @Test
    public void timeOfDayOrdersAsTheTimesDo() {
        final Random random = new Random(3);
        for (int trial = 0; trial < 20_000; trial++) {
            final LocalTime a = LocalTime.ofNanoOfDay((long) (random.nextDouble() * LocalTime.MAX.toNanoOfDay()));
            final LocalTime b = LocalTime.ofNanoOfDay((long) (random.nextDouble() * LocalTime.MAX.toNanoOfDay()));
            assertEquals(Integer.signum(a.compareTo(b)), Integer.signum(Long.compare(
                    TimeOfDay.HUSKY_CODER.encode(new TimeOfDay(a)), TimeOfDay.HUSKY_CODER.encode(new TimeOfDay(b)))));
        }
    }

    // ---------- SecondOfDay ----------

    @Test
    public void secondOfDayRefusesAFractionRatherThanDroppingIt() {
        try {
            new SecondOfDay(LocalTime.of(9, 30, 0, 1));
            fail("a nanosecond would be discarded");
        } catch (final IllegalArgumentException e) {
            assertTrue("the message should offer both ways out: " + e.getMessage(),
                    e.getMessage().contains("truncate") && e.getMessage().contains("TimeOfDay"));
        }
        assertEquals(LocalTime.of(9, 30), SecondOfDay.truncate(LocalTime.of(9, 30, 0, 999_999_999)).value());
        assertFalse(SecondOfDay.admits(LocalTime.of(0, 0, 0, 1)));
        assertTrue(SecondOfDay.admits(LocalTime.of(23, 59, 59)));
        assertEquals(0L, SecondOfDay.HUSKY_CODER.encode(new SecondOfDay(LocalTime.MIN)));
        assertEquals(86_399L, SecondOfDay.HUSKY_CODER.encode(new SecondOfDay(LocalTime.of(23, 59, 59))));
    }

    // ---------- IsoTimestamp ----------

    public record Stamped(IsoTimestamp when) { }

    /**
     * Encoded as one contiguous quantity rather than two concatenated fields, so this checks the
     * flattening really does order as a date-then-time comparison would.
     */
    @Test
    public void isoTimestampOrdersAsTheTimestampsDo() {
        final Random random = new Random(5);
        final long days = IsoDate.HIGHEST.toEpochDay() - IsoDate.LOWEST.toEpochDay();
        for (int trial = 0; trial < 20_000; trial++) {
            final LocalDateTime a = stamp(random, days), b = stamp(random, days);
            assertEquals("code order must be timestamp order for " + a + " and " + b,
                    Integer.signum(a.compareTo(b)), Integer.signum(Long.compare(
                            IsoTimestamp.HUSKY_CODER.encode(new IsoTimestamp(a)),
                            IsoTimestamp.HUSKY_CODER.encode(new IsoTimestamp(b)))));
        }
    }

    private static LocalDateTime stamp(final Random random, final long days) {
        return LocalDateTime.of(LocalDate.ofEpochDay(IsoDate.LOWEST.toEpochDay() + (long) (random.nextDouble() * days)),
                LocalTime.ofSecondOfDay(random.nextInt(86_400)));
    }

    @Test
    public void isoTimestampEnforcesBothHalvesOfItsInvariant() {
        try {
            new IsoTimestamp(LocalDateTime.of(LocalDate.of(10_000, 1, 1), LocalTime.MIN));
            fail("outside the ISO years");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("truncate"));
        }
        try {
            new IsoTimestamp(LocalDateTime.of(LocalDate.of(2026, 10, 4), LocalTime.of(9, 0, 0, 1)));
            fail("carries a nanosecond");
        } catch (final IllegalArgumentException e) {
            assertTrue("the message should say why no code can keep it: " + e.getMessage(),
                    e.getMessage().contains("69"));
        }
        assertEquals(IsoTimestamp.LOWEST, IsoTimestamp.truncate(LocalDateTime.MIN).value());
        assertEquals(IsoTimestamp.HIGHEST, IsoTimestamp.truncate(LocalDateTime.MAX).value());
        assertEquals(LocalDateTime.of(2026, 10, 4, 9, 30),
                IsoTimestamp.truncate(LocalDateTime.of(2026, 10, 4, 9, 30, 0, 123)).value());
        assertTrue(IsoTimestamp.admits(LocalDateTime.of(2026, 10, 4, 9, 30)));
        assertFalse(IsoTimestamp.admits(LocalDateTime.MAX));
    }

    // ---------- how they compose, including the one that does not ----------

    public record Filing(IsoDate filed, SecondOfDay at) { }

    @Test
    public void aDateAndASecondFitTogetherAndStayPerfect() {
        final CompositeHuskyCoder<Filing> c = RecordHuskyCoder.of(Filing.class);
        assertEquals("22 + 17", 39, c.bits());
        assertFalse(c.truncating());
        final Comparator<Filing> natural = Comparator.comparing(Filing::filed).thenComparing(Filing::at);
        final Random random = new Random(13);
        final Filing[] xs = new Filing[2000];
        final long days = IsoDate.HIGHEST.toEpochDay() - IsoDate.LOWEST.toEpochDay();
        for (int i = 0; i < xs.length; i++) {
            final LocalDateTime s = stamp(random, days);
            xs[i] = new Filing(new IsoDate(s.toLocalDate()), new SecondOfDay(s.toLocalTime()));
        }
        assertTrue(c.huskyEncode(xs).perfect);
        for (int i = 0; i < 200; i++)
            for (int j = 0; j < 200; j++)
                assertEquals(Integer.signum(natural.compare(xs[i], xs[j])),
                        Integer.signum(Long.compare(c.huskyEncode(xs[i]), c.huskyEncode(xs[j]))));
    }

    public record TooWide(IsoDate d, TimeOfDay t) { }

    /**
     * <b>The negative result, which is a fact about the method and not this implementation.</b>
     * A husky code is 64 bits; a nanosecond-resolution timestamp over four-digit years needs 69.
     * So the composite truncates --- from the low end, losing resolution in the time rather than
     * inverting anything --- and says it can never be perfect. {@link IsoTimestamp} exists to be
     * the combination that fits.
     */
    @Test
    public void aDateAndAFullPrecisionTimeCannotBothFit() {
        final CompositeHuskyCoder<TooWide> c = RecordHuskyCoder.of(TooWide.class);
        assertEquals("22 + 47", 69, c.declaredBits());
        assertEquals(64, c.bits());
        assertTrue(c.truncating());
        assertTrue("the time loses its low five bits, not the date: " + c, c.toString().contains("t=47(-5)"));
        assertEquals("and the fitting alternative is five bits narrower than the budget",
                39, IsoTimestamp.BITS);
        // Still never inverted: truncation can only tie two timestamps, never reorder them.
        final Random random = new Random(17);
        final Comparator<TooWide> natural = Comparator.comparing(TooWide::d).thenComparing(TooWide::t);
        for (int trial = 0; trial < 5000; trial++) {
            final TooWide x = tooWide(random), y = tooWide(random);
            final int byCode = Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y)));
            if (byCode != 0) assertEquals(Integer.signum(natural.compare(x, y)), byCode);
        }
    }

    private static TooWide tooWide(final Random random) {
        return new TooWide(new IsoDate(LocalDate.ofEpochDay(IsoDate.LOWEST.toEpochDay() + random.nextInt(3_652_058))),
                new TimeOfDay(LocalTime.ofNanoOfDay((long) (random.nextDouble() * LocalTime.MAX.toNanoOfDay()))));
    }

    // ---------- against the annotated form it replaces ----------

    public record AnnotatedPermitDate(@HuskyField(epoch = "2013-01-01", days = 1879) LocalDate filed) { }

    public record TypedDate(IsoDate filed) { }

    /**
     * The annotation is still the right answer when you know your data: {@code PermitCoder}'s
     * window is eleven bits where this type costs twenty-two. What the type buys is not width but
     * not having to know, and an invariant the annotation cannot enforce.
     */
    @Test
    public void theAnnotationIsStillNarrowerWhenYouKnowYourData() {
        assertEquals("the permit corpus spans 1,879 days, which is eleven bits",
                11, RecordHuskyCoder.of(AnnotatedPermitDate.class).bits());
        assertEquals("where any ISO date costs twenty-two", 22, RecordHuskyCoder.of(TypedDate.class).bits());
        // But the annotated one accepts a date outside its declared window and goes imperfect.
        final LocalDate outsideThePermitWindow = LocalDate.of(1999, 1, 1);
        assertFalse("the annotated field accepts a date outside its declared window and goes imperfect",
                RecordHuskyCoder.of(AnnotatedPermitDate.class)
                        .huskyEncode(new AnnotatedPermitDate[]{new AnnotatedPermitDate(outsideThePermitWindow)}).perfect);
        assertTrue("where IsoDate would have taken the same date and stayed perfect",
                IsoDate.admits(outsideThePermitWindow));
        assertTrue(RecordHuskyCoder.of(TypedDate.class)
                .huskyEncode(new TypedDate[]{new TypedDate(new IsoDate(outsideThePermitWindow))}).perfect);
    }
}
