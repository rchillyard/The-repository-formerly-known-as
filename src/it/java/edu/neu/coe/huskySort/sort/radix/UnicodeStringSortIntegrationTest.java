package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark.CHINESE_NAMES_CORPUS;

public class UnicodeStringSortIntegrationTest {


    @BeforeClass
    public static void beforeClass() throws IOException {
        config = Config.load(HuskySortBenchmark.class);
    }

    @Test
    public void test1() {
        final Config test1Config = config.copy("benchmarkstringsorters", "unicodemsdstringsort", "true");
        UnicodeMSDStringSort.setCutoff(8); // TODO remove this
        final HuskySortBenchmark huskySortBenchmark = new HuskySortBenchmark(test1Config);
        huskySortBenchmark.benchmarkUnicodeStringSortersSeeded(CHINESE_NAMES_CORPUS, HuskySortBenchmarkHelper.getWords(CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList), 200, 1, new Random(13));
    }

    private static Config config;
}