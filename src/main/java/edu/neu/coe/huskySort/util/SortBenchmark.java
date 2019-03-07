package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.Sort;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.System.out;

public class SortBenchmark<T> {
    private final String[] words;
    private final int nRuns;
    private final String normalizePrefix;
    private final Function<Double, Double> normalizeNormalizer;

    public SortBenchmark(String[] words, int nRuns, String normalizePrefix, Function<Double, Double> normalizeNormalizer) {
        this.words = words;
        this.nRuns = nRuns;
        this.normalizePrefix = normalizePrefix;
        this.normalizeNormalizer = normalizeNormalizer;
        out.println("Instantiated " + this);
    }

    public void run(int nWords, Sort<String> sorter) {
        out.println("SortBenchmark" + LocalDateTime.now() + ": starting " + sorter + " with " + nWords + " words");
        out.println(normalizePrefix + normalizeNormalizer.apply(new Benchmark<>(
                (Consumer<String[]>) sorter::sort,
                sorter.getHelper()::checkSorted
        ).run(() -> generateRandomStringArray(words, nWords), nRuns)));
    }

    private static String[] generateRandomStringArray(String[] lookupArray, int number) {
        Random r = new Random();
        String[] result = new String[number];
        for (int i = 0; i < number; i++) result[i] = lookupArray[r.nextInt(lookupArray.length)];
        return result;
    }

    @Override
    public String toString() {
        return "SortBenchmark with " + words.length + " words and " + nRuns + " runs";
    }
}
