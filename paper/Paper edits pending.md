# Every edit this week's work implies — complete list, 2026-09-02

Supersedes `MSD baseline draft text.md`. This is intended to be exhaustive: it lists the claims that
must change, the claims that must be added, the claims that should be softened, the optional
additions, **and the claims that were checked and found unaffected** — that last section exists so
nothing gets re-litigated later.

**Applied so far**: the appendix paragraph on unbounded recursion in the baselines; the whole of §3,
the §sec:radix reframing, which absorbed edits 1.2, 1.3 and 1.4; and §2's disclosure, now trimmed to
two lines. Sections marked DONE need no further action.

Still pending: **1.1** (the conclusion's "especially fast for Unicode character strings"), **1.5**
(a claim already in the tex that the table rewrite will falsify — read this one), **4.1** (softening
"always"), **5** (the two optional additions) and **7** (consolidating onto Graviton3).

## The measurements everything rests on

Two machines, both quiet, `sampling=withreplacement`, English corpus. M1 is Table `SysEnvCurrent`;
Graviton3 is Yunlu's run of 2026-09-01 at commit `a83e7ea` (PR #63), 5 forks × 10 iterations.

**radix/16 ÷ MSD** — above 1 means MSD is faster:

| n | M1 | Graviton3 |
| ---: | ---: | ---: |
| 32,000 | 1.02x (tied, overlapping) | 1.04x (tied, overlapping) |
| 200,000 | 1.10x | **1.34x** |
| 1,000,000 | **1.38x** | 1.09x |

**multikey ÷ radix/16** — the paper claims 1.3–1.75x:

| n | M1 | Graviton3 |
| ---: | ---: | ---: |
| 32,000 | 2.28x | 1.66x |
| 200,000 | 1.65x | 1.39x |
| 1,000,000 | 1.46x | 2.26x |

Note the shapes disagree between machines in both tables, and in opposite directions.

**Under the decision of §7, only the Graviton3 column is quotable.** The M1 column is kept here
because it is what tells us the margin is machine-dependent — a fact the paper should state
qualitatively — but no figure from it goes into the paper.

---

# 0a. The template — needs your decision, not mine

The paper is `\documentclass[acmtog]{acmart}` — ACM's class, in its Transactions on Graphics
variant — and is going to **SIAM ACDA**. I have suppressed everything that asserted an ACM identity
(see below), so the PDF no longer claims to be anything it isn't. What I have *not* done is change the
template, because that is a decision with consequences I cannot weigh:

- **SIAM normally requires `siamart`.** That class is not installed here (`kpsewhich` finds neither
  `siamart220329.cls` nor `siamart190516.cls`). Check ACDA's call for papers: some SIAM venues accept
  any reasonable format for review and only require `siamart` on acceptance.
- **A template change will change the page count.** The paper is currently 15 pages in acmart's
  two-column journal layout. `siamart` and acmart's own `manuscript` option are both single-column and
  would run considerably longer. If ACDA has a page limit, that is the thing to check first — before
  the 15th, not on it.
- Retaining acmart while suppressing its identity is defensible for a review copy and indefensible for
  a camera-ready one.

## What was changed, and what it was hiding

Page one previously read *"ACM Trans. Graph. 37, 4, Article 111 (August 2020)"* with the template's
placeholder DOI `10.1145/1122445.1122456`, a 2020 ACM copyright block, and `111:2` in the running
header of every page. All of it was unedited `sample-acmsmall` boilerplate.

| change | why |
| --- | --- |
| `\setcopyright{none}`, `\copyrightyear{2026}`, `\acmYear{2026}` | it asserted ACM copyright, in 2020 |
| `\acmDOI{}` | the DOI was the template's placeholder and resolved to nothing of ours |
| `\settopmatter{printacmref=false}` | removed the "ACM Trans. Graph. …" reference block |
| `\acmVolume{} \acmNumber{} \acmArticle{}` | 37 / 4 / 111 were invented; 111 was in every header |
| `\acmJournal{TOG}` **kept** | acmart refuses to build without a valid journal code; it no longer prints |
| `\renewcommand\footnotetextcopyrightpermission[1]{}` | the first page carried the journal line separately |
| `\pagestyle{plain}` and `\thispagestyle{plain}` after `\maketitle` | the journal footer printed "Vol. , No. , Article ." with the fields blanked, which looks broken rather than suppressed; `\maketitle` sets page one's style itself, hence both |

Verified: zero occurrences of any ACM identity anywhere in the built PDF, still 15 pages, and the four
remaining instances of "2020" are genuine citations.

## Two more things in the front matter that I have left alone

- **`\setcopyright{none}` is right for review and wrong for camera-ready.** Whatever ACDA requires,
  set it then.
- **The author footnotes describe the 2020 division of labour.** They credit Yunlu with "early
  benchmarking" and Sai Vineeth with having "wrote the paper with input from all authors". Yunlu has
  since produced every figure in the paper on his own hardware, across six benchmark runs and some
  twenty-five hours of machine time, and this year's rewriting is not his. Whether that changes the
  footnotes or the author order is yours and theirs to settle, but it should be settled deliberately
  rather than by inheritance.

---

# 0. The abstract — URGENT, due 2026-09-08

All figures below are from `doc/full-suite.json`, keyed on class *and* every parameter: dropping either
collides 32 rows and silently reports the wrong number.

| case | system | QuickHuskySort | best radix | QHS ÷ sys | radix ÷ sys |
| --- | ---: | ---: | ---: | ---: | ---: |
| integer (500k) | 123.68 | 87.92 | 25.69 | 1.41x | 4.81x |
| long (500k) | 138.71 | 99.26 | 27.74 | 1.40x | 5.00x |
| double (500k) | 144.54 | 93.50 | 31.47 | 1.55x | 4.59x |
| bigInteger (500k) | 210.23 | 151.03 | 54.01 | 1.39x | 3.89x |
| bigDecimal (500k) | 264.08 | 149.11 | 51.91 | 1.77x | 5.09x |
| Tuple (500k) | 212.83 | 149.63 | 59.99 | 1.42x | 3.55x |
| Permits (198.9k) | 161.96 | 69.98 | 33.95 | 2.31x | 4.77x |
| English (1M) | 1193.78 | 732.32 | 276.66 | 1.63x | 4.31x |
| Chinese (1M) | 441.76 | 218.79 | 67.16 | 2.02x | 6.58x |
| Dates (20k) | 3.83 | 4.35 | 0.73 | **0.88x** | 5.23x |

**RadixHuskySort: 3.6--6.6x over the system sort**, every case. Stronger and tighter than the abstract's
current "2--6x".

**QuickHuskySort: 1.4--2.3x — except Dates, where it is 0.88x and loses.** The husky variant there is
DutchHuskySort, and the encoding is not repaid for a type as cheap to compare as a timestamp; radix
still wins because its second phase is linear regardless. The current abstract's "1.2--2.1x" has no
such exception and cannot stand. The draft below scopes the claim to types whose comparison is
expensive, which is true and is also the paper's actual criterion.

## Proposed replacement

```latex
\begin{abstract}
Most sorting algorithms in the literature optimize for the number of comparisons or exchanges;
we argue the more appropriate yardstick is the total number of array accesses (the "work"),
which for divide-and-conquer sorts splits into a linear, $\textbf{O}(N)$, phase and a linearithmic,
$\textbf{O}(N \log N)$, phase.
Moving work out of the linearithmic phase and into the linear phase reduces this total and,
where comparison itself is expensive, reduces processing time as well.
The key concept is a 64-bit code that is, as far as possible, order-preserving,
and stands in as a proxy for each original element;
we call it a "Husky" code.
Its effect is to extract each element's sort key once, rather than once per comparison.
We present two algorithms built on it.
QuickHuskySort encodes each object in a linear preamble,
sorts the cheap encoded keys in place of the expensive originals,
and falls back to a cleanup pass only where the encoding is imperfect.
RadixHuskySort replaces that sort with a linear-time radix sort on the same keys,
moving the second phase itself from linearithmic to linear.
Measured against Java's system sort for objects --- across integers, arbitrary-precision numbers,
dates, tuples, English and Chinese text,
and two hundred thousand municipal records ordered by a composite key ---
RadixHuskySort is faster in every case, by 3.6--6.6x;
QuickHuskySort, which retains a comparison sort, by 1.4--2.3x wherever comparison is genuinely costly.
The margin is widest when the ordering is expensive to evaluate and the encoding is exact,
so that no cleanup pass is needed:
measured on input where that pass provably has nothing to correct,
it nonetheless accounts for a tenth to a quarter of total running time.
It is narrowest where algorithms specialised to the type already exist ---
on English words a tuned MSD radix sort matches or overtakes us ---
which bounds the result rather than contradicting it,
since the same mechanism applies unchanged to types for which no specialised sort exists.
\end{abstract}
```

## What changed and why

- **"such as Strings" is gone.** Strings were the exemplar and are the *least* favourable domain: the
  three string rows of Table `RadixImprovements` span nearly its whole range, and MSD beats us on
  English. The criterion that replaces it --- expensive or composite ordering, exact encoding, no
  specialised competitor --- is what the evidence actually supports.
- **The MSD result is stated in the abstract, not buried.** A referee who knows string sorting will
  look for it; conceding it in one clause, immediately bounded, is far stronger than being caught.
- **The cleanup-pass measurement is new evidence and belongs here.** It is the only direct measurement
  of what an imperfect encoding costs, and it anchors $p_{crit}$.
- **"confirmed independently across three CPU architectures" is dropped**, since §7 consolidates every
  figure onto one machine. If you want the generality claim back, add: *"Figures are from a single
  machine; the qualitative result was reproduced on two others of different architecture."*
- **Two hundred thousand municipal records** replaces nothing --- it is new, and it is the paper's only
  favourable case on real rather than generated data.

## If request 6 comes out as expected

Chinese personal names ordered by pinyin is the most expensive comparison in the paper --- a table
lookup per character --- and against a pinyin-correct system sort it should be the mechanism's clearest
demonstration. If so, add to the list of types and append one clause:

```latex
The effect is starkest for Chinese personal names in pinyin order,
where extracting the key is costly enough that doing it once per element rather than once per comparison
is the whole of the difference.
```

Do not add this before the numbers arrive.

---

# 1. Must fix — claims the evidence no longer supports

## 1.1 Line 1356, in the Conclusion: "especially fast for Unicode character strings"

> It is especially fast for Unicode character strings.

**This is now the opposite of what the paper's own data says, and it is in the conclusion.** Table
`RadixImprovements` gives 1.3–1.6x for the three string rows against 2.6–4.5x for every non-string
row, with no overlap; and MSD beats RadixHuskySort on English at both large sizes on both machines.
Strings are the mechanism's *weakest* domain, not its strongest.

Suggested replacement:

```latex
It is especially fast for types whose ordering is composite or expensive to evaluate
and whose keys encode exactly, where the encoding replaces the whole comparison and no cleanup pass is needed.
Strings are not that case:
they are the one domain with a mature specialised literature of its own (\S~\ref{sec:radix}),
and they yield this mechanism's narrowest margins.
```

## 1.5 Line 503 — the framing collapses; rewrite needed. URGENT

> Every non-string row of that table (2.6--4.5x) exceeds every string row (1.3--1.6x),
> and the two ranges do not overlap.

Table `RadixImprovements` rebuilt from Yunlu's full suite (radix over QuickHuskySort, largest size of
each), computed from `doc/full-suite.json` rather than from a summary:

