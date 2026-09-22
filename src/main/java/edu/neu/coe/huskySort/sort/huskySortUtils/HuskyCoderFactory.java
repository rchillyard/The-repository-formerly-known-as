/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

//import java.lang.reflect.Field;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.text.Collator;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Factory class for HuskyCoders.
 */
public final class HuskyCoderFactory {

    private static final int BITS_LONG = 64;
    private static final int BITS_BYTE = 8;
    private static final int BYTES_LONG = BITS_LONG / BITS_BYTE;
    private static final int MASK_BYTE = 0xFF;
    private static final int MASK_SHORT = 0xFFFF;

    private static final int BIT_WIDTH_ASCII = 7;
    private static final int MAX_LENGTH_ASCII = BITS_LONG / BIT_WIDTH_ASCII;
    private static final int MASK_ASCII = 0x7F;

    /**
     * The bottom of englishCoder's 6-bit window. A 6-bit slot cannot hold a character code
     * directly, since the letters start at 'A' = 65; subtracting this brings 64..127 into 0..63.
     */
    private static final int OFFSET_ENGLISH = 64;

    private static final int BIT_WIDTH_ENGLISH = 6;
    private static final int MAX_LENGTH_ENGLISH = BITS_LONG / BIT_WIDTH_ENGLISH;
    private static final int MASK_ENGLISH = 0x3F;

    private static final int BIT_WIDTH_UNICODE = 16;
    private static final int MAX_LENGTH_UNICODE = BITS_LONG / BIT_WIDTH_UNICODE;
    private static final int MASK_UNICODE = MASK_SHORT;

    private static final int BIT_WIDTH_UTF8 = 8;
    private static final int MAX_LENGTH_UTF8 = BITS_LONG / BIT_WIDTH_UTF8;
    private static final int MASK_UTF8 = MASK_BYTE;

