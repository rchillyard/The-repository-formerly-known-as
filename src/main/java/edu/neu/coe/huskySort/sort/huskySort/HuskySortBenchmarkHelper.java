package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.SortException;

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

import static java.lang.System.out;

class HuskySortBenchmarkHelper {
    // private constructor (singleton pattern)
    private HuskySortBenchmarkHelper() {
    }

    // TODO this needs to be unit-tested
    static String[] getWords(String resource, Function<String, List<String>> getStrings) throws FileNotFoundException {
        List<String> words = new ArrayList<>();
        FileReader fr = new FileReader(getFile(resource, QuickHuskySort.class));
        for (Object line : new BufferedReader(fr).lines().toArray()) words.addAll(getStrings.apply((String) line));
        words = words.stream().distinct().filter(new Predicate<String>() {
            private static final int MINIMUM_LENGTH = 2;

            @Override
            public boolean test(String s) {
                return s.length() >= MINIMUM_LENGTH;
            }
        }).collect(Collectors.toList());
        out.println("Testing with words: " + words.size() + " from " + resource);
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

//    // TODO this needs to be unit-tested
//    static void showTime(int nRuns, long start, String prefix, Function<Double, Double> normalizer) {
//        showTime((nanoTime() - start) / 1000000.0 / nRuns, prefix, normalizer);
//    }

    // TODO this needs to be unit-tested
    static void showTime(double time, String prefix, Function<Double, Double> normalizer) {
        out.println(prefix + normalizer.apply(time));
    }

    // TODO this needs to be unit-tested
    static String[] generateRandomStringArray(String[] lookupArray, int number) {
        Random r = new Random();
        String[] result = new String[number];
        for (int i = 0; i < number; i++) {
            result[i] = lookupArray[r.nextInt(lookupArray.length)];
        }
        return result;
    }

    // TODO this needs to be unit-tested
    private static String getFile(String resource, Class<?> clazz) throws FileNotFoundException {
        final URL url = clazz.getClassLoader().getResource(resource);
        if (url != null) return url.getFile();
        throw new FileNotFoundException(resource + " in " + clazz);
    }

}
