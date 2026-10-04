/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests for {@link English}, {@link Ascii} and {@link ExtendedAscii}: character types that carry
 * their own width, TODO.md item 54.
 * <p>
 * The claim being tested is not that they encode --- an annotated {@code char} does that --- but
 * that the <b>invariant</b> holds: because the constructor refuses anything outside the window, a
 * record of these components is always {@code perfect}, so a husky sort over it skips the cleanup
 * pass. An annotated char cannot promise that.
 */
public class CharacterWindowTypesTest {

    // ---------- the widths, which are the point ----------

    @Test
    public void eachTypeDeclaresTheWidthItsWindowNeeds() {
        assertEquals("64..127 is six bits", 6, English.HUSKY_CODER.bits());
        assertEquals("0..127 is seven", 7, Ascii.HUSKY_CODER.bits());
        assertEquals("0..255 is eight", 8, ExtendedAscii.HUSKY_CODER.bits());
        assertEquals(6, English.BITS);
        assertEquals(7, Ascii.BITS);
        assertEquals(8, ExtendedAscii.BITS);
    }

    /**
     * The widths match {@code HuskyCoderFactory}'s string coders, which is where they come from:
     * ten English characters or nine ASCII fill a machine word, as MAX_LENGTH_ENGLISH and
     * MAX_LENGTH_ASCII have it.
     */
    @Test
    public void theWidthsAgreeWithTheStringCodersTheyCameFrom() {
        assertEquals("ten English characters, as englishCoder packs", 60, 10 * English.BITS);
        assertTrue("and eleven would not fit", 11 * English.BITS > 64);
        assertEquals("nine ASCII characters, as asciiCoder packs", 63, 9 * Ascii.BITS);
        assertTrue(10 * Ascii.BITS > 64);
    }

    // ---------- the invariant ----------

    public record Word(English a, English b, English c, English d) { }

    private static Word word(final String s) {
        return new Word(new English(s.charAt(0)), new English(s.charAt(1)),
                new English(s.charAt(2)), new English(s.charAt(3)));
    }

