package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.ChineseCharacter;
import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.ConfigTest;
import edu.neu.coe.huskySort.util.Instrumenter;
import edu.neu.coe.huskySort.util.PrivateMethodInvoker;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class InstrumentedCountingSortHelperTest {

    final CharacterMap.UnicodeString 卞燕燕 = characterMap.getUnicodeString("卞燕燕");// XXX bian4 yan4 yan4
    final CharacterMap.UnicodeString 卞艳红 = characterMap.getUnicodeString("卞艳红");// XXX bian4 yan4 hong2
    final CharacterMap.UnicodeString 阿苏苏 = characterMap.getUnicodeString("阿苏苏");

    @BeforeClass
    public static void before() {
        characterMap = new CharacterMap(ChineseCharacter::new);
        config = ConfigTest.setupConfig("true", "0", "10", "1", "");
    }

    @Test
    public void testInstrumented() {
        assertTrue(new InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter>("test", config).instrumented());
    }

    @Test
    public void testLess() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", config);
        helper.init(2);
        assertFalse(helper.less(卞燕燕, 卞艳红, 0));
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(1, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testCompare() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", config);
        final CharacterMap.UnicodeString[] xs = new CharacterMap.UnicodeString[]{卞燕燕, 卞艳红};
        helper.init(xs.length);
        assertEquals(0, helper.compare(xs[0], xs[1], 0));
        assertEquals(0, helper.compare(xs[0], xs[1], 1));
        assertEquals(1, helper.compare(xs[0], xs[1], 2));
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(3, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(0, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testSorted() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", config);
        final CharacterMap.UnicodeString[] xs = new CharacterMap.UnicodeString[]{卞燕燕, 卞艳红};
        helper.init(xs.length);
        assertFalse(helper.sorted(xs));
        helper.swap(xs, 0, 1);
        assertTrue(helper.sorted(xs));
        final Instrumenter instrumenter = helper.getInstrumenter();
        final PrivateMethodInvoker privateMethodInvoker = new PrivateMethodInvoker(instrumenter);
        assertEquals(0, privateMethodInvoker.invokePrivate("getCompares"));
        assertEquals(1, privateMethodInvoker.invokePrivate("getSwaps"));
    }

    @Test
    public void testInversions() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", config);
        final CharacterMap.UnicodeString[] xs = new CharacterMap.UnicodeString[]{卞燕燕, 卞艳红};
        assertEquals(1, helper.inversions(xs));
        helper.swap(xs, 0, 1);
        assertEquals(0, helper.inversions(xs));
    }

    @Test
    public void testRandom() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", 3, 0L, config);
        final CharacterMap.UnicodeString[] xs = new CharacterMap.UnicodeString[]{卞燕燕, 卞艳红, 阿苏苏};
        final CharacterMap.UnicodeString[] strings = helper.random(CharacterMap.UnicodeString.class, r -> xs[r.nextInt(3)]);
        assertArrayEquals(new CharacterMap.UnicodeString[]{卞燕燕, 卞艳红, 卞艳红}, strings);
    }

    @Test
    public void testToString() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", 3, config);
        assertEquals("Instrumenting counting sort helper for test with 3 elements", helper.toString());
    }

    @Test
    public void testGetDescription() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", 3, config);
        assertEquals("test", helper.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testGetSetN() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", 3, config);
        assertEquals(3, helper.getN());
        helper.init(4);
        assertEquals(4, helper.getN());
    }

    @Test
    public void testGetSetNBis() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", config);
        assertEquals(0, helper.getN());
        helper.init(4);
        assertEquals(4, helper.getN());
    }

    @Test
    public void testClose() {
        final InstrumentedCountingSortHelper<CharacterMap.UnicodeString, UnicodeCharacter> helper = new InstrumentedCountingSortHelper<>("test", config);
        helper.close();
    }

    private static Config config;

    @Test
    public void swapInto() {
    }

    @Test
    public void copy() {
    }

    @Test
    public void testCompare1() {
    }

    @Test
    public void cutoff() {
    }

    @Test
    public void testToString1() {
    }

    @Test
    public void init() {
    }

    @Test
    public void preProcess() {
    }

    @Test
    public void postProcess() {
    }

    private static CharacterMap characterMap;
}