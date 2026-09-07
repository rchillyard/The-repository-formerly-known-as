# Every edit this week's work implies — complete list, 2026-09-02, audited 2026-09-05

Supersedes `MSD baseline draft text.md`. This is intended to be exhaustive: it lists the claims that
must change, the claims that must be added, the claims that should be softened, the optional
additions, **and the claims that were checked and found unaffected** — that last section exists so
nothing gets re-litigated later.

## Status as of 2026-09-05, checked line by line against `HuskySort.tex`

**Applied**: §2's baseline disclosure (tex 552–554); §3's reframing, which absorbed 1.2, 1.3 and 1.4
(tex 524–577); §1.1's replacement in the conclusion (tex 1477–1481); §5.3's crossovers and the 166 µs
floor (tex 1406–1428); §5.4's environment table (tex 851–864); §7's table rebuild — five results
tables now come from `doc/full-suite.json` (tex 1261–1281, 1332, and §sec:analysis's three benchmark
tables). Also new since this document was written and **not recorded anywhere below until now**:
Table `Guidance` (tex 1376–1404), the two adversarial tables (tex 1551, 1594), and the appendix's
paragraph on unbounded recursion in the baselines (tex 1633–1651).

**Still pending, in order of how much they cost if missed:**

| | what | where |
| --- | --- | --- |
| **8** | the DPQS guard (HS-13) falsifies the appendix's crash result and one sentence of its prose | tex 1562–1563, 1571–1573, 1648–1649 |
| **5.2** | **the abstract and Table `Guidance` both cite a measurement the body does not contain** | tex 251–254, 1392 |
| **1.5** | "every non-string row exceeds every string row" — false against the rebuilt table | tex 533–534 |
| **4.1** | "HuskySort is always faster than dual-pivot quicksort" | tex 1474 |
| **5.1** | the permits row is in the table; the case study is nowhere in the prose | tex 1277 |
| **7** | three tables are still from the 2017 Intel/Java 8 machine | tex 1082, 1111, 1234 |
| **0b** | whether to cite arXiv:2012.00866 — needs the panel's answer on anonymity | front matter |
| **0a** | template and author footnotes — needs your decision | tex 43–58 |

Sections marked DONE need no further action.

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

# 0b. The arXiv preprint and the prior rejection — check before submitting

The paper is **arXiv:2012.00866**, and that same version was **SIAM ACDA21 Submission 60, rejected**
(reviews verbatim in `paper/SIAM_ACDA21_Reviews.md`). So the preprint and the prior submission are the
same document, and this is a resubmission to the same conference series.

**The preprint is almost certainly not an obstacle.** arXiv is not a publication venue: no peer review,
no imprimatur, and SIAM's policies permit preprints. "Previously published" normally answers *no*.

**Two things to check in the call for papers, neither of which can be settled from here:**

1. **Is review anonymous?** A preprint under the same title with the same three authors defeats
   anonymity. Venues vary from "fine" to "disqualifying".
2. **How the submission form words it.** Some ask about preprints specifically rather than about
   publication, and some ask whether the work has been submitted to that venue before.

**The disclosure that matters more is the prior rejection**, and it has an unusually good answer.
ACDA21's Reviewer 3 asked *why not use radix sort*. RadixHuskySort is that answer and is now the
paper's strongest result. Reviewer 1 called the literature review "somewhat limited"; the paper now
compares empirically against three-way radix quicksort and MSD radix sort, and concedes where MSD wins.
If ACDA permits a cover letter or a response to previous reviewers, this should be made explicitly
rather than left to be noticed.

**One thing that is a real defect either way.** `HuskySort.tex` does not cite the preprint. `arXiv`
appears in `README.md` and two documents under `doc/`, and nowhere in the paper. A reader who finds
arXiv:2012.00866 sees the same title and authors attached to a 2020 paper with no radix sort and
different figures, with nothing connecting the two. Either cite it as the earlier version, or post an
updated v2 once the submission is settled. Doing neither invites exactly the wrong inference.

Not acted on: whether to cite the preprint depends on the anonymity answer above, which decides whether
a self-citation is required disclosure or a breach.

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
| `\setcopyright{none}`, `\copyrightyear{2027}`, `\acmYear{2027}` | it asserted ACM copyright, in 2020. Both years were set to 2026 when this table was written and corrected to 2027 in the tex afterwards, ACDA 27 being the venue |
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

