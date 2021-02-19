package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.SortException;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.neu.coe.huskySort.util.Utilities.formatWhole;

/**
 * Singleton class HuskySortBenchmarkHelper
 */
public final class HuskySortBenchmarkHelper {

    final static LazyLogger logger = new LazyLogger(HuskySortBenchmarkHelper.class);

    /**
     * Method to open a resource relative to this class and from the corresponding File, get an array of Strings.
     *
     * @param resource           the URL of the resource containing the Strings required.
     * @param stringListFunction a function which takes a String and splits into a List of Strings.
     * @return an array of Strings.
     */
    public static String[] getWords(final String resource, final Function<String, List<String>> stringListFunction) {
        try {
            final File file = new File(getPathname(resource, QuickHuskySort.class));
            final String[] result = getWordArray(file, stringListFunction, 2);
            logger.info("getWords: testing with " + formatWhole(result.length) + " unique words: from " + file);
            return result;
        } catch (final FileNotFoundException e) {
            logger.warn("Cannot find resource: " + resource, e);
            return new String[0];
        }
    }

    /**
     * Method to read given file and return a String[] of its content.
     *
     * @param file               the file to read.
     * @param stringListFunction a function which takes a String and splits into a List of Strings.
     * @param minLength          the minimum acceptable length for a word.
     * @return an array of Strings.
     */
    static String[] getWordArray(final File file, final Function<String, List<String>> stringListFunction, final int minLength) {
        try (final FileReader fr = new FileReader(file)) {
            return getWordList(fr, stringListFunction, minLength).toArray(new String[0]);
        } catch (final IOException e) {
            logger.warn("Cannot open file: " + file, e);
            return new String[0];
        }
    }

    /**
     * Method to split a String into list of Strings.
     *
     * @param line           a line of text.
     * @param lineMatcher    The regular expression used to match tokens in line.
     * @param stringSplitter The regular expression used to split Strings into words.
     * @return a list of Strings.
     */
    static List<String> splitLineIntoStrings(final String line, final Pattern lineMatcher, final Pattern stringSplitter) {
        final Matcher matcher = lineMatcher.matcher(line);
        if (matcher.find()) return Arrays.asList(stringSplitter.split(matcher.group(1)));
        else return new ArrayList<>();
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

    private static List<String> getWordList(final FileReader fr, final Function<String, List<String>> stringListFunction, final int minLength) {
        final List<String> words = new ArrayList<>();
        for (final Object line : new BufferedReader(fr).lines().toArray())
            words.addAll(stringListFunction.apply((String) line));
        return words.stream().distinct().filter(s -> s.length() >= minLength).collect(Collectors.toList());
    }

    // TEST
    private static String getPathname(final String resource, @SuppressWarnings("SameParameterValue") final Class<?> clazz) throws FileNotFoundException {
        final URL url = clazz.getClassLoader().getResource(resource);
        if (url != null) return url.getPath();
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

    public static final Pattern REGEX_STRING_SPLITTER = Pattern.compile("[\\s\\p{Punct}\\uFF0C]");
}
