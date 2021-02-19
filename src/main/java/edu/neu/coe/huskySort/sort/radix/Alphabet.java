package edu.neu.coe.huskySort.sort.radix;

public class Alphabet {
    public Alphabet(final int radix) {
        this.radix = radix;
    }

    final int radix;

    public static Alphabet ASCII = new Alphabet(256);
}
