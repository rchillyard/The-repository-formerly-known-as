/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.sort.simple.IntroSort;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.util.Benchmark;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.neu.coe.huskySort.sort.huskySort.AbstractHuskySort.UNICODE_CODER;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

public class HuskySortTest {

    public static String[] getWords(String resource, Function<String, List<String>> getStrings) throws FileNotFoundException {
        List<String> words = new ArrayList<>();
        FileReader fr = new FileReader(getFile(resource, QuickHuskySort.class));
        for (Object line : new BufferedReader(fr).lines().toArray()) words.addAll(getStrings.apply((String) line));
        words = words.stream().distinct().collect(Collectors.toList());
        out.println("Testing with words: " + words.size() + " from " + resource);
        String[] result = new String[words.size()];
        result = words.toArray(result);
        return result;
    }

    private static String getFile(String resource, Class<?> clazz) throws FileNotFoundException {
        final URL url = clazz.getClassLoader().getResource(resource);
        if (url != null) return url.getFile();
        throw new FileNotFoundException(resource + " in " + clazz);
    }

    public static void main(String[] args) throws IOException {
//        LocalDateTime[] LocalDateTimeArray = generateRandomLocalDateTimeArray(100000);
//        // Test on date using pure tim sort.
//        Benchmark<LocalDateTime[]> benchmarkPureTimSortOnDate = new Benchmark<>(
//                (xs) -> { return Arrays.copyOf(xs, xs.length); },
//                Arrays::sort
//        );
//        System.out.println("Sort dates using pure tim sort: \t" + benchmarkPureTimSortOnDate.run(LocalDateTimeArray, 100) + "ms");
//
//        // Test on date using husky sort.
//        QuickHuskySort<ChronoLocalDateTime<?>> dateHuskySort = new QuickHuskySort<>();
//        final Helper<ChronoLocalDateTime<?>> dateHelper = dateHuskySort.getHelper();
//        Benchmark<LocalDateTime[]> benchmarkHuskySortOnDate = new Benchmark<>(
//                (xs) -> Arrays.copyOf(xs, xs.length),
//                (xs) -> dateHuskySort.sort(xs, HuskySortHelper.chronoLocalDateTimeCoder),
//                (xs) -> { if (!dateHelper.sorted(xs)) System.err.println("not sorted"); }
//        );
//        System.out.println("Sort dates using husky sort: \t" + benchmarkHuskySortOnDate.run(LocalDateTimeArray, 100) + "ms");
//
//        // Test on date using husky sort with insertion sort.
//        InsertionSort<ChronoLocalDateTime<?>> insertionSort = new InsertionSort<>();
//        Benchmark<LocalDateTime[]> benchmarkHuskySortWithInsertionSortOnDate = new Benchmark<>(
//                (xs) -> Arrays.copyOf(xs, xs.length),
//                (xs) -> dateHuskySort.sort(xs, HuskySortHelper.chronoLocalDateTimeCoder, (xs2) -> insertionSort.sort(xs2, false)),
//                (xs) -> { if (!dateHelper.sorted(xs)) System.err.println("not sorted"); }
//        );
//        System.out.println("Sort dates using husky sort: \t" + benchmarkHuskySortWithInsertionSortOnDate.run(LocalDateTimeArray, 100) + "ms");

        final Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");
        benchmark(getWords("eng-uk_web_2002_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 10000, 1000);

        benchmark(getWords("eng-uk_web_2002_100K-sentences.txt", line -> getWords(regexLeipzig, line)), 100000, 200);

        benchmark(
                getWords("eng-uk_web_2002_1M-sentences.txt", line -> getWords(regexLeipzig, line)),
                500000,
                100
        );

        benchmark(getWords("3000-common-words.txt", line -> {
            List<String> words = new ArrayList<>();
            words.add(line);
            return words;
        }), 4000, 25000);

        benchmark(getWords("zho-simp-tw_web_2014_10K-sentences.txt", line -> getWords(regexLeipzig, line)), 5000, 1000);
    }

    private static List<String> getWords(Pattern regex, String line) {
        final Matcher matcher = regex.matcher(line);
        if (matcher.find()) {
            final String word = matcher.group(1);
            final String[] strings = word.split("[\\s\\p{Punct}\\uFF0C]");
            return Arrays.asList(strings);
        } else
            return new ArrayList<>();
    }

    private static void benchmark(String[] words, int nWords, int nRuns) {
        out.println("Testing with " + nRuns + " runs of sorting " + nWords + " words");
        Benchmark<String[]> benchmark;
        Function<Double, Double> normalizer = (time) -> time / nWords / Math.log(nWords) * 1e6;
//        Function<Double, Double> normalizer = Function.identity();

        out.println(LocalDateTime.now() + ": Starting Timsort test");
        benchmark = new Benchmark<>(Arrays::sort);
        showNormalizedTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizer
        );

        System.out.println(LocalDateTime.now() + ": Starting Quicksort test");
        Sort<String> quickSort = new QuickSort_3way<>();
        benchmark = new Benchmark<>(
                (xs) -> quickSort.sort(xs, false)
        );
        showNormalizedTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizer
        );

        System.out.println(LocalDateTime.now() + ": Starting IntroSort test");
        Sort<String> introSort = new IntroSort<>();
        benchmark = new Benchmark<>(
                (xs) -> {
                    introSort.sort(xs, false);
                },
                (xs) -> {
                    if (!introSort.getHelper().sorted(xs)) System.err.println("not sorted");
                }
        );
        showNormalizedTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizer
        );


