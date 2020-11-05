package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.lang.reflect.Field;

/**
 * This unused class contains code which seemed like a good idea at the time,
 * but which does not actually speed anything up in practice.
 * <p>
 * TODO revisit this code and see if it could be useful.
 */
public final class HuskySortHelperObsolete {
    private static long stringToLong(final String str, final int maxLength, final int bitWidth, final int mask) {
        if (isPreJava11) try {
            final Field field = String.class.getDeclaredField("value");
            field.setAccessible(true);
            return charsToLong((char[]) field.get(str), maxLength, bitWidth, mask);
        } catch (final Exception e) {
            throw new RuntimeException("Problem encoding String as long", e);
        }
        else return bytesToLong(str.getBytes(), maxLength, bitWidth, mask);
    }

    private static long charsToLong(final char[] charArray, final int maxLength, final int bitWidth, final int mask) {
        final int length = Math.min(charArray.length, maxLength);
        final int padding = maxLength - length;
        long result = 0L;
        if (((mask ^ 0xFFFF) & 0xFFFF) == 0)
            for (int i = 0; i < length; i++) result = result << bitWidth | charArray[i];
        else
            for (int i = 0; i < length; i++) result = result << bitWidth | charArray[i] & mask;

        result = result << bitWidth * padding;
        return result;
    }

    private static long bytesToLong(final byte[] bytes, final int maxLength, final int bitWidth, final int mask) {
        long result = 0L;
        final int length = Math.min(bytes.length, maxLength);
        final int padding = maxLength - length;
        if (((mask ^ 0xFF) & 0xFF) == 0)
            for (int i = 0; i < length; i++) result = result << bitWidth | bytes[i];
        else
            for (int i = 0; i < length; i++) result = result << bitWidth | bytes[i] & mask;
        result = result << bitWidth * padding;
        return result;
    }

    public final static boolean isPreJava11 = Double.parseDouble((String) System.getProperties().get("java.class.version")) < 55.0;
}
