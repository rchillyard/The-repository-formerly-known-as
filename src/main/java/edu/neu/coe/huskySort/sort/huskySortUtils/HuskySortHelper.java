/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.math.BigInteger;
import java.nio.LongBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("Duplicates")
public class HuskySortHelper {

    public final static HuskyCoder<String> asciiCoder = new HuskyCoder<String>() {
        @Override
        public long huskyEncode(String str) {
            return asciiToLong(str);
        }

        @Override
        public boolean imperfect() {
            return true;
        }
    };

    public final static HuskyCoder<String> unicodeCoder = new HuskyCoder<String>() {
        @Override
        public long huskyEncode(String str) {
            return unicodeToLong(str);
        }

        @Override
        public boolean imperfect() {
            return true;
        }
    };

    public final static HuskyCoder<String> utf8Coder = new HuskyCoder<String>() {
        @Override
        public long huskyEncode(String str) {
            return utf8ToLong(str);
        }

        @Override
        public boolean imperfect() {
            return true;
        }
    };

    public final static HuskyCoder<Date> dateCoder = new HuskyCoder<Date>() {
        @Override
        public long huskyEncode(Date date) {
            return date.getTime();
        }

        @Override
        public boolean imperfect() {
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
        public boolean imperfect() {
            // TODO this needs to be thoroughly checked
            return false;
        }
    };

    // This is used only by testSortDouble1
    public final static HuskyCoder<Double> doubleCoder = new HuskyCoder<Double>() {
        @Override
        public long huskyEncode(Double aDouble) {
            return Double.doubleToLongBits(aDouble);
        }

        @Override
        public boolean imperfect() {
            return true;
        }
    };

    public final static HuskyCoder<BigInteger> bigIntegerCoder = new HuskyCoder<BigInteger>() {
        @Override
        public long huskyEncode(BigInteger bigInteger) {
            return bigInteger.longValue();
        }

        @Override
        public boolean imperfect() {
            return true;
        }
    };

    public static long asciiToLong(String str) {
        return stringToLong(str, 9, 7);
    }

    public static long unicodeToLong(String str) {
        return stringToLong(str, 4, 16) >>> 1;
    }

    public static long utf8ToLong(String str) {
        return longArrayToLong(toUTF8Array(str), 8, 8) >>> 1;
    }

    private static long stringToLong(String str, int maxLength, int bitWidth) {
        return charArrayToLong(str.toCharArray(), maxLength, bitWidth);
    }

    private static long charArrayToLong(char[] charArray, int maxLength, int bitWidth) {
        int length = Math.min(charArray.length, maxLength);
        long result = 0;
        for (int i = 0; i < length; i++) result = result << bitWidth | (long) charArray[i];
        result = result << (bitWidth * (maxLength - length));
        return result;
    }

    private static long longArrayToLong(long[] utf8, int maxLength, int bitWidth) {
        int length = Math.min(utf8.length, maxLength);
        long result = 0;
        for (int i = 0; i < length; i++) result = result << bitWidth | utf8[i];
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

    public static double checkUnidentified(String[] words, int offcut) {
        int total = words.length;
        int count = 0;
        Set<String> exist = new HashSet<>();
        for (String word : words) {
            if (word.length() >= offcut) {
                String temp = word.substring(0, offcut);
                if (exist.contains(temp)) {
                    count++;
                } else {
                    exist.add(temp);
                }
            }
        }
        return (double) count / (double) total * 100.0;
    }

    public static Date[] generateRandomDateArray(int number) {
        Date[] result = new Date[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = new Date(random.nextLong(new Date().getTime()));
        }
        return result;
    }

    public static LocalDateTime[] generateRandomLocalDateTimeArray(int number) {
        LocalDateTime[] result = new LocalDateTime[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = LocalDateTime.ofEpochSecond(random.nextLong(new Date().getTime()), random.nextInt(0, 1000000000), ZoneOffset.UTC);
        }
        return result;
    }

    public static Double[] generateRandomDoubleArray(int number) {
        Double[] result = new Double[number];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = (random.nextDouble() - 0.5) * Double.MAX_VALUE;
        }
        return result;
    }
}
