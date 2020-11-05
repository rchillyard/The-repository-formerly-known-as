package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.InstrumentedHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortable;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.ConfigTest;
import edu.neu.coe.huskySort.util.PrivateMethodInvoker;
import edu.neu.coe.huskySort.util.StatPack;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper.generateRandomAlphaBetaArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("MagicConstant")
public class HuskySortTest {

    @Test
    public void testSplitLineIntoStrings() {
        final PrivateMethodInvoker invoker = new PrivateMethodInvoker(HuskySortBenchmarkHelper.class);
        @SuppressWarnings("unchecked") final List<String> words = (List<String>) invoker.invokePrivate("splitLineIntoStrings", "11204341\tConsider the extras not usually included with any 'FREE' car offer: CDW/LDW @ $12.95 to $13.95 a day.\n", Pattern.compile("[~\\t]*\\t((\\s*[a-zA-Z]*)*)"), HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER);
        assertEquals(8, words.size());
    }

    static final class Person implements HuskySortable<Person> {

        Person(String firstName, String lastName) {
            this.lastName = lastName;
            this.firstName = firstName;
        }

        public long huskyCode() {
            if (huskycode == 0L)
                huskycode = HuskyCoderFactory.asciiToLong(lastName);
            return huskycode;
        }

        @Override
        public int compareTo(Person x) {
            int cf = lastName.compareTo(x.lastName);
            if (cf != 0) return cf;
            return firstName.compareTo(x.firstName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(lastName, person.lastName) &&
                    Objects.equals(firstName, person.firstName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lastName, firstName);
        }

        @Override
        public String toString() {
            return lastName;
        }

        private final String lastName;
        private final String firstName;

        private long huskycode = 0L;
    }

    @Test
    public void testSortPerson() {
        Person[] xs = {new Person("Robin", "Hillyard"), new Person("Yunlu", "Liao Zheng"), new Person("Miranda", "Hillyard"), new Person("William", "Hillyard"), new Person("Ella", "Hillyard"), new Person("Paul", "Hillyard"), new Person("Mia", "Hillyard")};
        QuickHuskySort<Person> sorter = new QuickHuskySort<>(Person::huskyCode, config);
        Person[] sorted = sorter.sort(xs);
        assertTrue("sorted", sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortString1() {
        String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        assertTrue("sorted", sorter.getHelper().sorted(sorter.sort(xs)));
    }

    @Test
    public void testSortString2() {
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "");
        QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        final HuskyHelper<String> helper = sorter.getHelper();
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final int inversionsOriginal = helper.inversions(xs);
        System.out.println("inversions: " + inversionsOriginal);
        sorter.preProcess(xs);
        final String[] ys = sorter.sort(xs);
        assertTrue("sorted", helper.sorted(ys));
        sorter.postProcess(ys);
        final Helper<String> delegateHelper = InstrumentedHelper.getInstrumentedHelper(helper, (InstrumentedHelper<String>) helper.getHelper());
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(delegateHelper);
        final StatPack statPack = (StatPack) privateMethodInvoker.invokePrivate("getStatPack");
        System.out.println(statPack);
        assertEquals(0, helper.inversions(ys));
        final int fixes = (int) statPack.getStatistics(InstrumentedHelper.FIXES).mean();
        assertTrue(inversionsOriginal <= fixes);
    }

    @Test
    public void testSortString3() {
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "true");
        QuickHuskySort<String> sorter = new QuickHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        final HuskyHelper<String> helper = sorter.getHelper();
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> {
            int x = r.nextInt(1000000000);
            final BigInteger b = BigInteger.valueOf(x).multiply(BigInteger.valueOf(1000000));
            return b.toString();
        });
        final int inversionsOriginal = helper.inversions(xs);
        System.out.println("inversions: " + inversionsOriginal);
        sorter.preProcess(xs);
        final String[] ys = sorter.sort(xs);
        assertTrue("sorted", helper.sorted(ys));
        sorter.postProcess(ys);
        final Helper<String> delegateHelper = InstrumentedHelper.getInstrumentedHelper(helper, (InstrumentedHelper<String>) helper.getHelper());
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(delegateHelper);
        final StatPack statPack = (StatPack) privateMethodInvoker.invokePrivate("getStatPack");
        System.out.println(statPack);
        assertEquals(0, helper.inversions(ys));
        final int fixes = (int) statPack.getStatistics(InstrumentedHelper.FIXES).mean();
        assertTrue(inversionsOriginal <= fixes);
        final int ii = (int) statPack.getStatistics("interiminversions").mean();
        assertEquals(0, ii);

    }

    @Test
    public void testSortString4() {
        final int N = 1000;
        final Config config = ConfigTest.setupConfig("false", "0", "1", "", "");
        doTestIntroHuskySort(N, config, "false", false, "00000000", 0.0, 10.0);
    }

