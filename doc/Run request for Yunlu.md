# Benchmark run requests for Yunlu — HuskySort paper

| request | what | state |
| --- | --- | --- |
| 1 | English string baselines, with replacement | **done** — PR #63, thank you |
| 2 | The same over wholly distinct words | **done** — PR #63 |
| 3 | Real data: San Francisco building permits | **pending** — ~40 min |
| 4 | The full suite at the current commit | **pending** — ~4–5 hours, unattended |
| 5 | The small-N crossover | **pending** — ~30 min |

**Requests 1 and 2 are answered — please do not re-run them.** Their results are merged as
`doc/Run results from Yunlu 2026-09-01.md` and were exactly what was needed. Their background, and
what they settled, is in the appendix at the end of this document; nothing there needs acting on.

**What has changed since.** Robin has decided that every figure quoted in the paper should come from
one machine, and that machine should be yours. The paper currently mixes three: its original
comparison-sort results were measured on a 2017 Intel MacBook Pro running **Java 1.8.0_152**, and its
radix-sort results on an Apple M1. Yours becomes the single source of record; the other two survive
only as qualitative cross-checks, with no figures quoted from them.

Your full run of 2026-08-17 already covers most of what that needs, but at a commit predating this
week's repairs to two string-sort baselines. **Request 4 repeats it at the current commit, so that one
machine and one commit account for every number in the paper.**

Requests 3, 4 and 5 share one checkout and can be run in any order. Request 3 is the one Robin most
wants; request 4 is the one that makes the paper internally consistent.

---

## The checkout — common to requests 3, 4 and 5

Commit **`d3c359f`** on the **`Revisions`** branch — not `master`, which is `2c31d20` and predates all
of this. Nothing under `src/` has changed since `d3c359f`, so the branch tip behaves identically; the
commit is named so the paper can record one.

```
git fetch origin Revisions
git checkout d3c359f
mvn -Pjmh package -DskipTests
```

`mvn test` at this commit should be clean: **392 tests**, no failures. That is more than the 378 you
saw for requests 1 and 2 — the permit corpus of request 3 arrived with fourteen tests of its own.
Anything red is worth a message before you start, since it would mean the freeze is not what we think
it is.

Every command below wants five forks and ten iterations, which is more than the suite's own defaults,
because several of the comparisons are close enough to need tight intervals.

---

## Request 3 — real data: San Francisco building permits

About forty minutes. **Subsumed by request 4**, so skip it if you go straight to that; it stands
separately only because it is the result Robin most wants to see first.

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
- the encoding is **exact**, packing the whole ordering into 60 of 64 bits, so no cleanup pass runs at
  all;
- no sort specialised to municipal permit records exists.

A first measurement on our machine puts RadixHuskySort/16 at **4.40x** over the system sort at the full
corpus and **2.19x** over QuickHuskySort. Table `RadixImprovements` reports the advantage over
QuickHuskySort, so 2.19x is the comparable figure — below the synthetic Dates row's 4.5x rather than
above it. Permits are not the best number in the paper; they are the best number on real data. Full
results in [Permit benchmark results 2026-09-01.md](Permit%20benchmark%20results%202026-09-01.md).

### The pair to look at

`quickHuskySort` and `quickHuskySortWithCleanup` compute **identical codes**. They differ only in
whether the coder declares itself perfect, so one skips the cleanup pass and the other runs it and
finds nothing to do. The gap between them is therefore the cost of the cleanup pass on input where it
is provably unnecessary — the quantity the paper's $p_{crit}$ discussion turns on, and which has never
been isolated, because every other benchmark varies the encoding and the sort together. We measure
5.9% at n=32,000, 11.9% at 100,000 and **17.4% at 198,900**, growing with n, non-overlapping intervals
throughout. This may be the most useful single number in the whole set of requests.

### On trusting these numbers

`PermitCoderTest` verifies the exactness claim against every one of the 198,900 records — sorting by
code against sorting by the ordering, plus two million random pairs checked for sign agreement — and
`PermitSortCorrectnessTest` checks that every benchmarked sorter actually sorts, including over the
whole corpus. Both run under `mvn test`. The second exists because this repository has already shipped
a benchmarked sort that produced the wrong order at its largest size without anything noticing, and
JMH never checks its subject's output.

---

## Request 4 — the full suite at the current commit

The long one, and the one that makes the paper consistent. Your 2026-08-17 run took 2:37:37; at five
forks and ten iterations expect perhaps four to five hours. It can run unattended.

```
java -jar target/benchmarks.jar -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff full-suite.json
```

No filter and no `-p` flags: every benchmark class, every default parameter, every corpus.

### Why re-run what you already ran

Your 2026-08-17 run is on the right machine but at an older commit. Since then both of the paper's
string-sorting baselines have been repaired — each allocated a sorter per small subarray and compared
whole strings from character zero rather than from the depth the recursion had already established,
worth 15–18% to three-way radix quicksort and about 1.3x to MSD. Neither of those sorts appears in your
2026-08-17 tables, so nothing there is *wrong*; but a paper whose figures come from two commits invites
the question of which one each number belongs to.

### What it replaces

| paper table | currently measured on | will come from this run |
| --- | --- | --- |
| `HSComp` — HuskySort against the system sort | 2017 Intel MacBook Pro, Java 1.8.0_152 | `NumericSortBenchmarks`, `TupleSortBenchmarks`, `StringSortBenchmarks` |
| `RadixImprovements` — radix against QuickHuskySort | Apple M1 | the same three, plus `DateSortBenchmarks` |
| `ParallelRadix` | Apple M1 | `ParallelRadixSortBenchmarks` |
| the adversarial appendix | Apple M1 | `AdversarialSortBenchmarks` |

