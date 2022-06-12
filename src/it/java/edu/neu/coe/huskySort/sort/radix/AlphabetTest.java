package edu.neu.coe.huskySort.sort.radix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlphabetTest {

    @Test
    public void getCountLengthAscii() {
        assertEquals(Alphabet.RADIX_ASCII+2, new Alphabet().getCountLength());
    }

    @Test
    public void getCountLengthUnicode() {
        assertEquals(2 * Alphabet.RADIX_ASCII + 2, new Alphabet(Alphabet.RADIX_UNICODE).getCountLength());
    }

    @Test
    public void getCountIndexAscii() {
        Alphabet alphabet = new Alphabet();
        int countIndex = alphabet.getCountIndex((char) 253);
        assertEquals(253, countIndex);
    }
    @Test
    public void getCountIndexUnicode() {
        Alphabet alphabet = new Alphabet(Alphabet.RADIX_UNICODE);
        assertEquals(256, alphabet.getCountIndex((char) 300));
        assertEquals(256, alphabet.getCountIndex((char) 300));
        assertEquals(257, alphabet.getCountIndex((char) 400));
        assertEquals(258, alphabet.getCountIndex((char) (Alphabet.RADIX_UNICODE - 1)));
    }
}