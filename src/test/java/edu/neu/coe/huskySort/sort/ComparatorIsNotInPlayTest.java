package edu.neu.coe.huskySort.sort;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Guards the assumption that makes the dual-path comparison safe in this repository.
 * <p>
 * Three sorts -- {@code QuickSort_3way}, {@code QuickSort_DualPivot} and {@code IntroSort} --
 * compare one way when instrumented and another when not:
 * <pre>
 *     if (helper.instrumented()) ... helper.compare(...) ... else ... x.compareTo(y) ...
 * </pre>
 * In INFO6205 that shape was a live defect: the helper there holds a {@code Comparator}, so the
 * uninstrumented path ignored it and sorted into a different order -- a wrong ANSWER, not merely a
 * wrong count. Here the two branches cannot disagree, because every helper that is actually
 * constructed implements {@code compare(v, w)} as {@code v.compareTo(w)}.
 * <p>
 * The exception is {@link ComparatorSortHelper}, which does hold a {@code Comparator} and returns
 * {@code comparator.compare(v, w)}. It is dead code -- never constructed anywhere in main or test --
 * and that is precisely what keeps the three sorts safe. Wire it up and they become wrong.
 * <p>
 * The audit of 2026 recorded that as a sentence. This test makes it fail loudly instead, because a
 * warning in a document does not survive contact with a future refactor.
 */
public class ComparatorIsNotInPlayTest {

    /**
     * The only file allowed to name ComparatorSortHelper is the one that declares it.
     */
    @Test
    public void comparatorSortHelperIsStillUnused() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path p : sources()) {
            String name = p.getFileName().toString();
            // the class that declares it, and this test, which necessarily names it
            if (name.equals(DECLARING_FILE) || name.equals(THIS_TEST)) continue;
            String text = Files.readString(p);
            if (text.contains(HELPER)) offenders.add(p.toString());
        }
        assertEquals("ComparatorSortHelper has been brought into use. It returns comparator.compare(v, w)"
                + " where every other helper returns v.compareTo(w), so the uninstrumented branch of"
                + " QuickSort_3way, QuickSort_DualPivot and IntroSort would no longer agree with the"
                + " instrumented one. Revisit those three sorts before removing this test."
                + " Offending files: " + offenders, 0, offenders.size());
    }

    /**
     * Every helper that IS constructed must compare by the natural ordering, which is the other half
     * of the same assumption. Checked by behaviour rather than by reading the source.
     */
    @Test
    public void theHelpersInUseCompareNaturally() {
        ComparableSortHelper<String> plain = new ComparableSortHelper<>("test");
        assertTrue("ComparableSortHelper must agree with compareTo",
                sameSign(plain.compare("apple", "banana"), "apple".compareTo("banana")));
        assertTrue(sameSign(plain.compare("banana", "apple"), "banana".compareTo("apple")));
        assertEquals(0, plain.compare("apple", "apple"));
    }

    private static boolean sameSign(final int a, final int b) {
        return Integer.signum(a) == Integer.signum(b);
    }

    private static List<Path> sources() throws IOException {
        List<Path> result = new ArrayList<>();
        for (String root : new String[]{"src/main/java", "src/test/java"}) {
            Path dir = Path.of(root);
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.filter(p -> p.toString().endsWith(".java")).forEach(result::add);
            }
        }
        assertTrue("expected to find the sources; is the working directory the project root?",
                result.size() > 50);
        return result;
    }

    private static final String HELPER = "ComparatorSortHelper";
    private static final String DECLARING_FILE = HELPER + ".java";
    private static final String THIS_TEST = "ComparatorIsNotInPlayTest.java";
}
