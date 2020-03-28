package edu.neu.coe.huskySort.util;

import java.util.HashMap;

public class StatPack {

    public StatPack(int N, String... keys) {
        map = new HashMap<>();
        for (String key : keys) map.put(key, new Statistics(key, N));
    }

    public void add(String key, double x) {
        getStatistics(key).add(x);
    }

    public Statistics getStatistics(String key) {
        final Statistics statistics = map.get(key);
        if (statistics == null) throw new RuntimeException("StatPack.getStatistics(" + key + "): key not valid");
        return statistics;
    }

    public int getCount(String key) {
        return getStatistics(key).getCount();
    }

    public double total(String key) {
        return getStatistics(key).total();
    }

    public double mean(String key) {
        return getStatistics(key).mean();
    }

    public double stdDev(String key) {
        return getStatistics(key).stdDev();
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("StatPack{");
        for (String key : map.keySet()) stringBuilder.append(map.get(key).toString()).append(", ");
        return stringBuilder.toString();
    }

    private final HashMap<String, Statistics> map;
}