# 0. The abstract — APPLIED verbatim, tex 229–259. Abstract due 2026-09-08

The proposed replacement below was adopted without edit. **Two of its claims are not yet supported by
the body of the paper**, and both are tracked above rather than here: the cleanup-pass measurement
(5.2, and this is the serious one) and the permits case study (5.1). The abstract is the strongest part
of the revision and it is currently writing cheques §sec:pcrit and §sec:radix-results have not cashed.

The "if request 6 comes out as expected" clause at the end of this section has **not** been added, and
should not be until the numbers arrive.

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

## 1.1 The Conclusion: "especially fast for Unicode character strings" — DONE

Applied; the replacement below is at tex 1477–1481. The original read:

> It is especially fast for Unicode character strings.

**This was the opposite of what the paper's own data says, and it was in the conclusion.** MSD beats
RadixHuskySort on English at both large sizes on both machines, and the pinyin row is the smallest
margin in the table.

One caveat carried over from the draft below, now that the tables have been rebuilt: the replacement's
second sentence says strings "yield this mechanism's narrowest margins", which is true of the *narrowest*
margin (pinyin, 1.5x) but not of the string rows as a group — Chinese words at 3.3x sits fourth of
eleven. Read it alongside 1.5, which faces the same problem in §3 and resolves it in terms of spread
rather than rank. If 1.5's replacement is adopted, this sentence should be brought into line with it.

The replacement as applied:

```latex
It is especially fast for types whose ordering is composite or expensive to evaluate
and whose keys encode exactly, where the encoding replaces the whole comparison and no cleanup pass is needed.
Strings are not that case:
they are the one domain with a mature specialised literature of its own (\S~\ref{sec:radix}),
and they yield this mechanism's narrowest margins.
```

## 1.5 Tex 532–534 — the framing collapses; rewrite needed. URGENT

The table around it has been rebuilt; **this sentence was not**, and it now contradicts the table two
pages away from it:

> strings are the domain in which this mechanism is \emph{least} advantageous.
> Every non-string row of that table (2.6--4.5x) exceeds every string row (1.3--1.6x),
> and the two ranges do not overlap.

Table `RadixImprovements` as it now stands in the tex (radix over QuickHuskySort, largest size of
each), recomputed from `doc/full-suite.json` keyed on class *and* every parameter:

| | row |
| ---: | --- |
| **5.94x** | Dates (20k) |
| 3.58x | long (500k) |
| 3.42x | integer (500k) |
| **3.26x** | **Chinese words (1M) — string** |
| 2.97x | double (500k) |
| 2.87x | bigDecimal (500k) |
| 2.80x | bigInteger (500k) |
| **2.65x** | **English words (1M) — string** |
| 2.49x | Tuple (500k) |
| 2.06x | Permits (198.9k) |
| **1.52x** | **Chinese names, pinyin (1M) — string** |

Strings 1.52–3.26, non-strings 2.06–5.94. **Both parenthetical ranges in the sentence are wrong and so
is the claim**: Chinese words, a string row, beats both Tuples and Permits. So is the weaker
replacement drafted earlier ("no string row is among its widest" — Chinese words ranks 4th of 11).

> **Correction to an earlier version of this section.** It listed Dates at 3.49x and concluded it had
> dropped to second behind long. That came from a JSON extraction keyed on method and size only, which
> collided thirty-two rows. Dates is **5.94x** and remains the largest margin in the paper by a wide
> margin. The consequence matters: the tex's own sentence at 539–540, "Dates are the favourable extreme
> on all three counts … and yield the largest margin in the table", is **true and should be kept**. An
> earlier note here said it had to go or move; that note was wrong and is withdrawn.

**The "strings are the weakest domain" framing still has to go**, because it is not true: Chinese
words is among the table's best results. What the data actually shows is better, and still supports
the paper's argument:

**The string rows are the table's most variable, and the three factors explain why.** They span from
3.26x down to 1.52x, nearly the whole of the table's range below Dates. Chinese words is favourable
— an expensive Unicode comparison and an encoding that captures enough of it. English words is
middling and, more to the point, is the one row with a specialised competitor that beats us. Chinese
names by pinyin is the worst result in the paper, because the comparison is expensive but the encoding
is poor: two or three characters with recurring syllables. That is the three factors varying *within*
one data type, which is a stronger demonstration than a separation between types would have been.

