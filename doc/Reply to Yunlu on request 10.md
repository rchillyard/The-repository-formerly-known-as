# Reply to Yunlu — request 10 (PR #66)

Draft, for posting as a comment on PR #66. Not yet sent.

---

Yunlu — thank you, this is the most useful run we have had. Three things in it were
worth more than the numbers we asked for.

**Our hypothesis was wrong and your data says so plainly.** We told you the
15-workers-against-8-threads asymmetry was "very likely most of why" request 9 showed
the parallel sort losing. It was not. Twelve chunks moved the husky row from 36.4 ms
to 26.5–27.3 and it is still 2× behind a 13.3 ms baseline, with the ordering holding
on every one of ten forks. Thank you for stating it as flatly as you did; a politer
version would have cost us another week.

**The 32,000 row is a serial result and you caught it.** `chunks = max(1, min(16,
32000 / 16384)) = 1`, so the only size at which we beat `Arrays.parallelSort` is not
a parallel sort at all. That is now a labelling change to the tables — see Q4.

**Two incidental catches we have adopted.** The JMH `find()` semantics, so a pattern
anchored only at the end also selects `ParallelStringSortBenchmarks`; we will anchor
on the class in future, and your six-row `parallelsort-pinyin.json` is a free
replication rather than an error. And the `jshell -q -s -` rejection — `jshell -s -`
noted.

## Your five questions

**1. Should the parallel claim hold at n ≤ 200k, or be confined to `Long[]` at ≥ 10M?**

Neither, in the end: we are backing off claims for parallel work generally, and the
paper has already been revised to say so. The reason is your own Amdahl figures.
With a parallelizable fraction *f* of 0.06 on English words and 0.07 on Chinese
names, the ceiling is `1/(1 − f)`, so **even with infinitely many cores the most
parallelism can ever buy is about 6–7%**. That is a structural bound rather than a
measurement at eight threads, and no implementation work reaches past it.

One distinction we are keeping, because a referee would otherwise push back: the
method is not "inherently serial". The digit passes parallelize perfectly well —
which is why the variant wins 1.19× on `Long[]` at ten million — and so, we now find,
do the encoding and the cleanup. What is true is narrower and less comfortable: at
the time of your run **we had parallelized one phase of four**, and not the one with
the time in it. That bounds our implementation rather than the mechanism, which is
why the paper now claims only the former. Since your run we have added the second —
see Q2.

On the permits specifically, we are **not** willing to call them serial-encode-bound
yet. `PermitCoder` is perfect, so there is no cleanup pass there at all, and the
parallel version still beats its own serial form by only ~10%. Something serial
dominates, but we have not isolated it, and we would rather say so than name it.

**2. Is parallelising `huskyEncode` in scope, or is the variant "radix passes only"?**

**In scope, and now done** — we had this wrong twice before landing on it, so the
working is worth showing.

Your 76–90% figure is encode ÷ *baseline* total. The ratio that decides whether to
parallelize the encode is encode ÷ *our own* total, and we first computed that as
**24% / 27% / 28%**, concluding it was not worth the engineering. That was measured
with `UNICODE_CODER` — the coder we had just stopped using for english. Under
`englishSaturatingCoder` the encode costs **72.9 ms against the Unicode coder's
24.3** at a million elements, because it reads ten characters rather than four. Set
against the roughly 69 ms that pre-ordering saves the baseline, a *sequential*
saturating encode does not fit at all.

Parallelized it does. Measured at a million elements on eight cores, eight chunks:
english under the saturating coder **72.9 → 21.0 ms (3.47×)**, under the Unicode
coder 24.3 → 9.9 (2.47×), Chinese names under pinyin **124.5 → 33.6 (3.70×)**. That
leaves some 48 ms for the digit passes and the permutation, where before there was
nothing. So parallelizing step 1 is not a marginal gain; it is what makes the
saturating coder affordable at all.

`ParallelRadixHuskySort` therefore parallelizes **two** phases of four as of
`15cc2ff`. The coding step became an overridable method on `AbstractHuskySort`, so
every other sorter keeps the sequential form your serial figures were measured with.

We were also wrong about the **cleanup**, in the opposite direction, and you should
have the correction. We had assumed it could not usefully be parallelized:
`Arrays.parallelSort` TimSorts leaf blocks and merges them non-adaptively, so on a
nearly-sorted array it does more work than serial TimSort, which finds one run and
stops. The work analysis is right; the conclusion was not, because we forgot to
divide by the core count. Measured on a million English strings, `parallelSort` beats
serial `Arrays.sort` by **1.75× on an already-sorted array** and by 2.8–3.8× at every
level of disorder tried — about four times the work over seven workers, finishing in
4/7 of the time, 0.571 predicted against 0.571 measured.

So the cleanup is well parallelizable too; we simply have not done it. Our parallel
result bounds our implementation rather than the approach, and the paper now says
exactly that and no more.

**3. Fifteen against sixteen.**

Your recommendation, and your reasoning, stand: keep `availableProcessors()`. It is
moot for 10a since sixteen chunks are never reached, effectively symmetric on strings
since fifteen pool workers plus the calling thread is sixteen, and below measurement
precision at ≤0.15%. No `pAll-1` row needed. Doubly moot given Q1.

**4. Label the 32,000 rows "1 chunk (serial)"?**

Yes — and of the five this is the one that matters most, for the reason you gave: it
is the only size we win, so a row labelled `p8` or `pAll` invites precisely the wrong
inference. Digit width and pass count per row as well, since the automatic width
varies with size.

**5. A fixed `p16` row, or the unrun `Auto_p8_chunk4k`?**

Neither. `p16` would sit on the ~26 ms serial floor, `pAll` already reaching twelve
chunks at 198,900 for a ~10% gain over serial. `chunk4k` would at least settle
whether all sixteen cores are ever used — at 4,096 the permits do reach sixteen
chunks — but it measured neutral-to-worse on eight cores and cannot close a 2× gap.
Given Q1, neither earns its run time.

## What has changed at this end, and what we are asking next

Chasing *where* the time goes turned up three things, all in `TODO.md` items 36–38
with measurements:

- The english corpus was encoded with `UNICODE_CODER`, which captures four characters
  where ten were available. Natural runs left for the cleanup at n = 1,000,000 fall
  from **365,958 to 16,641**, and the residual inversion probability *p* by a factor
  of **245**.
- The coders **mask rather than saturate**, which is not monotonic: `englishCoder`
  gives `"don't"` and `"dongt"` identical codes, and `asciiCoder` sorts `"café"` as
  though it were `"cafi"`. Saturating variants are added alongside.
- §A.5 chose Timsort for the cleanup by beating an "insertion sort" that is in fact
  binary-search insertion, *n* log *n* whatever the input's order — not the *N* + *X*
  algorithm the paper's own cleanup term describes.

**Request 11 is in `doc/Run request for Yunlu.md`** and supersedes what we would
otherwise have asked. Part (a) is the cleanup-pass benchmarks, about forty minutes,
with our predictions stated in advance so a surprise reads as one. Part (b) is the
full suite again — and we do mean the whole thing, because `RadixHuskySort`'s
internals moved under every radix row in the suite and not only the string ones, so
the tables want replacing rather than extending — and, since the encoding is now
parallel too, every `ParallelRadixHuskySort` figure from request 10 predates that.
The commit to record is **`15cc2ff`**, 423 tests passing.

Two of your request-10 conclusions we would like to keep on the record as they stand:
the permits result, which we now agree with, and the fork bimodality at 198,900,
which we saw independently and which is the reason we will not quote a single-fork
figure again.