    @Test
    public void testSortString5() {
        final int N = 1000;
        final Config config = ConfigTest.setupConfig("false", "0", "1", "", "");
        doTestIntroHuskySort(N, config, "true", true, "000000", 230, 100.0);
    }

    @Test
    public void testSortString6() {
        final int N = 1000;
        final Config config = ConfigTest.setupConfig("false", "0", "1", "", "");
        doTestIntroHuskySort(N, config, "true", true, "0000000", 2700.0, 1000.0);
    }

    @Test
    public void testSortString7() {
        final int N = 1000;
        final Config config = ConfigTest.setupConfig("false", "0", "1", "", "");
        doTestIntroHuskySort(N, config, "true", true, "00000000", 27398.0, 2000);
    }

    @Test
    public void testSortString8() {
        final int N = 1000;
        final Config config = ConfigTest.setupConfig("true", "0", "1", "", "true");
        IntroHuskySort<String> sorter = IntroHuskySort.createIntroHuskySortWithInversionCount(HuskyCoderFactory.englishCoder, N, config);
        final HuskyHelper<String> helper = sorter.getHelper();
        helper.init(N);
        String[] xs = generateRandomAlphaBetaArray(N, 4, 10);
        final int inversionsOriginal = helper.inversions(xs);
        System.out.println("inversions: " + inversionsOriginal);
        sorter.preProcess(xs);
        final String[] ys = sorter.sort(xs);
        assertTrue("sorted", helper.sorted(ys));
        sorter.postProcess(ys);
        sorter.close();
        final Helper<String> delegateHelper = InstrumentedHelper.getInstrumentedHelper(helper, (InstrumentedHelper<String>) helper.getHelper());
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(delegateHelper);
        assertEquals(0, helper.inversions(ys));
        double ii = sorter.getMeanInterimInversions();
        assertEquals(0.0, ii, 10);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSortOldDate() {
        // NOTE it's OK that these methods are deprecated.
        Date[] xs = {new Date(2018, 11, 9), new Date(2018, 11, 6), new Date(2018, 10, 31), new Date(2018, 1, 1)};
        AbstractHuskySort<Date> sorter = new edu.neu.coe.huskySort.sort.huskySort.QuickHuskySort<>(HuskyCoderFactory.dateCoder, config);
        assertTrue("sorted", sorter.getHelper().sorted(sorter.sort(xs)));
    }

    @Test
    public void testSortJavaTime() {
        ChronoLocalDateTime<?> d1 = LocalDateTime.of(2018, 11, 6, 10, 6, 45);
        ChronoLocalDateTime<?> d2 = LocalDateTime.of(2018, 11, 9, 22, 3, 15);
        ChronoLocalDateTime<?> d3 = LocalDateTime.of(1963, 11, 22, 13, 30, 0);
        ChronoLocalDateTime<?> d4 = LocalDateTime.of(2018, 10, 31, 22, 3, 15);
        ChronoLocalDateTime<?> d5 = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
        ChronoLocalDateTime<?>[] xs = {d1, d2, d3, d4, d5};
        QuickHuskySort<ChronoLocalDateTime<?>> sorter = new edu.neu.coe.huskySort.sort.huskySort.QuickHuskySort<>(HuskyCoderFactory.chronoLocalDateTimeCoder, config);
        assertTrue("sorted", sorter.getHelper().sorted(sorter.sort(xs)));
    }

    @BeforeClass
    public static void before() throws IOException {
        config = Config.load(HuskySortTest.class);
    }

    private static Config config;

    private void doTestIntroHuskySort(int n, Config config, String countInterimInversions, Boolean hasDelegateHelper, final String prefix, final double expected, final double delta) {
        Config config1 = config.copy("huskyhelper", "countinteriminversions", countInterimInversions);
        IntroHuskySort<String> sorter = IntroHuskySort.createIntroHuskySortWithInversionCount(HuskyCoderFactory.asciiCoder, n, config1);
        final HuskyHelper<String> helper = sorter.getHelper();
        helper.init(n);
        final String[] xs = helper.random(String.class, r -> prefix + r.nextInt(10000));
        final int inversionsOriginal = helper.inversions(xs);
        System.out.println("inversions: " + inversionsOriginal);
        sorter.preProcess(xs);
        final String[] ys = sorter.sort(xs);
        assertTrue("sorted", helper.sorted(ys));
        sorter.postProcess(ys);
        sorter.close();
        SortWithHelper<String> adjunctSorter = sorter.getAdjunctSorter();
        Helper<String> helper1 = adjunctSorter.getHelper();
        final InstrumentedHelper<String> delegateHelper = InstrumentedHelper.getInstrumentedHelper(helper1, null);
        assertEquals(hasDelegateHelper, delegateHelper != null);
        if (hasDelegateHelper && sorter.isClosed()) assertEquals(expected, sorter.getMeanInterimInversions(), delta);
    }

}
