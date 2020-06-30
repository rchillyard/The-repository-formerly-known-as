/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.LongBuffer;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;

/**
 * Factory class for HuskyCoders.
 */
public class HuskyCoderFactory {

    /**
     * A Husky Coder for ASCII Strings.
     * <p>
     * This should work correctly for all 7-bit ASCII characters including all English letters (upper and lower case),
     * as well as the following all punctuation.
     * Additionally, many ASCII codes (non-printing) are included.
     * <p>
     * But, in any case, we are only optimizing for printable ascii characters here.
     * If the long encoding is off for some reason (like there's a number embedded in the name),
     * it's no big deal.
     * It just means that the final pass will have to work a bit harder to fix the extra inversion.
     */
    public final static HuskySequenceCoder<String> asciiCoder = new HuskySequenceCoder<String>() {
        /**
         * Method to determine if this Husky Coder is perfect for a sequence of the given length.
         * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
         * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
         *
         * @param length the length of a particular String.
         * @return true if length <= MAX_LENGTH_ASCII.
         */
        public boolean perfectForLength(int length) {
            return length <= MAX_LENGTH_ASCII;
        }

        public long huskyEncode(String str) {
            return asciiToLong(str);
        }
    };

    /**
     * A Husky Coder for English Strings.
     * <p>
     * This should work correctly for all 52 English characters (upper and lower case),
     * as well as the following 11 characters: @ [ \ ] ^ _ ` { | } ~
     * <p>
     * But, in any case, we are only optimizing for printable ascii characters here.
     * If the long encoding is off for some reason (like there's a number embedded in the name),
     * it's no big deal.
     * It just means that the final pass will have to work a bit harder to fix the extra inversion.
     */
    public final static HuskySequenceCoder<String> englishCoder = new HuskySequenceCoder<String>() {
        /**
         * Method to determine if this Husky Coder is perfect for a sequence of the given length.
         * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
         * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
         *
         * @param length the length of a particular String.
         * @return true if length <= MAX_LENGTH_ENGLISH.
         */
        @Override
        public boolean perfectForLength(int length) {
            return length <= MAX_LENGTH_ENGLISH;
        }

        public long huskyEncode(String str) {
            return englishToLong(str);
        }
    };

    /**
     * A Husky Coder for unicode Strings.
     */
    public final static HuskySequenceCoder<String> unicodeCoder = new HuskySequenceCoder<String>() {
        /**
         * Method to determine if this Husky Coder is perfect for a sequence of the given length.
         * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
         * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
         *
         * NOTE: a length equal to MAX_LENGTH_UNICODE would not be perfect because we have to drop one bit.
         *
         * @param length the length of a particular String.
         * @return true if length < MAX_LENGTH_UNICODE.
         */
        @Override
        public boolean perfectForLength(int length) {
            return length < MAX_LENGTH_UNICODE;
        }

        // TEST
        @Override
        public long huskyEncode(String str) {
            return unicodeToLong(str);
        }
    };

    /**
     * A Husky Coder for UTF Strings.
     */
    public final static HuskySequenceCoder<String> utf8Coder = new HuskySequenceCoder<String>() {
        /**
         * Method to determine if this Husky Coder is perfect for a sequence of the given length.
         * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
         * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
         *
         * NOTE: a length equal to MAX_LENGTH_UTF8 would not be perfect because we have to drop one bit.
         *
         * @param length the length of a particular String.
         * @return true if length < MAX_LENGTH_UTF8.
         */
        // TEST
        @Override
        public boolean perfectForLength(int length) {
            return length < MAX_LENGTH_UTF8;
        }

        // TEST
        @Override
        public long huskyEncode(String str) {
            return utf8ToLong(str);
        }
    };

    /**
     * A Husky Coder for Dates.
     */
    public final static HuskyCoder<Date> dateCoder = new HuskyCoder<Date>() {
        @Override
        public long huskyEncode(Date date) {
            return date.getTime();
        }

        /**
         * Method to determine if this Husky Coder is perfect for all Dates.
         *
         * @return true.
         */
        @Override
        public boolean perfect() {
            return true;
        }
    };

    /**
     * A Husky Coder for ChronoLocalDateTimes.
     */
    public final static HuskyCoder<ChronoLocalDateTime<?>> chronoLocalDateTimeCoder = new HuskyCoder<ChronoLocalDateTime<?>>() {
        @Override
        public long huskyEncode(ChronoLocalDateTime<?> x) {
            return x.toEpochSecond(ZoneOffset.UTC);
        }

        /**
         * Method to determine if this Husky Coder is perfect for all ChronoLocalDateTimes.
         *
         * @return true.
         */
        @Override
        public boolean perfect() {
            return true;
        }
    };

    /**
     * A Husky Coder for Doubles.
     */
    public final static HuskyCoder<Double> doubleCoder = HuskyCoderFactory::doubleToLong;

    /**
     * A Husky Coder for Integers.
     */
    public final static HuskyCoder<Integer> integerCoder = new HuskyCoder<Integer>() {
        @Override
        public long huskyEncode(Integer x) {
            return x.longValue();
        }

        /**
         * Method to determine if this Husky Coder is perfect for a class of objects (Integer).
         *
         * @return true.
         */
        @Override
        public boolean perfect() {
            return true;
        }
    };