    /**
     * <b>The reason the types exist.</b> Every value of the component type is in its window, so
     * every code is exact, so the array coding is perfect --- and
     * {@code AbstractHuskySort.postSort} returns immediately when it is.
     */
    @Test
    public void aRecordOfWindowTypesIsAlwaysPerfect() {
        final CompositeHuskyCoder<Word> c = RecordHuskyCoder.of(Word.class);
        assertEquals("four six-bit characters", 24, c.bits());
        assertFalse(c.truncating());
        final Random random = new Random(20261004);
        final Word[] xs = new Word[2000];
        for (int i = 0; i < xs.length; i++) {
            final StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 4; k++) sb.append((char) (English.LOWEST + random.nextInt(64)));
            xs[i] = word(sb.toString());
        }
        assertTrue("no value can be outside the window, so nothing can be inexact",
                c.huskyEncode(xs).perfect);
    }

    /**
     * And the ordering really is the characters' own, over the whole window rather than a sample.
     */
    @Test
    public void theCodeOrdersExactlyAsTheCharactersDo() {
        final CompositeHuskyCoder<Word> c = RecordHuskyCoder.of(Word.class);
        final Comparator<Word> natural = Comparator.comparing(Word::a).thenComparing(Word::b)
                .thenComparing(Word::c).thenComparing(Word::d);
        final Random random = new Random(7);
        for (int trial = 0; trial < 20_000; trial++) {
            final Word x = randomWord(random), y = randomWord(random);
            assertEquals("code order must be character order for " + x + " and " + y,
                    Integer.signum(natural.compare(x, y)),
                    Integer.signum(Long.compare(c.huskyEncode(x), c.huskyEncode(y))));
        }
    }

    private static Word randomWord(final Random random) {
        final char[] cs = new char[4];
        for (int i = 0; i < 4; i++) cs[i] = (char) (English.LOWEST + random.nextInt(64));
        return word(new String(cs));
    }

    /**
     * Every character of every window, against the type's own compareTo.
     */
    @Test
    public void everyCharacterInEveryWindowEncodesInOrder() {
        for (char a = English.LOWEST; a < English.HIGHEST; a++)
            assertTrue(English.HUSKY_CODER.encode(new English(a))
                    < English.HUSKY_CODER.encode(new English((char) (a + 1))));
        for (char a = Ascii.LOWEST; a < Ascii.HIGHEST; a++)
            assertTrue(Ascii.HUSKY_CODER.encode(new Ascii(a)) < Ascii.HUSKY_CODER.encode(new Ascii((char) (a + 1))));
        for (char a = ExtendedAscii.LOWEST; a < ExtendedAscii.HIGHEST; a++)
            assertTrue(ExtendedAscii.HUSKY_CODER.encode(new ExtendedAscii(a))
                    < ExtendedAscii.HUSKY_CODER.encode(new ExtendedAscii((char) (a + 1))));
        assertEquals("the window's bottom codes as zero", 0L, English.HUSKY_CODER.encode(new English(English.LOWEST)));
        assertEquals("and its top as 2^6 - 1", 63L, English.HUSKY_CODER.encode(new English(English.HIGHEST)));
    }

    // ---------- the constructor is what makes the invariant true ----------

    @Test
    public void theConstructorRefusesAnythingOutsideTheWindow() {
        for (final char c : new char[]{0, 32, 63, 128, 255, 0xFFFF})
            try {
                new English(c);
                fail("English must refuse " + (int) c);
            } catch (final IllegalArgumentException e) {
                assertTrue("the message should say what to do instead: " + e.getMessage(),
                        e.getMessage().contains("clamp") && e.getMessage().contains("@HuskyField"));
            }
        for (final char c : new char[]{128, 255, 0xFFFF})
            try {
                new Ascii(c);
                fail("Ascii must refuse " + (int) c);
            } catch (final IllegalArgumentException ignored) {
            }
        try {
            new ExtendedAscii((char) 256);
            fail("ExtendedAscii must refuse 256");
        } catch (final IllegalArgumentException ignored) {
        }
    }

    @Test
    public void clampSaturatesAndAdmitsAgrees() {
        assertEquals(English.LOWEST, English.clamp(' ').value());
        assertEquals(English.HIGHEST, English.clamp('ÿ').value());
        assertEquals('A', English.clamp('A').value());
        assertFalse(English.admits(' '));
        assertTrue(English.admits('A'));
        assertTrue(English.admits(English.LOWEST));
        assertTrue(English.admits(English.HIGHEST));
        // Weakly ordering only: everything below the window collapses to one value.
        assertEquals(English.clamp((char) 0), English.clamp((char) 63));
    }

    // ---------- the derivation finds the coder without being told ----------

    public record Mixed(Ascii first, ExtendedAscii second, @HuskyField(min = 0, max = 255) int n) { }

    /**
     * A window type needs no annotation, and mixes with annotated components.
     */
    @Test
    public void theDerivationUsesATypesOwnCoderWithoutAnnotation() {
        final CompositeHuskyCoder<Mixed> c = RecordHuskyCoder.of(Mixed.class);
        assertEquals("7 + 8 + 8", 23, c.bits());
        assertTrue("the fields are named for the components, not the types: " + c,
                c.toString().contains("first=7") && c.toString().contains("second=8") && c.toString().contains("n=8"));
    }

    public record Annotated(@HuskyField(min = 0, max = 1023) int low) { }

    public record TenEnglish(English a, English b, English c, English d, English e,
                             English f, English g, English h, English i, English j) { }

    /**
     * Ten of them fill the word the way {@code englishCoder} does, with four bits to spare.
     */
    @Test
    public void tenEnglishCharactersFitAMachineWord() {
        final CompositeHuskyCoder<TenEnglish> c = RecordHuskyCoder.of(TenEnglish.class);
        assertEquals(60, c.bits());
        assertFalse(c.truncating());
    }

    /**
     * The window types are records, and {@code RecordHuskyCoder} flattens a nested record into
     * its components --- which for these would reach the {@code char} and give it sixteen bits,
     * discarding the width the type exists to carry. The self-supplied coder must therefore be
     * found <b>before</b> the flattening, and this is what says so.
     */
    @Test
    public void aWindowTypeIsNotFlattenedIntoItsCharacter() {
        assertTrue("English is a record, which is the trap", English.class.isRecord());
        assertEquals("six bits, not the sixteen a bare char would take",
                6, RecordHuskyCoder.of(OneEnglish.class).bits());
    }

    public record OneEnglish(English only) { }

    public record MisAnnotated(@HuskyField(min = 0, max = 10) English c) { }

    /**
     * Annotating a type that declares its own width is an error rather than being ignored, as it
     * is for a boolean or an enum: a width declaration that silently does nothing is the failure
     * this whole mechanism exists to prevent.
     */
    @Test
    public void annotatingAWindowTypeIsRefused() {
        try {
            RecordHuskyCoder.of(MisAnnotated.class);
            fail("the type already declares its width");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("HUSKY_CODER"));
        }
    }

    // ---------- the gain over the annotated form ----------

    public record AnnotatedWord(@HuskyField(min = 64, max = 127) char a, @HuskyField(min = 64, max = 127) char b,
                                @HuskyField(min = 64, max = 127) char c, @HuskyField(min = 64, max = 127) char d) { }

    /**
     * <b>The comparison that justifies the types.</b> The annotated record declares the same
     * window and spends the same 24 bits, and for in-window data the two agree exactly. The
     * difference appears for data outside the window: the annotated record accepts it, clamps it
     * and reports itself imperfect --- so the sort must run a cleanup pass --- where the window
     * type could not have been built from it in the first place.
     */
    @Test
    public void theAnnotatedFormAgreesUntilTheDataLeavesTheWindow() {
        final CompositeHuskyCoder<Word> typed = RecordHuskyCoder.of(Word.class);
        final CompositeHuskyCoder<AnnotatedWord> annotated = RecordHuskyCoder.of(AnnotatedWord.class);
        assertEquals("the same width", typed.bits(), annotated.bits());

        final String inWindow = "HUSK";
        assertEquals("and the same code, for data both can hold",
                typed.huskyEncode(word(inWindow)),
                annotated.huskyEncode(new AnnotatedWord(inWindow.charAt(0), inWindow.charAt(1),
                        inWindow.charAt(2), inWindow.charAt(3))));

        final AnnotatedWord outside = new AnnotatedWord('h', 'i', ' ', '!');
        assertFalse("the annotated record accepts an out-of-window character and is then imperfect,"
                        + " so the sort pays for a cleanup pass",
                annotated.huskyEncode(new AnnotatedWord[]{outside}).perfect);
        assertFalse("which is exactly the value English refuses to be built from", English.admits(' '));
    }
}
