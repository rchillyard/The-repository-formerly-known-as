# AI use, and the SIAM disclosure policy

Opened 2026-10-19, at Robin's request, while deciding what the camera-ready acknowledgments should
say. The paper was submitted to SIAM ACDA27 on 2026-09-15; **author notification is 2026-11-02 and
camera-ready is due 2026-11-23**, so anything in the "Open actions" section below has that deadline.

This file exists because the policy is a *disclosure* requirement rather than a courtesy, and
because three of SIAM's four named failure modes are checkable against this repository. Transcribing
the policy without checking ourselves against it would miss the point of having it.

## The policy

Supplied by Robin, 2026-10-19. SIAM's own pages return HTTP 403 to automated fetches, so this
transcription and the saved copy of the submissions page (`paper/ACDA27 Submissions | SIAM.pdf`) are
our record; **confirm against the live policy before camera-ready.**

### Core principles

- **Human accountability.** AI tools must never replace human judgment, expertise, or
  accountability. Authors are fully responsible for the content of their submissions regardless of
  AI use.
- **Transparency and disclosure.** Human authors may use AI tools --- for literature searches,
  coding, drafting feedback, or proof/algorithm development --- provided they properly disclose that
  use according to the policy guidelines.

Authors must all be human. Nothing here contemplates an AI author, and we are not proposing one.

### The four failure modes SIAM names

1. **Fabricated references** --- references that do not exist, have incorrect titles or authors, or
   cite results not actually present in the source paper.
2. **Unattributed appropriation** --- AI systems reproducing key mathematical or algorithmic
   innovations without credit to the original source.
3. **Mathematical and coding errors** --- substantial, subtle errors in logic, code or proofs that
   appear plausible on the surface.
4. **Inaccurate figures** --- artificially generated or improperly graphed data giving a misleading
   visual representation.

