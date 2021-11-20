/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory.*;

/**
 * Various utilities to help with HuskySort.
 */
public final class HuskySortHelper {

    /**
     * Method to get a HuskySequenceCoder by name.
     *
     * @param name a string representing the name (case must match).
     * @return the appropriate HuskySequenceCoder.
     */
    public static HuskySequenceCoder<String> getSequenceCoderByName(final String name) {
        return sequenceCoderMap.getOrDefault(name, unicodeCoder);
    }

    /**
     * Generate a random String of (English) alphabetic characters.
     *
     * @param number    the number of Strings to generate.
     * @param minLength the minimum number of characters in a String.
     * @param maxLength the maximum number of characters in a String.
     * @return an array (of length number) of Strings, each of length between minLength and maxLength.
     */
    public static String[] generateRandomAlphaBetaArray(final int number, final int minLength, final int maxLength) {
        final char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        final String[] result = new String[number];
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            final StringBuilder tmp = new StringBuilder();
            final int length = random.nextInt(minLength, maxLength + 1);
            for (int j = 0; j < length; j++) tmp.append(alphabet[random.nextInt(0, alphabet.length)]);
            result[i] = tmp.toString();
        }
        return result;
    }

    /**
     * Generate a random array of LocalDateTime instances.
     *
     * @param number the required length of the resulting array.
     * @return a number-length array of random dates
     */
    public static LocalDateTime[] generateRandomLocalDateTimeArray(final int number) {
        final LocalDateTime[] result = new LocalDateTime[number];
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = LocalDateTime.ofEpochSecond(random.nextLong(new Date().getTime()), random.nextInt(0, 1000000000), ZoneOffset.UTC);
        }
        return result;
    }

    private static final Map<String, HuskySequenceCoder<String>> sequenceCoderMap;

    /*
     * Initialize the sequenceCoderMap.
     */
    static {
        sequenceCoderMap = new HashMap<>();
        addToSequenceCoderMap(asciiCoder);
        addToSequenceCoderMap(chineseEncoder);
        addToSequenceCoderMap(englishCoder);
        addToSequenceCoderMap(unicodeCoder);
    }

    private static void addToSequenceCoderMap(final HuskySequenceCoder<String> asciiCoder) {
        sequenceCoderMap.put(asciiCoder.name(), asciiCoder);
    }

    public final static boolean isPreJava11 = Double.parseDouble((String) System.getProperties().get("java.class.version")) < 55.0;

    // NOTE: not used.
    public static double checkUnidentified(final String[] words, final int offset) {
        final int total = words.length;
        int count = 0;
        final Set<String> exist = new HashSet<>();
        for (final String word : words) {
            if (word.length() >= offset) {
                final String temp = word.substring(0, offset);
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
    public static Date[] generateRandomDateArray(final int number) {
        final Date[] result = new Date[number];
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            result[i] = new Date(random.nextLong(new Date().getTime()));
        }
        return result;
    }
}
