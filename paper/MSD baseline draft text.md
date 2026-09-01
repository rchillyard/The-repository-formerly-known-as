# Draft reframing for §sec:radix — the boundary-condition version

Replaces the earlier drafts in this file, which were written before we knew the shape of the MSD
result and assumed it would go the other way. Edit 5 (the appendix paragraph on unbounded recursion)
is already applied to `HuskySort.tex`; nothing below is.

Placeholders are `<<...>>` and there are only two, both awaiting Yunlu's run. **The argument itself
needs no new data** — it rests on Table `RadixImprovements`, which is already in the paper.

## The observation this is built on

Table `RadixImprovements` already separates cleanly, and the paper does not currently say so:

| rows | advantage over QuickHuskySort |
| --- | --- |
| English words, Chinese words, Chinese names | 1.6x, 1.3x, 1.6x |
| Integer, Double, Long, BigInteger, BigDecimal, Tuples, Dates | 2.9x, 3.3x, 2.9x, 3.1x, 3.7x, 2.6x, 4.5x |

Every non-string row beats every string row, and the ranges do not overlap. Strings are RadixHuskySort's
**weakest** domain by the paper's own headline measurement. Saying so, and explaining why, is stronger
than leaving a reader to notice it — and it is what makes the MSD result a boundary rather than a
refutation.

## Replacement text

Replacing the passage from "RadixHuskySort's contribution is different in kind" through
"...remains future work."

```latex
RadixHuskySort's contribution is different in kind, not degree:
it is not a string-sorting algorithm, but a general mechanism applicable to any \textit{Comparable} type
with a 64-bit husky encoding (strings among them),
at the cost of the encoding's own fixed capture window (\S~\ref{sec:pcrit} above)
and a fallback cleanup pass whenever that encoding is imperfect.

That framing carries a consequence which Table ~\ref{tab:RadixImprovements} makes plain,
and which we state rather than leave to be noticed:
strings are the domain in which this mechanism is \emph{least} advantageous.
Every non-string row of that table (2.6--4.5x) exceeds every string row (1.3--1.6x),
and the two ranges do not overlap.
Three factors explain the ordering, and they compose.
The advantage grows with the cost of the type's native comparison, since that is what the encoding replaces;
it grows with the exactness of the encoding, since an imperfect one is paid for by the cleanup pass;
and it shrinks in the presence of algorithms specialised to the type.
Dates are the favourable extreme on all three counts — a provably perfect coding, no cleanup pass at all,
and no specialised competitor — and yield the largest margin in the table.
Chinese personal names are the instructive middle: the native comparison is expensive,
requiring a table lookup per character per comparison, which is exactly the cost husky encoding is meant to amortise,
but names of two or three characters with recurring syllables collide often enough that the cleanup pass
consumes what the encoding saves.

Strings are unfavourable on the third count in a way no other type in this paper is.
Half a century of specialised string sorting exists,
and a general mechanism should not be expected to beat it on its own ground.
We compared RadixHuskySort directly against three-way radix quicksort
(JMH, English and Chinese text, natural Unicode order so both algorithms sort under the same task):
RadixHuskySort is faster at every size tested, by <<a--b>>x on English and <<c--d>>x on Chinese,
non-overlapping 99.9\% confidence intervals throughout.
Against MSD radix sort the result is the other way at scale:
on English text MSD is faster than RadixHuskySort by <<e>>x at $N=200{,}000$ and <<f>>x at $N=1{,}000{,}000$,
again with non-overlapping intervals.
We report this because it locates the boundary rather than obscuring it.
MSD earns that result on a corpus that suits it and within limits that are its own:
our implementation indexes an alphabet of 256 characters beyond ASCII,
which English text fits and neither Chinese corpus does,
and it offers no pinyin ordering,
so there is no MSD row for either Chinese corpus at all.
RadixHuskySort reaches every corpus in this paper through one encoding,
with no per-alphabet provision and no per-type implementation.
That is the claim being made, and the English result bounds it without contradicting it.
A direct empirical comparison against burstsort remains future work;
it is a trie-based, cache-conscious algorithm designed to beat both baselines used here on exactly this workload,
and we have not implemented it.
```

## Two smaller consequential edits

**Line 501's "competitive baseline".** The clause "a real result against a real, competitive baseline,
not merely a theoretical argument" is dropped in the replacement above. With both baselines measured,
the paper's own numbers show three-way radix quicksort is the weaker of the two, and describing it as
*the* competitive one invites the objection that the weaker baseline was framed as strong.

**§sec:usecase.** The use-case guidance should inherit the same three factors, since it is the section
a practitioner reads. The one-line version: *reach for husky encoding when the ordering is composite or
the comparison expensive, when the key packs exactly into 64 bits, and when no sort specialised to your
type already exists.* Dates satisfy all three; English words satisfy none.

## What still needs Yunlu

Only the six numbers in the two placeholders. Both come from the first request in
[Run request for Yunlu.md](Run%20request%20for%20Yunlu.md). If the distinct-words run (second request)
holds up, the MSD figures should be quoted from the with-replacement rows for comparability with the
multikey figures, and the distinct-words result mentioned as confirming rather than as a separate claim.
