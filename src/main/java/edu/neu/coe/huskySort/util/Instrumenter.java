package edu.neu.coe.huskySort.util;

public class Instrumenter {
    public void incCompares() {
        if (countCompares)
            compares++;
    }

    public void incSwaps() {
        if (countSwaps)
            swaps++;
    }

    public void incSwaps(final int i) {
        if (countSwaps)
            swaps += i;
    }

    public void incCopies() {
        if (countCopies)
            copies++;
    }

    public void incCopies(final int i) {
        if (countCopies)
            copies += i;
    }

    /**
     * If instrumenting, increment the number of hits by n.
     *
     * @param n the number of hits.
     */
    public void incHits(final int n) {
        if (countHits) hits += n;
    }

    /**
     * If instrumenting, increment the number of copies by n.
     *
     * @param n the number of copies made.
     */
    public void incrementCopies(final int n) {
        if (countCopies) copies += n;
    }

    public void updateStats() {
        // TODO use Instrumenter for InstrumentedCountingSOrtHelper
        if (statPack == null) throw new RuntimeException("InstrumentedComparisonSortHelper.postProcess: no StatPack");
        if (countCompares)
            statPack.add(COMPARES, compares);
        if (countSwaps)
            statPack.add(SWAPS, swaps);
        if (countCopies)
            statPack.add(COPIES, copies);
        if (countFixes)
            statPack.add(FIXES, fixes);
        if (countHits)
            statPack.add(HITS, hits);
    }

    // CONSIDER do we really need this now?
    public void init(final int n) {
        compares = 0;
        swaps = 0;
        copies = 0;
        fixes = 0;
        hits = 0;
        if (n != this.n) {
            this.n = n;
            this.statPack = new StatPack(n, COMPARES, SWAPS, COPIES, INVERSIONS, FIXES, HITS, INTERIM_INVERSIONS);
            this.countCopies = config.getBoolean(INSTRUMENTING, COPIES);
            this.countSwaps = config.getBoolean(INSTRUMENTING, SWAPS);
            this.countCompares = config.getBoolean(INSTRUMENTING, COMPARES);
            this.countInversions = config.getInt(INSTRUMENTING, INVERSIONS, 0);
            this.countFixes = config.getBoolean(INSTRUMENTING, FIXES);
            this.countHits = config.getBoolean(INSTRUMENTING, HITS); // the number of array accesses
        }
    }

    public Instrumenter(final int n, final Config config) {
        this.config = config;
        init(n);
    }

    // NOTE: the following private methods are only for testing.
    private int getCompares() {
        return compares;
    }

    private int getSwaps() {
        return swaps;
    }

    private int getFixes() {
        return fixes;
    }

    private long getHits() {
        return hits;
    }

    public StatPack getStatPack() {
        return statPack;
    }

    private final Config config;

    private int n = 0;

    private StatPack statPack;
    private int compares = 0;
    private int swaps = 0;
    private int copies = 0;
    public int fixes = 0;
    public int countInversions;

    public static final String SWAPS = "swaps";
    public static final String COMPARES = "compares";
    public static final String COPIES = "copies";
    public static final String INVERSIONS = "inversions";
    public static final String INTERIM_INVERSIONS = "interiminversions";
    public static final String FIXES = "fixes";
    public static final String INSTRUMENTING = "instrumenting";
    public static final String HITS = "hits";


    private boolean countCopies;
    private boolean countSwaps;
    private boolean countCompares;

    public boolean isCountFixes() {
        return countFixes;
    }

    private boolean countFixes;

    private boolean countHits;
    private long hits;
}
