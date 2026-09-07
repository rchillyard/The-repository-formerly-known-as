package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.SortException;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        final String[] result = getWordArray(resource, stringListFunction, 2);
        logger.info("getWords: testing with " + formatWhole(result.length) + " unique words: from " + resource);
        return result;
    }

    /**
     * Method to read a classpath resource and return a String[] of its content.
     * <p>
     * NOTE: reads via the classloader as a stream (not by resolving a filesystem File path),
     * so this works whether the resource is on an exploded classpath directory or packaged
     * inside a jar (e.g. a JMH shaded benchmarks.jar).
     *
     * @param resource           the name of the resource to read.
     * @param stringListFunction a function which takes a String and splits into a List of Strings.
     * @param minLength          the minimum acceptable length for a word.
     * @return an array of Strings.
     */
    static String[] getWordArray(final String resource, final Function<String, List<String>> stringListFunction, final int minLength) {
        try (final InputStream is = DutchHuskySort.class.getClassLoader().getResourceAsStream(resource)) {
            if (is == null) throw new FileNotFoundException(resource);
            try (final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return getWordList(reader, stringListFunction, minLength).toArray(new String[0]);
            }
        } catch (final IOException e) {
            logger.warn("Cannot open resource: " + resource, e);
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

    private static List<String> getWordList(final Reader reader, final Function<String, List<String>> stringListFunction, final int minLength) {
        boolean firstLine = true;
        final List<String> words = new ArrayList<>();
        for (final Object line : new BufferedReader(reader).lines().toArray()) {
            String string = (String) line;
            if (firstLine && string.startsWith(UTF8_BOM)) string = string.substring(1);
            words.addAll(stringListFunction.apply(string));
            firstLine = false;
        }
        return words.stream().distinct().filter(s -> s.length() >= minLength).collect(Collectors.toList());
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
    private static final String UTF8_BOM = "\uFEFF";
}
