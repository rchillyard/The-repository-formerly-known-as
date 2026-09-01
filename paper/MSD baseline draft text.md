# Draft text for the MSD baseline — placeholders pending a clean run

Scratch file, meant to be consumed and deleted. Edit 5 (the appendix paragraph on unbounded
recursion) is already applied to `HuskySort.tex`; the four drafts below are not, because each needs
numbers from a dedicated-hardware run. See
[MSD baseline preview 2026-09-01.md](../doc/MSD%20baseline%20preview%202026-09-01.md) for the
workstation preview and why it must not be quoted.

Placeholders are written as `<<...>>`.

## What to run first

```
java -jar target/benchmarks.jar "StringSortBenchmarks.(msdStringSort|multikeyQuicksort|radixHuskySort11|systemSort)$" -p corpus=english -rf json -rff msd-clean.json
```

`msdStringSort` is English-only and throws for any other corpus, so `-p corpus=english` is required
rather than merely advisable.

The numbers needed are, for each of n = 32,000 / 200,000 / 1,000,000: the RadixHuskySort/11-over-MSD
ratio, and whether the 99.9% intervals overlap at each size. The 32,000 row is the one that decides
how paragraph 2 has to be written.

## 1. Replacing the "future work" sentence (line 502)

Currently:

```latex
A direct empirical comparison against MSD radix sort and burstsort specifically remains future work.
```

Becomes:

```latex
A direct empirical comparison against burstsort remains future work.
```

Burstsort stays deferred deliberately: it is a trie-based, cache-conscious algorithm designed to beat
both of the baselines used here on exactly this workload, and no implementation of it exists in our
repository, so deferring it is a statement about scope rather than a gap in the argument.

## 2. The MSD result

To follow line 501. **Two versions, because the shape of the claim depends on the 32,000 row.**

### 2a. If MSD is level with or ahead of RadixHuskySort at the smaller sizes

```latex
We also compared against MSD radix sort, the other classic string sort named above,
on the English corpus.
The result is more equivocal than the comparison with three-way radix quicksort,
and more informative for it:
MSD is the stronger of the two baselines at every size,
and at $N=<<n>>$ the two are separated by <<x>>\% or less,
with overlapping confidence intervals.
RadixHuskySort's advantage appears only at the largest size tested,
where it is faster by <<r>>x (<<CI statement>>).
We report this because it bounds the claim honestly:
against the best string-specialized baseline we implemented,
RadixHuskySort's advantage on English text is a large-$N$ effect rather than a uniform one.
```

### 2b. If the clean run puts RadixHuskySort ahead throughout

```latex
We also compared against MSD radix sort, the other classic string sort named above,
on the English corpus.
MSD is the stronger of the two baselines at every size tested,
and RadixHuskySort is faster than it by <<lo>>--<<hi>>x,
a narrower margin than the <<...>>x measured against three-way radix quicksort
and the more demanding of the two comparisons.
```

## 3. Softening line 501

Currently:

```latex
a real result against a real, competitive baseline, not merely a theoretical argument.
```

Becomes:

```latex
a real result against a real baseline, not merely a theoretical argument.
```

The word "competitive" has to go, or move to MSD, since with both baselines measured the paper's own
numbers show which of the two is the competitive one. Leaving it on three-way radix quicksort invites
the objection that the weaker baseline was the one framed as strong.

## 4. The two disclosures that must travel with the MSD number

Append to whichever of the paragraphs in §2 is used. Both are the same kind of disclosure the paper
already makes for its other baselines at lines 1421 and 1508.

```latex
Two qualifications attach to this baseline.
Its below-cutoff comparison originally allocated a fresh string per comparison;
we replaced that with an in-place comparison from the current depth,
worth roughly 1.3x on this corpus,
so that the measurement is of the algorithm rather than of the allocator.
And our MSD implementation indexes an alphabet of 256 characters beyond ASCII,
which the English corpus fits and neither Chinese corpus does
(<<3813>> and <<2270>> distinct such characters respectively),
so there is no MSD row for Chinese text.
That limitation is the baseline's rather than the comparison's,
but it is also the practical distinction being drawn:
RadixHuskySort reaches both corpora through the same encoding,
with no per-alphabet provision at all.
```

The `1.3x` figure is from the workstation and is stable enough to quote as "roughly" (see the preview
document), but the clean run is the place to confirm it. The two character counts are exact and need
no re-measurement.

## Sequencing note

§System Environment currently commits to three machines
(Tables `SysEnvOriginal`, `SysEnvCurrent`, `SysEnvAWS`).
If the MSD rows come from any of those three, nothing there changes.
If they come from a fourth, that section needs a sentence.
