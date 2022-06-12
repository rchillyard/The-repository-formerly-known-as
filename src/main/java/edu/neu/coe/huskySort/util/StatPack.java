package edu.neu.coe.huskySort.util;

import java.util.HashMap;

public class StatPack {

    public StatPack(final int N, final String... keys) {
        map = new HashMap<>();
        for (final String key : keys) map.put(key, new Statistics(key, N));
    }

    public void add(final String key, final double x) {
        getStatistics(key).add(x);
    }

    public Statistics getStatistics(final String key) {
        final Statistics statistics = map.get(key);
        if (statistics == null) throw new RuntimeException("StatPack.getStatistics(" + key + "): key not valid");
        return statistics;
    }

    public int getCount(final String key) {
        return getStatistics(key).getCount();
    }

    public double total(final String key) {
        return getStatistics(key).total();
    }

    public double mean(final String key) {
        return getStatistics(key).mean();
    }

    public double stdDev(final String key) {
        return getStatistics(key).stdDev();
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("StatPack {");
        if (map.isEmpty()) stringBuilder.append("<empty>}");
        for (final String key : map.keySet()) {
            final Statistics statistics = map.get(key);
            stringBuilder.append(statistics.toString()).append("; ");
        }
        return stringBuilder.toString().replaceAll("; $", "}");
    }

    private final HashMap<String, Statistics> map;
}