Suggested replacement for the passage from "That framing carries a consequence" to "consumes what the
encoding saves". **Note that this now keeps the Dates sentence rather than dropping it**, and reads it
as the endpoint of the same axis the string rows vary along:

```latex
That framing carries a consequence which Table ~\ref{tab:RadixImprovements} bears out,
and which we state rather than leave to be noticed:
the string rows of that table are its most variable by a wide margin.
They span from 3.26x down to 1.52x, nearly the whole of its range below dates,
where every other row falls between 2.06x and 3.58x.
Three factors account for the spread, and they compose.
The advantage grows with the cost of the type's native comparison, since that is what the encoding replaces;
it grows with the exactness of the encoding, since an imperfect one is paid for by the cleanup pass;
and it shrinks in the presence of algorithms specialised to the type.
Dates are the favourable extreme on all three counts --- a provably perfect coding, no cleanup pass at all,
and no specialised competitor --- and yield the largest margin in the table at 5.9x.
The string rows show the same three factors varying \emph{within} a single data type,
which is the more instructive demonstration.
Chinese words are favourable on the first two counts --- an expensive Unicode comparison,
captured well enough by the encoding --- and sit fourth of eleven, above every tuple and numeric row but two.
Chinese personal names are unfavourable on the second: the comparison is more expensive still,
requiring a table lookup per character, but names of two or three characters with recurring syllables
collide often enough that the cleanup pass consumes everything the encoding saves,
and this is the weakest result we report.
English words are unremarkable on the first two counts and distinctive on the third,
being the one case in this paper with a mature specialised literature ranged against it.
```

Two knock-on checks once this is applied:

- The tex's next paragraph opens "Strings are unfavourable on the third count in a way no other type in
  this paper is" (tex 546). That still holds and needs no change.
- §1.1's replacement in the conclusion says strings "yield this mechanism's narrowest margins". Bring it
  into line: narrowest *margin*, singular, not narrowest as a group.

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

# 3. The §sec:radix reframing — APPLIED, but two sentences of it are now stale

At tex 524–577. **Read 1.5 before treating this section as finished.** This block was written and
applied while Table `RadixImprovements` still held the pre-rebuild figures, in which the string rows
did sit below every non-string row. The rebuild moved Chinese words to 3.26x, above both Tuples and
Permits, so the third and fourth lines of the block below — "strings are the domain in which this
mechanism is *least* advantageous" and "Every non-string row … do not overlap" — are now false as
printed. 1.5 supplies the replacement.

Everything else in the block stands, including the Dates sentence, and including the whole of the
second and third paragraphs (the MSD result, the alphabet limitation, burstsort as future work), which
absorbed edits 1.2, 1.3 and 1.4.

Replaces the passage from "RadixHuskySort's contribution is different in kind" through
"...remains future work".

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

# 5. Additions — new evidence, not corrections. **No longer optional: the abstract already cites both.**

## 5.1 The permits case study (§sec:radix-results) — HALF APPLIED

The **row is in** Table `RadixImprovements` (tex 1277, 2.1x) and in Table `Guidance` (tex 1389). The
**prose is nowhere.** §sec:radix-results runs from 1251 to 1304 and never mentions permits; the reader
meets "Building permits (composite key)" in a table with no account of what the data is, where it came
from, or what the composite key consists of. The abstract meanwhile promises "two hundred thousand
municipal records ordered by a composite key" (tex 247–248). A row in a table does not discharge that.

Needed: a short paragraph in §sec:radix-results, and a line in §Data Source (tex 895, which carries no
`\label`) alongside the Leipzig corpora and the Chinese-names corpus, since permits is the paper's only
real-world dataset and every other favourable case is synthetic.

Figures for it. RadixHuskySort/16 over the system sort: 4.02x / 3.87x / 4.40x at 32,000 / 100,000 /
198,900. Over QuickHuskySort — what Table `RadixImprovements` reports — **2.06x** at 198,900, so it
enters that table tenth of eleven, below Tuples (2.49x). It is not the best number in the paper; it is
the best number on real data. Full results in
[Permit benchmark results 2026-09-01.md](../doc/Permit%20benchmark%20results%202026-09-01.md).