        QuickHuskySort<String> quickHuskySort = new QuickHuskySort<>();
        IntroHuskySort<String> introHuskySort = new IntroHuskySort<>();
        Helper<String> helper = new Helper<>("StringHelper", nWords, nanoTime());

        out.println(LocalDateTime.now() + ": Starting QuickHuskySort test");
        benchmark = new Benchmark<>(
                (Consumer<String[]>) xs1 -> quickHuskySort.sort(xs1, UNICODE_CODER),
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showNormalizedTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizer
        );

        out.println(LocalDateTime.now() + ": Starting IntroHuskySort test");
        benchmark = new Benchmark<>(
                (Consumer<String[]>) xs1 -> introHuskySort.sort(xs1, UNICODE_CODER),
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showNormalizedTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizer
        );

        out.println(LocalDateTime.now() + ": Starting QuickHuskySort test with insertion sort.");
        InsertionSort<String> insertionSort = new InsertionSort<>();
        benchmark = new Benchmark<>(
                (xs) -> {
                    quickHuskySort.sort(xs, UNICODE_CODER, (xs2) -> insertionSort.sort(xs2, false));
                },
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showNormalizedTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizer
        );

        out.println(LocalDateTime.now() + ": Starting IntroHuskySort test with insertion sort.");
        benchmark = new Benchmark<>(
                (xs) -> {
                    introHuskySort.sort(xs, UNICODE_CODER, (xs2) -> insertionSort.sort(xs2, false));
                },
                (xs) -> {
                    if (!helper.sorted(xs)) System.err.println("not sorted");
                }
        );
        showNormalizedTime(
                benchmark.run(() -> generateRandomStringArray(words, nWords), nRuns),
                normalizer
        );

        out.println(LocalDateTime.now() + ": Starting Husky sort test with printout inversions");
        long inversions = 0;
        for (int i = 0; i < nRuns; i++) {
            String[] xs = generateRandomStringArray(words, nWords);
            quickHuskySort.sort(xs, UNICODE_CODER, (xs2) -> {
                // do nothing, so we can count inversions.
            });
            inversions += InversionCounter.getInversions(xs);
        }
        inversions = inversions / nRuns;
        out.println("Mean inversions after first part: " + inversions);

        out.println();
    }

    private static void showMeanTime(int nRuns, long start) {
        out.println("Mean time per run (msecs): " + (nanoTime() - start) / 1000000.0 / nRuns);
    }

    private static void showMeanTime(double time) {
        out.println("Mean time per run (msecs): " + time);
    }

    private static void showNormalizedTime(double time, Function<Double, Double> normalizer) {
        out.println("Normalized time per run: " + normalizer.apply(time));
    }

    private static String[] generateRandomStringArray(String[] lookupArray, int number) {
        Random r = new Random();
        String[] result = new String[number];
        for (int i = 0; i < number; i++) {
            result[i] = lookupArray[r.nextInt(lookupArray.length)];
        }
        return result;
    }
}
