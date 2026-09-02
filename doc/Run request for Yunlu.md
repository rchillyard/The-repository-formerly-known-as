# Benchmark run requests for Yunlu — HuskySort paper, 2026-09-01 to 09-02

Five requests, of which two are already answered. **Start with the status table immediately below**;
the rest of the document is background, one section per request.

# Status, 2026-09-02 — read this first

| request | what | state |
| --- | --- | --- |
| 1 | English string baselines, with replacement | **done** — PR #63, thank you |
| 2 | The same over wholly distinct words | **done** — PR #63 |
| 3 | Real data: San Francisco building permits | **pending** |
| 4 | Full suite re-run, so every table comes from one machine and one commit | **pending, and now the big one** |
| 5 | Crossover sizes for the use-case section | **pending, short** |

**Requests 1 and 2 are answered — please do not re-run them.** Their results are already merged as
`doc/Run results from Yunlu 2026-09-01.md`, and they were exactly what was needed.

**What changed since you ran them.** Robin has decided that every figure quoted in the paper should
come from one machine, and that machine should be yours. The paper currently mixes three: its original
comparison-sort results were measured on a 2017 Intel MacBook Pro running **Java 1.8.0_152**, and the
radix-sort results on an Apple M1. Your machine becomes the single source of record, and the other two
survive only as qualitative cross-checks with no figures quoted from them.

Your full run of 2026-08-17 already covers most of what is needed, but it was taken at an older
commit — before two string-sort baselines were repaired this week. Request 4 repeats it at the current
commit so that **one machine and one commit** account for every number in the paper. That is the
tidiest possible state and it is worth a few hours of instance time.

Everything below is at the same commit, so `git fetch` and `git checkout` once and run requests 3, 4
and 5 in whatever order suits you. Request 3 is the one Robin most wants; request 4 is the one that
makes the paper internally consistent.

---

## Why any of this was needed (background to requests 1 and 2)

The paper compares RadixHuskySort against classic string sorts on English text. Two things changed
and both needed re-measuring on hardware we trust:

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

## The checkout — common to every request

Commit **`d3c359f`** on the **`Revisions`** branch — not `master`, which is 2c31d20 and predates all
of this. Please do not run an earlier commit: the string-sort baselines were repaired on 2026-09-01,
and anything before that is measuring different code. Nothing under `src/` has changed since
`d3c359f`, so the branch tip would behave identically; the commit is named so that the paper can
record one. (This document lives on `Revisions` too and has moved on since you last read it.)

