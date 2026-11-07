# ACDA27: rejected, the reviews, and what follows

Opened 2026-11-07. Notification arrived 2026-11-06 (two days late, flagged in advance). **Submission
47 was rejected**, scores **-3 (strong reject), -2, -2**. The committee invited a poster instead and
extended that deadline to **2026-11-12 AoE**.

This document holds two things: the three reviews with an assessment of each point, and the open
questions left over from the 2026-10-19/20 and 11-05/07 working sessions, which the rejection
reprioritises rather than cancels. The reviews are quoted as data.

## Which artifact the reviewers actually read --- and it is not the tag

**Checked against the wrong build at first, corrected 2026-11-07 on Robin's challenge.** The
`acda27-submitted` tag marks `8660534`, the **15 September** build --- the deadline build. But the
submission was edited afterwards to upload the anonymised PDF, and that file,
`paper/RadixHuskySort-anon.pdf`, is timestamped **17 September 17:11**. Three commits touched
`paper/` in between:

| commit | time | what |
| --- | --- | --- |
| `36fa361` | 09-17 15:16 | Say in the paper that the advantage is a serial one |
| `97337ae` | 09-17 16:58 | Say what was parallelized and what that does not prove |
| `15cc2ff` | 09-17 17:12 | Parallelize the encoding in the parallel sorter |

The anon PDF was built at 17:11, one minute before `15cc2ff`, and its text matches `97337ae`
(which first introduces "the advantage demonstrated here is a serial one"). **So the reviewed source
is `97337ae`, not the tag.** `acda27-submitted` marks what we meant to submit, not what was reviewed.
Worth a second tag, or at minimum do not use the existing one to reconstruct the reviewers' view.

### A defect in the reviewed version that no reviewer named explicitly

The build they read **contradicts itself on the central parallel question**:

> Introduction: "the encoding and the cleanup dominate on the corpora we measure, and **we did not
> parallelize either**."

> Conclusion: "A proxy-key sort has four phases... **We parallelized two of them, the encoding** and
> the digit passes."

One says the encoding was not parallelised; the other says it was. This is the seam between
`36fa361`/`97337ae`, which rewrote the conclusion, and an introduction that was not updated to match
--- committed in the two hours before the anonymised build went up. It is very likely a contributor
to R1's "nearly impossible to keep track of the authors' train of thought", and it is more damaging
than either of the factual errors in R1 below, because it is ours.

---

# 1. What the reviewers agree on

**All three reviewers independently say the paper is hard to follow, and say it in structural
terms.** That is the finding. Nobody disputes the measurements.

> R1: "poorly written, not respecting traditional paper structure, mixing discussion of results with
> presentation of the technique, containing too many forward references"

> R2: "there is no clear distinction into a description of a theoretical algorithm, its
> implementation and empirical evaluation. All three are (partially) intermixed"

> R3: "it is badly written. Even the above summary was hard to extract. Also there is not hint up
> front of how the husky reduction to 64-bits works... the paper needs to be self contained."

Three independent readers converging on one complaint is as clear a signal as peer review produces.
It is also the most fixable class of problem: no measurement has to be redone.

**A specific sub-case worth naming, because we caused it.** R1 objects that the conclusion rests on
material in the appendix; R3 objects that the body is not self-contained. Both are consequences of
the page strategy: the body sat at exactly the 12-page limit while the appendix grew from 8 pages to
10 as we moved things out of the body to fit. We optimised for the limit, and the reviewers read the
body and found it incomplete. **At an unlimited venue that pressure disappears entirely**, which is
the single biggest argument for where to go next.

## Two checkable claims in R1 that are wrong

Recorded so that R1 is weighted correctly --- not to dismiss the review, whose structural points are
echoed by the other two. **Both were re-verified against `RadixHuskySort-anon.pdf`, the build the
reviewers read**, after the first pass wrongly used the 15 September tag; both findings survive the
correction unchanged.

> R1: "Section 4.1 claims that appendix A3 specifies the details of the three machines used in the
> evaluation. It does not."

It does. §4.1 reads "§ A.3 specifies all three", and A.3 *is* "Benchmark environments", containing
Table A.4 with model, processor, cache, memory, OS and JVM for machines A, B and C.

> R1: "The conclusion claims that the paper has shown that ParallelRadixHuskySort outperforms
> Arrays.parallelSort. This is not included in the paper."

The conclusion says "ParallelRadixHuskySort leads Arrays.parallelSort by 1.19x at ten million
\textit{Long}s, **trails it at two million, and on the permits trails its own serial form** (§ A.7)",
and §A.7's Table A.5 carries the data. So the claim is in the paper, it is cited, and it is stated
against us in two of three cases. R1's *structural* objection --- that an appendix should not carry
something the conclusion depends on --- stands, and is worth acting on. The factual framing does not.

---

# 2. Reviewer points worth acting on

Ordered by how much they would change the paper.

