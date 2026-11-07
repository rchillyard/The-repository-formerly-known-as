# The TALG rewrite: plan and handover

Opened 2026-11-07, after the ACDA27 rejection (`doc/ACDA27 rejection and what follows.md`).
**Target: the Empirical Track of ACM Transactions on Algorithms.** This is the starting point for
the rewrite and the handover for whoever picks it up; read it before `TODO.md`, much of which is now
written against constraints that no longer apply.

## Why TALG, and the one thing to check

JEA, which this project had been aiming at for weeks, **ceased publication after 2023** and no
longer accepts unsolicited submissions; its site redirects authors to TALG's Empirical Track. That
track's scope is discrete algorithms and data structures studied empirically, with algorithm
engineering explicitly in scope. It is also the successor to the journal that published
López-Ortiz et al., our nearest prior work.

**To confirm in a browser** (`dl.acm.org` sits behind bot verification, so neither Robin nor Claude
could read it from here): whether any page limit exists --- almost certainly not, but that is an
inference from ACM journal practice, not a quotation --- and the exact wording of the Empirical
Track's requirement to link to a repository where others can independently verify the empirical
claims. We exceed that requirement already: 47 JMH JSONs, a declared machine of record, and a public
repository.

## The load-bearing change: what the paper is *about*

**This matters more than the restructure.** Reviewer 2 summarised the submitted paper as: "The main
difference compared to the previous version of the algorithm is to use radix sort instead of quick
sort for step 2." That summary is *fair to what was written*, and framed that way "incremental" is
the right verdict.

The contribution is not the radix swap. It is this:

> A proxy key need only be **approximately order-preserving**. Relaxing exactness is a design lever
> with a measurable price, and that relaxation is what licenses codes cheap enough for the method to
> pay.

Prior work on order-preserving compression for sorting --- López-Ortiz et al. (JEA 2005),
Antoshenkov et al. (ICDE 1996), **neither cited in the submitted version** --- keeps exactness and
pays for it in variable-length codes. We fix the width at 64 bits and pay in $p$, with $p_{crit}$ as
the cost model and the cleanup pass as the price. That contrast is the paper's claim to novelty and
it has to be made in the first two pages, not discovered by a reader on page five.

A rewrite that fixes the structure but keeps the old framing will produce a clearer presentation of
a small idea. **Section 2 below is the section that decides whether this is worth submitting.**

## Proposed structure

Each section names the criticism it answers. Reviewer quotations are in
`doc/ACDA27 rejection and what follows.md`.

| | section | content | answers |
| --- | --- | --- | --- |
| 1 | Problem | comparison cost dominates for objects with expensive orderings; the three-phase shape | motivates before mechanism |
| 2 | **Concept** | approximate / $p$-order-preserving keys; $p$ and $p_{crit}$; the contrast with exact order-preserving compression; IPS4O and the modern-sorts literature | R1 "assumptions introduced too late"; R3 "no hint up front"; R1 "missing references to modern, performant algorithms" |
| 3 | **Algorithm** | encode / sort / repair, with husky coding explained **completely here** | R3 "the paper needs to be self contained"; R3 on the arXiv-only foundation |
| 4 | RadixHuskySort | the radix second phase; the array-access argument | states the delta plainly |
| 5 | **Implementation** | coders, the cleanup choice, the parallel variant | R1 "implementation should not be discussed in the evaluation section but prior to it" |
| 6 | **Methodology** | machine of record, JMH, the fork as the unit of replication (item 43H) | the Empirical Track's own criterion |
| 7 | **Results** | by data type, **with the parallel-versus-parallel comparison in the body** | R1 "appendices should only expand upon the main paper" |
| 8 | Limits and open questions | where it loses; F3; the unexplained residuals | R2 praised the honesty --- make it structural |
| 9 | Related work, conclusion | | R1 "the conclusion should not introduce new material" |

The appendix becomes genuinely supplementary: derivations, the full tables, the corpus details.

## What is no longer constrained

Everything below was deferred for page space that TALG does not charge. **Reconsider each on merit,
not on budget.**

- **B2** --- masking versus saturation, and the coupling result that masking wins *only because the
  cleanup is Timsort*. Currently parked in `doc/Second paper.md`. This is one of the most
  interesting things the project knows and it was cut for space. Strong candidate for section 5.
- **43E** --- the encode, not the cleanup, is the dominant serial term.
- **43G** --- the character-count lever as design guidance; naming `AdaptiveInsertionSort`.
- **43F** --- the warm-up finding (english ratios are *understated*) and the "1 chunk (serial)"
  labelling for $n = 32{,}000$. The warm-up decision is still open: quote the conservative figures
  with a clause, or re-measure with long warm-up.
- **Item 56** --- the third shape for the exactness test, in `doc/Code and corpus notes.md`. It was
  parked because changing the encode path would re-invalidate request 13's 205 rows. **If the
  rewrite re-measures anyway, that objection disappears.**

## What is now stale in `TODO.md`

- **Item 19** --- ACDA27, rejected. Its requirements (12 pages, appendix excluded, the dates) are
  the record of what was submitted against, not a live constraint.
- **Item 57** and round 2 of `doc/Tasks for Sai Vineeth.md` --- scoped for an ACDA27 camera-ready
  that will not happen. **57.1 is done** (the bibliography is verified; see `57fbf3d`). **57.5, the
  voice read, matters more than ever**: three reviewers independently called the writing hard to
  follow, and it needs a reader who is neither Robin nor Claude.
- All page-budget arithmetic --- the ~300-word slack, the donor-section analysis, the measured spill
  thresholds. Historically interesting, operationally dead.

## Carried-over questions, not yet answered

From `doc/ACDA27 rejection and what follows.md` §3, still open: the Sort Race tension (we cite it for
"quicksort is fastest" and then contradict that, and R1 complained about the generalisation); the
Auger attribution (the bound is proved for *Python's* TimSort); three uncited bibliography entries;
the acknowledgments decision; and where the AI-use disclosure goes, which now depends on ACM's
policy rather than SIAM's.

## Provenance, so the reviewed version stays findable

- `acda27-submitted` → `8660534`, the 15 September deadline build.
- `acda27-reviewed` → `15cc2ff`, the tree the anonymised PDF was built from and **what the committee
  actually read**. It contains a known defect: the introduction says the encoding was not
  parallelised while the conclusion says it was.

Use `acda27-reviewed` when checking any reviewer claim. Figures in the current working tree have
moved since: items 43A, 43B1, 43C, 43D and 43H have all landed, and 43D in particular replaced the
parallel claims wholesale.