```
git fetch origin Revisions
git checkout d3c359f
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

If you run `mvn test` first, it should be clean at this commit — **392 tests**, no failures. Note that
is more than the 378 you saw for requests 1 and 2: the permit corpus of request 3 arrived with fourteen
tests of its own. Anything red is worth telling us about before you start, since it would mean the
freeze is not what we think it is.

## What we were trying to settle (request 1 — ANSWERED)

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

## Second request: the same comparison over wholly distinct words — ANSWERED

**Answered in PR #63 — no action needed.** Retained because the reasoning below explains what the
result means, and because the conclusion it reached (duplicates do not explain MSD's advantage) is one
the paper now relies on.

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

## Third request: real data, the case the mechanism is best suited to — PENDING

New since the other two, and the one Robin is most likely to want in the paper. About forty minutes.

```
java -jar target/benchmarks.jar "PermitSortBenchmarks" -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff permits.json
```

No `-p` flags: the sizes and the corpus are the benchmark's own.

### Why

Every favourable case in the paper is synthetic — `Tuple.create()` generates composite keys,
`generateRandomLocalDateTimeArray` generates dates — and the largest margin we report (4.5x, on dates)
rests on generated data. This is the same shape of case on real data: San Francisco's published
building permit record, 198,900 permits from 2013 to 2018, sorted by Assessor's block, then lot, then
filing date, which is the order the records are actually browsed in.

It is favourable on all three counts that decide the mechanism's advantage, which no other case in the
paper manages simultaneously:

- the native comparison is composite and expensive — two Strings and a date;
- the encoding is **exact**, packing the whole ordering into 60 of 64 bits, so no cleanup pass runs
  at all;
- no sort specialised to municipal permit records exists.

A first measurement on our machine (4 forks, tight intervals) puts RadixHuskySort/16 at **4.40x** over
the system sort at the full corpus, and **2.19x** over QuickHuskySort. Note that Table
`RadixImprovements` reports the advantage over QuickHuskySort, so 2.19x is the comparable figure —
below the synthetic Dates row's 4.5x rather than above it. Permits are not the best number in the
paper; they are the best number on real data. Full results in
[Permit benchmark results 2026-09-01.md](Permit%20benchmark%20results%202026-09-01.md).

### The pair to look at

`quickHuskySort` and `quickHuskySortWithCleanup` compute **identical codes**. They differ only in
whether the coder declares itself perfect, so one skips the cleanup pass and the other runs it and
finds nothing to do. The difference between them is therefore the cost of the cleanup pass on an input
where it is provably unnecessary — which is the quantity the paper's $p_{crit}$ discussion turns on,
and which has never been measured directly, because every other benchmark varies the encoding and the
sort together. We measure it at 5.9% at n=32,000, 11.9% at 100,000 and **17.4% at 198,900** — growing
with n, with non-overlapping intervals throughout. A pass that finds nothing to correct still costs a
sixth of the running time at the full corpus. This may be the most useful single number in the three
requests.

### On trusting these numbers

`PermitCoderTest` verifies the exactness claim against every one of the 198,900 records — sorting by
code against sorting by the ordering, plus two million random pairs checked for sign agreement — and
`PermitSortCorrectnessTest` checks that every benchmarked sorter actually sorts, including over the
whole corpus. Both run in `mvn test`. We added the second of those because this repository has already
shipped a benchmarked sort that produced the wrong order at its largest size without anything noticing,
and JMH never checks its subject's output.

## Fourth request: the full suite at the current commit — PENDING

The long one, and the one that makes the paper consistent. Your 2026-08-17 run took 2:37:37 for the
whole suite; at five forks and ten iterations this will take longer, perhaps four to five hours. It
can run unattended.

```
java -jar target/benchmarks.jar -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff full-suite.json
```

No filter and no `-p` flags: every benchmark class, every default parameter. That includes
`PermitSortBenchmarks`, so **if you run this, request 3 is covered by it** — request 3 exists
separately only in case you want the permits result sooner, since it is the one Robin most wants.

### Why re-run what you already ran

Two reasons, and only the second is about the numbers.

Your 2026-08-17 run is on the right machine but at an older commit. Since then, both of the paper's
string-sorting baselines have been repaired: each allocated a sorter per small subarray and compared
whole strings from character zero rather than from the depth the recursion had already established.
That is worth 15–18% to three-way radix quicksort and about 1.3× to MSD. Those two sorts do not appear
in your 2026-08-17 tables, so nothing there is *wrong* — but a paper whose figures come from two
different commits invites the question of which one each number belongs to.

Second, and more simply: the paper is being consolidated onto your machine as its single source of
figures. Having every table trace to one command, one commit and one instance is worth more than the
few hours it costs.

### What it replaces

| paper table | currently measured on | will come from this run |
| --- | --- | --- |
| `HSComp` — HuskySort against the system sort | 2017 Intel MacBook Pro, Java 1.8.0_152 | `NumericSortBenchmarks`, `TupleSortBenchmarks`, `StringSortBenchmarks` |
| `RadixImprovements` — radix against QuickHuskySort | Apple M1 | the same three, plus `DateSortBenchmarks` |
| `ParallelRadix` | Apple M1 | `ParallelRadixSortBenchmarks` |
| the adversarial appendix | Apple M1 | `AdversarialSortBenchmarks` |

### Please match your 2026-08-17 environment as closely as you can

This is the environment the paper will describe, and the closer this run is to the one already
reported the less there is to reconcile. From `doc/JMH Benchmark Results 2026-08-17.md`:

| item | value to match |
| --- | --- |
| Instance | AWS EC2 `c7g.4xlarge` (AWS Graviton3), ARM Neoverse V1, aarch64 |
| vCPUs | 16 — 16 cores × 1 thread/core, no SMT, 1 socket, 1 NUMA node |
| Cache | L1d 64 KiB/core, L1i 64 KiB/core, L2 1 MiB/core, L3 32 MiB shared |
| CPU clock | not exposed to the guest; Graviton3 documented at 2.6 GHz fixed |
| Memory | **30 GiB** total, 0 B swap |
| OS / kernel | Amazon Linux 2023, kernel 6.12.95-124.187.amzn2023.aarch64 |
| JDK | OpenJDK **21.0.12** (2026-07-21 LTS), Amazon Corretto, 64-bit Server VM |
| Maven | Apache Maven **3.9.16** |
| Instance class | non-burstable — no CPU credits, so sustained performance rather than a burst window |
| Load average at collection | 0.36 / 0.36 / 0.27 on 16 CPUs |

Two small drifts between that run and yesterday's, worth pinning down rather than leaving:

- yesterday you reported kernel **6.12.100** against 6.12.95 in August, and Maven **3.9.9** against
  3.9.16. Neither should matter, but if it is easy to use the same AMI and Maven as August, do; if not,
  just tell us which you used and we will record the range.
- yesterday's report gives memory as **32 GiB** where August's gives **30 GiB**. The paper says 30, so
  we have assumed August is right — please confirm which, since it goes in a table.

Please send back `full-suite.json`, plus `lscpu`, `free -h`, `uname -r`, `java -version` and
`mvn -v` output so the environment table can be written from fact rather than from memory.

## Fifth request: the small-size crossover — PENDING

Short, perhaps half an hour, and the last gap.

```
java -jar target/benchmarks.jar "StringSortBenchmarks.(insertionSort|systemSort|quickHuskySort|radixHuskySort16)$" -p corpus=english -p n=4,10,20,50,100,200,500,1000,2000,10000 -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-crossover.json
```

The paper's use-case guidance identifies, size by size, which sorter to reach for below ten thousand
elements — where the system sort wins, where plain insertion sort wins outright, and where
QuickHuskySort takes over. Those crossovers were measured on the M1 only, and the paper says so. They
are the one set of figures the full suite will not produce, because its parameters start at 32,000.

Expect very small numbers at the low end; that is fine, the crossovers are what matter rather than the
absolute times.

## A note on the Chinese corpora

For **requests 1, 2 and 5** English is the whole question: `msdStringSort` cannot run on the Chinese
corpora at all, and the string baselines we repaired are not used for pinyin ordering. Do not pass
`-p corpus=chinese` to any of those.

**Request 4 is different** — it runs every corpus, because Table `RadixImprovements` has Chinese rows
and those figures now need to come from your machine too. It handles the corpora itself; no flags.

## Why not simply run a larger array

Because past 1,000,000 the English corpus stops being able to supply distinct words, and the
benchmark turns into a test of duplicate handling. The codebase already makes this objection about
the `commonwords` corpus, whose ~3,000 words sampled into a 200,000-element array it describes as
"artificial duplicate-heavy skew". At 1,000,000 the English corpus is approaching the same problem
from the same direction, which is the second request's whole point.