| | row |
| ---: | --- |
| 3.58x | long (500k) |
| 3.49x | Dates (20k) |
| 3.42x | integer (500k) |
| **3.26x** | **Chinese words (1M) — string** |
| 2.97x | double (500k) |
| 2.87x | bigDecimal (500k) |
| 2.80x | bigInteger (500k) |
| **2.65x** | **English words (1M) — string** |
| 2.49x | Tuple (500k) |
| 2.06x | Permits (198.9k) |
| **1.52x** | **Chinese names, pinyin (1M) — string** |

Strings 1.52–3.26, non-strings 2.06–3.58. The sentence is false, and so is the weaker replacement
drafted earlier ("no string row is among its widest" — Chinese words ranks 4th of 11).

**The whole "strings are the weakest domain" framing has to go**, because it is not true: Chinese
words is among the table's best results. What the data actually shows is better, and still supports
the paper's argument:

**The string rows are the table's most variable, and the three factors explain why.** They span almost
its entire range while every non-string row sits between 2.06x and 3.58x. Chinese words is favourable
— an expensive Unicode comparison and an encoding that captures enough of it. English words is
middling and, more to the point, is the one row with a specialised competitor that beats us. Chinese
names by pinyin is the worst result in the paper, because the comparison is expensive but the encoding
is poor: two or three characters with recurring syllables. That is the three factors varying *within*
one data type, which is a stronger demonstration than a separation between types would have been.

