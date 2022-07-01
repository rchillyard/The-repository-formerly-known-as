package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.SortException;

public class HuskyCoderChinesePinyin implements HuskyCoder<String> {
    /**
     * Encode x as a long.
     * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
     * If this cannot be guaranteed, then the result of imperfect(z) will be true.
     *
     * @param s the X value to encode.
     * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
     */
    public long huskyEncode(final String s) {
        if (dialect.equalsIgnoreCase("Hanyu")) return encodeHanyu(s);
        else if (dialect.equalsIgnoreCase("BoPoMoFo")) return encodeHanyu(s);
        else throw new SortException("huskyEncode: unsupported dialect: " + dialect);
    }

    /**
     * For names of four characters or fewer, this encoding will be perfect.
     * <p>
     * NOTE: We lose one bit of precision in the fourth character but the probability that it will be significant is very small.
     *
     * @return true.
     */
    @Override
    public boolean perfect() {
        // NOTE: in Hanyu, we can accommodate 10 pinyin characters with perfect encoding.
        if (dialect.equalsIgnoreCase("Hanyu")) return true;
        else if (dialect.equalsIgnoreCase("BoPoMoFo")) return true; // XXX see encodeHanyu for more detail.
        else throw new SortException("huskyEncode: unsupported dialect: " + dialect);
    }

    public HuskyCoderChinesePinyin(final String dialect) {
        this.dialect = dialect;
    }

    private static long encodeHanyu(final String s) {
        final String[] tokens = ChineseCharacter.parsePinyin(ChineseCharacter.convertToPinyin(s), s.length());
        final StringBuilder result = new StringBuilder();
        for (final String token : tokens)
            if (token != null) {
                if (token.endsWith("ü"))
                    // TODO sort this out.
                    result.append(token.substring(0, token.length() - 1)).append('~');
                else result.append(token);

            }
        final String pinyin = result.toString();
        final HuskySequenceCoder<String> coder = HuskyCoderFactory.englishCoder;
        return coder.huskyEncode(pinyin);
    }

    private static long encodeBoPoMoFo(final String s) {
        final Long[] codes = ChineseCharacter.parsePinyin(Long.class, ChineseCharacter.convertToPinyin(s), s.length(), xs -> {
            long result = 0L;
            for (int i = 0; i < xs.length; i++) {
                final int shift = ChineseCharacter.getShift(i);
                final long x = ChineseCharacter.lookupPinyin(i, xs[i]);
                assert x >= 0 : "chineseEncoderPinyin: logic error";
                result = (result << shift) | x;
            }
            return result;
        });
        long result = 0L;
        int bits = 0;
        int shift = 16;
        for (final long x : codes) {
            result = (result << shift) | x;
            bits += shift;
            if (bits == 48) shift = 15;
            if (bits >= 63) break;
        }
        // TODO Pad the remaining 47, 31, 15 bits as necessary.
        return result;
    }

    private final String dialect;
}
