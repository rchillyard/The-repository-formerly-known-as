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

/**
 * Singleton class HuskySortBenchmarkHelper
 */
class HuskySortBenchmarkHelper {
    // TEST
    static String[] getWords(String resource, Function<String, List<String>> getStrings) throws FileNotFoundException {
        List<String> words = new ArrayList<>();
        FileReader fr = new FileReader(getFile(resource, QuickHuskySort.class));
        for (Object line : new BufferedReader(fr).lines().toArray()) words.addAll(getStrings.apply((String) line));
        words = words.stream().distinct().filter(new Predicate<String>() {
            private static final int MINIMUM_LENGTH = 2;

            public boolean test(String s) {
                return s.length() >= MINIMUM_LENGTH;
            }
        }).collect(Collectors.toList());
        logger.info("Testing with words: " + words.size() + " from " + resource);
        String[] result = new String[words.size()];
        result = words.toArray(result);
        return result;
    }

    static List<String> getWords(Pattern regex, String line) {
        final Matcher matcher = regex.matcher(line);
        if (matcher.find()) {
            final String word = matcher.group(1);
            final String[] strings = word.split("[\\s\\p{Punct}\\uFF0C]");
            return Arrays.asList(strings);
        } else
            return new ArrayList<>();
    }

    static void logNormalizedTime(double time, String prefix, Function<Double, Double> normalizer) {
        logger.info(prefix + normalizer.apply(time));
    }

    // TEST
    static String[] generateRandomStringArray(String[] lookupArray, int number) {
        if (lookupArray.length == 0) throw new SortException("lookupArray is empty");
        Random r = new Random();
        String[] result = new String[number];
        for (int i = 0; i < number; i++) result[i] = getRandomElement(lookupArray, r);
        return result;
    }

    // TEST
    private static String getFile(String resource, Class<?> clazz) throws FileNotFoundException {
        final URL url = clazz.getClassLoader().getResource(resource);
        if (url != null) return url.getFile();
        throw new FileNotFoundException(resource + " in " + clazz);
    }

    private static String getRandomElement(String[] strings, int length, Random r) {
        return strings[r.nextInt(length)];
    }

    private static String getRandomElement(String[] strings, Random r) {
        return getRandomElement(strings, strings.length, r);
    }

    // private constructor (singleton pattern)
    private HuskySortBenchmarkHelper() {
    }

    final static LazyLogger logger = new LazyLogger(HuskySortBenchmarkHelper.class);
}