Suggested replacement for the passage from "That framing carries a consequence" to "consumes what the
encoding saves":

```latex
That framing carries a consequence which Table ~\ref{tab:RadixImprovements} bears out,
and which we state rather than leave to be noticed:
the string rows of that table are its most variable by a wide margin.
They span almost its entire range, from 3.26x down to 1.52x,
where every other row falls between 2.06x and 3.58x.
Three factors account for the spread, and they compose.
The advantage grows with the cost of the type's native comparison, since that is what the encoding replaces;
it grows with the exactness of the encoding, since an imperfect one is paid for by the cleanup pass;
and it shrinks in the presence of algorithms specialised to the type.
Chinese words are favourable on the first two counts --- an expensive Unicode comparison,
captured well enough by the encoding --- and sit near the top of the table.
Chinese personal names are unfavourable on the second: the comparison is more expensive still,
requiring a table lookup per character, but names of two or three characters with recurring syllables
collide often enough that the cleanup pass consumes everything the encoding saves,
and this is the weakest result we report.
English words are unremarkable on the first two counts and distinctive on the third,
being the one case in this paper with a mature specialised literature ranged against it.
```

The Dates sentence that followed ("the favourable extreme on all three counts") must also go or move:
Dates is now 3.49x, second in the table rather than first, behind long at 3.58x.

