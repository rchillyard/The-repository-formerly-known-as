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

Commit **`a83e7ea`** on `master`. Please do not run an earlier commit — the baselines changed
underneath these numbers repeatedly today, and anything before this is measuring different code.
(This document itself lives on `master` and may have moved on; the commit above is the one to run.)

```
git checkout a83e7ea
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

If you run `mvn test` first, it should be clean at this commit — 378 tests, no failures. Anything
red there is worth telling us about before you start the benchmark, since it would mean the freeze
is not what we think it is.

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

## Second request: the same comparison over wholly distinct words

Shorter than the first — one size, three sorts, about twenty minutes. Please run it, but run the
first one first; if you only have time for one, the first is the one we need.

```
java -jar target/benchmarks.jar "StringSortBenchmarks.(msdStringSort|multikeyQuicksort|radixHuskySort16)$" -p corpus=english -p sampling=distinct -p n=32000,200000,250000 -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-distinct.json
```

### Why

The harness builds its array by sampling the corpus **with replacement**, and the English corpus
holds only 275,333 distinct words. So the larger the array, the more often the same word recurs:

| n | distinct values present | average copies of each |
| ---: | ---: | ---: |
| 32,000 | 30,226 | 1.06 |
| 200,000 | 142,100 | 1.41 |
| 1,000,000 | 268,112 | 3.73 |

Duplicate density therefore rises with n — and so does MSD's advantage over RadixHuskySort, from
nothing at 32,000 to 1.32x at 1,000,000. Those two facts may be the same fact. A bucketing sort gets
equal keys cheaply: they land in one bucket, and are recognised as needing no further work. A
comparison sort, and RadixHuskySort's cleanup pass, do not get that for free.

If the advantage is really about duplicates, then "MSD beats RadixHuskySort on English at scale" is
the wrong conclusion, and "MSD handles duplicate-heavy input better" is the right one — a much
narrower claim, and one that says as much about the corpus as about the algorithms.

`-p sampling=distinct` draws without replacement instead, from a shuffled corpus, so every element is
a different word. That bounds n by the corpus size, which is why the largest size here is 250,000
rather than 1,000,000 — 250,000 distinct words is the most this corpus honestly supports.

Note that 32,000 and 200,000 appear in **both** requests, so those two rows can be compared directly
with and against replacement. That comparison is the actual experiment.

### What we already found, which points the other way

We ran this ourselves at n=200,000, three forks, before writing the request. It does **not** support
the hypothesis above:

| sampling | MSD | radix/16 | multikey | MSD vs radix/16 |
| --- | ---: | ---: | ---: | ---: |
| distinct | 28.723 ± 1.967 | 37.850 ± 0.817 | 45.703 ± 0.974 | **1.32x** |
| with replacement | 25.839 ± 1.272 | 31.106 ± 2.297 | 39.573 ± 0.310 | 1.20x |

Removing the duplicates makes MSD's advantage *larger*. Both sorts slow down on all-distinct input --
MSD by 11%, RadixHuskySort/16 by 22% -- so the duplicates were, if anything, helping RadixHuskySort.
The confound we suspected is not there.

That makes this request more worth running rather than less: the duplicate-free comparison is the
cleaner one, and it is currently the least favourable number we have. Two of the three rows also come
from different runs on a loaded machine, so the 1.32x against 1.20x could be partly noise, and that
is exactly the sort of thing your machine settles and ours cannot.

## One thing that does not need re-running

The Chinese corpora. `msdStringSort` cannot run on them at all, and the pinyin comparisons are
unaffected by any of this week's changes. English is the whole question.

## Why not simply run a larger array

Because past 1,000,000 the English corpus stops being able to supply distinct words, and the
benchmark turns into a test of duplicate handling. The codebase already makes this objection about
the `commonwords` corpus, whose ~3,000 words sampled into a 200,000-element array it describes as
"artificial duplicate-heavy skew". At 1,000,000 the English corpus is approaching the same problem
from the same direction, which is the second request's whole point.
