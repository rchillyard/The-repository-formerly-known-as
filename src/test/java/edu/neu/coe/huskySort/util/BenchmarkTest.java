/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import org.junit.Ignore;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class BenchmarkTest {

    int pre = 0;
    int run = 0;
    int post = 0;

    // TODO figure out why this sometimes fails
    @Ignore
    public void testWaitPeriods() throws Exception {
        int nRuns = 2;
        int warmups = 2;
        Benchmark<Boolean> bm = new Benchmark<>(
                "testWaitPeriods", b -> {
            GoToSleep(100L, -1);
            return null;
        },
                b -> {
                    GoToSleep(200L, 0);
                },
                b -> {
                    GoToSleep(50L, 1);
                    return true;
                });
        double x = bm.run(true, nRuns);
        assertEquals(nRuns, post);
        assertEquals(nRuns + warmups, run);
        assertEquals(nRuns + warmups, pre);
        assertEquals(200, x, 10);
    }

    private void GoToSleep(long mSecs, int which) {
        try {
            Thread.sleep(mSecs);
            if (which == 0) run++;
            else if (which > 0) post++;
            else pre++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO figure out why this sometimes fails
    @Ignore
    public void getWarmupRuns() {
        assertEquals(2, Benchmark.getWarmupRuns(0));
        assertEquals(2, Benchmark.getWarmupRuns(20));
        assertEquals(3, Benchmark.getWarmupRuns(30));
        assertEquals(10, Benchmark.getWarmupRuns(100));
        assertEquals(10, Benchmark.getWarmupRuns(1000));
        Benchmark.setMinWarmupRuns(0);
        assertEquals(0, Benchmark.getWarmupRuns(1));
    }
}