## 5.2 The cleanup pass, measured directly (§sec:pcrit) — **NOT APPLIED, AND TWICE CITED. URGENT**

**This is the most serious inconsistency in the paper as it stands.** The measurement is not in the
body anywhere — `grep` finds none of 9.8, 25.3 or 19.0 in `HuskySort.tex` — yet two places already
rely on it:

| where | what it says |
| --- | --- |
| tex 251–254, the **abstract** | "measured on input where that pass provably has nothing to correct, it nonetheless accounts for a tenth to a quarter of total running time" |
| tex 1392, Table `Guidance` | "that pass costs 10--25\% even when it has nothing to correct (\S~\ref{sec:radix-results})" |

The second is worse than a gap: it is a **dangling forward reference**. It sends the reader to
§sec:radix-results for a figure that section does not contain. A referee checking the abstract's most
distinctive claim will follow that pointer and find nothing.

Either the measurement goes into the body — §sec:pcrit (tex 659) is its natural home, since that is the
discussion it anchors — or both citations come out. It should go in: it is the strongest new result in
the revision and the only direct measurement of what an imperfect encoding costs.

The result. Two benchmarks compute identical codes and differ only in whether the coder declares itself
perfect, so the gap is the cost of a cleanup pass that has nothing to correct. On the machine of record:
**9.8% at 32,000, 25.3% at 100,000, 19.0% at 198,900**, non-overlapping intervals throughout.

**Do not write "grows with N".** That is what our own machine showed (5.9 / 11.9 / 17.4) and it did
not replicate: Yunlu's figures are larger at every size and peak in the middle. The defensible claim
is that the pass costs between a tenth and a quarter of the running time at every size past 32,000,
while provably having nothing to correct. Note that Table `Guidance`'s "10--25\%" is already the right
formulation and the abstract's "a tenth to a quarter" matches it. The $p_{crit}$ discussion has never
had this isolated, because every other benchmark varies the encoding and the sort together.

---

# 5.3 §sec:usecase — the crossovers move, and gain an explanation — DONE

Applied at tex 1406–1428, and the section was reorganised around the three factors rather than array
size, with Table `Guidance` (tex 1376–1404) put ahead of the crossover prose. Figure `usecase` was
redrawn in TikZ to match and gained an MSD band; see `Paper deletions.md`.

Request 5 supplied the small-N figures on the machine of record. They differed from the M1 ones the
paper gave (its line then said they were M1-only), so the guidance was restated:

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

# 5.4 §Implementation — the System Environment table, from fact — DONE, with one choice left

Table `SysEnvAWS` (tex 851–864) now reads 30 GiB and Corretto 21.0.12. **Kernel and swap were not
added**, and deliberately: the table has five rows (Instance / Processor / Memory / OS / JVM) and the
other two environment tables have the same shape, so adding kernel and zram to one of the three would
make them non-comparable. If a referee asks for kernel-level detail, the verbatim captures are in the
appendix of `doc/Run results from Yunlu 2026-09-02.md` and can be quoted there rather than tabulated.

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

# 6. Checked and found unaffected *by the baseline repairs* — do not re-open on that ground

These were checked against the MSD/multikey/cutoff work only. **Four of them were nevertheless
rewritten later, by §7's decision to consolidate onto one machine** — a different reason entirely.
Struck through below where that happened, so this list is not read as saying they still hold.

- **Line 514, the pinyin comparison (1.6–2.7x).** Verified against the diff of `7752569`: the pinyin
  fallback has always been `Arrays.sort` with `NAME_ORDER` and never used the allocating
  `InsertionSort`, which was reached only from the natural-order entry point. It gained an ignored
  parameter and nothing else. **Still stands.**
- **Table `HSComp`** — HuskySort against the system sort. Neither baseline changed. **Still stands on
  that ground, but the table is proposed for deletion for another** — it is the last results table from
  the 2017 Intel/Java 8 machine. See `Paper deletions.md`.
- ~~**Table `RadixImprovements`** — radix against QuickHuskySort. Neither changed.~~ **Rebuilt** from
  `doc/full-suite.json` under §7; now at tex 1261–1281. See 1.5.
- ~~**Dates 4.5x**, and Chinese names as the smallest margin.~~ Dates is **5.9x**; Chinese names at
  1.5x is still the smallest margin, so half of this survives.