Sources: [SIAM News, "A Contract of Trust"](https://www.siam.org/publications/siam-news/articles/a-contract-of-trust-artificial-intelligence-usage-for-siam-journal-submissions/),
[SIAM Publications editorial policy on AI](https://epubs.siam.org/artificial-intelligence).

## What was actually used, and where

Claude (Anthropic), in Claude Code, from 2026-07-22 to the present. The repository carries a
machine-readable record: **296 of the 580 commits on `parallel-redesign` end with a
`Co-Authored-By: Claude` trailer**, so which work involved the tool is checkable commit by commit
rather than asserted. Use spans all four of the categories the policy permits --- literature
search, coding, drafting, and algorithm work --- plus benchmark analysis, which the policy's own
examples cover as "running experiments".

Robin is the author and is accountable for all of it. Yunlu Liaozheng ran every benchmark quoted in
the paper, on his own hardware, and several of the findings below are his.

## Audit against the four failure modes

### 1. Fabricated references --- **the highest-risk area here, and not yet fully discharged**

`paper/sample-base.bib` was **reconstructed with AI assistance** in `1f0697a` (2026-09-07), because
switching away from `acmart` required regenerating the bibliography and no `.bib` existed. This is
precisely the zone SIAM warns about.

Mitigations that were in place at the time:

- The entries were produced **by transcription from the existing `.bbl`**, not from the model's
  memory of the literature. The citation keys before and after are identical, so no entry was
  invented and none was lost.
- Two entries came back thinner than the rest and were **marked INCOMPLETE rather than guessed at**
  --- Jugé's *Adaptive Shivers Sort* (no year, booktitle or publisher in the `.bbl`) and Zhang, Meng
  and Liang's *Sort Race* (title and a misspelled eprint id only). Both have since been completed
  and now carry a year and a DOI or eprint.

Current state: 23 entries, 9 with a DOI and 7 with a URL.

**Open action.** Transcription from a `.bbl` guarantees fidelity to the previous bibliography, not
to the actual publications --- an error already present would have been carried forward faithfully.
Before camera-ready, every entry should be checked against the real source, and in particular that
each cited work says what the paper says it says. That last check is the one SIAM words most
specifically ("cite results not actually present in the source paper") and it is not covered by any
transcription argument.

### 2. Unattributed appropriation

No general-literature algorithm here originated with the tool. Where an idea has a specific human
origin inside the project, the record names it:

- The **hoist** shipped in PR #73 is Robin's, proposed as a variant in request 13's question 1; the
  commit and `TODO.md` item 55 both say so.
- The **split-loop alternative** was Yunlu's, and he argued against his own version after measuring
  it.
- **Item 48's wrong-answer bug** --- length is necessary but not sufficient for exactness --- was
  Yunlu's finding in requests 11c/11d, and the javadoc on `exactlyEncodable` credits him.
- The **measurement methodology** of 43H (the fork as the unit of replication) is Yunlu's practice,
  adopted after his method caught an error of ours.

The paper's relationship to the prior literature (MSD radix sort, multikey quicksort, burstsort) was
added as item 20 and cites that literature rather than absorbing it.

One live question rather than a finding: the 2020 acknowledgments name Darshan Dedhia, Akshay
Bhusare and Kartik Kumar. Dedhia's single 2020 commit implemented the first version of the
encoded-exactly test in `HuskyCoder` / `HuskySequenceCoder`, which is the direct ancestor of the
`exactlyEncodable` machinery that items 48, 55 and 56 are about. Cutting him from the camera-ready
acknowledgments would be an attribution loss, and it saves no space (measured: none).

### 3. Mathematical and coding errors --- **several occurred; all were caught, and how matters**

This is the failure mode that actually bit, repeatedly. Recording the instances is more useful than
asserting care, and the pattern in them is consistent: the output looked right while the reasoning
under it was wrong.

| what was claimed | what was true | how it was caught |
| --- | --- | --- |
| `f43ff52`: "No measurable cost" for the per-element exactness check | 11--15 % slower on the english encode | Yunlu's request-12 fork-level A/B. Our method --- two variants inside one JVM --- **cannot see** an inlining effect, because both arms share one set of compilation decisions. This became item 43H. |
| The IntroSort depth bug explained heapSort's absence from the benchmarks | It did not; benchmarks go through `mutatingSort`, which never took the broken path | Robin challenged the claim; verified by running the same probe against pre-fix code, identical row for row |
| INFO6205's MergeSort tests missed a fault because of weak assertions | The cause was coverage: at N=8 under cutoff 20 the recursion never runs | Fault injection --- all 28 tests passed with the fault present |
| `sort(X[], int, int, int)`'s third argument is the current depth | It is the fourth | Robin caught it on reading |
| Predicted 26--28 % english gain from the encode saving | 17.6 % / 21.1 % measured, later 22.8 % / 25.9 % after the fix | Yunlu's measurement |
| A 6 % regression on the sign-bias A/B | The sign flipped when the arms were measured in the other order | Re-running in the opposite order before reporting |
| The ACDA27 venue had closed and the paper was unsubmitted | It was submitted on the deadline, 2026-09-15 | Robin said so; confirmed by the `Revisions` tip |

Two general lessons are already recorded in the paper's own checklist and should survive into it:
a tight loop's speed is a property of its compiled form rather than its source (43H), and a
"74 % cleanup" figure once quoted came from dividing a serial measurement by a parallel total (43E).

What makes this tractable is not that the tool is careful but that the claims are cheap to test: a
fault injected into a passing test, a probe run against pre-fix code, an A/B run in both orders.
Every entry above was settled by running something, not by re-reading.

### 4. Inaccurate figures

No figure or table in the paper contains generated or illustrative data. Every number comes from a
JMH JSON committed under `doc/`, and the practice since request 12 has been to **re-derive each
quoted figure from the raw JSON rather than copy it from a report** --- including from our own
collaborator's reports, and including the AI's own tables.

Concretely, for request 13 (PR #73) every headline was recomputed before merging: the gate's
geometric means, the 205-row count, the baseline drift, the per-corpus medians and the warm-up
flags all reproduced. For item 43D the same check found that the figures *in our own checklist* had
been superseded and were wrong in direction, not merely stale --- the permits had gone from "beats
its own serial form at no size" to beating it at two of three.

Supporting practices worth keeping:

- Rows whose first five measurement iterations exceed the last five by more than 10 % in some fork
  are marked and excluded from claims; this is why the paper makes **no english claim** against
  `Arrays.parallelSort`.
- Projections are recorded separately from measurements, so that a wrong one is visible afterwards.
  One was: 43D projected chinese at 200,000 would regress to ~0.6x under a parallel cleanup; it
  measured 1.10x.

## Draft disclosure statement for the camera-ready

To be checked against the live policy for required placement --- SIAM's examples suggest a
disclosure statement rather than the acknowledgments, and other publishers vary between methods and
acknowledgments.

> **Use of AI tools.** The authors used Claude (Anthropic) throughout the preparation of this work,
> for implementation, benchmark analysis, literature search and drafting. All measurements reported
> here were produced by JMH runs on the hardware described in Appendix~\ref{sec:environments} and
> the raw results are archived with the artifact; every figure quoted in the paper was re-derived
> from those raw results. The authors reviewed and verified all content and are solely responsible
> for it.

Do **not** phrase this as thanks in the acknowledgments. Acknowledgment credits a contributor;
disclosure states what tool was used and for what, and only the second is what the policy asks for.

## Open actions before 2026-11-23

1. **Verify all 23 bibliography entries** against their real sources, and check that each cited work
   supports the claim made of it. Highest-risk item; see §1.
2. **Confirm the live SIAM policy wording**, in particular the required placement of the disclosure.
   The transcription above came via Robin and the links may have moved.
3. **Decide the acknowledgments** on attribution grounds; they cost no page space either way, and
   Dedhia's contribution has a direct line into this paper's subject matter.
4. **Settle the disclosure text** and place it.
