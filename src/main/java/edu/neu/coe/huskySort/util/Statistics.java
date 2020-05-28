package edu.neu.coe.huskySort.util;

public class Statistics {

    public Statistics(String property, int N) {
        this.property = property;
        doubles = new double[N];
    }

    public void add(double x) {
        if (count >= doubles.length) resize(2 * doubles.length);
        doubles[count] = x;
        count = count + 1;
        stale();
    }

    public int getCount() {
        return count;
    }

    public double total() {
        if (total == null) {
            double sum = 0;
            for (int i = 0; i < count; i++) sum += doubles[i];
            total = sum;
        }
        return total;
    }

    public double mean() {
        return total() / count;
    }

    public double stdDev() {
        if (stdDev == null) {
            double mean = mean();
            double variance = 0;
            for (int i = 0; i < count; i++) variance += (doubles[i] - mean) * (doubles[i] - mean);
            stdDev = Math.sqrt(variance / count);
        }
        return stdDev;
    }

    @Override
    public String toString() {
        //noinspection StringBufferReplaceableByString
        return new StringBuilder("Statistics for ").append(property).append(": mean=").append(mean()).append(", stdDev=").append(stdDev()).toString();
    }

    private void resize(int n) {
				double[] result = new double[n];
				System.arraycopy(doubles, 0, result, 0, doubles.length);
				doubles = result;
		}

    private void stale() {
        total = null;
        stdDev = null;
    }

    private Double total;
    private Double stdDev;

		private int count = 0;
		private double[] doubles;
		private final String property;
}
