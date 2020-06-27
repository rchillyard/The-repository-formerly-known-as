/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.lang.reflect.Field;
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
     * This should work correctly for all 52 English characters (upper and lower case),
     * as well as the following 11 characters: @ [ \ ] ^ _ ` { | } ~
     * <p>
     * But, in any case, we are only optimizing for printable ascii characters here.
     * If the long encoding is off for some reason (like there's a number embedded in the name),
     * it's no big deal.
     * It just means that the final pass will have to work a bit harder to fix the extra inversion.
     */
    public final static HuskyCoder<String> asciiCoder = new HuskyCoder<String>() {
        public long huskyEncode(String str) {
            return asciiToLong(str);
        }

        public boolean imperfect(int length) {
            return length > MAX_LENGTH_ASCII;
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
    public final static HuskyCoder<String> printableAsciiCoder = new HuskyCoder<String>() {
        public long huskyEncode(String str) {
            return printableAsciiToLong(str);
        }

        public boolean imperfect(int length) {
            return length > MAX_LENGTH_PRINTABLE;
        }
    };

    public final static HuskyCoder<String> unicodeCoder = new HuskyCoder<String>() {
        // TEST
        @Override
        public long huskyEncode(String str) {
            return unicodeToLong(str);
        }

        // TEST
        @Override
        public boolean imperfect(int length) {
            return true;
        }
    };

    public final static HuskyCoder<String> utf8Coder = new HuskyCoder<String>() {
        // TEST
        @Override
        public long huskyEncode(String str) {
            return utf8ToLong(str);
        }

        // TEST
        @Override
        public boolean imperfect(int length) {
            return true;
        }
    };

    public final static HuskyCoder<Date> dateCoder = new HuskyCoder<Date>() {
        @Override
        public long huskyEncode(Date date) {
            return date.getTime();
        }

        @Override
        public boolean imperfect(int length) {
            // TODO this needs to be thoroughly checked
            return true;
        }
    };

    public final static HuskyCoder<ChronoLocalDateTime<?>> chronoLocalDateTimeCoder = new HuskyCoder<ChronoLocalDateTime<?>>() {
        @Override
        public long huskyEncode(ChronoLocalDateTime<?> chronoLocalDateTime) {
            return chronoLocalDateTime.toEpochSecond(ZoneOffset.UTC);
        }

        @Override
        public boolean imperfect(int length) {
            // TODO this needs to be thoroughly checked
            return false;
        }
    };

    // This is used only by testSortDouble1
    public final static HuskyCoder<Double> doubleCoder = new HuskyCoder<Double>() {
        // TEST
        @Override
        public long huskyEncode(Double aDouble) {
            return Double.doubleToLongBits(aDouble);
        }

        // TEST
        @Override
        public boolean imperfect(int length) {
            return true;
        }
    };

    public final static HuskyCoder<Integer> integerCoder = new HuskyCoder<Integer>() {
        @Override
        public long huskyEncode(Integer integer) {
            return integer.longValue();
        }

        @Override
        public boolean imperfect(int length) {
            return true;
        }
    };

    public final static HuskyCoder<BigInteger> bigIntegerCoder = new HuskyCoder<BigInteger>() {
        // TEST
        @Override
        public long huskyEncode(BigInteger bigInteger) {
            return bigInteger.longValue();
        }

        // TEST
        @Override
        public boolean imperfect(int length) {
            return true;
        }
    };

    public static long asciiToLong(String str) {
        return stringToLong(str, MAX_LENGTH_ASCII, BIT_WIDTH_ASCII, MASK_ASCII);
    }

    // TEST
    static long utf8ToLong(String str) {
        return longArrayToLong(toUTF8Array(str), 8, 8) >>> 1;
    }

    // TEST
    private static long unicodeToLong(String str) {
        return stringToLong(str, 4, 16, 0xFFFF) >>> 1;
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
        return stringToLong(str, MAX_LENGTH_PRINTABLE, BIT_WIDTH_PRINTABLE, MASK_PRINTABLE);
    }

    // TEST
    private static long longArrayToLong(long[] utf8, @SuppressWarnings("SameParameterValue") int maxLength, @SuppressWarnings("SameParameterValue") int bitWidth) {
        int length = Math.min(utf8.length, maxLength);
        long result = 0;
        for (int i = 0; i < length; i++) result = result << bitWidth | utf8[i];
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

    // TEST
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

    // TEST
    public static Date[] generateRandomDateArray(int number) {
        Date[] result = new Date[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = new Date(random.nextLong(new Date().getTime()));
        }
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
        char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        String[] result = new String[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            StringBuilder tmp = new StringBuilder();
            int length = random.nextInt(minLength, maxLength + 1);
            for (int j = 0; j < length; j++) tmp.append(alphabet[random.nextInt(0, 52)]);
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

    public static final int BITS_LONG = 64;

    public static final int BIT_WIDTH_ASCII = 7;
    public static final int MAX_LENGTH_ASCII = BITS_LONG / BIT_WIDTH_ASCII;
    public static final int MASK_ASCII = 0x7FFF;

    public static final int BIT_WIDTH_PRINTABLE = 6;
    public static final int MAX_LENGTH_PRINTABLE = BITS_LONG / BIT_WIDTH_PRINTABLE;
    public static final int MASK_PRINTABLE = 0x3F;
}
