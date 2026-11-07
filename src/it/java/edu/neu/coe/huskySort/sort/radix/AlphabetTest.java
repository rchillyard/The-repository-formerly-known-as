package edu.neu.coe.huskySort.sort.radix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlphabetTest {

    @Test
    public void getCountLengthAscii() {
        assertEquals(Alphabet.RADIX_ASCII + 2, new Alphabet().getCountLength());
    }

    @Test
    public void getCountLengthUnicode() {
        assertEquals(2 * Alphabet.RADIX_ASCII + 2, new Alphabet(Alphabet.RADIX_UNICODE).getCountLength());
    }

    @Test
    public void getCountIndexAscii() {
        final Alphabet alphabet = new Alphabet();
        final int countIndex = alphabet.getCountIndex((char) 253);
        assertEquals(253, countIndex);
    }

    /**
     * NOTE: {@code prepare} is not optional. Positions beyond ASCII are assigned in code-point
     * order over the whole input, rather than on first encounter as this class once did, so
     * {@code getCountIndex} refuses a character it has not been shown. The three expectations below
     * are the code-point order of the three characters prepared.
     */
    @Test
    public void getCountIndexUnicode() {
        final Alphabet alphabet = new Alphabet(Alphabet.RADIX_UNICODE);
        alphabet.prepare(new String[]{String.valueOf((char) 300), String.valueOf((char) 400), String.valueOf((char) (Alphabet.RADIX_UNICODE - 1))});
        assertEquals(256, alphabet.getCountIndex((char) 300));
        assertEquals(256, alphabet.getCountIndex((char) 300));
        assertEquals(257, alphabet.getCountIndex((char) 400));
        assertEquals(258, alphabet.getCountIndex((char) (Alphabet.RADIX_UNICODE - 1)));
    }
}