package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.util.TreeMap;

public abstract class UnicodeCharacter implements Comparable<UnicodeCharacter> {
    @Override
    public int compareTo(final UnicodeCharacter o) {
        return Long.compare(longCode, o.longCode);
    }

    public abstract long encode(String x);

    public abstract String alt(char x);

    public static final UnicodeCharacter NullChar = new UnicodeCharacter((char) 0) {
        public long encode(final String x) {
            return 0;
        }

        public String alt(final char x) {
            return "";
        }
    };

    public UnicodeCharacter(final char unicode) {
        this.unicode = unicode;
        this.alt = alt(unicode);
        this.longCode = encode(this.alt);
    }

    protected final char unicode;
    protected final long longCode;

    protected final String alt;

    protected final static TreeMap<Character, Long> map = new TreeMap<>();

}
