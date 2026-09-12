package edu.neu.coe.huskySort.sort.huskySortUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link PermitCoder#perfect()} returns true, which tells a husky sort it may skip its cleanup pass
 * altogether. That is the strongest claim a coder can make and the whole reason this corpus is
 * interesting, so it is checked against the data rather than argued from the bit budget.
 * <p>
 * The property required is that the encoding be a monotonic map from the ordering: for any two
 * permits, {@code compareTo} and the comparison of their codes must agree in sign, with codes equal
 * exactly when the permits compare equal. If that holds over the whole corpus then sorting by code
 * sorts by the ordering, with nothing left for a cleanup pass to do.
 */
public class PermitCoderTest {

    @BeforeClass
    public static void loadCorpus() {
        permits = PermitLoader.getPermits();
    }

    @Test
    public void theCorpusIsWhatWeThinkItIs() {
        assertEquals("the published record for 2013-01-02 to 2018-02-23", 198900, permits.length);
        for (final Permit p : permits) {
            assertTrue("block within its field width", p.getBlock().length() <= PermitCoder.BLOCK_WIDTH);
            assertTrue("lot within its field width", p.getLot().length() <= PermitCoder.LOT_WIDTH);
        }
    }

    /**
     * Every character in the corpus has a code of its own, which is what makes the encoding exact
     * rather than merely close. A character outside the alphabet would be coded as its nearest
     * neighbour below, and the coder would no longer deserve perfect().
     */
    @Test
    public void everyCharacterInTheCorpusIsInItsAlphabet() {
        for (final Permit p : permits) {
            for (final char c : p.getBlock().toCharArray())
                assertTrue("block character '" + c + "' is not in BLOCK_ALPHABET",
                        PermitCoder.BLOCK_ALPHABET.indexOf(c) >= 0);
            for (final char c : p.getLot().toCharArray())
                assertTrue("lot character '" + c + "' is not in LOT_ALPHABET",
                        PermitCoder.LOT_ALPHABET.indexOf(c) >= 0);
        }
    }

    /**
     * The claim itself, over the whole corpus. Sorting by code and sorting by compareTo must give the
     * same sequence -- which is exactly what a husky sort with no cleanup pass would produce.
     */
    @Test
    public void sortingByCodeAgreesWithSortingByOrdering() {
        final Permit[] byOrdering = permits.clone();
        Arrays.sort(byOrdering);
        final Permit[] byCode = permits.clone();
        Arrays.sort(byCode, Comparator.comparingLong(Permit::huskyCode));
        // compare the orderings rather than the arrays, since equal permits may be interchanged
        for (int i = 0; i < byCode.length; i++)
            assertEquals("position " + i, 0, byOrdering[i].compareTo(byCode[i]));
    }

    /**
     * Pairwise, on a large random sample: sign agreement in both directions, including the equal case.
     * The sort above could in principle pass while individual pairs disagreed; this cannot.
     */
    @Test
    public void codesAgreeWithComparisonPairwise() {
        final Random random = new Random(42);
        for (int trial = 0; trial < 2_000_000; trial++) {
            final Permit a = permits[random.nextInt(permits.length)];
            final Permit b = permits[random.nextInt(permits.length)];
            final int byOrdering = Integer.signum(a.compareTo(b));
            final int byCode = Integer.signum(Long.compare(a.huskyCode(), b.huskyCode()));
            assertEquals(a + " against " + b, byOrdering, byCode);
        }
    }

    /**
     * Sixty bits of a positive long, so the codes never reach the sign bit and unsigned-versus-signed
     * comparison cannot become an issue.
     */
    @Test
    public void codesFitInSixtyBits() {
        long widest = 0;
        for (final Permit p : permits) {
            final long code = p.huskyCode();
            assertTrue("codes must be non-negative", code >= 0);
            if (code > widest) widest = code;
        }
        assertTrue("expected at most 60 bits, found " + widest + " needing " + Long.toBinaryString(widest).length(),
                widest < (1L << 60));
    }

    /**
     * The three fields must dominate one another in the order compareTo uses them, which is what makes
     * the packing an ordering rather than a hash.
     */
    @Test
    public void fieldsDominateInTheRightOrder() {
        final Permit base = new Permit("0326", "023", LocalDate.of(2015, 5, 6));
        final Permit laterDate = new Permit("0326", "023", LocalDate.of(2016, 5, 6));
        final Permit laterLot = new Permit("0326", "024", LocalDate.of(2013, 1, 2));
        final Permit laterBlock = new Permit("0327", "001", LocalDate.of(2013, 1, 2));
        assertTrue(base.huskyCode() < laterDate.huskyCode());
        assertTrue("a later lot outranks any date", laterDate.huskyCode() < laterLot.huskyCode());
        assertTrue("a later block outranks any lot", laterLot.huskyCode() < laterBlock.huskyCode());
    }

    /**
     * A shorter identifier sorts before one that extends it, matching String.compareTo. This is what
     * the zero padding buys, and it is the case most likely to be got wrong.
     */
    @Test
    public void aPrefixSortsBeforeWhatExtendsIt() {
        final LocalDate d = LocalDate.of(2015, 1, 1);
        assertTrue(new Permit("072", "023", d).huskyCode() < new Permit("0720", "023", d).huskyCode());
        assertTrue(new Permit("0326", "02", d).huskyCode() < new Permit("0326", "020", d).huskyCode());
        assertEquals("0326".compareTo("072") < 0,
                new Permit("0326", "001", d).huskyCode() < new Permit("072", "001", d).huskyCode());
    }

    /**
     * Equal permits must encode equal, or a sort by code would separate records the ordering says are
     * identical.
     */
    @Test
    public void equalPermitsEncodeEqually() {
        final Permit a = new Permit("0326", "023", LocalDate.of(2015, 5, 6));
        final Permit b = new Permit("0326", "023", LocalDate.of(2015, 5, 6));
        assertEquals(a, b);
        assertEquals(a.huskyCode(), b.huskyCode());
    }

    /**
     * The corpus is not already sorted -- otherwise every benchmark over it would be measuring the
     * best case.
     */
    @Test
    public void theCorpusIsNotAlreadyInOrder() {
        int inversions = 0;
        for (int i = 1; i < permits.length && inversions < 100; i++)
            if (permits[i - 1].compareTo(permits[i]) > 0) inversions++;
        assertEquals("the published order is not the sorted order", 100, inversions);
    }

    /**
     * Guards the loader against a corpus whose fields have moved.
     */
    @Test
    public void theLoaderReadsTheFieldsInOrder() {
        final Permit first = permits[0];
        assertArrayEquals(new String[]{"0326", "023"}, new String[]{first.getBlock(), first.getLot()});
        assertEquals(LocalDate.of(2015, 5, 6), first.getFiledDate());
    }

    private static Permit[] permits;
}
