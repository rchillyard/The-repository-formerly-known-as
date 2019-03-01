/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.functions.Try;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.simple.QuickSort_3way;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.util.Benchmark;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class HuskySortBenchmark {

    public static void main(String[] args) throws Throwable {
        String[] lookupArray = loadCommonWordsFromFile(str -> true, "3000-common-words.txt");
        System.out.println("Testing on 3000 common English words.");
        System.out.printf("Percentage of unidentified: %.2f%%\n", checkUnidentified(lookupArray, 9));
        int number = 100000;
        String[] array = generateRandomStringArray(lookupArray, number);

        // Test on strings using pure quick sort.
        Sort<String> stringQuickSort3way = new QuickSort_3way<>();
        Benchmark<String[]> benchmarkPureQuickSortOnString = new Benchmark<>(
                (xs) -> { return Arrays.copyOf(xs, xs.length); },
                (xs) -> stringQuickSort3way.sort(xs, 0, xs.length - 1)
                );
        System.out.println("Sort strings using pure quick sort: \t" + benchmarkPureQuickSortOnString.run(array, 100) + "ms");

        // Test on strings using pure tim sort.
        Benchmark<String[]> benchmarkPureTimSortOnString = new Benchmark<>(
                (xs) -> { return Arrays.copyOf(xs, xs.length); },
                Arrays::sort);
        System.out.println("Sort strings using pure tim sort: \t" + benchmarkPureTimSortOnString.run(array, 100) + "ms");

        // Test on string using husky sort.
        HuskySort<String> stringHuskySort = new HuskySort<>(HuskySortHelper.asciiCoder);
        final Helper<String> stringSortHelper = stringHuskySort.getHelper();
        Benchmark<String[]> benchmarkHuskySortOnString = new Benchmark<>(
                (xs) -> Arrays.copyOf(xs, xs.length),
                (xs) -> stringHuskySort.sort(xs, false),
                (xs) -> { if (!stringSortHelper.sorted(xs)) System.err.println("not sorted"); }
                );
        System.out.println("Sort string using husky sort: \t" + benchmarkHuskySortOnString.run(array, 100) + "ms");

        System.out.println("------------------------------------------------------------------------");

        Date[] dateArray = generateRandomDateArray(number);
        // Test on date using pure tim sort.
        Benchmark<Date[]> benchmarkPureTimSortOnDate = new Benchmark<>(
                (xs) -> { return Arrays.copyOf(xs, xs.length); },
                Arrays::sort
        );
        System.out.println("Sort dates using pure tim sort: \t" + benchmarkPureTimSortOnDate.run(dateArray, 100) + "ms");

        // Test on date using husky sort.
        HuskySort<Date> dateHuskySort = new HuskySort<>(HuskySortHelper.dateCoder);
        final Helper<Date> dateHelper = dateHuskySort.getHelper();
        Benchmark<Date[]> benchmarkHuskySortOnDate = new Benchmark<>(
                (xs) -> Arrays.copyOf(xs, xs.length),
                (xs) -> dateHuskySort.sort(xs, false),
                (xs) -> { if (!dateHelper.sorted(xs)) System.err.println("not sorted"); }
        );
        System.out.println("Sort dates using husky sort: \t" + benchmarkHuskySortOnDate.run(dateArray, 100) + "ms");

        System.out.println("------------------------------------------------------------------------");

        LocalDateTime[] LocalDateTimeArray = generateRandomLocalDateTimeArray(number);
        // Test on date using pure tim sort.
        Benchmark<LocalDateTime[]> benchmarkPureTimSortOnLocalDateTime = new Benchmark<>(
                (xs) -> { return Arrays.copyOf(xs, xs.length); },
                Arrays::sort
                );
        System.out.println("Sort localDateTimes using pure tim sort: \t" + benchmarkPureTimSortOnLocalDateTime.run(LocalDateTimeArray, 100) + "ms");

        // Test on date using husky sort.
        HuskySort<ChronoLocalDateTime<?>> chronoLocalDateHuskySort = new HuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder);
        final Helper<ChronoLocalDateTime<?>> chronoHelper = chronoLocalDateHuskySort.getHelper();
        Benchmark<LocalDateTime[]> benchmarkHuskySortOnLocalDateTime = new Benchmark<>(
                (xs) -> Arrays.copyOf(xs, xs.length),
                (xs) -> chronoLocalDateHuskySort.sort(xs, false),
                (xs) -> { if (!chronoHelper.sorted(xs)) System.err.println("not sorted"); }
                );
        System.out.println("Sort localDateTimes using husky sort: \t" + benchmarkHuskySortOnLocalDateTime.run(LocalDateTimeArray, 100) + "ms");
    }

    private static double checkUnidentified(String[] words, int offcut) {
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

    private static String[] loadCommonWordsFromFile(Function<String, Boolean> filter, String fileName) throws Throwable {
        List<String> commonWords = new ArrayList<>();
        Optional<URL> uo = Optional.ofNullable(HuskySortBenchmark.class.getClassLoader().getResource(fileName));
        Try<BufferedReader> fy = liftBufferedReader(Try.toTry(uo.map(URL::getFile)));
        if (fy.isSuccess()) {
            for (Object line : fy.get().lines().toArray()) {
                String temp = (String) line;
                if (filter.apply(temp))
                    commonWords.add(temp);
            }
            String[] result = new String[commonWords.size()];
            result = commonWords.toArray(result);
            return result;
        } else throw fy.getMessage();
    }

    private static Try<BufferedReader> liftBufferedReader(Try<String> sy) throws FileNotFoundException {
        if (sy.isSuccess()) return Try.success(getBufferedReader(sy.get()));
        else return Try.failure(sy.getMessage());
    }

    private static BufferedReader getBufferedReader(String s) throws FileNotFoundException {
        return new BufferedReader(new FileReader(s));
    }

    private static String[] generateRandomStringArray(String[] lookupArray, int number) {
        Random r = new Random();
        String[] result = new String[number];
        for (int i = 0; i < number; i++) {
            result[i] = lookupArray[r.nextInt(lookupArray.length)];
        }
        return result;
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
}