| # | point | source | assessment |
| --- | --- | --- | --- |
| 1 | Separate algorithm / implementation / evaluation; remove forward references | R1, R2 | **Accept.** The dominant complaint. A structural rewrite, not a rewording. |
| 2 | The body must be self-contained; explain husky coding up front | R1, R3 | **Accept.** §3.3 introduces the assumptions on page 5; all three reviewers wanted them earlier. |
| 3 | Missing references to modern performant sorts, e.g. **IPS4O** | R1 | **Accept, cheap, and a real gap.** We cite no in-place parallel super-scalar samplesort. Item 20 added classic string-sorting literature and stopped there. |
| 4 | "the paper also claims to analyze the cost of the last phase... but I do not see any formally proven theorem" | R2 | **Partly accept.** §`sec:pcrit` gives a cost *model* plus measurement, not a theorem. Either prove something or stop implying analysis. Related to 43A, already corrected. |
| 5 | Prior HuskySort work is "only in an ArXiv paper with few if any citations" | R3 | **A positioning problem, not a citation problem.** The paper leans on the 2020 preprint for the husky code rather than restating it. Fixing 2 fixes this. |
| 6 | Parallel speedups "underwhelming: 8 cores gives only ~30%" | R2 | **Superseded.** That figure predates the parallel encode and the parallel cleanup. Item 43D now reports leads of 1.25--1.59x on permits and 1.10--1.49x on chinese. The reviewers read the old story. |
| 7 | Time formulas missing lower-order terms | R1 | **Partly accept, partly addressed.** 43A fixed the attribution of $T_3$; R1's separate point is that asymptotic forms are being used for absolute timing. |
| 8 | Generalisation that quicksort is the fastest | R1 | **Accept and already flagged** this session --- see §3, the Sort Race tension. |
| 9 | Too much time on strings; "argued that strings are the wrong data type" | R1 | **Reject as stated, but it signals a framing failure.** Strings are the motivating case; §`sec:chinese-coders` explains why they vary. A reader took that as self-refutation, which means the framing invited it. |
| 10 | Chinese personal names evaluated using code-point order | R1 | **Already fixed.** The pinyin-correct baseline landed in request 11d; 43D's tables use it. The reviewers read the superseded version. |
| 11 | Font sizes in figures too small | R1 | **Accept.** Trivial. |
| 12 | Histogram-combine not parallelised: "Why did they not?" | R1 | **Fair.** Named as future work in the paper; `doc/Second paper.md` carries it. |

Note how many of 6, 10 and parts of 7 are **things we fixed after submitting**. The reviewers read
the 15 September version; items 43A, 43B1, 43C, 43D and 43H have landed since. That is a real
argument for a prompt resubmission rather than a long pause.

---

# 3. Open questions from this session, unresolved

These predate the rejection and are not cancelled by it. Numbering is local to this document.

## Paper content

1. **43F, the warm-up decision.** 35 of 81 english String rows were unconverged at 2 s iterations,
   so the paper's english ratios are **understated**: `systemSort / radixHuskySortAuto` reads
   3.98x / 2.90x where steady state is nearer 4.5x / 3.2x. Quoting the stronger figures needs the
   long-warm-up data; quoting the current ones and calling them conservative needs a clause. Also in
   43F: label the $n = 32{,}000$ parallel rows "1 chunk (serial)", since `chunks = max(1, min(p,
   n/16384)) = 1` there and a row labelled `p8` invites the wrong inference.
2. **43E and 43G** are drafted in `TODO.md` and not written into the paper. Both were deferred for
   page space that a different venue would not charge us.
3. **The Sort Race tension.** The paper cites Zhang et al. for "quicksort is the fastest
   general-purpose sorting algorithm" and then says quicksort underperforms Timsort on partially
   sorted data. The second sentence is ours, not theirs --- but Sort Race specifically found
   quicksort *adaptive to nearly sorted inputs*, so a referee who knows it may see a contradiction.
   R1 complained about exactly this generalisation. **Worth rewording.**
4. **The Auger attribution.** We give $\mathbf{O}(N + N \log r)$ for $r$ runs. Auger et al. prove it
   for **Python's** TimSort and describe Java's as "not without very involved technical details".
   Our table says "Timsort". Consider saying which.
5. **Three uncited bibliography entries** --- Gilreath's hash sort, Sedgewick on Shellsort, Xiang on
   quicksort complexity. They never reach the bibliography. Cite them or drop them.

## Process and admin

6. **The acknowledgments** (item 57.3). Suppressed by `\ifanonymous`, so never seen by reviewers.
   Cutting them frees no page space --- measured. Purely attribution: Dedhia's 2020 commit is the
   ancestor of the `exactlyEncodable` machinery that items 48, 55 and 56 are about.
7. **The AI disclosure statement** (57.2, 57.4). Draft exists in
   `doc/AI use and the SIAM disclosure policy.md`. Placement depends on the target venue's policy,
   which is now an open question again.
8. **The voice read** (57.5, Sai Vineeth's 2.5). Still wants an outside reader, and now matters
   *more*: three reviewers called the writing hard to follow. **This is no longer a nice-to-have.**
9. **Item 56**, the third shape for the exactness test, in `doc/Code and corpus notes.md`. It was
   parked because changing the encode path would re-invalidate request 13's 205 rows. **If we
   re-measure for a new venue anyway, that objection disappears.**
10. **B2**, the masking/saturation argument, in `doc/Second paper.md`. Parked because adding
    unreviewed argument at camera-ready would have been wrong. **There is no camera-ready now**, so
    it can come back into the paper if we want it.

---

# 4. Decisions now needed

1. **The poster, by 2026-11-12 AoE.** Robin's position before the decision was that he was not
   especially interested. The committee has now invited one explicitly and extended the deadline.
   Non-archival, so it forecloses nothing. Still costs a trip to Pittsburgh in February.
2. **The venue.** JEA and SEA 2027 were the shortlist. SEA's deadline is unannounced but
   historically early February. JEA has no deadline and no page limit --- and given that the body's
   page budget is implicated in the single most-repeated criticism, that is now a substantive
   argument rather than a convenience.
3. **One paper or two.** The rejection changes this. With no page limit, B2 and the parallel work
   could return to one larger paper; or the split stands and the first paper gets shorter and
   clearer, which is what the reviewers actually asked for.
4. **How much to rewrite before resubmitting.** The reviews ask for a restructure, not new results.
   Nothing in them requires a benchmark run.
