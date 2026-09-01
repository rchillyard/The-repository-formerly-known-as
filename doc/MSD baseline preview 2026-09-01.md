# MSD string sort as a baseline — preview run, 2026-09-01

**These numbers are not publication-grade and must not go into the paper as they stand.** They were
taken on a shared workstation that was building and testing at the time. Relative confidence-interval
widths run from 10% to 55%, against under 2% for the dedicated-hardware run in
[JMH Benchmark Results 2026-08-17.md](JMH%20Benchmark%20Results%202026-08-17.md). The purpose here is
to answer whether adding MSD to the paper is worth doing, not to answer by how much.

## Why it was added

The paper's literature section names three classic string sorts — three-way radix quicksort, MSD
radix sort, and burstsort — and, until now, compared empirically against only the first, saying that
"a direct empirical comparison against MSD radix sort and burstsort specifically remains future
work". `MSDStringSort` was already in the repository and already benchmarked in the legacy harness,
so the MSD half of that sentence was cheap to close. `msdStringSort` is now a `@Benchmark` in
`StringSortBenchmarks`.

**English only.** `MSDStringSort`'s `Alphabet` has room for 256 distinct characters beyond ASCII, and
the Chinese corpora contain 3,813 and 2,270 of them. It is an extended-ASCII MSD, not a Unicode one.
The benchmark throws for any other corpus rather than returning a misleading number. Run it with
`-p corpus=english`.

## The run

English corpus, `-p corpus=english`, 2 forks × 5 measurement iterations, average time in ms/op,
99.9% confidence intervals.

| n | MSD | multikey QS | QuickHuskySort | RadixHuskySort/11 | System sort |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 32,000 | **3.220 ± 0.625** | 6.317 ± 1.360 | 5.925 ± 0.811 | 3.747 ± 0.857 | 11.308 ± 4.598 |
| 200,000 | 44.141 ± 8.237 | 54.814 ± 4.330 | 61.316 ± 7.902 | **41.387 ± 5.316** | 85.385 ± 4.315 |
| 1,000,000 | 316.728 ± 175.509 | 303.751 ± 55.626 | 295.877 ± 20.077 | **219.212 ± 40.420** | 540.871 ± 62.338 |

## What it suggests

**The existing multikey result reproduces.** RadixHuskySort/11 over multikey quicksort comes out at
1.69× / 1.32× / 1.39×, against the paper's stated 1.3–1.75× on English. Nothing to revisit there.

**MSD is a considerably stronger baseline than multikey quicksort, and the paper's margin against it
is not the same story.** RadixHuskySort/11 over MSD is 0.86× / 1.07× / 1.44× — that is, **MSD is
faster at 32,000**, the two are within noise of each other at 200,000, and RadixHuskySort is ahead
only at 1,000,000, where MSD's interval is ±55% and settles nothing.

That is a real finding and an awkward one, which is the honest reason to want it measured rather than
left as future work: a reviewer who runs MSD themselves will find what is above. Better to state it,
with the caveat that MSD here handles only extended ASCII while RadixHuskySort handles arbitrary
`Comparable` types with a husky encoding — which is the paper's actual claim of contribution, and is
untouched by any of this.

## Before any of it is quoted

Re-run on the dedicated hardware, alongside the rest of the suite, as
[JMH Benchmark Results 2026-08-17.md](JMH%20Benchmark%20Results%202026-08-17.md) was. In particular
the 1,000,000 MSD row is presently worthless, and the 32,000 comparison — the one that goes against
the paper — needs intervals tight enough to say whether MSD really is ahead there.

## Two defects fixed to make this possible

Both in `MSDStringSort`, both found by running it rather than reading it, and neither reaching any
published figure.

1. **It could not sort any array containing as many equal strings as its cutoff.** An exhausted
   string reports character 0, so equal strings all fall into the same bucket at every greater
   depth; recursing into that bucket never shrank the range, and `d` climbed until the stack ran
   out. Exactly 14 equal strings sorted and 15 threw `StackOverflowError`, 15 being the cutoff.
   This is why the 1,000,000 row failed at first: the harness samples with replacement, so some word
   occurs 15 or more times. `UnicodeMSDStringSort` already had the guard that was missing here.
   `MSDStringSortDuplicatesTest` covers it — every test in `MSDStringSortTest` is commented out,
   which is how it survived.
2. **The alphabet's capacity check was off by two**, so overflowing it raised
   `ArrayIndexOutOfBoundsException` rather than the `SortException` the call site catches and
   reports. That is what the Chinese corpora hit.

Separately, and before this run, `MSDStringSort`'s below-cutoff comparison was changed from
`v.substring(d).compareTo(w.substring(d))` to an in-place character comparison — about 1.3× on this
corpus. Had that not been fixed first, every number above would have been measuring the allocator.
See [Audit against INFO6205.md](../docs/Audit%20against%20INFO6205.md).
