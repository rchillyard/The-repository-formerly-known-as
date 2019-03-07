package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.huskySort.InversionCounter;

import java.util.Random;

import static java.lang.System.out;

public class StringSortBenchmark {
    private final String[] words;
    private final int nRuns;

    public StringSortBenchmark(String[] words, int nRuns) {
        this.words = words;
        this.nRuns = nRuns;
    }

    public void run(Sort<String> sorter, int nWords) {
        final AnnotatedBenchmark<String[]> annotatedBenchmark = new AnnotatedBenchmark<>(
                out::println,
                "StringSortBenchmark starting at %tD %<tT: " + sorter + " with " + nWords + " words",
                new CheckedSortBenchmark<>(sorter),
                "Normalized time per run: %f",
                (time) -> time / nWords / Math.log(nWords) * 1e6);
        annotatedBenchmark.run(() -> generateRandomStringArray(words, nWords), nRuns);
    }

    public void runWithInversionCount(int nWords, Sort<String> sorter) {
        class Inversions {
            long inversions = 0;

            public void increment(long increment) {
                inversions += increment;
            }
        }

        final Inversions inversions = new Inversions();
        Aggregator<String[]> aggregator = new Aggregator<>(sorter::mutatingSort, xs -> inversions.increment(InversionCounter.getInversions(xs)));

        final AnnotatedAggregator<String[]> annotatedAggregator = new AnnotatedAggregator<>(
                out::println,
                "StringSortBenchmark starting inversion count %tD %<tT: " + sorter + " with " + nWords + " words",
                aggregator,
                "Mean inversions after first part: %f");

        annotatedAggregator.run(() -> generateRandomStringArray(words, nWords), nRuns, () -> 1.0 * inversions.inversions / nRuns);

//        out.println("Normalized mean inversions: " + inversions.inversions / Math.log(nWords) / nRuns);
    }

    private static String[] generateRandomStringArray(String[] lookupArray, int number) {
        Random r = new Random();
        String[] result = new String[number];
        for (int i = 0; i < number; i++) result[i] = lookupArray[r.nextInt(lookupArray.length)];
        return result;
    }

    @Override
    public String toString() {
        return "StringSortBenchmark with " + words.length + " words and " + nRuns + " runs";
    }
}