- ~~**The AWS confirmation (2.51x / 2.85x)**~~ — superseded. Those were the 2026-08-17 run; the paper
  now quotes 2.7x / 3.3x from the full suite, and the paragraph itself was rewritten as the generality
  paragraph at tex 1300–1304.
- ~~**The parallel results** — `Long[]`, untouched by any string-sort work.~~ True of the sort work, but
  Table `ParallelRadix` (tex 1332) was rebuilt from the Graviton3 run under §7.
- **The configured-cutoff fix** touches nothing published: `cutoff` was empty in both config files, so
  both paths were already using the default. **Still stands.**

---

# 7. Consolidating onto one machine

**Decision taken 2026-09-02: Graviton3 becomes the primary machine. No results table quotes figures
from any other.** The other two environments stay in §Implementation as qualitative cross-checks only.

The reason is that the paper currently quotes three machines — and the oldest, which supplies its
original core data, is a 2017 Intel MacBook Pro running **Java 1.8.0_152**. Two JVM generations and a
different instruction set.
## The rebuild — DONE

Not from `doc/JMH Benchmark Results 2026-08-17.md`, as this section originally proposed, but from
`doc/full-suite.json` — Yunlu's run of 2026-09-01 at the branch tip, which supersedes it and which adds
the permits and adversarial classes the earlier run did not have. Every figure below was recomputed
from that JSON keyed on benchmark class **and every parameter**; keying on method and size alone
collides thirty-two rows and silently reports the wrong number, which is how an earlier draft of 1.5
came to list Dates at 3.49x.

Five tables now come from the Graviton3:

| paper table | tex | was |
| --- | ---: | --- |
| `HS_BM_N`, `HS_BM_S`, `HS_BM_T` (§sec:analysis) | 1156, 1175, 1195 | M1 |
| `RadixImprovements` | 1263 | M1 |
| `ParallelRadix` | 1332 | M1 |

Radix over QuickHuskySort, as Table `RadixImprovements` now reads (tex 1268–1278):

| type | N | now | before the rebuild |
| --- | ---: | ---: | ---: |
| Dates | 20,000 | **5.9x** | 4.5x |
| Long | 500,000 | 3.6x | 2.9x |
| Integer | 500,000 | 3.4x | 2.9x |
| Chinese words (natural order) | 1,000,000 | **3.3x** | 1.3x |
| Double | 500,000 | 3.0x | 3.3x |
| BigDecimal | 500,000 | 2.9x | 3.7x |
| BigInteger | 500,000 | 2.8x | 3.1x |
| English words | 1,000,000 | **2.7x** | 1.6x |
| Tuples | 500,000 | 2.5x | 2.6x |
| Building permits (composite key) | 198,900 | 2.1x | *new* |
| Chinese names (pinyin order) | 1,000,000 | **1.5x** | 1.6x |

The string rows improved most, which is what broke §3's framing. See 1.5.

## What is still on the old machine — the remaining half of this decision

Three tables in §sec:analysis were never rebuilt. Tex 806 and 1078 say so in as many words for one of
them; the other two are simply undated. All are from Table `SysEnvOriginal`, a 2017 quad-core Intel i7
running **Java 1.8.0_152**.

| table | tex | why it was not rebuilt |
| --- | ---: | --- |
| `HSComp` | 1080–1103 | cannot be rebuilt — thirteen sizes JMH does not run. Proposed for deletion; see `Paper deletions.md` |
| `TimvsInsertion` | 1109–1132 | same thirteen sizes, same machine, but it never says so. Worth keeping — it is the only evidence for choosing Timsort in step 3 — so it needs a sentence naming its machine rather than a rebuild |
| `Improvements Summary` | 1232–1249 | cannot be rebuilt — quoted over continuous size bands ("4,000—500,000 elements") that JMH's three sizes per type cannot reproduce, and it too is unattributed |

Table `Comparison` (tex 1059–1073) looks like a fourth but is not one: it counts array accesses from the
model of §sec:radix and never touched a benchmark.

**This is the gap between the decision as stated and the paper as it stands.** The opening claim of
this section — "no results table quotes figures from any other" machine — is not yet true, and a
referee reading tex 806 will see it stated plainly. Either finish the consolidation or narrow the
claim to the radix results, which is where it actually holds.

## Two wrinkles, both still live

