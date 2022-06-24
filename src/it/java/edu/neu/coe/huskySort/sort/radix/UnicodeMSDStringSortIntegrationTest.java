package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.Sorter;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.util.Benchmark;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.function.Supplier;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.CHINESE_NAMES_CORPUS;

public class UnicodeMSDStringSortIntegrationTest {


    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(HuskySortBenchmark.class);
    }

    @Test
    public void test1() {
        final Config test1Config = config.
                copy("benchmarkstringsorters", "unicodemsdstringsort", "true").
                copy("helper", "cutoff", "8");
        Benchmark.setMinWarmupRuns(0);
        final HuskySortBenchmark huskySortBenchmark = new HuskySortBenchmark(test1Config);
        huskySortBenchmark.benchmarkUnicodeStringSortersSeeded(CHINESE_NAMES_CORPUS, HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList), 1000, 1, new Random(0L));
    }

    @Test
    public void test2() {
        final Config test1Config = config.
                copy("benchmarkstringsorters", "unicodemsdstringsort", "true").
                copy("helper", "cutoff", "8");
        Benchmark.setMinWarmupRuns(0);
        final String[] words = HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        final Sorter<String> sorter = new UnicodeMSDStringSort(new CharacterMap(ChineseCharacter::new, '阿'));
        final Benchmark<String[]> benchmark = new Benchmark<>("UnicodeMSDStringSort (Chinese Names)", null, sorter::sortArray, HuskySortBenchmark::checkChineseSorted);
        final Supplier<String[]> wordSupplier = HuskySortBenchmark.getWordSupplier(words, 1000, new Random(0L));
        final double time = benchmark.run(wordSupplier, 100);
        System.out.println("time: " + time);
    }

    private static Config config;
}