### Please match your 2026-08-17 environment as closely as you can

This is the environment the paper will describe, so the closer this run is to the one already reported,
the less there is to reconcile. From `doc/JMH Benchmark Results 2026-08-17.md`:

| item | value to match |
| --- | --- |
| Instance | AWS EC2 `c7g.4xlarge` (AWS Graviton3), ARM Neoverse V1, aarch64 |
| vCPUs | 16 — 16 cores × 1 thread/core, no SMT, 1 socket, 1 NUMA node |
| Cache | L1d 64 KiB/core, L1i 64 KiB/core, L2 1 MiB/core, L3 32 MiB shared |
| CPU clock | not exposed to the guest; Graviton3 documented at 2.6 GHz fixed |
| Memory | **30 GiB** total, 0 B swap |
| OS / kernel | Amazon Linux 2023, kernel `6.12.95-124.187.amzn2023.aarch64` |
| JDK | OpenJDK **21.0.12** (2026-07-21 LTS), Amazon Corretto, 64-bit Server VM |
| Maven | Apache Maven **3.9.16** |
| Instance class | non-burstable — no CPU credits, so sustained rather than burst performance |
| Load average at collection | 0.36 / 0.36 / 0.27 on 16 CPUs |

Three small drifts between that run and yesterday's, worth pinning down rather than leaving:

- kernel **6.12.100** yesterday against 6.12.95 in August;
- Maven **3.9.9** against 3.9.16;
- memory reported as **32 GiB** yesterday against **30 GiB** in August. The paper says 30, so we have
  assumed August is right — please confirm which, since it goes in a table.

None of the first two should matter. If the same AMI and Maven are easy to reach, use them; if not,
just tell us which you used and we will record it.

Please send back `full-suite.json`, plus the output of `lscpu`, `free -h`, `uname -r`, `java -version`
and `mvn -v`, so the environment table can be written from fact rather than from memory.

---

## Request 5 — the small-N crossover

Short, perhaps half an hour, and the last gap.

```
java -jar target/benchmarks.jar "StringSortBenchmarks.(insertionSort|systemSort|quickHuskySort|radixHuskySort16)$" -p corpus=english -p n=4,10,20,50,100,200,500,1000,2000,10000 -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-crossover.json
```

The paper's use-case guidance identifies, size by size, which sorter to reach for below ten thousand
elements — where the system sort wins, where plain insertion sort wins outright, and where
QuickHuskySort takes over. Those crossovers were measured on the M1 only, and the paper says so. They
are the one set of figures request 4 will not produce, because the suite's own parameters start at
32,000.

Expect very small absolute numbers at the low end. That is fine: the crossover points are what matter.

---

## A note on the Chinese corpora

`msdStringSort` cannot run on them at all — its alphabet holds 256 characters beyond ASCII and the
Chinese corpora hold 3,813 and 2,270 — and the two string baselines we repaired are not used for
pinyin ordering. So requests 1, 2 and 5 are English-only and take `-p corpus=english`.

**Request 4 is the exception**: it runs every corpus, because Table `RadixImprovements` has Chinese
rows and those figures now need to come from your machine too. It selects corpora itself; pass no
flags.

---

# Appendix — requests 1 and 2, answered 2026-09-01

Kept for the record. **Nothing here needs running.**

## What prompted them

Two things had changed in the string-sort comparison and both needed measuring on hardware we trust:

1. **MSD radix sort was added as a second baseline**, after three correctness defects were fixed that
   had prevented it being benchmarked at all. On our workstation it was *beating* RadixHuskySort at the
   larger sizes.
2. **Both baselines' small-range fallbacks were handicapped** in the same way and both were repaired.
   Multikey quicksort's fallback alone cost it 15–18%, so the paper's published margin over multikey
   was partly measuring our own overhead.

Our own measurements came from a shared MacBook that spent the day fighting Spotlight, an IT
monitoring agent and, at one point, a closed lid: intervals from 1% to 23%, and one benchmark
returning 232, 259 and 330 ms on three occasions.

## What they settled

- **MSD does beat RadixHuskySort at 200,000 and 1,000,000**, with non-overlapping intervals, on a
  second architecture. But the margin's shape is machine-dependent: 1.34x at 200,000 and 1.09x at
  1,000,000 on Graviton3, against roughly 1.10x and 1.38x on the M1.
- **The tie at 32,000 is real** and not an M1 cache artefact — it reproduces on a server-class ARM core
  with an entirely different cache hierarchy.
- **The margin over the repaired multikey is 1.66x / 1.39x / 2.26x**, so the paper's stated 1.3–1.75x
  breaks at the top end on Graviton3 — the opposite end from the M1, which broke it at the bottom.
- **Duplicate density does not explain MSD's advantage.** Drawing without replacement leaves the
  200,000 margin at 1.31x against 1.34x with replacement.

## Why request 2 could not simply use a larger array

Past 1,000,000 the English corpus stops being able to supply distinct words — it holds 275,333 — and
the benchmark becomes a test of duplicate handling. Average copies of each word run 1.06 at 32,000,
1.41 at 200,000 and 3.73 at 1,000,000. The codebase already makes this objection about the
`commonwords` corpus, whose ~3,000 words sampled into a 200,000-element array it calls "artificial
duplicate-heavy skew"; at 1,000,000 the English corpus approaches the same problem from the same
direction. Drawing without replacement bounds n by the corpus size, which is why request 2 stopped at
250,000.