**The sizes do not line up.** `HSComp` is quoted over "4,000–500,000 elements" and `Improvements
Summary` over similar bands; JMH uses 32,000/200,000/1,000,000 for strings and 20,000/100,000/500,000
for numerics. Those two tables must be re-cast at JMH's sizes or dropped; the current ranges cannot be
reproduced.

**The pinyin row is awkward.** At N=1,000,000 on Graviton3, the natural-order system sort beats every
pinyin-correct variant (851.3 against 955.5 for RadixHuskySort/16 — 0.89x). Request 6 exists precisely
to settle whether that comparison means anything, since the system sort there does no pinyin lookup at
all. Do not restate it in §sec:summary until request 6 comes back.

## The generality paragraph — DONE

Applied at tex 1300–1304, and tex 815–819 keeps the three-machine cross-check qualitatively. (The
draft that stood here had its `\ref` commands broken across lines by a stray carriage return; it has
been replaced by the applied text rather than repaired.)

```latex
The measurements were reproduced qualitatively on the two machines of Tables ~\ref{tab:SysEnvOriginal}
and ~\ref{tab:SysEnvCurrent}, which differ from the machine of record and from each other in vendor,
instruction set, core design and JVM generation.
We quote no figures from them, since mixing environments within a comparison would rob it of meaning,
but every ordering reported here holds on all three.
```

## §sec:usecase — DONE

Request 5 supplied the crossovers on the machine of record. See 5.3.

---

# 8. The dual-pivot guard (HS-13) — the appendix now overstates its own result

Committed as `e92610f` on 2026-09-04, after everything above was written. `PureDualPivotQuicksort` — a
2011 JDK copy adapted to objects, and the "Raw quicksort" column of Table `AdversarialBits` — had **no
recursion depth bound at all**. It now has one: sixty-four levels, then a heapsort fallback, which is
what later JDKs added to the primitive original.

Verified before and after on identical data and an identical 1 MB stack, 3,024 trials: **two
StackOverflowErrors at fixedHighBits 60 and 63 without the guard, zero with it.** Full suite 396 tests
green.

## What this falsifies in the paper

| tex | what it says | what it becomes |
| ---: | --- | --- |
| 1562–1563 | Raw quicksort *crashed* at 60 and 63 fixed bits | two timings, once request 7 returns |
| 1571–1573 | "degrades by more than an order of magnitude and then fails outright with a stack overflow" | it degrades by more than an order of magnitude, full stop — which is the honest result and still makes the point |
| 1648–1649 | "All three of the comparison baselines carry an unbounded-recursion failure mode: we repaired two of them, and the third is reported above as a result in its own right" | **flatly false now.** All three are repaired |

## Why this is an improvement rather than a loss

The 21x degradation at 56 fixed bits is a property of two-way partitioning around a heavily duplicated
pivot. It is unaffected by the guard and is the result the appendix actually needs. What the crash added
was not evidence but a straw man: a baseline that failed where the algorithm it copies would merely slow
down. Removing it makes the surviving claim stronger, not weaker.

Two disclosures belong with it, neither about the guard:

- **The JDK never applies dual-pivot quicksort to objects.** `Arrays.sort(Object[])` is
  `ComparableTimSort`; `DualPivotQuicksort` is primitives-only. Our baseline is that algorithm hand-
  adapted, and a reader meeting "dual-pivot quicksort" in the tables should not take it for the system
  sort. Tex 1222 already says dual-pivot is "the Java system sort for primitives", which is correct and
  makes the distinction available; the appendix should draw on it.
- **The guard makes this baseline an introsort**, in the classic sense. Worth one clause, since tex 391
  and 439 already discuss Introsort as the mitigation for exactly this failure mode.

## Blocked on

Request 7, written up in `doc/Run request for Yunlu.md` and pinned at `e92610f`. It tells Yunlu that
rows 0–56 should reproduce within noise and rows 60 and 63 should now carry timings, with a falsifier: if
the baseline comes back *fast* at 60 and 63, the guard is firing too early and needs re-tuning rather
than celebrating.

**Nothing in §8 can be applied until those numbers arrive.** If they do not arrive before the 15th, the
fallback is to delete the two crashed rows and the sentence at 1648–1649, and report the sweep as far as
56 fixed bits — which loses the two least informative columns and no argument.