## 1.2 Line 500: the multikey range — DONE

> by 1.3--1.75x on English and 2--3.1x on Chinese

Measured against the repaired multikey on Graviton3: **1.66x / 1.39x / 2.26x** at 32,000 / 200,000 /
1,000,000. The stated range breaks at the top. (The M1 gave 2.28 / 1.65 / 1.46, breaking it at the
bottom instead — not quoted, but the reason for the "by machine" clause below.)

```latex
by 1.4--2.3x on English, non-overlapping 99.9\% confidence intervals throughout,
with the margin varying by array size rather than holding a single value.
```

The Chinese figure (2–3.1x) is untouched — see §4.1.

## 1.3 Line 501: "competitive baseline" — DONE

> a real result against a real, competitive baseline, not merely a theoretical argument.

With both baselines now measured, the paper's own numbers show three-way radix quicksort is the weaker
of the two. Describing it as *the* competitive one invites the obvious objection. Drop the word:

```latex
a real result against a real baseline, not merely a theoretical argument.
```

## 1.4 Line 502: MSD is no longer future work — DONE

> A direct empirical comparison against MSD radix sort and burstsort specifically remains future work.

MSD is measured. Burstsort stays deferred. See §3 for the replacement passage, which absorbs this
line.

---

# 2. Must add — the baseline disclosure — DONE

**Applied, and cut down to the property rather than the history.** The original draft here recounted
what had been wrong with the two fallbacks and what fixing each was worth. That is git's business, not
the paper's. What a reader actually needs is that the baselines are ours and are tuned, which is now
two lines inside §3:

```latex
Both baselines are our own implementations,
each tuned so that its fallback below the partitioning cutoff allocates nothing
and compares from the depth the partitioning has already established.
```

The alphabet limitation that the original draft also carried here is already stated later in the same
§3 passage, so it is not repeated.

---

# 3. The §sec:radix reframing — DONE