    /**
     * A Husky Coder for Longs.
     */
    public final static HuskyCoder<Long> longCoder = new HuskyCoder<Long>() {
        @Override
        public long huskyEncode(Long x) {
            return x;
        }

        /**
         * Method to determine if this Husky Coder is perfect for a class of objects (Long).
         *
         * @return true.
         */
        @Override
        public boolean perfect() {
            return true;
        }
    };

    /**
     * A Husky Coder for BigIntegers.
     */
    public final static HuskyCoder<BigInteger> bigIntegerCoder = x -> doubleToLong(x.doubleValue());

    /**
     * A Husky Coder for Decimals.
     */
    public final static HuskyCoder<BigDecimal> bigDecimalCoder = x -> doubleToLong(x.doubleValue());

    // CONSIDER making this private
    public static long asciiToLong(String str) {
        return stringToLong(str, MAX_LENGTH_ASCII, BIT_WIDTH_ASCII, MASK_ASCII);
    }

    static long utf8ToLong(String str) {
        // TODO Need to test that the mask value is correct. I think it might not be.
        return longArrayToLong(toUTF8Array(str), MAX_LENGTH_UTF8, BIT_WIDTH_UTF8, MASK_UTF8) >>> 1;
    }

    private static long unicodeToLong(String str) {
        return stringToLong(str, MAX_LENGTH_UNICODE, BIT_WIDTH_UNICODE, MASK_UNICODE) >>> 1;
    }

    private static long stringToLong(String str, int maxLength, int bitWidth, int mask) {
        final int length = Math.min(str.length(), maxLength);
        final int padding = maxLength - length;
        long result = 0L;
        if (((mask ^ 0xFFFF) & 0xFFFF) == 0)
            for (int i = 0; i < length; i++) result = result << bitWidth | str.charAt(i);
        else
            for (int i = 0; i < length; i++) result = result << bitWidth | str.charAt(i) & mask;

        result = result << bitWidth * padding;
        return result;
    }

    private static long englishToLong(String str) {
        return stringToLong(str, MAX_LENGTH_ENGLISH, BIT_WIDTH_ENGLISH, MASK_ENGLISH);
    }

    @SuppressWarnings("SameParameterValue")
    private static long longArrayToLong(long[] xs, int maxLength, int bitWidth, int mask) {
        int length = Math.min(xs.length, maxLength);
        long result = 0;
        if (((~mask)) == 0)
            for (int i = 0; i < length; i++) result = result << bitWidth | xs[i];
        else
            for (int i = 0; i < length; i++) result = result << bitWidth | xs[i] & mask;
        result = result << (bitWidth * (maxLength - length));
        return result;
    }

    private static long[] toUTF8Array(String str) {
        int length = str.length();
        LongBuffer byteBuffer = LongBuffer.allocate(length << 2);
        int count = 0;
        char[] codes = str.toCharArray();
        for (int i = 0; i < length; i++) {
            char code = codes[i];
            if (code < 0x80) {
                count++;
                byteBuffer.put(code);
            } else if (code < 0x800) {
                count += 2;
                byteBuffer.put(0xC0 | (code >> 6));
                byteBuffer.put(0x80 | (code & 0x3F));
            } else if (code < 0xD800 || code >= 0xE000) {
                count += 3;
                byteBuffer.put(0xE0 | (code >> 12));
                byteBuffer.put(0x80 | ((code >> 6) & 0x3F));
                byteBuffer.put(0x80 | (code & 0x3F));
            } else {
                // TEST
                i++;
                int tempCode = 0x10000 + (((code & 0x3FF) << 10) | codes[i] & 0x3FF);
                count += 4;
                byteBuffer.put(0xF0 | (tempCode >> 18));
                byteBuffer.put(0x80 | ((tempCode >> 12) & 0x3F));
                byteBuffer.put(0x80 | ((tempCode >> 6) & 0x3F));
                byteBuffer.put(0x80 | (tempCode & 0x3F));
            }
        }
        long[] result = new long[count];
        byteBuffer.rewind();
        byteBuffer.get(result);
        return result;
    }

    /**
     * This method is required because doubleToLongBits does not increase monotonically with its input value.
     *
     * @param value a double.
     * @return an appropriate long value.
     */
    private static long doubleToLong(double value) {
        long doubleToLongBits = Double.doubleToLongBits(value);
        long sign = doubleToLongBits & 0x8000000000000000L;
        long result = doubleToLongBits & 0x7FFFFFFFFFFFFFFFL;
        return sign == 0 ? result : -result;
    }

    private static final int BITS_LONG = 64;

    private static final int BIT_WIDTH_ASCII = 7;
    private static final int MAX_LENGTH_ASCII = BITS_LONG / BIT_WIDTH_ASCII;
    private static final int MASK_ASCII = 0x7F;

    private static final int BIT_WIDTH_ENGLISH = 6;
    private static final int MAX_LENGTH_ENGLISH = BITS_LONG / BIT_WIDTH_ENGLISH;
    private static final int MASK_ENGLISH = 0x3F;

    private static final int BIT_WIDTH_UNICODE = 16;
    private static final int MAX_LENGTH_UNICODE = BITS_LONG / BIT_WIDTH_UNICODE;
    private static final int MASK_UNICODE = 0xFFFF;

    private static final int BIT_WIDTH_UTF8 = 8;
    private static final int MAX_LENGTH_UTF8 = BITS_LONG / BIT_WIDTH_UTF8;
    private static final int MASK_UTF8 = 0xFF;

}
