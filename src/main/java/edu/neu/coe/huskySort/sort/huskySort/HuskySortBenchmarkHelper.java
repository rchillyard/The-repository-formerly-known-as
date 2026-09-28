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

    /**
     * A Leipzig line is {@code <id>\t<sentence>}, so the sentence is simply everything after the
     * first tab. Taking it in one piece is deliberate: any attempt to validate the sentence inside
     * this pattern truncates it silently.
     * <p>
     * It used to read {@code [~\t]*\t(([\s\p{Punct}，]*\p{L}+)*)}, which required the captured
     * text to be an alternation of ASCII punctuation and Unicode letters. Java's {@code \p{Punct}}
     * is POSIX, hence ASCII-only, so the group stopped at the first character that was neither a
     * Unicode letter nor ASCII punctuation --- and {@code String.split} was then handed only that
     * prefix. Digits qualified, so did the pound sign, the copyright sign and every non-ASCII dash
     * or quotation mark. Measured on the corpora of record, 2026-09-22:
     * <pre>
     *     eng-uk_web_2002_1M   15.2% of sentence characters discarded; 275,387 -> 304,959 distinct
     *                          words, 16.39M -> 18.86M tokens
     *     zho-simp-tw_web_2014 51.5% discarded (the ideographic full stop U+3002 is not ASCII
     *                          punctuation); 24,215 -> 50,009 distinct, 25,745 -> 56,134 tokens
     * </pre>
     * "With Amelie (Cert 15) Jeunet combines the best of his two previous films..." yielded "With
     * Amelie (Cert"; "The figure size must not exceed A4 or 8.5 x 11in..." yielded "The figure size
     * must not exceed A". The repair is purely additive --- no word either corpus produced before
     * is lost --- but it changes every english and chinese string benchmark, so every such figure
     * measured before this commit is superseded. chinesenames is unaffected: it is loaded by
     * {@code lineAsList}, not by this pattern.
     */
    final static Pattern REGEX_LEIPZIG = Pattern.compile("[~\\t]*\\t(.*)");

    /**
     * Split a sentence into words, a word being a maximal run of Unicode letters.
     * <p>
     * This used to read {@code [\s\p{Punct}，]}: whitespace, ASCII punctuation, and the
     * fullwidth comma named explicitly because it is not ASCII. Enumerating separators that way
     * cannot be complete --- the ideographic full stop, the en dash and the curly apostrophe were
     * all missing --- so the complement is used instead. On the text the old
     * {@link #REGEX_LEIPZIG} captured, the two are equivalent, since that text
     * held nothing but letters, whitespace and ASCII punctuation; the {@code +} additionally
     * collapses runs of separators, which the old form left as empty tokens. The behaviour change
     * comes from the line pattern, not from here.
     * <p>
     * Note that this keeps the existing definition of a word: letters only, so no token contains a
     * digit, apostrophe or hyphen. Neither did any token before this commit. Admitting them would
     * be a change of definition rather than a repair, and is not made here.
     */
    public static final Pattern REGEX_STRING_SPLITTER = Pattern.compile("[^\\p{L}]+");
    private static final String UTF8_BOM = "\uFEFF";
}
