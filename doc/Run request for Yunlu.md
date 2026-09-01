# Benchmark run request — string sorts on English, 2026-09-01

Everything here is one command. The rest of the document explains why it is worth running and what
we will do with the answer, but if you only read one section, read "The run".

## Why

The paper compares RadixHuskySort against classic string sorts on English text. Two things changed
this week and both need re-measuring on hardware we trust:

1. **MSD radix sort has been added as a second baseline**, and had three correctness defects fixed
   before it could be benchmarked at all. On our workstation it is *beating* RadixHuskySort at the
   larger sizes, which — if it holds — changes what the paper can claim.
2. **Both baselines' small-range fallbacks were handicapped** in the same way, and both have been
   repaired. Multikey quicksort's fallback alone was costing it 15–18%, which means the paper's
   published margin over multikey was partly measuring our own overhead.

Our measurements come from a shared MacBook that spent the day fighting Spotlight, an IT monitoring
agent, and at one point a closed lid. Confidence intervals ranged from 1% to 23% between runs, and
the same benchmark came out at 232, 259 and 330 ms on three occasions. That is not good enough for a
result this consequential.

## The run

Commit **`7752569`** on `master`. Please do not run an earlier commit — the baselines changed
underneath these numbers repeatedly today, and anything before this is measuring different code.

```
git checkout 7752569
mvn -Pjmh package -DskipTests
java -jar target/benchmarks.jar "StringSortBenchmarks.(msdStringSort|multikeyQuicksort|radixHuskySort8|radixHuskySort11|radixHuskySort16|systemSort)$" -p corpus=english -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-baselines.json
```

`-p corpus=english` is required, not optional: `msdStringSort` throws on any other corpus by design
(its alphabet holds 256 distinct characters beyond ASCII, and the Chinese corpora contain 3,813 and
2,270). The three sizes come from the benchmark's own `@Param` and need no flag.

Five forks and ten iterations rather than the usual, because two of the comparisons below are close
enough that we need the intervals tight. Expect roughly an hour.

Please send back `english-baselines.json`, and the machine's model, core count, OS and JVM version so
we can add it to the System Environment table if these numbers go in the paper.

## What we are trying to settle

Three questions, in order of how much they matter.

**1. Does MSD really beat RadixHuskySort at 200,000 and 1,000,000?** Two of our runs say yes, by
1.20–1.32× at 1,000,000, with non-overlapping intervals both times. This is the one that would change
the paper's claims, so it is the row to get right.

**2. Are they genuinely tied at 32,000?** Our intervals overlap there. A clean answer either way is
useful, and this size is also where we suspect an M1 cache-hierarchy effect might be doing the work —
your machine is a different architecture, which is exactly why it is worth asking you.

**3. What is the real margin over multikey quicksort?** The paper says 1.3–1.75× on English. Against
the repaired multikey we get roughly 2.0× / 1.27× / 1.38×, so the claim survives but the range is
wrong at both ends. We need a number we can print.

## What we already know, so you can sanity-check the output

From our (noisy) machine, an Apple M1 with OpenJDK 21, in ms/op:

| n | MSD | radix/16 | radix/11 | multikey (repaired) | system |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 32,000 | ~2.3 | ~2.3 | ~2.5 | ~4.6 | ~8.1 |
| 200,000 | ~26 | ~31 | ~32 | ~40 | ~79 |
| 1,000,000 | ~130 | ~177 | ~181 | ~245 | ~497 |

If your numbers are ordered very differently — particularly if `systemSort` is not comfortably the
slowest — something is wrong and it is worth a message before you spend the hour.

## One thing that does not need re-running

The Chinese corpora. `msdStringSort` cannot run on them at all, and the pinyin comparisons are
unaffected by any of this week's changes. English is the whole question.
