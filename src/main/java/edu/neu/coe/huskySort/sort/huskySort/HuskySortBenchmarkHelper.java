package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.SortException;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * Singleton class HuskySortBenchmarkHelper
 */
final class HuskySortBenchmarkHelper {

    final static LazyLogger logger = new LazyLogger(HuskySortBenchmarkHelper.class);

    // TEST
    static String[] getWords(final String resource, final Function<String, List<String>> getStrings) {
        try {
            final FileReader fr = new FileReader(getFile(resource, QuickHuskySort.class));
            List<String> words = new ArrayList<>();
            for (final Object line : new BufferedReader(fr).lines().toArray())
                words.addAll(getStrings.apply((String) line));
            words = words.stream().distinct().filter(new Predicate<String>() {
                private static final int MINIMUM_LENGTH = 2;

                public boolean test(final String s) {
                    return s.length() >= MINIMUM_LENGTH;
                }
            }).collect(Collectors.toList());
            logger.info("getWords: testing with " + formatWhole(words.size()) + " unique words: from " + resource);
            String[] result = new String[words.size()];
            result = words.toArray(result);
            return result;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    static List<String> getWords(final Pattern regex, final String line) {
        final Matcher matcher = regex.matcher(line);
        if (matcher.find()) {
            final String word = matcher.group(1);
            final String[] strings = word.split("[\\s\\p{Punct}\\uFF0C]");
            return Arrays.asList(strings);
        } else
            return new ArrayList<>();
    }

    static void logNormalizedTime(final double time, final String prefix, final Function<Double, Double> normalizer) {
        logger.info(prefix + normalizer.apply(time));
    }

    // TEST
    static String[] generateRandomStringArray(final String[] lookupArray, final int number) {
        if (lookupArray.length == 0) throw new SortException("lookupArray is empty");
        final Random r = new Random();
        final String[] result = new String[number];
        for (int i = 0; i < number; i++) result[i] = getRandomElement(lookupArray, r);
        return result;
    }

    // TEST
    private static String getFile(final String resource, @SuppressWarnings("SameParameterValue") final Class<?> clazz) throws FileNotFoundException {
        final URL url = clazz.getClassLoader().getResource(resource);
        if (url != null) return url.getFile();
        throw new FileNotFoundException(resource + " in " + clazz);
    }

    private static String getRandomElement(final String[] strings, final int length, final Random r) {
        return strings[r.nextInt(length)];
    }

    private static String getRandomElement(final String[] strings, final Random r) {
        return getRandomElement(strings, strings.length, r);
    }

    // NOTE private constructor (singleton pattern)
    private HuskySortBenchmarkHelper() {
    }
}
