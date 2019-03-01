package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.util.PrivateMethodTester;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuskySortTest {

    @Test
    public void testGetWords() {
        final PrivateMethodTester tester = new PrivateMethodTester(edu.neu.coe.huskySort.sort.huskySort.HuskySortTest.class);
        //noinspection unchecked
        final List<String> words = (List<String>) tester.invokePrivate("getWords", Pattern.compile("[~\\t]*\\t((\\s*[a-zA-Z]*)*)"), "11204341\tConsider the extras not usually included with any 'FREE' car offer: CDW/LDW @ $12.95 to $13.95 a day.\n");
        assertEquals(8, words.size());
    }

    @Test
    public void testInt() {
        Integer[] xs = {0, 3, 1000000000, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};
        Sort<Integer> sorter = new HuskySort<>();
        Integer[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testLong1() {
        Long[] xs = {0L, 3L, 1000000000000000000L, -1L, Long.MAX_VALUE, Long.MIN_VALUE};
        Sort<Long> sorter = new HuskySort<>();
        sorter.getHelper();
        Long[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testLong2() {
        Sort<Long> sorter = new HuskySort<>();
        Helper<Long> helper = sorter.getHelper();
        Long[] xs = helper.random(10000, Long.class, Random::nextLong);
        Long[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortDouble0() {
        Double[] xs = {Math.PI, Math.E, -Math.PI, Math.E / 2};
        Sort<Double> sorter = new HuskySort<>();
        Double[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortDouble1() {
        Double[] xs = {Math.PI, Math.E, -Math.PI, Math.E / 2};
        Sort<Double> sorter = new HuskySort<>(HuskySortHelper.doubleCoder);
        Double[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortDouble2() {
        Double[] xs = {Math.PI, Math.E, -Math.PI, Math.E / 2};
        Sort<Double> sorter = new HuskySort<>(new HuskyCoder<Double>() {
            @Override
            public long huskyEncode(Double x) {
                return Double.doubleToLongBits(x);
            }

            @Override
            public boolean imperfect() {
                return true;
            }
        });
        Double[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testDouble3() {
        Sort<Double> sorter = new HuskySort<>();
        Helper<Double> helper = sorter.getHelper();
        Double[] xs = helper.random(10000, Double.class, Random::nextDouble);
        Double[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testFloat() {
        Float[] xs = {(float) Math.PI, (float) -Math.PI, 1E21F, 0F, 1F};
        Sort<Float> sorter = new HuskySort<>();
        Float[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testShort() {
        Short[] xs = {0, 3, 10000, -1, Short.MAX_VALUE, Short.MIN_VALUE};
        Sort<Short> sorter = new HuskySort<>();
        Short[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testBigDecimal1() {
        BigDecimal[] xs = {BigDecimal.valueOf(Math.PI), BigDecimal.valueOf(Math.PI).negate(), BigDecimal.valueOf(1E21), BigDecimal.ZERO, BigDecimal.ONE.negate()};
        Sort<BigDecimal> sorter = new HuskySort<>();
        BigDecimal[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testBigDecimal2() {
        Sort<BigDecimal> sorter = new HuskySort<>();
        Helper<BigDecimal> helper = sorter.getHelper();
        BigDecimal[] xs = helper.random(10000, BigDecimal.class, r -> BigDecimal.valueOf(r.nextDouble()));
        BigDecimal[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortBigInteger0() {
        BigInteger[] xs = {BigInteger.TEN, BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE.negate()};
        Sort<BigInteger> sorter = new HuskySort<>();
        BigInteger[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortBigInteger1() {
        BigInteger[] xs = {BigInteger.TEN, BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE.negate()};
        Sort<BigInteger> sorter = new HuskySort<>(HuskySortHelper.bigIntegerCoder);
        BigInteger[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    class MyClass implements HuskySortable<MyClass> {
        MyClass(String x) {
            this.x = x;
        }

        @Override
        public long huskyCode() {
            return HuskySortHelper.unicodeToLong(x);
        }

        @Override
        public int compareTo(MyClass x) {
            return this.x.compareTo(x.x);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyClass myClass = (MyClass) o;
            return Objects.equals(x, myClass.x);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x);
        }

        @Override
        public String toString() {
            return x;
        }

        private final String x;
    }

    @Test
    public void testSortMyClass() {
        MyClass[] xs = {new MyClass("Hello"), new MyClass("Goodbye"), new MyClass("Ciao"), new MyClass("Welkommen")};
        Sort<MyClass> sorter = new HuskySort<>();
        MyClass[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    class Person implements HuskySortable<Person> {
        Person(String firstName, String lastName) {
            this.lastName = lastName;
            this.firstName = firstName;
        }

        @Override
        public long huskyCode() {
            return HuskySortHelper.unicodeToLong(lastName);
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
    }

    @Test
    public void testSortPerson() {
        Person[] xs = {new Person("Robin", "Hillyard"), new Person("Yunlu", "Liao Zheng"), new Person("Miranda", "Hillyard"), new Person("William", "Hillyard"), new Person("Ella", "Hillyard"), new Person("Paul", "Hillyard"), new Person("Mia", "Hillyard")};
        Sort<Person> sorter = new HuskySort<>();
        Person[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortString() {
        String[] xs = {"Hello", "Goodbye", "Ciao", "Welkommen"};
        Sort<String> sorter = new HuskySort<>(HuskySortHelper.asciiCoder);
        String[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSortOldDate() {
        Date[] xs = {new Date(2018, 11, 9), new Date(2018, 11, 6), new Date(2018, 10, 31), new Date(2018, 1, 1)};
        Sort<Date> sorter = new HuskySort<>(HuskySortHelper.dateCoder);
        Date[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }

    @Test
    public void testSortJavaTime() {
        ChronoLocalDateTime<?> d1 = LocalDateTime.of(2018, 11, 6, 10, 6, 45);
        ChronoLocalDateTime<?> d2 = LocalDateTime.of(2018, 11, 9, 22, 3, 15);
        ChronoLocalDateTime<?> d3 = LocalDateTime.of(1963, 11, 22, 13, 30, 0);
        ChronoLocalDateTime<?> d4 = LocalDateTime.of(2018, 10, 31, 22, 3, 15);
        ChronoLocalDateTime<?> d5 = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
        ChronoLocalDateTime<?>[] xs = {d1, d2, d3, d4, d5};
        Sort<ChronoLocalDateTime<?>> sorter = new HuskySort<>(HuskySortHelper.chronoLocalDateTimeCoder);
        ChronoLocalDateTime<?>[] sorted = sorter.sort(xs, false);
        assertTrue(sorter.getHelper().sorted(sorted));
    }
}