    /**
     * Method to create a generic HuskyCoder for a class which is HuskySortable.
     *
     * @param <X> a class which is HuskySortable.
     * @return a HuskyCoder&lt;X&gt;.
     */
    public static <X extends HuskySortable<X>> HuskyCoder<X> createGenericCoder() {
        return HuskySortable::huskyCode;
    }

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
    public final static HuskySequenceCoder<String> asciiCoder = new BaseHuskySequenceCoder<>("ASCII", MAX_LENGTH_ASCII) {

        /**
         * Encode x as a long.
         * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
         * If this cannot be guaranteed, then the result of imperfect(z) will be true.
         *
         * @param str the X value to encode.
         * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
         */
        public long huskyEncode(final String str) {
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
    public final static HuskySequenceCoder<String> englishCoder = new BaseHuskySequenceCoder<>("English", MAX_LENGTH_ENGLISH) {
        /**
         * Encode x as a long.
         * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
         * If this cannot be guaranteed, then the result of imperfect(z) will be true.
         *
         * @param str the X value to encode.
         * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
         */
        public long huskyEncode(final String str) {
            return englishToLong(str);
        }
    };

    /**
     * A Husky Coder for ASCII Strings which <b>saturates</b> rather than masks.
     * <p>
     * {@link #asciiCoder} narrows each character with {@code & 0x7F}, which is not monotonic: a
     * character above the 7-bit range wraps to an arbitrary position inside it. 'é' is 233, and
     * {@code 233 & 0x7F} is 105, which is 'i' -- so "café" encodes as though it were "cafi" and
     * sorts seventeen letters early, before "cafz" instead of after it. The mojibake form is worse:
     * 'Ã' is 195, masking to 67, which is 'C', so it sorts before every lowercase word.
     * <p>
     * Saturating with {@code Math.min(c, 127)} instead puts such a character at the top of the
     * range, which is its correct position relative to every ASCII character. The residual
     * imprecision is a <i>tie</i> between two different out-of-range characters rather than a
     * long-range mis-ordering, and a tie is what the cleanup pass resolves cheaply -- Timsort's cost
     * follows the number of runs, and a mis-ordering breaks a run where a tie does not.
     * <p>
     * Measured on the Leipzig english corpus at n = 1,000,000: 30,079 natural runs after the radix
     * phase against asciiCoder's 30,921. Small here, because only 0.446% of that vocabulary holds a
     * non-ASCII character at all, and free -- but it matters much more on text containing
     * punctuation or any non-Latin content. See TODO.md item 36.
     */
    public final static HuskySequenceCoder<String> asciiSaturatingCoder = new BaseHuskySequenceCoder<>("ASCII-saturating", MAX_LENGTH_ASCII) {
        public long huskyEncode(final String str) {
            return stringToLongSaturating(str, MAX_LENGTH_ASCII, BIT_WIDTH_ASCII, 0);
        }
    };

    /**
     * A Husky Coder for English Strings which <b>saturates</b> rather than masks, and so is
     * monotonic over the whole character range.
     * <p>
     * {@link #englishCoder} narrows with {@code & 0x3F}, which is order-preserving only within the
     * 64-character window 64..127. Outside it the wrap-around is not merely imprecise but
     * ambiguous: an apostrophe is 39 and masks to 39, and 'g' is 103 which also masks to 39, so
     * "don't" and "dongt" receive identical codes. Real English text is full of apostrophes and
     * hyphens.
     * <p>
     * This coder maps a character to {@code clamp(c - 64, 0, 63)}: everything below the window ties
     * at the bottom, the window maps through unchanged, and everything above ties at the top. That
     * is non-decreasing across every char value, so it never mis-orders -- it only ties, which the
     * cleanup pass fixes locally.
     * <p>
     * Measured on the Leipzig english corpus at n = 1,000,000: 16,641 natural runs against
     * englishCoder's 17,506, and against 30,079 for the nine-character saturating ASCII coder. The
     * tenth character is worth far more than the saturation, but the saturation is what makes the
     * tenth character safe to take. See TODO.md item 36.
     */
    public final static HuskySequenceCoder<String> englishSaturatingCoder = new BaseHuskySequenceCoder<>("English-saturating", MAX_LENGTH_ENGLISH) {
        public long huskyEncode(final String str) {
            return stringToLongSaturating(str, MAX_LENGTH_ENGLISH, BIT_WIDTH_ENGLISH, OFFSET_ENGLISH);
        }
    };

    /**
     * A Husky Coder for unicode Strings.
     */
    public final static HuskySequenceCoder<String> unicodeCoder = new BaseHuskySequenceCoder<>("Unicode", MAX_LENGTH_UNICODE - 1) {
        /**
         * Encode x as a long.
         * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
         * If this cannot be guaranteed, then the result of imperfect(z) will be true.
         *
         * @param str the X value to encode.
         * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
         */
        public long huskyEncode(final String str) {
            return unicodeToLong(str);
        }
    };

    /**
     * A Husky Coder for Chinese UTF8 Strings which are ordered according to the CHINA-local collator.
     */
    public final static HuskySequenceCoder<String> chineseEncoderCollator = new SequenceEncoder_Collator(Collator.getInstance(Locale.CHINA));

    /**
     * A Husky Coder for Chinese UTF8 which are ordered according to a pinyin dialect.
     */

    public final static HuskyCoder<String> chineseEncoderPinyin = new HuskyCoderChinesePinyin("Hanyu");

    /**
     * The same pinyin ordering, encoded as one 15-bit rank per character rather than as a 9-bit
     * syllable plus a 3-bit tone -- four characters in 60 bits, and <b>exactly order-preserving</b>
     * for any string of at most four CJK characters, where {@link #chineseEncoderPinyin} is not.
     * <p>
     * The difference is that the ordinal encoding implements two of the three levels
     * {@code NAME_ORDER} compares on and drops the third, the code-point tie-break between true
     * homonyms. On `Chinese_Names_Corpus.txt` that collapses 1,145,009 names into 818,114 codes and
     * accounts for all of the disorder the cleanup pass then has to remove. Ranking by
     * {@code pinyinCharacterKey} -- the comparator's own key -- removes it by construction: sorting
     * the whole corpus by code alone leaves zero descents, against 59,332 per 300,000 names for the
     * ordinal coder. It also encodes 3.2x faster, one array index against a memoized pinyin4j
     * lookup. See {@code HuskyCoderChinesePinyin.encodeHanyuRank} and TODO.md item 44.
     * <p>
     * Added alongside rather than in place of the ordinal coder, so the two can be measured against
     * each other before anything switches.
     */
    public final static HuskyCoder<String> chineseEncoderPinyinRank = new HuskyCoderChinesePinyin("HanyuRank");

    /**
     * A Husky Coder for Dates.
     */
    public final static HuskyCoder<Date> dateCoder = new HuskyCoder<>() {
        /**
         * Encode x as a long.
         * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
         * If this cannot be guaranteed, then the result of imperfect(z) will be true.
         *
         * @param date the Date value to encode.
         * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
         */
        public long huskyEncode(final Date date) {
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
    public final static HuskyCoder<ChronoLocalDateTime<?>> chronoLocalDateTimeCoder = new HuskyCoder<>() {
        @Override
        public long huskyEncode(final ChronoLocalDateTime<?> x) {
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
    public final static HuskyCoder<Integer> integerCoder = new HuskyCoder<>() {
        @Override
        public long huskyEncode(final Integer x) {
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
    public final static HuskyCoder<Long> longCoder = new HuskyCoder<>() {
        @Override
        public long huskyEncode(final Long x) {
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
     * Method to yield a probabilistic encoder based on the given probability p of coding errors.
     *
     * @param p   the probability that there will be a coding error.
     * @param <X> the type of Number to be encoded.
     * @return a long.
     */
    public static <X extends Number & Comparable<X>> ProbabilisticEncoder<X> createProbabilisticCoder(final double p) {
        return new ProbabilisticEncoder<>(p) {
        };
    }

    /**
     * Abstract class which implements a probabilistic encoder on generic type X.
     * <p>
     * TODO: this only works correctly for where X is Byte -- need to generalize.
     *
     * @param <X> the type of the input to huskyEncode.
     */
    static abstract class ProbabilisticEncoder<X extends Number & Comparable<X>> implements HuskyCoder<X> {
        @Override
        public long huskyEncode(final X x) {
            final long longValue = x.longValue();
            return longValue ^ (isEvent() ? 0xFFFFFFFFFFFFFFFFL : 0);
        }

        public ProbabilisticEncoder(final double p, final long seed) {
            this.p = p;
            this.random = new Random(seed);
        }

        public ProbabilisticEncoder(final double p) {
            this(p, System.currentTimeMillis());
        }

        private final double p;

        boolean isEvent() {
            return random.nextDouble() < p;
        }

        private final Random random;
    }

    /**
     * A Husky Coder for BigIntegers.
     */
    public final static HuskyCoder<BigInteger> bigIntegerCoder = x -> doubleToLong(x.doubleValue());

    /**
     * A Husky Coder for BigDecimals.
     */
    public final static HuskyCoder<BigDecimal> bigDecimalCoder = BigDecimal::longValue;

    /**
     * A Husky Coder for scaled BigDecimals.
     * NOTE: use this if you know that your range of BigDecimals is particularly large or small.
     *
     * @param scale the power of ten by which each BigDecimal will be increased before conversion to long.
     *              Thus, if you have say numbers in the range 0 to 1, you might want to choose a scale of 18.
     *              Alternatively, if your numbers are in the range -1E100 through 1aE100, you should choose a scale of -82.
     * @return a HuskyCoder&lt;BigDecimal@gt;
     */
    public static HuskyCoder<BigDecimal> scaledBigDecimalCoder(final int scale) {
        return x -> x.movePointRight(scale).longValue();
    }

    // CONSIDER making this private
    public static long asciiToLong(final String str) {
        // CONSIDER an alternative coding scheme which would use str.getBytes(Charset.forName("ISO-8859-1"));
        // and then pack the first 8 bytes into the long.
        // NOTE: to be compatible with this current encoding, we would take only 7 bits from each byte, leaving the first bit unset,
        // and thus allowing 9 characters to be significant.
        return stringToLong(str, MAX_LENGTH_ASCII, BIT_WIDTH_ASCII, MASK_ASCII);
    }

    static long utf8ToLong(final String str) {
        // TODO Need to test that the mask value is correct. I think it might not be.
        return longArrayToLong(toUTF8Array(str), MAX_LENGTH_UTF8, BIT_WIDTH_UTF8, MASK_UTF8) >>> 1;
    }

    private static long unicodeToLong(final String str) {
        return stringToLong(str, MAX_LENGTH_UNICODE, BIT_WIDTH_UNICODE, MASK_UNICODE) >>> 1;
        // CONSIDER an alternative coding scheme which would use str.getBytes(Charset.forName("UTF-16"));
        // ignore the first two bytes and take the next eight bytes (or however many there are) and then pack them byte by byte into the long.
//        int startingPos = 2; // We need to account for the BOM
//        return stringToBytesToLong(str, MAX_LENGTH_UNICODE, StandardCharsets.UTF_16, startingPos) >>> 1;
    }

    /**
     * Method to pack the leading characters of str into a long, saturating each character into the
     * available bits rather than masking it.
     * <p>
     * The slot value is {@code clamp(c - offset, 0, 2^bitWidth - 1)}, which is non-decreasing in c.
     * Masking is not: it wraps an out-of-range character to an arbitrary in-range value, which
     * mis-orders the element and breaks a run. Saturating ties instead, and ties are cheap for an
     * adaptive cleanup sort.
     *
     * @param str       the String to encode.
     * @param maxLength the number of characters that fit in a long at this bit width.
     * @param bitWidth  the number of bits per character.
     * @param offset    the bottom of the representable window; 0 where the alphabet starts at 0.
     * @return a long, monotonically non-decreasing with respect to the natural ordering of str.
     */
    private static long stringToLongSaturating(final String str, final int maxLength, final int bitWidth, final int offset) {
        final int ceiling = (1 << bitWidth) - 1;
        final int length = Math.min(str.length(), maxLength);
        long result = 0L;
        for (int i = 0; i < length; i++)
            result = result << bitWidth | Math.max(0, Math.min(str.charAt(i) - offset, ceiling));
        return result << bitWidth * (maxLength - length);
    }

    private static long stringToLong(final String str, final int maxLength, final int bitWidth, final int mask) {
        final int length = Math.min(str.length(), maxLength);
        final int padding = maxLength - length;
        long result = 0L;
        if (((mask ^ MASK_SHORT) & MASK_SHORT) == 0)
            for (int i = 0; i < length; i++) result = result << bitWidth | str.charAt(i);
        else
            for (int i = 0; i < length; i++) result = result << bitWidth | str.charAt(i) & mask;
        result = result << bitWidth * padding;
        return result;
    }

    // NOTE: this method ought to be faster, but I don't think it is.
    // If you uncomment this, you must also uncomment the static initializer at the end of this file.
//    private static long stringToLongAvoidLength(final String str, final int maxLength, final int bitWidth, final int mask) {
//        char[] chars = new char[maxLength];
//        try {
//            char[] value = (char[]) fieldStringValue.get(str);
//            final int length = Math.min(value.length, maxLength);
//            System.arraycopy(value, 0, chars, 0, length);
//
//            str.getChars(0, length, chars, 0);
//            final int padding = maxLength - length;
//            long result = 0L;
//            if (((mask ^ MASK_SHORT) & MASK_SHORT) == 0)
//                for (int i = 0; i < length; i++) result = result << bitWidth | chars[i];
//            else
//                for (int i = 0; i < length; i++) result = result << bitWidth | chars[i] & mask;
//            result = result << bitWidth * padding;
//            return result;
//        } catch (IllegalAccessException e) {
//            System.err.println("Cannot get value of private field value of String class: " + e.getLocalizedMessage());
//            return 0L;
//        }
//    }

    // NOTE: this method seems considerably slower than stringToLong, even though it uses a Java library function (getBytes)
    private static long stringToBytesToLong(final String str, final int maxLength, final Charset charSet, final int startingPos) {
        final byte[] bytes = str.substring(0, Math.min(maxLength, str.length())).getBytes(charSet);
        return bytesToLong(startingPos, bytes);
    }

    static long bytesToLong(final int startingPos, final byte[] bytes) {
        int bytesIndex = startingPos;
        int resultIndex = 0;
        long result = 0L;
        for (; resultIndex < BYTES_LONG && bytesIndex < bytes.length; resultIndex++)
            result = result << BITS_BYTE | bytes[bytesIndex++] & MASK_BYTE;
        for (; resultIndex < BYTES_LONG; resultIndex++) result = result << BITS_BYTE;
        return result;
    }

    private static long englishToLong(final String str) {
        return stringToLong(str, MAX_LENGTH_ENGLISH, BIT_WIDTH_ENGLISH, MASK_ENGLISH);
    }

    @SuppressWarnings("SameParameterValue")
    private static long longArrayToLong(final long[] xs, final int maxLength, final int bitWidth, final int mask) {
        final int length = Math.min(xs.length, maxLength);
        long result = 0;
        if (((~mask)) == 0)
            for (int i = 0; i < length; i++) result = result << bitWidth | xs[i];
        else
            for (int i = 0; i < length; i++) result = result << bitWidth | xs[i] & mask;
        result = result << (bitWidth * (maxLength - length));
        return result;
    }

    private static long[] toUTF8Array(final String str) {
        final int length = str.length();
        final LongBuffer byteBuffer = LongBuffer.allocate(length << 2);
        int count = 0;
        final char[] codes = str.toCharArray();
        for (int i = 0; i < length; i++) {
            final char code = codes[i];
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
                i++;
                final int tempCode = 0x10000 + (((code & 0x3FF) << 10) | codes[i] & 0x3FF);
                count += 4;
                byteBuffer.put(0xF0 | (tempCode >> 18));
                byteBuffer.put(0x80 | ((tempCode >> 12) & 0x3F));
                byteBuffer.put(0x80 | ((tempCode >> 6) & 0x3F));
                byteBuffer.put(0x80 | (tempCode & 0x3F));
            }
        }
        final long[] result = new long[count];
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
    private static long doubleToLong(final double value) {
        final long doubleToLongBits = Double.doubleToLongBits(value);
        final long sign = doubleToLongBits & 0x8000000000000000L;
        final long result = doubleToLongBits & 0x7FFFFFFFFFFFFFFFL;
        return sign == 0 ? result : -result;
    }

    // NOTE: uncomment the following if you want to use the method stringToLongAvoidLength
//    private static Field fieldStringValue;
//
//    static {
//        try {
//            fieldStringValue = String.class.getDeclaredField("value");
//            fieldStringValue.setAccessible(true);
//        } catch (NoSuchFieldException e) {
//            System.err.println("Cannot access private field value of String class: " + e.getLocalizedMessage());
//        }
//    }
}
