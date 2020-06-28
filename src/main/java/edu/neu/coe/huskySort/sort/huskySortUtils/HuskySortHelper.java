/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.LongBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class HuskySortHelper {

    /**
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
            return printableAsciiToLong(str);
        }
    };

    public final static HuskySequenceCoder<String> unicodeCoder = new HuskySequenceCoder<String>() {
        /**
         * Method to determine if this Husky Coder is perfect for a sequence of the given length.
         * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
         * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
         *
         * @param length the length of a particular String.
         * @return false if the resulting long for the String will likely not be unique.
         */
        @Override
        public boolean perfectForLength(int length) {
            return length <= MAX_LENGTH_UNICODE - 1;
        }

        // TEST
        @Override
        public long huskyEncode(String str) {
            return unicodeToLong(str);
        }
    };

    public final static HuskySequenceCoder<String> utf8Coder = new HuskySequenceCoder<String>() {
        /**
         * Method to determine if this Husky Coder is perfect for a sequence of the given length.
         * If the result is false for a particular length, it implies that inversions will remain after the first pass of Husky Sort.
         * If the result is true for all actual lengths, then the second pass of Husky Sort would be superfluous.
         *
         * @param length the length of a particular String.
         * @return true if length <= MAX_LENGTH_UTF8 - 1.
         */
        @Override
        public boolean perfectForLength(int length) {
            return length <= MAX_LENGTH_UTF8 - 1;
        }

        // TEST
        @Override
        public long huskyEncode(String str) {
            return utf8ToLong(str);
        }
    };

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

    // TEST
    // This is used only by testSortDouble1
    public final static HuskyCoder<Double> doubleCoder = Double::doubleToLongBits;

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

    // TEST this needs to be tested because longValue will obviously drop information.
    public final static HuskyCoder<BigInteger> bigIntegerCoder = x -> Double.doubleToLongBits(x.doubleValue());

    // TEST this needs to be tested because longValue will obviously drop information.
    public final static HuskyCoder<BigDecimal> bigDecimalCoder = BigDecimal::longValue;

    // CONSIDER making this private
    public static long asciiToLong(String str) {
        return stringToLong(str, MAX_LENGTH_ASCII, BIT_WIDTH_ASCII, MASK_ASCII);
    }

    // TEST
    static long utf8ToLong(String str) {
        // TODO Need to test that the mask value is correct. I think it might not be.
        return longArrayToLong(toUTF8Array(str), MAX_LENGTH_UTF8, BIT_WIDTH_UTF8, MASK_UTF8) >>> 1;
    }

    // TEST
    private static long unicodeToLong(String str) {
        return stringToLong(str, MAX_LENGTH_UNICODE, BIT_WIDTH_UNICODE, MASK_UNICODE) >>> 1;
    }

    private static long stringToLong(String str, int maxLength, int bitWidth, int mask) {
        if (isGetCharArray) try {
            Field field = String.class.getDeclaredField("value");
            field.setAccessible(true);
            return charsToLong((char[]) field.get(str), maxLength, bitWidth, mask);
        } catch (Exception e) {
            throw new RuntimeException("Problem encoding String as long", e);
        }
        else return bytesToLong(str.getBytes(), maxLength, bitWidth, mask);
    }

    private static long charsToLong(char[] charArray, int maxLength, int bitWidth, int mask) {
        final int length = Math.min(charArray.length, maxLength);
        final int padding = maxLength - length;
        long result = 0L;
        for (int i = 0; i < length; i++) result = result << bitWidth | charArray[i] & mask;
        result = result << bitWidth * padding;
        return result;
    }

    private static long bytesToLong(byte[] bytes, int maxLength, int bitWidth, int mask) {
        long result = 0L;
        final int length = Math.min(bytes.length, maxLength);
        final int padding = maxLength - length;
        for (int i = 0; i < length; i++) result = result << bitWidth | bytes[i] & mask;
        result = result << bitWidth * padding;
        return result;
    }

    private static long printableAsciiToLong(String str) {
        return stringToLong(str, MAX_LENGTH_ENGLISH, BIT_WIDTH_ENGLISH, MASK_ENGLISH);
    }

    // TEST
    @SuppressWarnings("SameParameterValue")
    private static long longArrayToLong(long[] xs, int maxLength, int bitWidth, int mask) {
        int length = Math.min(xs.length, maxLength);
        long result = 0;
        for (int i = 0; i < length; i++) result = result << bitWidth | xs[i] & mask;
        result = result << (bitWidth * (maxLength - length));
        return result;
    }

    // TEST
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
     * Generate a random String of (English) alphabetic characters.
     *
     * @param number    the number of Strings to generate.
     * @param minLength the minimum number of characters in a String.
     * @param maxLength the maximum number of characters in a String.
     * @return an array (of length number) of Strings, each of length between minLength and maxLength.
     */
    public static String[] generateRandomAlphaBetaArray(int number, int minLength, int maxLength) {
        final char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        String[] result = new String[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            StringBuilder tmp = new StringBuilder();
            int length = random.nextInt(minLength, maxLength + 1);
            for (int j = 0; j < length; j++) tmp.append(alphabet[random.nextInt(0, alphabet.length)]);
            result[i] = tmp.toString();
        }
        return result;
    }

    // TEST
    public static LocalDateTime[] generateRandomLocalDateTimeArray(int number) {
        LocalDateTime[] result = new LocalDateTime[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = LocalDateTime.ofEpochSecond(random.nextLong(new Date().getTime()), random.nextInt(0, 1000000000), ZoneOffset.UTC);
        }
        return result;
    }

    private final static boolean isGetCharArray = Double.parseDouble((String) System.getProperties().get("java.class.version")) < 55.0;

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

    // NOTE: not used.
    public static double checkUnidentified(String[] words, int offset) {
        int total = words.length;
        int count = 0;
        Set<String> exist = new HashSet<>();
        for (String word : words) {
            if (word.length() >= offset) {
                String temp = word.substring(0, offset);
                if (exist.contains(temp)) {
                    count++;
                } else {
                    exist.add(temp);
                }
            }
        }
        return (double) count / (double) total * 100.0;
    }

    // NOTE: not used.
    public static Date[] generateRandomDateArray(int number) {
        Date[] result = new Date[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = new Date(random.nextLong(new Date().getTime()));
        }
        return result;
    }
}