Replaces the passage from "RadixHuskySort's contribution is different in kind" through
"...remains future work". Rests on Table `RadixImprovements`, which already separates cleanly and
which the paper does not currently remark on.

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
Dates are the favourable extreme on all three counts --- a provably perfect coding, no cleanup pass at all,
and no specialised competitor --- and yield the largest margin in the table.
Chinese personal names are the instructive middle: the native comparison is expensive,
requiring a table lookup per character per comparison, which is exactly the cost husky encoding is meant to amortise,
but names of two or three characters with recurring syllables collide often enough that the cleanup pass
consumes what the encoding saves.

Strings are unfavourable on the third count in a way no other type in this paper is.
Half a century of specialised string sorting exists,
and a general mechanism should not be expected to beat it on its own ground.
We compared RadixHuskySort against both of the classic string sorts named above.
Against three-way radix quicksort RadixHuskySort is faster at every size,
by 1.4--2.3x on English and 2--3.1x on Chinese.
Against MSD radix sort the result goes the other way at scale:
on English text the two are statistically indistinguishable at $N=32{,}000$,
and MSD is faster by 1.34x at $N=200{,}000$ and by 1.09x at $N=1{,}000{,}000$,
with non-overlapping intervals in both cases.
We repeated this comparison on a second machine of different microarchitecture and obtained the same
qualitative result --- a tie at the smallest size and MSD ahead at both larger ones ---
but with the margin distributed differently across $N$,
so the magnitude should be read as machine-dependent even though the direction is not.
We give this result because it locates the boundary rather than obscuring it.
MSD earns it on a corpus that suits it and within limits that are its own:
our implementation indexes an alphabet of 256 characters beyond ASCII,
which English text fits and neither Chinese corpus does,
and it offers no pinyin ordering,
so there is no MSD row for either Chinese corpus.
RadixHuskySort reaches every corpus in this paper through one encoding,
with no per-alphabet provision and no per-type implementation.
That is the claim being made, and the English result bounds it without contradicting it.
A direct empirical comparison against burstsort remains future work;
it is a trie-based, cache-conscious algorithm designed to beat both baselines used here on exactly this workload,
and we have not implemented it.
```

---

# 4. Should soften — not wrong, but overstated

## 4.1 Line 1352: "always"

> HuskySort is always faster than dual-pivot quicksort.

Unqualified, in a conclusion that four lines later says the advantage "is specific to a particular
kind of workload, not universal", and in a paper whose §sec:usecase identifies two regimes where other
sorts win outright. There is also no measurement against dual-pivot at small $N$ — the crossover work
compares against System sort and insertion sort. Suggest "faster than dual-pivot quicksort at every
size we measured" or similar.

---

# 5. Optional additions — new evidence, not corrections

## 5.1 The permits case study (§sec:radix-results)

Real data, and the case the mechanism is best suited to. RadixHuskySort/16 over the system sort:
4.02x / 3.87x / 4.40x at 32,000 / 100,000 / 198,900. Over QuickHuskySort — which is what Table
`RadixImprovements` reports — **2.19x**, so it enters that table below Dates (4.5x) and beside Tuples
(2.6x). It is not the best number in the paper; it is the best number on real data, and every other
favourable case in the paper is synthetic. Full results in
[Permit benchmark results 2026-09-01.md](../doc/Permit%20benchmark%20results%202026-09-01.md).

## 5.2 The cleanup pass, measured directly (§sec:pcrit)

The strongest new result, and it needs no reframing of anything. Two benchmarks compute identical
codes and differ only in whether the coder declares itself perfect, so the gap is the cost of a
cleanup pass that has nothing to correct. On the machine of record: **9.8% at 32,000, 25.3% at
100,000, 19.0% at 198,900**, non-overlapping intervals throughout.

**Do not write "grows with N".** That is what our own machine showed (5.9 / 11.9 / 17.4) and it did
not replicate: Yunlu's figures are larger at every size and peak in the middle. The defensible claim
is that the pass costs between a tenth and a quarter of the running time at every size past 32,000,
while provably having nothing to correct. The $p_{crit}$ discussion has never had this
isolated, because every other benchmark varies the encoding and the sort together. Awaiting Yunlu's
confirmation (third request).

---

# 5.3 §sec:usecase — the crossovers move, and gain an explanation

Request 5 supplies the small-N figures on the machine of record. They differ from the M1 ones the
paper currently gives (line 1307 says they are M1-only), so the guidance needs restating:

| regime | paper says (M1) | Graviton3 |
| --- | --- | --- |
| system sort wins | at N=20 and N=50 | **N ≤ 20**, and thin at 20 (0.462 vs insertion's 0.489) |
| insertion sort wins outright | roughly N=100 to 200 | **N=50 to 200** |
| QuickHuskySort best | N=500 to ~2,000 | **N=500 to 2,000** — unchanged |
| RadixHuskySort takes over | not stated precisely | between **2,000 and 10,000** |

And one number worth having, which the paper currently gestures at rather than states. RadixHuskySort's
curve is a flat **~166 µs floor** from n=4 to n=500 — the allocation and setup of the two
65,536-entry counting structures — which only the O(n) growth begins to dominate past a few thousand
elements. The present text says merely that "RadixHuskySort's own fixed digit-pass setup cost is not
yet amortized below that point"; a measured floor is a much better answer, and explains the shape of
the whole low-N region.

# 5.4 §Implementation — the System Environment table, from fact

Yunlu answered every drift question and volunteered one we had not asked:

| item | value |
| --- | --- |
| Memory | **30 GiB** — `free -h` says `30Gi`. Our 30-vs-32 question is settled: the paper is right, and the 32 was the instance's nominal size. |
| Kernel | **6.12.100-125.179.amzn2023.aarch64** — the AMI moved on since August's 6.12.95 and it is not reversible on that host. Record it rather than reconcile it. |
| Maven | **3.9.16**, pinned back deliberately for these runs. |
| JDK | **Corretto 21.0.12.8.1 (21.0.12+8-LTS)** — byte-identical to August. The system JDK auto-patched itself to 21.0.12.1+9 overnight and he caught it and pinned back, which is why the runs are comparable at all. |
| Swap | **8 GiB zram** where August recorded 0 B. Compressed RAM rather than disk, and the heaps stayed resident, but the table should say what was true on the day. |

Verbatim `lscpu`, `free -h`, `swapon --show`, `uname -r`, `java -version` and `mvn -v` are in the
appendix of `doc/Run results from Yunlu 2026-09-02.md`, so the table can be written from those rather
than from memory.

# 6. Checked and found unaffected — do not re-open

- **Line 514, the pinyin comparison (1.6–2.7x).** Verified against the diff of `7752569`: the pinyin
  fallback has always been `Arrays.sort` with `NAME_ORDER` and never used the allocating
  `InsertionSort`, which was reached only from the natural-order entry point. It gained an ignored
  parameter and nothing else.
- **Table `HSComp` (lines 1164–1171)** — HuskySort against the system sort. Neither baseline changed.
- **Table `RadixImprovements` (lines 1193–1202)** — radix against QuickHuskySort. Neither changed.
- **Line 1218, Dates 4.5x**, and line 1216, Chinese names as the smallest margin — same table, same
  reasoning.
- **Line 1222, the AWS confirmation (2.51x / 2.85x)** — RadixHuskySort against QuickHuskySort.
- **Lines 1265–1277, the parallel results** — `Long[]`, untouched by any string-sort work.
- **The configured-cutoff fix** touches nothing published: `cutoff` was empty in both config files, so
  both paths were already using the default.

---

# 7. Consolidating onto one machine

**Decision taken 2026-09-02: Graviton3 becomes the primary machine. No results table quotes figures
from any other.** The other two environments stay in §Implementation as qualitative cross-checks only.

The reason is that the paper currently quotes three machines — and the oldest, which supplies its
original core data, is a 2017 Intel MacBook Pro running **Java 1.8.0_152**. Two JVM generations and a
different instruction set.

## No new runs are needed for the existing tables

Yunlu's full run of 2026-08-17 (PR #62, merged) covers every benchmark class on the Graviton3, and
already holds `systemSort`, `quickHuskySort` and every radix width for every type. Both results tables
can be rewritten from `doc/JMH Benchmark Results 2026-08-17.md` without asking for anything further.

| paper table | currently from | rewrite from |
| --- | --- | --- |
| `HSComp` (§sec:analysis) | Intel i7, **Java 8** | Graviton3 System vs QuickHuskySort |
| `RadixImprovements` | M1 | Graviton3 "vs QHS" columns |
| `ParallelRadix` | M1 | Graviton3 `ParallelRadixSortBenchmarks` |

### The figures to use

Radix over QuickHuskySort, replacing Table `RadixImprovements`:

| type | N | Graviton3 | currently says |
| --- | ---: | ---: | ---: |
| English words | 1,000,000 | **2.51x** | 1.6x |
| Chinese words | 1,000,000 | **2.85x** | 1.3x |
| Chinese names (pinyin) | 1,000,000 | **1.54x** | 1.6x |
| Integer | 500,000 | 3.28x | 2.9x |
| Double | 500,000 | 3.05x | 3.3x |
| Long | 500,000 | 3.74x | 2.9x |
| BigInteger | 500,000 | 2.81x | 3.1x |
| BigDecimal | 500,000 | 3.00x | 3.7x |
| Tuples | 500,000 | 2.41x | 2.6x |
| Dates | 20,000 | **~5.6x over DutchHuskySort, 4.9x over System sort** | 4.5x |

Most rows improve. Note the string rows improve *most*, which does not disturb §3's argument: that
argument rests on the gap between the string rows and the non-string rows, and at 2.51/2.85/1.54
against 2.41–3.74 the gap narrows but the pinyin row remains the smallest margin in the table.
**§3's wording must be checked against the rewritten table before it is used** — if the string rows no
longer sit below every non-string row, the sentence "every non-string row exceeds every string row"
becomes false and the argument needs restating in terms of the three factors alone.

## Two wrinkles

**The sizes do not line up.** `HSComp` is quoted over "4,000–500,000 elements" and
`RadixImprovements` at 500,000 and 1,000,000; JMH uses 32,000/200,000/1,000,000 for strings and
20,000/100,000/500,000 for numerics. The tables must be re-cast at JMH's sizes; the current ranges
cannot be reproduced.

**The pinyin row gets more awkward.** At N=1,000,000 on Graviton3, System sort beats every
pinyin-correct variant (851.3 against 955.5 for RadixHuskySort/16 — 0.89x). The paper already
discusses this, but it becomes more prominent when Graviton3 is the sole source, and §sec:summary's
framing should be checked against it.

## The generality paragraph, lines 735–745

Currently argues the finding is "checked across three machines that differ in vendor, architecture,
and JVM build". That defence should survive without quoting figures:

```latex
The results below were all measured on the machine of Table ~
ef{tab:SysEnvAWS},
so that every comparison in this paper is between figures taken under one configuration.
A natural question is whether the finding generalizes rather than being an artifact of that environment.
It does: the same qualitative result --- HuskySort, and radix sort, beating the system sort ---
was obtained independently on the two machines of Tables ~
ef{tab:SysEnvOriginal} and ~
ef{tab:SysEnvCurrent},
which differ from it and from each other in vendor, instruction set, core design and JVM generation,
one of them running a JVM two major releases older.
We quote no figures from those machines, since mixing environments within a comparison would make the
comparison meaningless, but the agreement across all three is what licenses the claim.
```

## §sec:usecase is the one gap — a possible fourth request

The crossover measurements (N=4 through 10,000) are M1-only, and line 1307 says so explicitly. They
are not in the 2026-08-17 run, whose `@Param` sizes start at 32,000. Under the rule adopted here they
are figures from another machine.

They are runnable — `StringSortBenchmarks` already has `insertionSort`, `systemSort` and
`quickHuskySort` — with explicit sizes:

```
java -jar target/benchmarks.jar "StringSortBenchmarks.(insertionSort|systemSort|quickHuskySort|radixHuskySort16)$" -p corpus=english -p n=4,10,20,50,100,200,500,1000,2000,10000 -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-crossover.json
```

Either ask Yunlu for that as a fourth request, or keep §sec:usecase's figures with their existing
caveat and state plainly that the crossover was measured on a different machine and not repeated.
Robin's call; the first is tidier and costs Yunlu perhaps half an hour.
