# Every edit this week's work implies — started 2026-09-02, last updated 2026-09-08

Supersedes `MSD baseline draft text.md`. This is intended to be exhaustive: it lists the claims that
must change, the claims that must be added, the claims that should be softened, the optional
additions, **and the claims that were checked and found unaffected** — that last section exists so
nothing gets re-litigated later.

## Status as of 2026-09-07, checked line by line against `HuskySort.tex`

**Line numbers throughout this document were re-derived on 2026-09-07 after the day's three changes:
the removal of `HSComp` and `Improvements Summary`, the draft revision stamp, and Figure 5's caption
cut. Anything quoted in an older revision is off by up to forty lines.** They are worth re-deriving
again after the SIAM template switch, which will not move them but will renumber every table.

**Applied**: §2's baseline disclosure (tex 589–591); §3's reframing, which absorbed 1.2, 1.3 and 1.4
(tex 561–614); §1.1's replacement in the conclusion (tex 1443–1447); §5.3's crossovers and the 166 µs
floor (tex 1401–1423, unmoved); §5.4's environment table (tex 890–903); §7's table rebuild — five results
tables now come from `doc/full-suite.json` (tex 1255–1275 and §sec:analysis's three benchmark tables).
Also new since this document was written and **not recorded anywhere below until now**: Table
`Guidance` (tex 1371–1399), the two adversarial tables (tex 1519, 1562), and the appendix's paragraph
on unbounded recursion in the baselines (tex 1599–1617).

**Sequencing, decided by Robin on 2026-09-07:** fix the substantive issues first, then see what has to
be done to squeeze the result into 12 pages. **The first phase is now complete** — every table is
rebuilt from the merged PR #64 data and the appendix is rewritten — so the squeeze is the remaining
work. Body measured at 13.0 pages after the rebuild. Moving §sec:pcrit's scaling caveat and the permits case
study into the appendix (Robin's instruction, 2026-09-07) bought about half a page, leaving the body at
**12.46 pages** — roughly **half a page still to find**.

Candidates for the rest, cheapest first:

| where | what | rough |
| --- | --- | ---: |
| §sec:usecase | the crossover prose partly restates Table `Guidance` in sentences | 0.2--0.3 |
| §Data Source | the permits paragraph can shrink now that §A.3 carries the case study | 0.1 |
| §sec:parallel-radix | the Amdahl's-law discussion is three sentences where one would do | 0.1--0.2 |
| Conclusion | the three-bullet list restates §sec:summary's own elimination of non-use-cases | 0.15 |

None of these costs an argument, which is the test worth applying: everything moved so far was detail
rather than claim, and the appendix is free precisely because the program committee reads it at its
discretion. So the page-budget arithmetic recorded in 0c and 0d is
still accurate and still worth having, but it is **not a constraint on the edits below** — do not trade
away a correction to save a line. The squeeze is a separate, later pass over whatever the paper says by
then.

**Still pending, in order of how much they cost if missed:**

| | what | where |
| --- | --- | --- |
| ~~**4.1**~~ | ~~"HuskySort is always faster than dual-pivot quicksort"~~ — **APPLIED 2026-09-07**, tex 1441–1447 |
| ~~**8**~~ | ~~the appendix's crash result~~ — **APPLIED 2026-09-07**: both columns now shown, prose rewritten |
| ~~**0h**~~ | ~~PR #64 rebuild~~ — **APPLIED 2026-09-07**: five tables, every derived figure, the appendix, the abstract |
| **9** | **the squeeze**: body still runs onto page 13. **Do not attempt further trimming under acmart** — see 0j |
| ~~**5.2**~~ | ~~a measurement the body does not contain~~ — **APPLIED 2026-09-07**, tex 729–758. But see the new item below about the abstract's range |
| **0e** | **the abstract's "a tenth to a quarter" is not what the body now says** — and the abstract is submitted tomorrow | tex 251–254 |
| ~~**1.5**~~ | ~~"every non-string row exceeds every string row"~~ — **APPLIED 2026-09-07**, tex 567–588 |
| ~~**5.1**~~ | ~~the permits case study is nowhere in the prose~~ — **APPLIED 2026-09-07**, tex 899–905 and 1352–1365 |
| ~~**7**~~ | ~~tables from the 2017 Intel/Java 8 machine~~ — **CLOSED 2026-09-07.** `HSComp` and `Improvements Summary` removed; `TimvsInsertion` kept and now attributed at tex 806 | — |
| **0c** | **the SIAM proceedings template** — blocked only on fetching `siamproceedings.sty` **and `siamplain.bst`**; Robin has asked Sai Vineeth. See `doc/Tasks for Sai Vineeth.md` | tex 43–58 |
| **0k** | **front-matter anonymisation** — names, emails, affiliations *and ORCIDs*. Decided 2026-09-08; goes with the template switch | tex 174–176, 199–245 |
| ~~**0c**~~ | ~~anonymisation~~ — split into **0k** (front matter) and **0i** (the repository link), both decided |
| ~~**0c**~~ | ~~12 pages excluding references~~ — **CLOSED**: 12.0 under acmart. Must be re-measured after the SIAM reflow | — |
| ~~**0b**~~ | ~~cite arXiv:2012.00866~~ — **APPLIED 2026-09-07**, Introduction + `HuskySort.bbl` |
| ~~**0f**~~ | ~~`sample-base.bib` is missing~~ — **RECONSTRUCTED 2026-09-07** from the `.bbl`; BibTeX now runs clean and all 19 citations resolve |
| ~~**0g**~~ | ~~no general composite-key coder~~ — **APPLIED 2026-09-07** as appendix §A.4, which costs nothing against the page limit |
| **0a** | **superseded by 0c.** The template question is answered; only the author footnotes remain, and they come out for review anyway | tex 43–58 |

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

# 0c. The venue's rules — CONFIRMED from the ACDA27 submissions page

Source: `paper/ACDA27 Submissions | SIAM.pdf`, saved by Robin on 2026-09-07, together with
`paper/SIAM Conference on Applied and Computational Discrete Algorithms (ACDA27) | SIAM.pdf`. These
supersede the ACDA25 wording this section previously carried as a stand-in.

**One inference in that stand-in was wrong, and it was the consequential one.** From ACDA25's "All
accepted proceedings papers must use the double-column LaTeX macro" I inferred that acmart would
probably pass for review. ACDA27 says, of submissions rather than accepted papers:

> Submissions should use the LaTeX macros at the web page: https://www.siam.org/publications/proceedings/

So **the template has to change before the 15th.** 0a is no longer a judgement call.

## Deadlines, as published

| date | what |
| --- | --- |
| **September 8, 2026** | short abstract **and submission registration** |
| **September 15, 2026** | archival proceedings paper |
| November 2, 2026 | author notification |
| November 23, 2026 | camera-ready |
| Feb 22–24, 2027 | conference, Pittsburgh, co-located with CSE27 |

Submission is via EasyChair, <https://easychair.org/conferences/?conf=acda27>. There are **two submissions,
46 and 47**, both made on 2026-09-08 within about half a minute of each other: Sai Vineeth's, which did
not carry Robin's email and so was invisible under his account, and Robin's, made because of that
apparent absence. **Decided 2026-09-08: withdraw 46, keep 47**, Robin to speak to Sai
Vineeth on the 9th first. It has to happen **before the 15th** — the paper PDF uploads against the
surviving submission, so uploading first and withdrawing after would discard the paper along with the
duplicate. **Check 47's author list before uploading:** it was entered by Robin in a hurry and may name
only him, where it needs all three authors with correct emails — a missing address is what made 46
invisible, and it also governs who receives the November 2 notification and who can read the reviews. There is a rebuttal
phase: "The submissions will be refereed and authors will be provided an opportunity to respond to the
reviews."

We are submitting in the **archival proceedings papers** category. The other two — non-archival
extended abstracts and posters — are 2-page, non-archival, and **not** double-blind. Nothing below
applies to them.

## Page limit — confirmed, and the budget

> Submissions may be up to 12 pages in length, excluding references, and must present original research
> that is not published or submitted elsewhere.

> Authors may, at their option, include an appendix containing proofs, details, or additional
> experimental results. The appendix will be read by the program committee members at their discretion
> and will not be included in the proceedings.

**Measured 2026-09-07, after all three removals: 14 pages total, and the counted content is 12.0 —
within the limit.**

The body ends at the foot of page 12. `ACKNOWLEDGMENTS` is the first item on page 13, at the top text
margin, and the last body float is Table `Guidance` on page 12, so nothing spills forward. References
and the appendix occupy 13–14.

| removal | bought |
| --- | ---: |
| Tables `HSComp` and `Improvements Summary` | ~0.6 page |
| Figure `usecase` | ~0.4 page |
| **total, 13.0 → 12.0** | **1.0 page** |

> **How to measure this, because I got it wrong once.** After the two table deletions I reported "12
> pages, exactly the limit". Robin's own rendering came in at 12.5 and he was right. The error was in
> the method: I found the end of the body by locating the page where `REFERENCES` begins, which misses
> body content that *floats past* it. Figure 5 was a full-width `figure*` with no top slot free on page
> 12, so it landed at the top of page 13 — ahead of the acknowledgments, which began 42% down that page.
>
> **Measure by the last body float, not the first reference.** Concretely: take the page of the last
> `tab:`/`fig:` label from `HuskySort.aux` that belongs to the body, and confirm with
> `pdftotext -f N -l N -bbox` that the acknowledgments start at the top text margin of the following
> page rather than partway down it.

Two further notes:

1. **None of this survives the template change.** SIAM's double-column proceedings macro will reflow
   everything, and the page count under it is unknown until we build it. Re-measure as the first thing
   after the class switch — and measure it the right way, by finding the last body float rather than
   the first reference.
2. **The appendix being free is the main lever.** Anything the 12 pages cannot hold that is detail
   rather than argument — §5.1's permits provenance, §5.2's cleanup-pass table, and possibly Figure 5
   itself — can go there rather than be cut, provided the main part still carries the argument. The
   page is explicit that it must: "The main part of the submission should therefore contain a clear
   technical presentation of the merits of the paper".

## How the page was recovered — CLOSED 2026-09-07

- Figure 5's caption said two things twice over, an accretion from the figure's two reworkings.
  Deduplicated — lossless, about a third off the caption — but it did *not* move the figure off page 13,
  page 12 already carrying Tables `ParallelRadix` and `Guidance`.
- **Figure `usecase` was then dropped entirely**, on Robin's decision, from four options weighed: move
  it to the appendix, drop it, shrink it to a single column, or trim §6.5's prose. Dropping it has the
  best independent argument: it is organised along the size axis, and Table `Guidance` exists precisely
  because the paper stopped recommending that way of choosing. See `Paper deletions.md`.
- `paper/UseCaseGuidance.tex` and `.pdf` are no longer part of the build. The generator is kept in case
  a journal version under a looser limit wants the figure back; Robin may prefer them deleted.

## The template — action required, and it needs a download

Nothing SIAM is installed on this machine: `kpsewhich` finds no `siamproceedings.sty`, no
`siamplain.bst`, none of `soda2e.cls`, `siamproc.cls`, `siamltex.cls`, `siamart0216.cls`,
`siamart190516.cls` or `siamart220329.cls`, and `/usr/local/texlive/2020/texmf-dist/tex/latex/siam*`
does not exist. Note that the thing needed is a **package**, `siamproceedings.sty`, not a class — which
is why an earlier search for a `.cls` came up empty and concluded too much from it.

**Getting the macros means downloading them from <https://www.siam.org/publications/proceedings/>, which
is Robin's call to make — I have not done it.** Note that the required class is the *proceedings*
double-column macro, which is a different thing from `siamart`, the journal class 0a went looking for
and failed to find. That is why 0a's conclusion ("not installed here") was true but aimed at the wrong
file.

## Double-blind — confirmed, with two provisions that change what we do

> ACDA will employ a lightweight double-blind reviewing process for proceedings papers ... Proceedings
> submissions should not reveal the identity of the authors in any way. In particular, authors' names,
> affiliations, and email addresses should not appear at the beginning or in the body of the submission.
> Authors should ensure that any references to their own related work is in the third person.

But it is explicitly *lightweight*, and the page bounds it in both directions:

> The purpose of the double-blind reviewing is to help PC members and external reviewers come to an
> initial judgment about the paper without bias, not to make it impossible for them to discover the
> authors if they were to try. **Nothing should be done in the name of anonymity that weakens the
> submission or makes the job of reviewing the paper more difficult. In particular, important references
> should not be omitted or anonymized.**

**This settles 0b in the opposite direction from caution: cite arXiv:2012.00866.** It is an important
reference — it is the paper's own earlier version — and omitting it in the name of anonymity is what
the page tells us not to do. Cite it in the third person, as prior work by others would be cited.

And on the repository link:

> We strongly encourage also making code and data available; please keep in mind the double-blind nature
> of the review process. We recommend including a link to an anonymized version ... (e.g., using
> Anonymous Github or an anonymous Dropbox/Google Drive folder).

**So the GitHub URL should be replaced rather than deleted.** An anonymised mirror is encouraged, and for
a paper whose entire contribution is an implementation with 396 tests behind it, dropping the code link
would weaken the submission — exactly what the paragraph above forbids.

### What has to change in the tex

| tex | what | treatment |
| ---: | --- | --- |
| 173–210 | three `\author` blocks: names, `@northeastern.edu` emails, ORCID, institution, street address | remove for the review copy. Note this is the **PDF only** — EasyChair still collects real author metadata, and anonymising *that* would be a mistake |
| 177 | `\url{https://github.com/rchillyard/HuskySort}` in an `\authornote` | replace with an Anonymous GitHub link, per the recommendation above |
| 173–210 | the `\authornote`s describing the 2020 division of labour | out for the review copy regardless, which defers the question 0a raises about whether they still describe the work accurately |
| **736** | "We chose the name Husky Sort instead, after the husky, **Northeastern University's mascot, where this work was developed.**" | **the one that survives a front-matter-only pass.** Nine pages from any author block, mid-paragraph, and it names the institution outright |
| — | the name "HuskySort" throughout, plus the arXiv preprint | leave alone. The page permits preprints outright and disclaims any attempt at unbreakable anonymity. Renaming the algorithm would weaken the submission for no gain |

## Who to ask about what is left

The organizing committee co-chairs are **Paul Hovland** (Argonne) and **Henning Meyerhenke** (KIT). The
**program committee** co-chairs are **Gonzalo Navarro** (University of Chile) and **Yihan Sun** (UC
Riverside) — and the page directs double-blind questions specifically to them: "Authors with further
questions on double-blind reviewing are encouraged to contact the PC chairs." That is consistent with
the organizing chair's advice to Robin to direct further questions to one of the other names.

Nothing in this section now needs asking, though: the page answers all four questions that were open.

---


# 0b. The arXiv preprint and the prior rejection — RESOLVED by 0c; cite the preprint

The paper is **arXiv:2012.00866**, and that same version was **SIAM ACDA21 Submission 60, rejected**
(reviews verbatim in `paper/SIAM_ACDA21_Reviews.md`). So the preprint and the prior submission are the
same document, and this is a resubmission to the same conference series.

**The preprint is almost certainly not an obstacle.** arXiv is not a publication venue: no peer review,
no imprimatur, and SIAM's policies permit preprints. "Previously published" normally answers *no*.

**Update 2026-09-07: ACDA25 answered both of the questions below, favourably — see 0c.** It permitted
arXiv preprints in as many words, and it ran double-blind. So the preprint is not an obstacle, but the
anonymity requirement means that if we cite it, the citation must be third-person. Neither is confirmed
for ACDA27. The two questions as originally posed:

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

# 0a. The template — SUPERSEDED by 0c. Retained for what it records about the front matter

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
- ~~**The author footnotes describe the 2020 division of labour.**~~ **DELETED 2026-09-08** on Robin's
  instruction — they added nothing to the narrative, and they had become inaccurate: they credited
  Yunlu with "early benchmarking" when he has since produced every figure in the paper across six runs
  and some twenty-five hours of machine time, and credited Sai Vineeth with having "wrote the paper"
  when this year's rewriting is not his. Deleting them settles the question rather than answering it,
  which is the right outcome for a review copy that carries no author block at all. **Author order is
  still worth settling deliberately rather than by inheritance**, and that is untouched.
  No page effect: the SIAM conversion was already stripping `\authornote`, so they had never appeared
  in the measured build.

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

# 0d. Table `Guidance` — wording, after Robin's pass of 2026-09-07

Robin rewrote four cells and then asked what "consider array size, below" was supposed to mean. It was
mine, and it was bad twice over.

**What it was for.** It replaced "see Figure~\ref{fig:usecase}" when that figure was dropped, and it was
meant to send the reader to the crossover measurements in the prose that follows the table.

**Why it failed.** "Below" is a *positional* reference from inside a float. `table*` floats to the top of
whatever page has room, so it can end up above, beside, or a page away from the prose it points at —
the very thing that had just happened to Figure 5, which is what made the figure unaffordable. A float
cannot say "below" about anything.

Replaced with `by array size (\S~\ref{sec:usecase})`, which is resolvable wherever the table lands. The
mild oddity of a table citing the section it sits in is the right trade: if it drifts, the number still
finds the prose.

**One other cell of mine was wrong, not just vague.** The specialised-sort row read "a tuned MSD radix
sort beats us, though **ours** indexes extended ASCII only". "Us" means HuskySort and "ours" means our
MSD — the referent flips mid-sentence, and the reading a reviewer would reach first is that HuskySort is
the extended-ASCII-only one, which inverts the concession. Now "though **our MSD** indexes extended
ASCII only".

## Still open in that table — all yours, none urgent

| where | issue |
| --- | --- |
| caption | says "The questions are ordered by how much they decide", but the first column is headed "Your situation" and its rows are situations, not questions. One of the two words should change |
| column head | "Your situation" is the only second-person address in the paper, which otherwise says "we" throughout. Defensible in a guidance table, but it is a deliberate register change and reads as one |
| the cleanup-pass row | its `\S~\ref{sec:radix-results}` is the **dangling reference of 5.2** — that section does not contain the 10--25\% figure. Fixing 5.2 fixes this cell; nothing to do here separately |
| last row, Evidence | "each sort's fixed setup cost is repaid at a different $N$" is a *reason*, where every other row in that column gives a measured *fact*. The honest fact is "crossovers measured on English text, $N=4$ to $10{,}000$", which is four characters too long to fit — see the note on the page budget below |

## What the page budget cost this table — kept as a record, no longer a constraint

Robin's decision later the same day was to fix content first and squeeze afterwards, so none of the
below should stop an edit that makes the table clearer. It is worth keeping only because it shows how
little slack there is, and therefore how much the eventual squeeze will have to find. Measured in order:

| version of the last two rows | body |
| --- | ---: |
| before this pass | 12.00 |
| naming all four crossovers in the row | 12.20, and 15 pages total |
| naming the one $N\approx2{,}000$ threshold | 12.12 |
| `(\S~\ref{sec:usecase})`, with "the implementation we tested" | 12.05 |
| `(\S~\ref{sec:usecase})`, with "our MSD" | **12.00** |

Twenty-four characters in the MSD cell was worth 0.05 of a page, because it wrapped that cell to a
further line. When the squeeze does come, measure it by the method in 0c: last body float, then check
the acknowledgments start at the top text margin of the next page.

---

# 0e. The abstract's cleanup-pass range — RESOLVED 2026-09-07, twice over

The abstract (tex 251–254) says:

> measured on input where that pass provably has nothing to correct,
> it nonetheless accounts for **a tenth to a quarter** of total running time.

That is exactly right for `permits.json`, whose figures are 9.8% and 25.3% at the ends. It is **not
supported by the second run**, where the smallest size gives 6.2% — a sixteenth, not a tenth. The body
as now written takes the conservative reading and says "under a tenth of total running time at the
smallest size measured and between a fifth and a quarter at the two larger ones".

**So the abstract and the body no longer say quite the same thing**, and a referee who checks the one
against the other will see it. Three ways out:

1. **"as much as a quarter"** — true under both runs, keeps the force of the claim, one-line change.
   This is what I would do.
2. **"a fifth to a quarter at the sizes where it matters"** — more precise, slightly more hedged, and
   matches the body's own wording and Table `Guidance`'s.
3. **Leave it.** Defensible: the abstract quotes the dedicated permits run, which is the measurement
   designed for this, and the body discloses the second run honestly. But it means the paper's most
   distinctive claim is stated at its most flattering.

**Both changes were made.** Sai Vineeth takes the abstract from the branch tip, so it had to be right
here rather than negotiated separately. First "a tenth to a quarter" became "as much as a quarter", to
scope it to what both runs support. Then Robin's question about the denominator showed a quarter was
itself the wrong reading, and it became **"as much as a fifth"** — see the denominator note in 5.2.

Robin also asked whether the share should not *diminish* with $N$, the pass being linear where the sort
is linearithmic. It should, and §sec:pcrit now says so and explains why the measured range does not show
it: $k_3$ grows almost sevenfold over these three sizes while $\ln N$ grows by a fifth, so the cache
effect swamps the logarithm. The extrapolation, with $k_3$ held at its settled value, gives some 18% at
$N=10^6$ and 15% at $N=10^7$ — a real but logarithmic decline. Flagged in the paper as a model rather
than a measurement, since the sort's own per-element cost also grows faster than $\ln N$ here and the
corpus caps at 198,900 records.

---

# 0f. `sample-base.bib` — **RECONSTRUCTED 2026-09-07**, and it was blocking the template switch

Found while adding the self-citation, and it is a submission risk rather than a tidiness problem.

`HuskySort.tex` line 1572 says `\bibliography{sample-base}`. **There is no `sample-base.bib`** —
not in `paper/`, not anywhere in the repository, and not in the TeX tree. `HuskySort.blg` records the
last BibTeX run failing outright:

> I couldn't open database file sample-base.bib ... I found no database files

What the paper builds from is `HuskySort.bbl`, a generated artefact that is **committed to git** and is
now the only copy of the bibliography in existence. It holds 19 entries; all 19 citations resolve; the
PDF's references are correct. Nothing is broken *today*.

**But it is one command away from being unrecoverable.** Anyone who runs `bibtex HuskySort` — or a
`make` target, or an IDE build, or Overleaf's default pipeline — overwrites the `.bbl` with an empty
bibliography and the references vanish. There is no `.bib` to regenerate them from.

This lands directly on the critical path, because **the SIAM template switch (0c) requires a rebuild**,
and the natural way to do that is latex/bibtex/latex/latex.

## Done, and it turned out to be a hard prerequisite rather than hygiene

Reconstructed by transcription from the `.bbl`; BibTeX now runs, regenerates all 19 entries, and every
citation resolves. Keys before and after are identical, so nothing was lost or invented.

**It was blocking the SIAM template switch, which was not obvious until tested.** The `.bbl` produced by
`ACM-Reference-Format.bst` self-guards nine `\show*` macros but *not* `\bibfield`, `\bibinfo`,
`\natexlab`, `\showeprint` or `\urldef` — those come from acmart itself. Compiling that `.bbl` under
`\documentclass{article}` gives twelve undefined control sequences. So changing document class required
regenerating the bibliography, which required a `.bib`, which did not exist. The two items in 0c and 0f
were one item.

**Two entries came back thinner than the others**, and both are marked `INCOMPLETE` in the file, because
the `.bbl` never carried the fields and guessing them would be worse than flagging them:

- `doi:10.1137/1.9781611975994.101` — Jugé, *Adaptive Shivers Sort*. No year, host booktitle or
  publisher. It is SODA 2020.
- `sort` — Zhang, Meng and Liang, *Sort Race*. Title and eprint only, and the eprint type was misspelled
  `arxi` in the `.bbl`, which is why it renders without an arXiv label. Corrected to `arxiv` in the
  `.bib`, so that entry now displays properly.

Both were completed on 2026-09-07: Jugé from the DOI, which resolves to the SODA 2020 proceedings
volume (Thirty-First Annual, pages 1639–1654), and *Sort Race* from its arXiv id with the eprint type
corrected. BibTeX no longer reports an empty year, booktitle or publisher for either, and the "[n.d.]"
that had been printing against Jugé is gone.

---

# 0g. There is no general composite-key coder — and the paper implies there is one

Robin asked on 2026-09-07 whether the code has a method to husky-encode a general tuple: a set of
values from an element's primary key, as with the permit records. **It does not.** The inventory:

| what exists | where |
| --- | --- |
| `PermitCoder` — block/lot/date packed into 60 bits, and the only place that gets all three order-preserving properties right and names them | `huskySortUtils/PermitCoder.java` |
| `HuskySortBenchmark.Tuple.huskyCode()` — birthYear/zip/name, hand-rolled with literal shifts `17` and `38` and a hand-written mask that silently truncates the name's code | `huskySort/HuskySortBenchmark.java:614` |
| `HuskyCoderFactory.createGenericCoder()` — **an adapter, not a composer.** It is `HuskySortable::huskyCode`; it lifts a hand-written code into a `HuskyCoder` and does no composition at all | `huskySortUtils/HuskyCoderFactory.java:52` |

Everything else in `HuskyCoderFactory` is single-valued. `HuskyCoder` has no combinator — no `andThen`,
`compose` or `withBits`. `HuskySortable` is two methods, and composition is left entirely to the
implementor. There is no bit-width allocator: nothing takes a list of field widths and derives shifts
and masks, and **nothing checks that the total fits in 64 bits.** `Tuple`'s 8 + 17 + 38 = 63 fits by
hand-checked luck. `PermitCoder`'s 60-bit budget is verified by prose arithmetic in a class comment
plus a corpus test, not by code.

The one reusable-shaped primitive is `PermitCoder.encodeString(String, int width, int bits, String
alphabet)`, which is `private static`, holds no permit-specific state, and would need only a visibility
change and a home. Its companion `codeOf` implements the property that matters — an out-of-alphabet
character collapses to the largest symbol at or below it, so the ordering *weakens* rather than
inverting.

## Why this is a paper problem and not only a code one

Table `Guidance`'s second row tells a practitioner to reach for RadixHuskySort when "the ordering is
composite or expensive, \emph{and} the key packs exactly into 64 bits". The paper does not say how one
would obtain such a packing, and the honest answer is: by hand, with shifts and masks, getting three
non-obvious properties right — fields packed most-significant-first in the same order as `compareTo`;
codes running from 1 up with 0 reserved so that a short field right-pads and sorts low; and
out-of-range symbols collapsing downward rather than wrapping. `PermitCoder` documents all three
because getting any of them wrong silently produces a coder that inverts instead of weakening.

## Robin's framing, 2026-09-07, which is the one to write up

> it's not essential for the paper, but if the method ever got taken up (by Python or Java, etc.) we'd
> need to provide something to make it easy/automatic to encode.

That is the right way round, and it makes this a **future-work** item rather than a limitation to
apologise for. The claim the paper makes is about a mechanism; the claim this would support is about
adoption, and adoption is what a library vendor would have to be persuaded of. Worth separating the two
words, because they are different pieces of work:

**Easy** is a combinator, and it is small. A field abstraction (`X -> long` plus a declared bit width),
an ordered-alphabet string field promoted out of `PermitCoder` (its `encodeString`/`codeOf` need only a
visibility change), an MSB-first fold accumulating shifts, a 64-bit budget check that fails fast instead
of truncating silently, and a `perfect()` that is true only when every constituent field is exact and
the total fits. `PermitCoder` has already solved every hard part; this is packaging.

**Automatic** is the interesting one, and it is where the argument for adoption actually lives. The
whole difficulty of hand-writing a coder is keeping two declarations in agreement: the packing order
must match `compareTo`, and nothing checks that it does.

> **A correction, since it bears directly on the API.** Robin's reading on 2026-09-07 was that field
> order does not matter when the encoding is perfect, and matters when it is imperfect. **It is the
> other way round.**
>
> `perfect()` is documented as "true if the resulting longs are perfect for ANY value of X" — which is
> a claim of order preservation *with respect to that type's own `compareTo`*, not a claim of
> losslessness. Pack `Permit` as date/block/lot rather than block/lot/date and every record still gets
> a distinct code; nothing is lost and all three fields could be decoded back. But those codes are
> monotone in date-major order while `compareTo` is block-major, so sorting by them yields the wrong
> order, `perfect()` returns true, **step 3 is skipped, and the sort silently returns wrong output.**
>
> Make the same mistake with an imperfect coder and the cleanup pass runs, Timsort re-sorts by the real
> `compareTo`, and the answer is **correct** — merely slow, because $p$ is now enormous and step 2 did
> no useful work. The array reaching step 3 is badly permuted rather than nearly sorted, so $T_3$ goes
> from the $N-1$ comparisons measured in §sec:pcrit to something approaching a full Timsort.
>
> So: **imperfect encoding, wrong field order costs time; perfect encoding, it costs correctness**, and
> nothing downstream can catch it. `PermitCoder`'s class comment states this as the first of its three
> properties, and `PermitCoderTest` pins it with "a later block outranks any lot" and "a later lot
> outranks any date" — those tests exist because the property is not self-enforcing.
>
> Robin's intuition is right about **cost**: when the encoding is perfect, field order does not affect
> performance at all. Any packing is a 64-bit long, radix runs the same fixed passes, no cleanup runs.
> Order-independence holds for time and fails for correctness.
>
> **This is the strongest argument for declaring the key with the tuple.** Today
> `PermitCoder.perfect()` returns a hardcoded `true`, and what makes that trustworthy is a test
> sweeping all 198,900 records. If field order and widths came from the same declaration as the
> comparison, `perfect()` could be **computed** — true exactly when every field encoder is exact and
> the widths fit — rather than asserted and separately verified. That converts the mechanism's central
> safety property from a promise into a derivation. But in both languages named, the comparison is
very often *already* derived from field declaration order — Java records and `Comparator.comparing(...)
.thenComparing(...)`, Python dataclasses with `order=True`. Where that holds, the encoding can be
derived from the same declaration as the comparison, and **the correctness obligation is discharged by
construction rather than by hand.** That is a materially stronger statement than "we provide a helper".

Two honest obstacles, both worth naming rather than glossing:

- **Automation needs each field's range, not its type.** An `int` field needs 32 bits unless the domain
  is known to be narrower, and it is the narrowness that makes composite keys fit at all: permits works
  because block is 5 characters of a 31-symbol alphabet, not because it is a `String`. So either fields
  carry declared ranges (an annotation, a dataclass field argument) or the range is inferred from the
  data. **There is precedent for the latter in this repository**: `Alphabet.prepare(String[])` derives
  its alphabet by sweeping the corpus and assigning code points in order, which is exactly the shape of
  the inference required.
- **64 bits runs out fast.** Three fields fit; six probably do not. This is where the paper's existing
  passage on key widths (tex 566--577) stops being a throwaway remark and becomes load-bearing: radix
  sort takes a $k$-bit key in $k/b$ passes with no change to the algorithm, so a 128-bit code costs
  twice the passes and nothing else. A comparison-based husky sort has no equivalent escape. **An
  automatic encoder is therefore a better argument for RadixHuskySort specifically than for
  QuickHuskySort**, which is a point the paper is currently in a position to make and does not.

### Suggested treatment: a short future-work paragraph, not new code

Draft, to sit at the end of §sec:radix where the key-width discussion already is:

```latex
One practical obstacle stands between this mechanism and a library implementation of it.
Each composite encoding in this paper was written by hand,
and a hand-written coder must satisfy three properties that nothing checks:
fields packed most-significant-first in the same order as the type's own comparison,
symbol codes assigned from one upwards so that a short field pads with zero and sorts low,
and out-of-range symbols collapsing downward, so that an unexpected value weakens the ordering rather than inverting it.
Getting any of these wrong produces a coder that is silently incorrect rather than merely imprecise.
We expect that most of this can be automated rather than merely assisted.
Where a type's ordering is already derived from its field declarations ---
as with Java records, or Python dataclasses declared with an ordering ---
the encoding can be derived from the same declaration,
which removes by construction the possibility that packing order and comparison order disagree.
What such a derivation additionally requires is each field's range rather than its type,
since it is the narrowness of the fields that lets a composite key fit at all;
that is either declared or inferred from the data.
We note that this argues specifically for the radix variant:
when the fields do not fit in 64 bits, a wider code costs RadixHuskySort proportionally more passes and nothing else,
where a comparison-based husky sort has no comparable escape.
```

Cost: about fifteen lines, no new code, and it converts the weakest thing about the mechanism into the
clearest statement of what adopting it would take.

**Applied 2026-09-07 as appendix §A.4**, `\label{sec:composite-future}`, placed directly after the
permits case study since that is the paper's one worked example of a hand-written composite coder.
Body page count 12.51, essentially unchanged, confirming the appendix is genuinely free.

> **It was initially unfindable, which Robin caught.** Two faults, both mine. The heading read
> "Composite keys, and what a library would have to provide" and the text never used the phrase
> *future work* anywhere, so searching the PDF for it returned only the burstsort line in §sec:radix.
> And unlike every other appendix section it had **no inbound reference from the body** — §A.2 is cited
> from §sec:pcrit and §A.3 from §sec:radix-results, but §A.4 could only be reached by reading the
> appendix through. Retitled to "Future work: composite keys, and what a library would have to
> provide", the opening now says it is future work, and §sec:radix-results points at it alongside its
> existing pointer to §A.3.

It absorbed the `perfect()` correction as its middle paragraph, which is the part worth having: field
order matters *most* where the encoding is at its best, because a perfect encoding skips the cleanup
pass and so has nothing downstream to catch a mismatch, where the same mistake in an imperfect one is
merely slow.

**One trade-off to be aware of.** ACDA27 does not print the appendix — the program committee reads it
at its discretion. So this argument reaches referees but not the published proceedings. That is the
right call for future work, but the last paragraph is arguably more than future work: that a wider key
costs RadixHuskySort proportionally more passes and nothing else, where a comparison-based husky sort
has no escape, is an argument for the radix variant over the quicksort one. If the reflow under the
SIAM class leaves room, that sentence is the one worth promoting into \S~\ref{sec:radix}.

---

# 0h. PR #64 — both blockers answered, and one of them against us

Yunlu reran **all seven requests at one checkout** (tip `6dd4ef9`), 31.4 h, every JMH row Cnt=50, with
an external correctness harness (37/37) run before any timing was trusted. Results doc:
`doc/Run results from Yunlu 2026-09-06.md`. **Not yet merged.**

He also corrects the request doc: the last `src/`-touching commit is `e92610f` (HS-13), not `5ed60a0`.
`e92610f` is what the paper should record.

## Request 6 — the chinesenames corpus is transformed

Against `systemSortPinyin`, the baseline doing the same job, radix/16 wins **3.03x / 3.62x / 4.05x** at
32k / 200k / 1M (3119.9 ms against 770.3 at 1M), and **the margin grows with $n$**, which is exactly
the premise: extract the key once per element rather than $\log n$ times per element in comparisons.
With HS-12 the encoder is cheap enough that radix/16 at 1M (770.3) now beats even the *unfair*
code-point `systemSort` (799.8).

Byte-identical output was verified across `systemSortPinyin`, `quickHuskySort`, `radixHuskySort16` and
pinyin multikey; plain `systemSort` differs at **200,000 of 200,000 positions**, which settles the
"different and cheaper problem" claim with a number.

**The corpus flips from the paper's weakest result to its clearest demonstration.** Note that the
`RadixImprovements` row is separately still the table's smallest margin (1.63x, now a legitimate
same-task figure), so §1.5's framing survives intact — the two facts are about different comparisons
and both belong.

One flag from Yunlu, correctly raised against the falsifier in the request: `huskyEncodeOnly` at 1M
went 408.4 to 211.1 ms, about half, not the 5.7x measured per character. That is expected — the
benchmark includes array traversal and allocation, which HS-12 does not touch — but it should be
stated as ~2x end-to-end rather than 5.7x anywhere the paper quotes it.

## Request 7 — the pre-registered falsifier fired. The guard is right; the appendix is wrong

| fixedHighBits | guarded (09-06) | unguarded (d3c359f) |
| ---: | ---: | --- |
| 0 | 335.9 | 335.7 |
| 48 | 333.3 | 354.6 |
| 56 | **717.2** | **7097.7** |
| 60 | 805.5 | StackOverflowError |
| 63 | **186.9** | StackOverflowError |

124/124 combinations, zero crashes. But the 21x degradation at 56 became 2.1x, and at 63 the baseline
is *faster than its own fhb=0 case*. Yunlu concluded the guard engages too early and needs re-tuning.

**It does not. Two things were checked:**

**1. The guard matches the JDK exactly.** JDK 21 has `DELTA = 3 << 1` (6) and
`MAX_RECURSION_DEPTH = 64 * DELTA` (384), and its recursion does `bits += DELTA` — so its effective
limit is 384/6 = **64 levels**, which is what `PureDualPivotQuicksort` now uses. Verified against
`src.zip`, not from memory.

**2. The guard is nevertheless the whole cause**, and the pathology behind it is far worse than the
paper ever claimed. Timing the same generator and seed locally at $n=10^6$, varying only the depth
limit (single-shot, no JMH, so read the ratios and not the absolutes):

| fixedHighBits | d=64 | d=384 | d=4096 | d=100,000 |
| ---: | ---: | ---: | ---: | ---: |
| 0 | 606 ms | 581 | 485 | 402 |
| 48 | 203 | 149 | 253 | 153 |
| 56 | 464 | 1651 | 4480 | 4172 |
| 60 | 617 | 3718 | 32261 | **98129** |
| 63 | 294 | 1762 | 17314 | **363411** |

The sweep completed after this was first written, and the last cell is the most extreme of the lot: at
**63 fixed bits an unbounded run takes 363 seconds against 294 ms guarded — a factor of 1236** — and
against its own fhb=0 baseline at the same depth limit (402 ms), a **904x degradation**. At 60 fixed
bits the figures are 98 seconds against 0.6 s.

The quadratic behaviour is therefore real, catastrophic, and entirely masked by the guard, which
converts it into heapsort almost immediately on duplicate-heavy partitions. Note the direction: the
*worst* case is 63, the most degenerate input, which is what one would expect and which the guarded
column inverts completely — 186.9 ms, the fastest cell in Yunlu's table. That inversion is the clearest
single piece of evidence that what the guarded column measures is heapsort, not quicksort.

## What this means for the appendix — a better story, not a lost one

The old text said a naive baseline degrades by an order of magnitude and then crashes. That was true of
*our* unguarded copy of 2011 code, and a referee could fairly call it a straw man. The three findings
now available are stronger, and all three are measured:

1. **The pathology is real and catastrophic.** Unguarded, 56 fixed bits costs 21x and 60 or 63
   exhausts the stack outright; locally, with depth unbounded, 63 costs 904x.
2. **A JDK-equivalent guard removes it — by abandoning quicksort.** 64 levels is what the JDK ships,
   and on this input it means heapsort does most of the work: 2.1x rather than 21x, and no crash. What
   you get is not a dual-pivot quicksort that copes; it is a sort that detects it cannot cope and
   changes algorithm.
3. **Radix needs no such provision.** Its cost is flat across the whole sweep because it does not
   depend on the distribution at all, which is the only one of the three properties that is a property
   of the algorithm rather than of a safety net bolted to it.

**No further run is needed.** Both columns of the table above are already measured on the machine of
record — the unguarded numbers from the d3c359f run merged in PR #63, the guarded ones from PR #64 —
so the appendix can show the pathology and its mitigation side by side without asking Yunlu for
anything.

## What else PR #64 changes, and it is not small

- **All five results tables must be rebuilt again.** `full-suite.json` is refreshed (415 rows) and
  supersedes the version every current table was built from. Rows shared with the dedicated runs agree
  within ~2%, so nothing will move much, but the paper must not quote two suites.
- **MSD at 1M is now a dead heat.** 271.9 against 271.5, i.e. **1.00x**, after 1.09x and 1.05x. The
  paper says "MSD is faster by 1.34x at 200,000 and by 1.09x at 1,000,000"; the honest statement is now
  MSD ahead by 1.17x at 200,000 and level at 1M, with the 1M margin spanning 1.00-1.09x across three
  runs. **This strengthens the paper** and the concession should be rescoped, not deleted.
- **The cleanup pass moved again, and my correction of an hour ago over-corrected.** This run gives
  8.7% / 25.1% / 15.7% as a share of total (9.5 / 33.6 / 18.6 as overhead added). The maximum share of
  the total across all four runs is now **25.1%**, so the abstract's **"as much as a quarter" was right
  after all** and should go back; "as much as a fifth" is now too weak. Table `Guidance` should read a
  sixth to a quarter. The mid-peaking shape reproduced, which is now two runs for the peak and two for
  monotone growth — the paper's "no reproducible shape" wording holds and is if anything better
  supported.
- **Permits improved**: 4.83x over system sort and 2.32x over QuickHuskySort at the full corpus, from
  4.77x and 2.06x.
- **Crossovers unchanged**: the ladder and the ~167 µs radix setup floor both reproduce exactly.
- **Environment drift**: kernel now 6.12.103 after a 09-03 reboot; swap now 8 GiB zram plus a 32 GiB
  file, 0 B used during runs. Table `SysEnvAWS` carries neither field, so nothing needs changing, but
  §5.4's note should record it.

---

# 0i. The repository link — an anonymous mirror would not be anonymous

ACDA27 does not merely permit a code link, it encourages one, and tells us how:

> We strongly encourage also making code and data available; please keep in mind the double-blind
> nature of the review process. We recommend including a link to an anonymized version that makes a
> 'best-effort' to avoid revealing the identity of the authors (e.g., using Anonymous Github or an
> anonymous Dropbox/Google Drive folder).

So the `\url{https://github.com/rchillyard/HuskySort}` at tex 206 should be **replaced, not deleted** —
deleting it would weaken a submission whose contribution is an implementation, which the same page
tells us not to do. But pointing Anonymous GitHub at this repository as it stands would not achieve
anonymity, and it is worth knowing why before anyone sets one up.

## Fixed on 2026-09-07

| where | what it was |
| --- | --- |
| `HuskySortTest.java:82` | `new Person("Robin", "Hillyard")` and five more family members, plus `new Person("Yunlu", "Liao Zheng")`. A signature, not a hint. The test only asserts `sorted()`, never a specific order, so the names were free to change; replaced with synthetic ones preserving both properties the data exercised — a surname shared by all but one element, and one surname containing a space |
| `InsertionSortCorrectnessTest.java:17` | a comment naming Robin |
| `StringSortBenchmarks.java:196` | a comment naming Robin, which I wrote |

`grep` over `src/**/*.java` for the four identifying strings now returns nothing. Full suite green at
396 tests. The corpus files under `src/main/resources` match "robin" but that is the English word in
Leipzig sentences, not a leak.

## The one that cannot be fixed cheaply

**Every source file is under `package edu.neu.coe.huskySort`** — `neu` and `coe` being Northeastern and
its College of Engineering. That is 165 Java files, the directory tree itself, and every import
statement. Anonymous GitHub rewrites the repository's *name and owner*; it does not rewrite file
contents or paths.

That is a hint rather than a signature — a reader must already know the mapping — and the page is
explicit that the bar is best-effort and that the purpose "is not to make it impossible for them to
discover the authors if they were to try". So it is arguably tolerable. Renaming the package across
165 files is mechanical but touches every file in the repository, and is not something to do eight days
out.

## What a mirror would have to exclude

Anonymous GitHub takes an exclusion list. These paths name Robin, or Northeastern, or both, throughout —
the working documents of this revision are the worst offenders, since they are written in terms of what
Robin asked for:

`paper/` (the whole directory — it contains the paper itself, author block included, plus
`Paper edits pending.md` and `Paper deletions.md`), `doc/` (the run requests and results, which address
Yunlu by name and quote Robin's decisions, and `Audit against INFO6205.md`, moved there on 2026-09-10
when the near-empty `docs/` was retired), `logs/`, `TODO.md`, `.idea/`, `dependency-reduced-pom.xml`.

What remains after those exclusions — `src/` and `pom.xml` — is the code the paper is actually about,
and is clean apart from the package name.

## Decided 2026-09-07: Anonymous GitHub over `src/` only

Accepting the package name, which is proportionate to a best-effort standard. Assigned to Sai Vineeth
as task 2 of [Tasks for Sai Vineeth.md](../doc/Tasks%20for%20Sai%20Vineeth.md), together with the
exclusion list; creating the mirror needs an account and publishes something, so it is not mine to do.
The tex change at line 206 is one line once the URL exists.

**The package rename is a separate, optional task 3** — Robin's view being that there is no good reason
to carry that much identifying information in package names regardless of anonymity. Scope measured:
164 Java files for `package edu.neu.coe.huskySort`, plus `<groupId>edu.neu.coe.seis</groupId>` at
`pom.xml:10`, with nothing in resources or config. **The two are separate identifiers** — the groupId
ends `.seis`, not `.huskySort` — so an IDE package refactor, or a find-and-replace on the package name,
will miss it. To be done only if tasks 1
and 2 are complete and the paper is otherwise ready, and never in the same commit as anything else.

---

# 0j. Why the squeeze must wait for the SIAM class — measured, 2026-09-08

Four trims were applied and measured individually. Three helped, one hurt badly, and removing a whole
table hurt too. **The metric is dominated by float placement, not by content length**, so trimming
under acmart is not converging on anything and would not transfer anyway.

The measurement used throughout is where `ACKNOWLEDGMENTS` starts on page 13, the body running onto
that page at all being what puts us over twelve. Lower is better; 10% is the top text margin, i.e.
success.

| state | ACKS on p13 |
| --- | ---: |
| before this pass | 55.8% |
| §Data Source trimmed, Amdahl passage fixed, conclusion bullets to prose | **40.7%** |
| …plus Table `Guidance`'s caption shortened | **39.4%** |
| …plus §sec:usecase's two restating passages compressed | 83.0% |
| A/B/C + caption, but with Table `Guidance` deleted outright | 55.7% |

The last two rows are the point. Compressing five lines of §sec:usecase cost **42 percentage points**,
and deleting a six-row full-width table cost **16**. Removing content made the overflow worse in both
cases, because LaTeX re-deferred floats and text redistributed across pages 12 and 13 in ways that had
nothing to do with how much text there was.

**Kept** (rows 2 and 3 above): they are improvements on their own merits, not just page count.

- §Data Source's permits paragraph was six lines duplicating §A.3, which now carries the case study.
- The parallel-scaling passage **contradicted itself**: it said "the explanation we favour is bandwidth
  rather than scheduling", then two sentences later "this points instead to a property of the design
  itself", endorsing both. It now names both, says we cannot separate them without hardware counters,
  and observes that only the design one would survive better hardware.
- The conclusion's three-bullet `itemize` restated §sec:summary; it is now one sentence.
- Table `Guidance`'s caption said "The questions are ordered by…" while its first column is headed
  "Your situation". Shortening it reconciled that mismatch, which 0d had recorded separately.

**Reverted, and worth keeping for later**: the §sec:usecase compression. The prose change is good — it
removes a genuine restatement — but it costs 42 points under this class. Retry it after the switch.

```latex
Every one of these crossovers has the same shape --- each successive algorithm carries a larger fixed
setup cost, repaid only once there is enough work to amortize it against --- which is the story
\S~\ref{sec:parallel-radix} finds again one level down, in thread and barrier setup.
They were measured for String keys only; the costs involved are architectural rather than
type-specific, so other types plausibly behave similarly, but we have not confirmed it.
```

## What to do instead

Nothing, until `siamproceedings.sty` lands and the paper builds under it. Then measure once and cut against
that layout. The structural levers, in the order they cost the argument least, are: move Table
`Guidance` to the appendix and keep §sec:usecase's prose in the body, which the prose supports since it
already states every crossover; move the whole of §sec:usecase to the appendix, which is guidance
rather than results; or drop Table `ParallelRadix`, which is two rows and whose figures the surrounding
prose quotes in full anyway.

---

# 0k. Front-matter anonymisation — decided 2026-09-08

> Numbering note: I referred to this as "0e" in conversation, which was wrong — 0e is the abstract's
> cleanup-pass range. Anonymisation had been a line of 0c; it is split out here because the repository
> link half of it grew into 0i.

## Reviewers do see the front matter

The PDF uploaded to EasyChair is what reviewers download and read; nothing strips page one. ACDA27
names this as the place it cares about:

> authors' names, affiliations, and email addresses should not appear **at the beginning** or in the
> body of the submission

## The ORCIDs go too, and this was the question worth asking

Robin asked whether the ORCIDs could stay, on the reasoning that a reviewer would have to actively look
up the name. **They resolve.** `orcid.org/0000-0001-9734-7358` returns a public page carrying the
person's name, affiliation and publication list — so it is not a hint requiring a search, it is the
name one click away.

Strictly an ORCID is not a name, an affiliation or an email address, so it falls outside the letter of
the rule while sitting squarely inside its purpose. ACDA's bar is a "best-effort" and a resolvable
person-identifier does not meet it.

**Keeping only the ORCIDs would be the worst of the options**, and not merely on a technicality. An
ORCID's whole function is to disambiguate a *named* author for indexing; with no name printed there is
nothing to disambiguate, so it delivers no benefit to a review copy while carrying nearly all of the
risk. To a reviewer who notices, it reads worse than either clean anonymity or full disclosure —
anonymity with a back door left open.

It would also be arbitrary, because of an accident in the current front matter. **Only one of the three
ORCIDs is actually printed:**

| macro | value | used? |
| --- | --- | --- |
| `\orcidauthorA` (tex 174) | 0000-0001-5446-5645 | **no** — the `\orcidA{}` usage at tex 201 is commented out |
| `\orcidauthorB` (tex 175) | 0000-0002-5538-8378 | **no** — declared and never referenced |
| `\orcidauthorC` (tex 176) | 0000-0001-9734-7358 | **twice**, at tex 233 and 242 — and 242 is inside `\affiliation{}`, which is a misuse |

So "keep just the ORCIDs" would identify one author and not the other two.

## Decision

Names, emails, affiliations and ORCIDs all come out of the review copy. **Keep the whole block
commented out immediately above the anonymous version**, so camera-ready is a one-step restore rather
than a retyping job from memory.

Two pre-existing defects to fix when that block is restored, not before — they are wrong in the
camera-ready either way, and fixing them now would only be undone by the class switch:

1. Authors A and B have ORCIDs declared but never printed. Either print all three or none.
2. Author C's ORCID appears twice, and the second is inside `\affiliation{}` rather than alongside
   `\author`.

## The contingency, which matters while we wait on the template

**acmart has an `anonymous` class option** (`acmart.cls:131`). If `siamproceedings.sty` does not
arrive in time, `\documentclass[acmtog,anonymous]{acmart}` suppresses the author block in a single
word. So anonymity is never what blocks the submission, even in the worst case — and that is worth
knowing before anyone starts hand-deleting the front matter under time pressure.

The SIAM package has no equivalent option, so under it this is manual.

---

# 0l. The SIAM conversion — it builds, and the paper is four and a half pages over

**Updated 2026-09-08 with the complete macro package**, which Robin found and downloaded to
`paper/siamproceedingsmacros_022425/`. That supplied the three things the loose `.sty` had not:
`siamplain.bst`, `example_doublecolumn.tex`, and `example_doublecolumn.pdf`. The trial was rebuilt
against SIAM's own preamble and bibliography style; the figures below are from that build, and they
match the first attempt, so the number is solid.

The two copies of `siamproceedings.sty` were **identical apart from line endings** — the package copy
CRLF, the loose one LF — so the loose `paper/siamproceedings.sty` was deleted on 2026-09-08. The single
authoritative copy is now `paper/siamproceedingsmacros_022425/siamproceedings.sty`.

**One consequence to handle at switch time.** `acmart.cls` sits directly in `paper/`, which is why the
paper builds there with no path configuration. `siamproceedings.sty` and `siamplain.bst` are now one
directory down, so a build of `paper/HuskySort.tex` will not find them. Either copy those two files up
into `paper/` when the switch is made, or put the package directory on `TEXINPUTS` and `BSTINPUTS`.
Copying two files up is the simpler answer and keeps the package folder intact as the pristine
download.

`paper/ltexpprt.all`, the superseded package, was deleted on 2026-09-08. It was referenced by nothing
and contained only `ltexpprt.tex` and `ltexpprt.sty` concatenated — no `.bst`, which is what sent us
looking further in the first place.

## SIAM's own preamble, which the trial now uses verbatim

```latex
\documentclass[twoside,leqno,twocolumn]{article}
\usepackage[letterpaper]{geometry}
\usepackage{siamproceedings}
\usepackage[T1]{fontenc}
\usepackage{amsfonts}
\usepackage{graphicx}
\usepackage{epstopdf}
\usepackage{enumitem}
```

`twoside` and `leqno` were missing from my first guess of `[twocolumn]`.

## The style restrictions, verbatim, and two of them bind on us

From `example_doublecolumn.tex`:

> 2. **Do not change the margins or page size! Do not change from the default text font or the default
> text size of 10pt!**

**That closes off the only remaining hope.** There is no denser configuration to reach for; the layout
is the layout, and 16.5 pages is what the paper is.

> 5. No running heads are to be used.
>
> 8. Page numbering is not included in this macro since pagination will be set by the program committee.

The second of those affects the draft revision stamp, which lives in the page footer alongside
`\thepage`. Under this package there is no page number and no footer to join. Setting
`\draftstampfalse` before submission was already the plan (0k), so this only means the stamp cannot be
carried into the SIAM build at all rather than merely being switched off.

> 3. We recommend that you use BibTeX and siamplain.bst … If you do use BibTeX, please supply your bib
> or bbl file with the manuscript file.

So `sample-base.bib` ships with the submission. Another reason yesterday's reconstruction mattered.

---

## The original trial write-up follows, 2026-09-08

Robin downloaded `siamproceedings.sty` into `paper/`, along with `ltexpprt.all`, the superseded
package, since deleted. I converted the paper in the scratchpad, leaving `paper/HuskySort.tex`
untouched. The working trial is kept as
`doc/HuskySort-siam-trial.tex` (removed 2026-09-11 once the class switch of §0c made it redundant;
recoverable from git history if ever wanted) — it is **not** the paper, it is a proof that the
conversion works and a
record of what it took.

## It builds: zero errors, zero undefined references

`\documentclass[twocolumn]{article}` plus `\usepackage{siamproceedings}`, two-column, letter. Nine
changes were needed, and the first is the one nobody would guess.

| # | change | why |
| ---: | --- | --- |
| 1 | shim `\AddToHook` to a no-op | **`siamproceedings.sty` is newer than this machine's LaTeX.** It calls `\AddToHook`, added to the kernel in 2020-10-01; TeX Live 2020 here has kernel 2020-02-02. Ten uses, all `\crefalias` for theorem environments this paper does not contain, so discarding them costs nothing — but on an unpatched TL2020 the build dies with 844 errors |
| 2 | `algorithm2e` gains the `algo2e` option; six `algorithm` environments renamed to `algorithm2e` | `siamproceedings` requires the `algorithm` package and `algorithm2e` defines the same environment name. `algo2e` is the documented escape. The paper uses `\KwIn` ×8, `\KwOut` ×4, `\KwTo`, `\eIf`, so algorithm2e has to stay |
| 3 | add `xcolor`, `booktabs`, `tabularx`, `comment` | all four were being provided by acmart |
| 4 | drop `\setcopyright`, `\copyrightyear`, `\acmYear`, `\acmDOI`, `\settopmatter`, `\acmJournal`, `\acmVolume`, `\acmNumber`, `\acmArticle`, `\acmMonth`, `\citestyle`, `\footnotetextcopyrightpermission` | acmart-only |
| 5 | drop the `CCSXML` block and `\ccsdesc[...]{...}` | ACM's classification scheme. **Note the optional argument** — a regex for `\ccsdesc{...}` misses `\ccsdesc[500]{...}` |
| 6 | drop `\Description{...}` from figures | acmart's accessibility macro |
| 7 | `\begin{acks}` → `\section*{Acknowledgments}` | acmart environment |
| 8 | remove the ORCID icon machinery | it uses tikz's `\foreach`, which nothing else loads. **And it was already inside `\begin{comment}`** — see the correction to 0k below |
| 9 | `\bibliographystyle{siamplain}` | **resolved** — `siamplain.bst` came with the full package |

## The bad news: 16.5 pages of body against a limit of 12

| | acmart | siamproceedings |
| --- | ---: | ---: |
| body | ~13 pages | **~16.5** |
| total | 16 | 21 |
| references start | p13 | p17 |
| appendix | 13–16 | 18–21 |

Section starts under SIAM: §1 p2, §2 and §3 p3, §4 p8, §5 p10, §6 p12, §7 p16, appendix p18.

The Conclusion runs onto page 17, interleaved with the start of the references. **So the SIAM layout
costs about three and a half pages relative to acmart**, and the squeeze is a four-and-a-half page
problem rather than the half-page one we were planning for.

The cause is straightforward: `siamproceedings` sets `\textwidth` to 41pc and `\textheight` to 52.5pc
and forces 10pt via `\renewcommand\@ptsize{}`, where acmart's `acmtog` uses a smaller body font and
tighter leading. There is no size option to turn — the package overrides `\@ptsize` itself.

**This changes the nature of the remaining work.** Trimming four and a half pages of prose is not a
tidying exercise.

## Checked 2026-09-08: the twelve pages are counted in this layout

The submissions page states the limit and the required format in adjacent sentences of the same
paragraph:

> Submissions may be up to 12 pages in length, excluding references, and must present original research
> that is not published or submitted elsewhere. […] **Submissions should use the LaTeX macros at the web
> page**: https://www.siam.org/publications/proceedings/

So there is no reading under which the limit was written for a denser layout. Twelve pages means twelve
pages of `siamproceedings`, which is what the trial measures. References are excluded, and so in effect
is the appendix, which "will not be included in the proceedings".

**But the same paragraph also constrains what may be moved there**, and this is the sentence to plan
against:

> The main part of the submission should therefore contain a clear technical presentation of the merits
> of the paper, including a discussion of the paper's significance within the context of prior work and
> a description of the key technical and conceptual ideas used to achieve its main claims.

Merits, significance against prior work, and the key technical ideas stay in the main part. Supporting
detail may go. That is a workable test and it is the venue's own.

## Where the sixteen pages actually go

From the trial's own table of contents. Spans are approximate, being start-page differences:

| pages | section | |
| ---: | --- | --- |
| 1–2 | title, abstract, Introduction | |
| 3–5 | §3 Algorithm, of which §3.2 RadixHuskySort is **3 pages** | the mechanism; not movable |
| 6–7 | §3.3–3.5 encoding, $p_{crit}$, why it works | |
| 8–9 | §4 Implementation | §4.1 System Environment is three tables of machine specs — **a candidate** |
| 10–11 | §5 Test Case and Analysis, §5.2 Analysis **2 pages** | the array-access model |
| 12–13 | §6.1–6.3 Benchmarks, Summary, Radix Sort Results | five results tables sit here |
| 14 | §6.4 Parallel Radix Sort | **a candidate**: one table, and the prose quotes every figure in it |
| 15 | §6.5 Use-Case Guidance | **a candidate**: guidance rather than results, and §0j already lists it |
| 16–17 | §7 Conclusion | |

The three candidates already flagged — §6.5, §6.4 and the environment tables — come to roughly three
pages between them, which is most but not all of what is needed. The remainder would have to come from
prose density across §3 and §5, or from a fourth structural decision.

None of that is mine to take.

## Two caveats on the measurement

The trial has **no front matter at all** — no title block, no author block — so the real thing will be
slightly longer, not shorter. The bibliography style caveat is now resolved: the rebuild uses
`siamplain.bst`.

## A correction to 0k

0k said authors A and B had ORCIDs "declared but never used". That was wrong: **all three
`\orcidauthor` macros sit inside a `\begin{comment}` block at tex 150–177, so none of them is declared
at all.** The one ORCID that does print comes from a literal `\orcid{0000-0001-9734-7358}` at tex 233
and 242, not from the macros. The conclusion of 0k is unaffected — the ORCIDs come out — but the reason
is simpler than stated.

---

# 0m. Strategy for the four and a half pages — measured, 2026-09-08

Robin proposed three avenues: tighten the text, delete fluff, move appendix-worthy material. The
measurements say the second and third can work and the first cannot, because of where the pages
actually are.

## The whole overage is floats. The prose is exactly at the limit.

Four builds of the SIAM trial, differing only in which floats the body carries:

| configuration | body | implies |
| --- | ---: | --- |
| as it stands, 17 body floats | ~16.5 pages | |
| minus the 4 body figures | ~15 | **the figures cost ~1.5 pages** |
| minus the 13 body tables | ~13 | **the tables cost ~3.5 pages** |
| minus every body float | **~12** | **the prose is 12 pages — the limit exactly** |

So tightening prose cannot solve this. Even a heroic pass would only buy headroom for floats we have
already decided to keep, and today's experiment under acmart showed prose trims being swamped by float
reflow in any case (§0j). **Deprioritise avenue 1.** It is the expensive one and it is aimed at the
wrong target.

Avenues 2 and 3 both work, but only when applied to floats rather than to sentences.

## And a fourth avenue, which the numbers argue for

**Consolidate floats.** Thirteen tables cost 3.5 pages, an average of 0.27 each, and a good part of that
is fixed overhead — caption, rules, inter-float spacing — rather than data. Merging tables converts N
overheads into one without losing a single number, which none of the other three avenues can claim.

## Ranked targets

| target | avenue | ~saving | why it is defensible |
| --- | --- | ---: | --- |
| ~~Figure `TvsN`~~ | **DONE 2026-09-08** | | a 2020 measurement plot from the Intel/Java 8 machine — the category `HSComp` went for, and §7's audit covered tables and missed figures. A PNG, so unrebuildable |
| ~~Figure `Introsort`~~ | **DONE** | | a flow diagram of someone else's algorithm, explained in prose at two places |
| ~~Figure `Example`~~ | **DONE** | | a picture of what a corpus file looks like |
| ~~the three `SysEnv` tables~~ | **DONE — consolidated to one 3-column `table*`, `tab:SysEnv`** | | machines A, B and C in one table; the prose now names them rather than citing three floats. No content lost except two "Cache"/"OS" cells that were never filled for all three |

**Measured after those four: body 16.5 → 15.4 pages.** Then, on Robin's decision of 2026-09-08 to
reframe the paper around RadixHuskySort and de-emphasize QuickHuskySort:

| | body |
| --- | ---: |
| after the figures and `SysEnv` | 15.40 |
| `TimvsInsertion` to the appendix, three benchmark tables merged | 15.58 — **worse** |
| the same, with my own added prose trimmed back | **14.55** |

The middle row is the lesson, and it is the second time today the same trap has caught me (§0j). I
replaced three one-line `\item` entries with a ten-line explanatory paragraph and added `\midrule`s and
a repeated type name to the merged table. **The consolidation was worth 0.85 of a page; the prose I
wrote to justify it cost 1.03.** Trimming the paragraph to four lines, dropping the rules and dittoing
the repeated labels recovered all of it and more.

**2.55 pages still to find.** Remaining targets:
| ~~`TimvsInsertion`~~ | **DONE** — now appendix §A.2, `sec:cleanup-choice` | | design justification for Timsort in step 3, not a headline result. It also carries the paper's only non-machine-of-record figures, which the appendix now states outright |
| ~~`HS_BM_N` + `HS_BM_S` + `HS_BM_T`~~ | **DONE** — merged as `tab:HS_BM` | | one table, 14 rows, dittoed type labels. The dual-pivot and raw-quicksort columns went to appendix §A.3, `sec:comparison-baselines`, since they bear on QuickHuskySort rather than radix |
| **`Guidance`** | ~~appendix~~ **KEEP** | | Robin's decision, and right: it is the practical contribution and answers a reviewer concern |
| **`ParallelRadix`** | ~~appendix~~ **KEEP** | | Robin's decision, and I had it wrong before. Under a radix-focused paper the digit passes are precisely what parallelizes, so this is core content rather than an appendix curiosity |

## Still available under the reframing

Robin's decision to centre the paper on RadixHuskySort opens three more, none yet done:

- **`RadixImprovements` reframed as radix over the *system sort*** rather than over QuickHuskySort.
  No pages, but the range becomes 3.6--6.8x instead of 1.5--5.9x — larger, tighter at the bottom, and
  the comparison a reader of a radix paper actually wants.
- **§sec:summary**, 25 lines of QuickHuskySort-over-Timsort framing, is now the most obviously
  mis-aimed section in the paper. Compressing it is worth perhaps 0.3.
- **The conclusion's dual-pivot passage** can shrink to a sentence now that its table is in the
  appendix.

**The boundary to hold:** de-emphasize QuickHuskySort as an *algorithm under evaluation*, but keep the
encoding material — the array-access model, $T_1$/$T_2$/$T_3$, $p_{crit}$, and the cleanup-pass
measurement. Those are shared by both algorithms and are the intellectual core. And keep enough
QuickHuskySort to make the contrast work: the model's claim is that work moves out of the linearithmic
phase, and QuickHuskySort is the case where that phase is still there.

The four remaining rows come to roughly 1.6 pages of the 3.0 still needed. The last 1.4 has to come
from either `RadixImprovements` (12 rows, and the largest table left), moving all of §sec:usecase rather
than just its table, or a genuine prose pass accepted as the last resort rather than the first.

## The conversion is now scripted

`doc/siam-convert.py` performs the whole acmart→SIAM conversion from `paper/HuskySort.tex` in one pass,
so the page count can be re-measured after any change without redoing the work by hand:

```
python3 doc/siam-convert.py paper/HuskySort.tex /tmp/try/HuskySort.tex
```

It needs `siamproceedings.sty`, `siamplain.bst`, `sample-base.bib` and the `.png` files alongside the
output. `doc/HuskySort-siam-trial.tex` is its current output, kept as evidence rather than as the
paper.

## The test to apply, which is the venue's own

> The main part of the submission should therefore contain a clear technical presentation of the merits
> of the paper, including a discussion of the paper's significance within the context of prior work and
> a description of the key technical and conceptual ideas used to achieve its main claims.

Merits, significance against prior work, and the key ideas stay. Everything above passes that test:
none of it is an argument, and the two deletions are a diagram of another algorithm and a screenshot of
a file.

---

# 0n. Algorithm 6: `stringToLong` replaced by the Permit coder, 2026-09-08

Robin's question: do we need the `stringToLong` listing, and would the Permit coder not be better?
Yes to the second, and the reason is sharper than "more interesting".

**`stringToLong` was the one listing whose content the adjacent prose already fully stated.**
§Implementation says the wrappers differ "only in which bit-width, maximum length, and mask constant
they pass in", and Table `EncodingConstants` gives those constants, so the listing added nothing a
reader could not reconstruct from the two sentences beside it — shift left, OR in the character, pad
the tail. It also displayed its loop body twice, differing only by `\& mask`.

**The Permit coder shows what a reader cannot guess**, and it is exactly what §A.4 argues is the hard
and unchecked part of the mechanism: fields most-significant-first in `compareTo` order; symbol codes
from 1 up with 0 reserved so a short field right-pads and sorts low; and an out-of-alphabet symbol
collapsing to the largest at or below it, so the order weakens rather than inverting. Those three were
asserted in prose with no code to make them concrete. Robin asked for `codeOf` to be shown alongside
`huskyEncode`, which is right — `codeOf` is where the third property lives, and it is the one that
fails silently.

## I got the page cost wrong, for the third time today in the same way

I predicted the swap would be "comparable in length". The first version cost **0.83 of a page**, taking
the body from 14.55 to 15.38, because I wrote three long `\tcp` comments and a three-line caption.
Folding `encodeString` into the caption and cutting the comments to one line each brought it back to
**14.55 — page-neutral**, which is what I had claimed at the outset.

That is the same mistake as §0j and as the table merge: **the explanation I write to justify a change
costs more than the change saves.** Three times in one day is a pattern rather than bad luck. The rule
for the rest of this work: make the structural change first, measure, and only then write the prose —
and measure again after writing it.

## Knock-ons, all done

- §Overview's "For the details of the husky-coding itself" now reads "For a worked example of a husky
  coding", since the listing is one type's coder rather than the general mechanism.
- §Implementation's sentence drops the algorithm reference and keeps the prose plus
  `EncodingConstants`, which is where the constants were anyway.
- §A.3, the permits case study, gains "Algorithm ~\ref{alg:permitcoder} gives the coder."
- The label changed from `alg:the_alg4` to `alg:permitcoder`. The old label survives nowhere.
- The "nine 7-bit ASCII characters per 64-bit code" claim in the adversarial appendix rests on
  `EncodingConstants`, not on the deleted listing, so it is untouched.

---

# 0o. RHSort naming, and two explanations Robin asked for, 2026-09-08

## The naming problem was real

The paper used bare "radix sort" **26 times to mean RadixHuskySort**, while also discussing MSD radix
sort, LSD radix sort and generic fixed-width-key radix sort. A reader could not always tell which was
meant.

Robin's first suggestion was `rhs`, which I pushed back on: at a SIAM venue with equations throughout,
`RHS` reads as *right-hand side* first. He settled on **`RHSort`**, now defined at first mention in
§1 ("RadixHuskySort, or RHSort") and used at the sixteen sites where the bare form was ambiguous,
including the table column headers, which become `RHSort/8` and `RHSort/16`.

**Twenty-one bare uses survive and all are correct**: MSD radix sort (4), LSD radix sort's counting
structure, the generic $\textbf{O}(N \cdot k/b)$ result, radix sort applied directly to strings,
LaMarca and Ladner's naive radix sort, and the two places that describe RHSort as *using* a radix sort,
which is true and not a name.

## The two explanations, and what they cost

**Why Chinese words beat Chinese names.** Both are Chinese text; the difference is entirely in how the
two coders spend their 64 bits. The Unicode coder gives each character sixteen bits drawn from
thousands of code points, so a word of one to four characters is often captured whole. The pinyin coder
gives each character nine bits for a syllable out of a few hundred, plus a tone out of five — and names
are two or three characters whose surnames come from a smaller pool again. Few distinct codes, frequent
ties, and a cleanup pass that does real work. **The more expensive comparison is paired with the weaker
encoding**, which is why the two Chinese rows sit at nearly opposite ends of the table.

**Why pinyin applies to the names and not the words.** A directory of people has one conventional
ordering and it is by pinyin; a word list has several — by radical, stroke count, pinyin or frequency,
according to purpose — so code-point order there is one collation among many rather than the wrong one,
and it keeps the Chinese word row comparable with the English one. The paper had asserted the first
half and never addressed the second, which is the obvious referee question.

**Page cost: about 0.8, and nothing recovers it.** The first draft took the body from 14.55 to 15.29; a
tighter rewrite gave 15.38, *worse*; and moving the mechanism to appendix §A.6 on Robin's instruction
left it at **15.38 again**. Four prose interventions, no movement.

## What that finally establishes: prose is no longer the lever

Measured on the current file by stripping body floats and rebuilding:

| what | pages |
| --- | ---: |
| prose alone, all body floats and listings removed | **11.29** |
| the 8 body tables | **~3.4** |
| the 6 body pseudocode listings | **~1.4** |
| everything, as it stands | **15.38** |
| the limit | 12 |

**Prose already fits.** At 11.29 it leaves 0.71 of a page for floats that currently cost over four. So
±0.8 of a page of explanation moves the total not at all — which is exactly what the four
interventions above measured — and the remaining 3.4 pages must come from floats.

The alphabet-size mechanism is nonetheless better placed in §A.6 than in §sec:radix: the *claim* stays
in the body where the argument is, and the arithmetic is where a reader who wants it will look. It cost
nothing to move and it reads better. But it was not a page saving and the document should not pretend
otherwise.

## The eight body tables, ranked as candidates

Guidance and ParallelRadix are kept on Robin's instruction, and `HS_BM` and `RadixImprovements` are the
headline results. That leaves four, of which two are strong:

| table | rows | verdict |
| --- | ---: | --- |
| ~~`PriorAlgorithms`~~ | 5 | **MOVED to appendix §A.1, 2026-09-08.** A background table of textbook sorting algorithms; §BACKGROUND's prose carries the argument without it |
| ~~`SysEnv`~~ | 6 | **MOVED to appendix §A.2, 2026-09-08.** Machine specifications are methodology, not argument. Its introduction became one sentence in the body |
| `EncodingConstants` | 4 | small, and it now carries the bit-widths that the deleted `stringToLong` listing used to show |
| `Comparison` | 4 | the array-access model's own output. This is argument, and it should stay |

## And the six listings, under the RHSort reframing

| listing | verdict |
| --- | --- |
| `RadixHuskySort.sort`, `radixSortIndices` | core; keep |
| `PermitCoder` (new) | Robin asked for it, and it carries §A.5's three properties; keep |
| `huskyEncode` | shared by both algorithms; keep |
| ~~`HuskySort.sort`~~ | **REMOVED 2026-09-08.** QuickHuskySort's top level, the algorithm now positioned as prior work. Its two references were reworded: §Overview's steps stand on their own, and §Implementation now says the top-level `sort` follows those three steps directly |
| ~~`huskyEncode` for a character sequence~~ | **REMOVED.** String-specific, strings being explicitly not the sweet spot — and it was **orphaned**, with no reference anywhere in the paper |

**Measured: 14.45 → 14.29, worth only 0.16** against the 0.5 estimated. Four listings remain, all
earning their place: `RadixHuskySort.sort`, `radixSortIndices`, `huskyEncode`, and the `PermitCoder`.
**2.29 still to find**, and nothing cheap is left.

**Measured: those two were worth 0.93 — body 15.38 → 14.45**, the first substantial move since the
figures, and confirming that floats rather than prose are where the pages are. **2.45 still to find.**
The two candidate listings are perhaps 0.5 of it; the rest has to come from `HS_BM` or
`RadixImprovements` — the two headline tables — or from accepting a shorter paper than the one we have.

---

# 0p. The abstract rewritten, and two gaps it exposed, 2026-09-08

Robin's judgement: the abstract was too long and no longer matched the paper's focus. Both were right.
**423 words down to 237**, and QuickHuskySort now retires in eight third-person words — "Earlier work
sorted such codes with quicksort" — which is what §1 had already said and the abstract had not caught up
with.

Dropped, all of it still in the body: QuickHuskySort's own paragraph and its 1.4--2.5x range; the
pinyin baseline disclosure at length; the cleanup pass's figures, reduced to "we measure directly, for
the first time"; and "which bounds the result rather than contradicting it".

**Page-neutral.** 193 words out of the abstract moved the body not at all, consistent with everything
else measured today.

## Gap 1: the parenthetical, and where it belongs

Robin agreed to restoring the pinyin baseline disclosure but questioned whether the abstract was the
place. The resolution was placement rather than presence: attached to the *claim* it reads as a hedge,
attached to the *list item* it reads as specifying what was measured. It now sits on the item ---
"Chinese personal names in pinyin order (against a system sort performing that same ordering)".

## Gap 2: the body never reported the figure the claim rests on

Checking that disclosure turned up something worse, and it predates the rewrite. **Table `HS_BM` had no
Chinese-names row at all.** The corpus appeared only in `RadixImprovements`, as 1.6x over
QuickHuskySort. So the abstract claimed RHSort beats the system sort "in every case" while listing a
corpus for which the body gave no system-sort comparison — the same defect class as §5.2, and present
in the previous abstract too.

Fixed by adding three Chinese-names rows to `HS_BM`. **`full-suite.json` does carry
`systemSortPinyin`**, so they come from the same run as every other row and no environments are mixed:
69.1, 526.2 and 3112.9\,ms against RHSort/16's 23.1, 154.9 and 767.2 — **4.06x at the largest size**,
which is what the abstract's range needed. The caption now states that this row's system-sort column is
a pinyin-performing system sort and that code-point order is a different, cheaper task.

## And a precision fix the new rows forced

With those rows visible, a reader can compute 2.99x at $N=32{,}000$ — outside the abstract's stated
"3.6 to 6.8x". That was already true of other types: English gives 3.28x at 32,000 and tuples 2.86x at
20,000. **The range was always over the largest size of each type and the abstract never said so.**
It now reads "by 3.6 to 6.8x at the largest size measured for each", which costs six words and makes
the claim exact.

## The permits data is now cited and licensed

The permits corpus had been **uncited** while both other corpora were — conspicuous now that the
abstract foregrounds "two hundred thousand municipal records". Robin supplied the licence, Open Data
Commons DbCL v1.0, and then the source: the Kaggle dataset
`aparnashastry/building-permit-applications-data`.

**That second message corrected a mistake in my first attempt.** I had credited DataSF as publisher,
inferring it from `PermitLoader`'s class comment. But the data was obtained from a Kaggle
redistribution, and it is that redistribution the DbCL licence attaches to — Kaggle's usual licence for
datasets of this kind, which is why the licence pointed there. The entry now credits **Aparna Shastry**
as the Kaggle author with the full URL, notes the underlying record as San Francisco's published by
DataSF, and carries the licence. §Data Source says the rows are "San Francisco's published
building-permit record, obtained from a public redistribution of it".

**I had also written `year = {2026}`, which was a guess and is removed.** No year or access date is
recorded anywhere in the repository, so the entry renders as "[n.d.]" — the honest rendering of an
undated web resource, and the same treatment the Jugé entry got until its DOI supplied a year.
**Robin chose 2018**, the end of the declared coverage window. Every entry in the bibliography is now
dated; no `[n.d.]` survives anywhere in the built PDF.

## A third message, and a consistency check it invited

Robin then supplied the primary source — DataSF dataset `i98e-djp9` — and the coverage period, 2013 to
2018. The citation now names the canonical dataset with the redistribution and its licence in the note:
primary source for the reader, honest chain of custody for us.

The coverage period prompted a check, because `PermitCoder` allocates **eleven bits to the date**, and
eleven bits admit 2,048 consecutive days where 2013 to 2018 is over 2,100. **It holds.** The extract
runs 2013-01-02 to 2018-02-23 — a span of **1,878 days** — and `PermitCoder`'s own class comment
records exactly that figure, with a guard that throws for any date outside the window rather than
encoding it wrongly.

Two things followed. The prose said "between 2013 and 2018", which overstates: February 2018 is where
the data stops, and 2018 contributes 5,953 of the 198,900 rows against 35,000--41,000 for each full
year. It now reads "between January 2013 and February 2018". And §A.5 gained the reason eleven bits
suffice — that the field depends on the corpus rather than the schema, and that the window is checked
rather than assumed, which is what makes `perfect()` a verified property here.

## The declared window differs from the data's own, and why that is worth a sentence

Robin's fourth message gave Kaggle's declared range as 2013-01-01 to 2018-02-25. The extract's own
range is 2013-01-02 to 2018-02-23, and the three missing days explain themselves: 1 January 2013 was
New Year's Day, and 24 and 25 February 2018 were a Saturday and a Sunday. **The declared window is
calendar-based; the data's boundary is business-day-based.**

Checked rather than assumed, because I had written the claim before verifying it: **zero of the 198,900
filings fall on a Saturday or Sunday** — the weekday histogram runs Monday to Friday only — and **all
sixteen weekday fixed-date federal holidays in range have zero filings** too (New Year, Independence
Day, Christmas, Veterans Day, 2013--2018).

§A.5 now says so, because it is the sharpest available evidence for the one claim the permits data
exists to support — that its distribution is the city's rather than ours. A generated corpus would not
have that structure unless someone had thought to put it there.

---

# 0q. Wasted space around Figure 1 and between the algorithm listings, 2026-09-08

Robin spotted three layout problems by eye. All three were real, all three are fixed, and **none of
them moved the page count** — which is itself the useful result.

## Figure 1: 25% of the image was blank margin

A 208pt gap sat in page 2's left column after the Fig. 1 caption — about a third of a column. Decoding
`HuskySortFlow.png` directly (no PIL on this machine, so a small pure-Python PNG reader) gave the ink
bounding box as x 133..755, y 85..817 of 806x976 pixels. **Blank margins of 85px top, 158px bottom,
133px left and 50px right — 25\% of the height and 23\% of the width, baked into the file.**

Fixed with `trim` and `clip` rather than by editing the PNG, so the file stays as published. `pHYs`
declares 144 dpi, so the trim values are in bp (pixels x 72/144) and are held a few bp inside the
measured margins so no ink can be clipped.

**One correction to myself along the way.** Cropping at the original `width=0.5\linewidth` made the
*diagram* 30\% bigger rather than the *space* smaller — the box height barely changed while the ink
grew from 109pt to 141pt. To recover space the width had to come down too, to `0.39\linewidth`, which
is $0.5 \times 623/806$ and keeps the diagram at its original apparent size, 92pt by 109pt of pure ink.
**The gap is now 136pt, down from 208.**

## Algorithms 1 and 2, and 3 and 4

45pt and 49pt between consecutive listings. The cause is float separation: `\floatsep`,
`\textfloatsep` and `\intextsep` at their defaults, which are generous for a two-column layout under a
page limit. Now 8pt, 10pt and 8pt, set inside `\AtBeginDocument` so the class cannot override them
afterwards. The 45pt and 49pt gaps are gone; one 37pt gap remains between Algorithms 3 and 4.

## But the page count did not improve

**14.29 before, 14.40 after** — marginally worse, which is float reflow within noise. Seventy-two
points of gap removed from one column and a dozen float separations tightened, and the body is where it
was.

That is now the fifth measurement today saying the same thing (§0j, §0m, §0o, §0n): **the SIAM body
sits at about 14.3--14.4 pages almost regardless of local changes**, because float *placement* rather
than float *size* decides where the pages break. It reinforces that the remaining 2.4 pages have to
come from removing whole floats, not from making the existing ones smaller or the gaps tighter.

Three end-of-column gaps survive at 58pt, 76pt and 51pt on pages 9, 10 and 11. Those are columns cut
short because the next float would not fit, which is inherent to float placement and not addressable by
settings.

---

# 0r. The Analysis section: two placeholders, and the derivation moved, 2026-09-08

Robin's observation was that §Analysis carries "not-terribly-well-justified mathematics", mostly
pertaining to QuickHuskySort, and that some labels still read "HuskySort". Both right, and looking into
it found something worse.

## Two unresolved 2020 placeholders were printing in the PDF

> Wild and Nebel's average-case analysis of dual-pivot quicksort **[Nebel]** that these values are…
>
> Our experiments **[refer to Quicksort Analysis table]** show that each comparison requires 2 array accesses…

Both rendered literally. Nothing in a week of table rebuilds and figure audits had caught them, because
neither is a number.

- **`[Nebel]`** is now `\cite{WildNebel12}` — Wild and Nebel, *Average Case Analysis of Java 7's Dual
  Pivot Quicksort*, ESA 2012. Their result is **exactly** the 1.9 $N \ln N$ comparisons and
  0.6 $N \ln N$ swaps the paper quotes, so the figures were right all along and only the attribution
  was missing.
- **`[refer to Quicksort Analysis table]`** pointed at a table that does not exist in the paper. It
  needed no table: what followed it is arithmetic, not an experiment — a comparison reads two elements,
  a swap does two reads and two writes — so the sentence now says that instead.

## The stale label

`tab:Comparison`'s header read `merge sort | HuskySort | Radix`. Now `merge sort | QuickHuskySort |
RHSort`. It was the only stale one inside a table; `\caption{HuskySort Flow}` and `\title{HuskySort}`
are the family name and are left alone.

## The derivation moved, and it is the largest single saving of the day

**Body 14.40 → 13.45, worth 0.95.** Equations 1--5, the QuickHuskySort-versus-merge-sort counting
argument, are now appendix §A.2 (`sec:qhs-analysis`). The body keeps the framing, the encoding-cost
measurement, the conclusion (break-even at $N = 4$, widening thereafter), Table `Comparison`, and the
RHSort derivation, which is the part that is ours.

Robin identified the weak step correctly. Equation 1 is $A = 4 + 0.6 \times 4 = 6.4$, where 0.6 stands
for the proportion of object-reference swaps whose targets are not already cached — asserted from "these
will not, in general, have been pre-fetched", never measured — and it then propagates through equations
2, 4 and 5 into $A_h = 6.4 N \ln N + 9N$ and thence into Table `Comparison`. A soft coefficient ends up
looking like a derived constant. **The new appendix section says so in its second paragraph**, and
observes that the RHSort figure needs no such factor: $4 \cdot 64/b$ digit passes is a count.

## And a stale-figure defect the move exposed

The encoding-cost paragraph — which stays in the body, being a real measurement rather than weak
arithmetic — had **six figures from a superseded run.** Every one was wrong:

| | said | actual |
| --- | ---: | ---: |
| English encode | 52.7\,ms | **67.7** |
| … as a share of QuickHuskySort | 13.7\% | **9.7\%** |
| … as a share of RHSort | 21.1\% | **24.8\%** |
| pinyin encode | 311.6\,ms | **211.1** |
| … as a share of QuickHuskySort | 29.1\% | **16.9\%** |
| … as a share of RHSort | 40.0\% | **27.8\%** |

The §7 rebuild swept tables and derived ratios; this was prose in a section nobody was looking at.
Both claims the paragraph makes survive — encoding is a minority of total time in every case, and its
share is larger for RHSort than for QuickHuskySort — and one improves: the pinyin encoding is 27.8\% of
RHSort's total rather than the 40\% claimed.

A sweep of every millisecond figure quoted in prose against `full-suite.json` now returns no
unmatched value.

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

## 1.5 The framing collapses — **APPLIED 2026-09-07**, at tex 567–588

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

## 4.1 "always" — **APPLIED 2026-09-07**, at tex 1441–1447

> HuskySort is always faster than dual-pivot quicksort.

This was logged as an overstatement. It is worse than that. Table `HS_BM_N`, on the machine of record,
at $N=500{,}000$, QuickHuskySort against the dual-pivot baseline:

| type | QuickHuskySort | DualPivotQuicksort | |
| --- | ---: | ---: | --- |
| Integer | 87.9 | **81.7** | dual-pivot wins |
| Double | 93.5 | **92.8** | dual-pivot wins |
| Long | 99.3 | **90.5** | dual-pivot wins |
| BigInteger | **151.0** | 209.7 | HuskySort wins |
| BigDecimal | **149.1** | 243.7 | HuskySort wins |

**Three of five numeric types go the other way**, and the pattern is not noise — it is exactly what the
paper's own argument predicts. Husky encoding pays for itself when comparison is expensive; `Integer`,
`Double` and `Long` are the cases where it is cheapest, so there is nothing for the encoding to amortise.
`BigInteger` and `BigDecimal`, where comparison is genuinely costly, go the paper's way by a wide margin.

So the sentence is not just unqualified — it asserts something two tables away is refuted by, and the
refutation is a positive illustration of the paper's thesis rather than an embarrassment. It was found
by following the pointer that tex 1138 now carries, which sends the reader to `HS_BM_N` for precisely
this comparison.

Suggested replacement:

```latex
HuskySort is faster than dual-pivot quicksort wherever the ordering is expensive to evaluate,
by 1.4x on \textit{BigInteger} and 1.6x on \textit{BigDecimal} (Table ~\ref{tab:HS_BM_N});
on \textit{Integer}, \textit{Double} and \textit{Long}, where a comparison costs almost nothing,
the encoding has nothing to amortise and dual-pivot quicksort is slightly ahead.
This is the same criterion that governs everything else in this paper, applied to a single data type.
```

The two further caveats originally recorded here still stand: the claim sat in a conclusion that four
lines later says the advantage "is specific to a particular kind of workload, not universal", and there
is no measurement against dual-pivot at small $N$, the crossover work comparing against System sort and
insertion sort only.

---

# 5. Additions — new evidence, not corrections. **No longer optional: the abstract already cites both.**

## 5.1 The permits case study — **APPLIED 2026-09-07**

Two additions. §Data Source (tex 899–905), which also gained the `\label{sec:data-source}` it had
always lacked, now says that every other dataset in the paper is generated and this one is not. And
§sec:radix-results (tex 1352–1365) separates the permits row from the rest of Table
`RadixImprovements`: 5.25x, 4.53x and 4.77x over the system sort, 2.56x, 2.17x and 2.06x over
QuickHuskySort, all from `full-suite.json` so that they match the table's own 2.1x.

The framing is that tenth-of-eleven is the point rather than an embarrassment — it is the best result
on data whose distribution we did not choose, and it lands where the three factors predict.

### The original note follows

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

## 5.2 The cleanup pass, measured directly — **APPLIED 2026-09-07**, at tex 729–758

Written into §sec:pcrit, immediately after the $T_1$/$T_2$/$T_3$ decomposition, which is the discussion
it anchors. Table `Guidance`'s dangling pointer went with it: that cell cited §sec:radix-results, which
never held the figure, and now cites §sec:pcrit and reads "a fifth to a quarter past $N=100{,}000$".

**Checked in the source before writing, rather than taken from this document.** `Permit.huskyCode()`
delegates to `PermitCoder.INSTANCE.huskyEncode`, and `HuskyCoderFactory.createGenericCoder()` is
`HuskySortable::huskyCode`, so the two benchmarks genuinely do compute identical codes and differ only
in `perfect()`. The paper says so, and it is true.

### A denominator error, caught by Robin on 2026-09-07

The first version of this passage said "6.2\%, 22.8\% and 24.2\% **of total running time**". Those are
$\Delta/(T_1+T_2)$ — the overhead *added* relative to the cleanup-free sort — not the pass's share of
the total. The share of the total is $\Delta/(T_1+T_2+T_3)$:

| n | as overhead added | **as share of total** |
| ---: | ---: | ---: |
| 32,000 | 6.2% | **5.8%** |
| 100,000 | 22.8% | **18.6%** |
| 198,900 | 24.2% | **19.5%** |

Across all three runs the maximum share of the total is **20.2%**, not 25.3%. So the abstract's "as
much as a quarter" overstated it and is now **"as much as a fifth"**; Table `Guidance` now reads "a
sixth to a fifth past $N=100{,}000$" (the range at $N \geq 100{,}000$ being 15.9--20.2%).

### The figures, and a new finding

Two Graviton3 runs measured this, and **they disagree about the shape:**

| n | `permits.json` (request 3) | `full-suite.json` (request 4) |
| ---: | ---: | ---: |
| 32,000 | 9.8% | 6.2% |
| 100,000 | 25.3% | 22.8% |
| 198,900 | **19.0%** | **24.2%** |

Intervals are non-overlapping within each run at every size, so neither is noise at the level of the
individual measurement — yet one peaks in the middle and the other rises throughout. With the M1's
5.9 / 11.9 / 17.4, that is **three runs and three shapes.**

So the earlier instruction here — "do not write grows with N, Yunlu's figures peak in the middle" — was
half right for the wrong reason. It is not that the true shape is a mid-range peak; it is that there is
no reproducible shape at all. The paper now says exactly that: the magnitude replicates and the shape
does not, and it draws only the conclusion both runs support — under a tenth at 32,000, a fifth to a
quarter at the two larger sizes.

### The original note follows

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

## What was still on the old machine — **CLOSED 2026-09-07**

Three tables in §sec:analysis were never rebuilt, all from Table `SysEnvOriginal`, a 2017 quad-core
Intel i7 running **Java 1.8.0_152**. Robin's instruction of 2026-09-07 settled all three:

| table | outcome |
| --- | --- |
| `HSComp` | **removed.** Could not be rebuilt — thirteen sizes JMH does not run. All seven inbound references resolved; see `Paper deletions.md` |
| `Improvements Summary` | **removed.** Could not be rebuilt — continuous size bands that JMH's three sizes per type cannot reproduce. Its ranges were also simply wrong on the new machine: Chinese was 1.4–2.1x and is 2.0–2.3x |
| `TimvsInsertion` | **kept, and attributed.** It is the only evidence for choosing Timsort in step 3, and 2,424 ms against 11,018 ms at N=4,000,000 does not become wrong on a newer JVM. Tex 806 was repointed from `HSComp` to it, so §Implementation now names it as what `SysEnvOriginal` measured |

Table `Comparison` looked like a fourth but was not one: it counts array accesses from the model of
§sec:radix and never touched a benchmark.

**So this section's opening claim is now true.** Every results table in the paper quotes the machine of
record except `TimvsInsertion`, which is not a results table — it is a design justification for step 3 —
and which now says which machine it came from. The 12-page limit and this decision turned out to want
the same two deletions.

## The one wrinkle that remains

**The sizes did not line up, and that is why both tables went rather than being re-cast.** JMH uses
32,000/200,000/1,000,000 for strings and 20,000/100,000/500,000 for numerics; neither deleted table's
sizes were reproducible. Nothing in the paper now quotes a size band that cannot be reproduced.

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

---

# 0s. The all-at-once table and naming audit — 2026-09-08

Robin found five defects in one reading ("I think you need a break") — Table 4's "Radix sort", Table 9's
bare "Husky", Table 10 showing something unexplained, A.12 saying "HuskySort", and "several" more
"HuskySort" references. Piecemeal fixing is what produced them, so instead: **all 12 tables** (number,
page, caption, every header) and **all 43 bare "HuskySort" occurrences** were read in a single pass.

**24 corrections, plus 3 more once Table 10 was checked against the benchmark source.**

## What Robin listed

| site | was | now |
| --- | --- | --- |
| Table 4 caption | "Radix sort" — which one? | "RHSort's advantage over QuickHuskySort, as a direct multiplier" |
| Table 9 header | bare "Husky" ×2 | "QuickHuskySort, Timsort cleanup" / "…, insertion-sort cleanup" |
| Table 10 caption | said nothing about what it shows | defines both baselines and what each measures |
| A.12 | "HuskySort" throughout | QuickHuskySort where meant; also "40 Percent" → "40 percent" |
| 15 further sites | "HuskySort" meaning QuickHuskySort | named explicitly |

## Table 10 needed a second correction, because my first was wrong

The caption I wrote said *quicksort (raw)* "sorts the husky codes alone". It does not.
`NumericSortBenchmarks.rawLongQuicksort` unboxes each element to a primitive `long` (or `double`) and
calls `Arrays.sort` on that array — the benchmark's own note says "what's being measured here is the
primitive-array sort + box round trip". For `Double`, `BigInteger` and `BigDecimal` a husky code is not
what gets sorted. The "bounds from below" claim survives; the description of the mechanism did not.

I nearly compounded it by adding that the unboxing is lossy for the two `Big` types. It is not, for this
data: the generators are `BigInteger.valueOf(r.nextLong())` and `BigDecimal.valueOf(double)`, both of
which round-trip exactly. Checked before writing, not after.

**Table 10 also had three unlabelled rows.** They are the Permit tuple at N = 20,000 / 100,000 /
500,000, carried over from the old Tuples table when it was merged into this one; the row label was lost
in the merge and the table had been printing a blank "Data type" cell ever since.

## Two defects the audit found that were not on Robin's list

- **A duplicated paragraph, mine, an hour old.** The bridge added when the derivation moved to §A.1
  restated the conclusion text still sitting at 994–999. Trimmed to the pointer it was meant to be.
- **The worked example still quoted pre-PR64 figures** — 732\,ms, 1,194\,ms, ratio 1.63. Against the
  current `HS_BM` run (system 1120.87, QuickHuskySort 697.88) it is **698, 1,121, 1.61**.

## The 22 remaining "HuskySort" uses are deliberate

Family-level by intent: the introduction and the shared three-step strategy, the "Why HuskySort Works"
heading, the ShellSort comparison, the GitHub URL, the code identifiers `HuskySortBenchmark` and
`HuskySortBenchmarkHelper`, §sec:summary's target applications, "not either HuskySort variant", the
coding-accuracy appendix, and the conclusion's own gloss, "The original idea behind HuskySort, that's to
say QuickHuskySort". That gloss is why the line four sentences later needs no change.

## Page count, re-measured under SIAM rather than acmart

Worth recording because it caught me out: `paper/HuskySort.tex` is still acmart, and its body measures
**10.2 pages** — which means nothing, since the limit applies to the SIAM reflow. Regenerating the trial
through `doc/siam-convert.py` and measuring to where REFERENCES starts:

| | body |
| --- | ---: |
| cfb51b8, before the audit | 13.75 |
| a104898, after it | **13.69** |

So the audit paid for its own added caption text and a little more, the trimmed duplicate being body
prose while the caption growth is in the appendix. **~1.7 pages still to find.**

Note for re-measuring: the trial needs `TEXINPUTS` to include both `paper/` (for `HuskySortFlow.png`)
and `paper/siamproceedingsmacros_022425/`, else the figure silently falls back to a draft box.

---

# 0t. The Chinese-words result — kept, but honestly labelled — 2026-09-08

Robin asked whether to delete it: not compared against MSD radix sort, and code-point order may not be
an order anyone wants. Both objections are sound. The proposed remedy was space, and that premise fails.

## Deleting it saves 0.036 pages

Measured, not estimated. A trial removing the result everywhere — three `HS_BM` rows, the
`RadixImprovements` row, the summary-range clause, the multikey figure, and the whole of §A.13 — was
converted through `siam-convert.py` and built:

| | body |
| --- | ---: |
| current | 13.690 |
| Chinese words deleted throughout | 13.655 |

Two lines, against the ~1.7 pages needed. **Kept.** It is our best string result (3.4x, fourth of
eleven, against English at 2.6x), and it is the positive pole of the within-type contrast in
§sec:usecase — delete it and Chinese names and English words contrast with nothing.

## What the objections are worth

- **MSD**: correct that there is no MSD row for Chinese, and the paper says why at 611--616 (our MSD
  indexes 256 characters, which neither Chinese corpus fits). But Chinese words *is* measured against
  three-way radix quicksort at 3.1--4.0x, which is Bentley--Sedgewick, not a strawman. "Not compared"
  overstates it; "compared against one of the two classic baselines" is exact. The residual exposure is
  presentational: the baseline that beats us cannot run on the corpus carrying our best string number.
- **Code-point order**: correct, and the paper's defence was thin. It argued a word list has several
  conventional orderings so code-point order is "one collation among many"; the real reason is the
  clause after it, that it keeps the row comparable with the English one. Reworded to say that plainly.

## One term, everywhere

The paper had three names for one thing — "natural Unicode order" (595, 623), "natural order" (625,
1126), "code-point order" (627, 1056). Now **code-point order** throughout, that being the precise one
and the one the `HS_BM` caption already used.

`HS_BM` also now labels the collation on the words rows, mirroring "(pinyin order)" on the names rows
directly below, so the table declares it instead of making the reader find it in prose.

## And two more bare-"Chinese" ambiguities, same family as §0s

- 1143 "holds roughly constant on Chinese" — which corpus? True of both (words 1.96/2.31/2.03, names
  2.60/2.81/2.48), so: "on both Chinese corpora".
- 601 "3.1--4.0x on Chinese" — the words corpus, names in code-point order being explicitly disclaimed
  twenty lines later. Now says so.

Body unchanged at 13.69: the label and the disambiguations pay for the §A.13 trim.

---

# 0u. Retitled, and a measurement bug of mine that flattered the page count — 2026-09-08

## Title

`\title{HuskySort}` → **`RadixHuskySort: A Linear Second Phase for Proxy-Key Sorting`**.

The old title promised the 2020 paper, when the point of this one is that it is not that algorithm; it
also matches the contributions sentence at 406 ("The contributions here are RadixHuskySort, or RHSort
… which replaces that algorithm's comparison sort with a linear-time radix sort"). The subtitle exists
because the bare name is opaque to anyone who does not already know what a HuskySort is.

Deliberately **not** "in linear time": both phases are linear only when the encoding is exact, and
§sec:pcrit is precisely the measurement of what the cleanup pass costs when it is not. A title claiming
linear time would overclaim the thing the paper is most careful about.

## The SIAM trial was measuring an orphan page — every figure quoted before now was ~0.7 too high

`doc/siam-convert.py` left the front matter in **acmart's** order, with `\begin{abstract}` before
`\maketitle`. That is correct for acmart and wrong for `article`/`siamproceedings`, where `\maketitle`
issues `\twocolumn[...]`: the abstract was typeset alone on page 1, the page then broke, and the title
started page 2. Nearly a blank page, counted as body, in every measurement made through this script.

Caught only because the retitle prompted a check that the new title rendered — it did under acmart and
did not in the trial. The style file was innocent; a minimal document with `siamproceedings` sets the
title correctly with or without an `\author`. So was the `\AddToHook` no-op shim for TL2020, which
discards nothing but `crefalias` declarations for theorem environments this paper does not use.

Fixed in the script, which now moves the abstract to just after `\maketitle` and asserts it found one.

| | body | total |
| --- | ---: | ---: |
| as measured all day (orphan page 1) | 13.69 | 22 |
| **corrected** | **13.0** | 21 |

REFERENCES now starts at the very top of page 14, left column, and the last body float
(`tab:Guidance`) is on page 12. **So the overage is 1.0 page, not the 1.7 reported earlier today.**
Every relative comparison made today still holds — they were all measured the same wrong way, so the
deltas (0.036 for the Chinese-words deletion, 0.06 for the audit) are unaffected.

---

# 0v. Page limit met, and Figure 5 stays out on the merits — 2026-09-09

## Where the length went

Body **11.50 pages** against a 12-page limit, measured on `HuskySort-anon.pdf`, which is now the
figure that counts (see 0w below). The last two days, in order of what each avenue actually bought:

| change | pages |
| --- | ---: |
| float placement: reclaiming the page-9 float page | **0.61** |
| reframing as a RadixHuskySort paper, citing the 2020 work | 0.89 |
| moving PriorAlgorithms, SysEnv, TimvsInsertion and the QHS derivation to the appendix | ~1.4 |
| the whole table-and-naming audit | 0.06 |
| deleting the Chinese-words result entirely (rejected) | 0.036 |

Worth keeping in view: **one float parameter was worth ten times the entire prose audit**, and 17x
the deletion that was proposed to save space. When room is needed again, look at float placement
first, structure second, prose last. Prose is where the effort goes and where the least is gained.

## Figure 5 stays out

Restoring it was measured, not estimated: 0.76 pages as the spanning `figure*` it was (12.26, over
the limit), 0.30 as a single-column figure (12.00, exactly on it). Neither is affordable against a
metric that moved 0.61 on one parameter.

But Robin's reason is the better one and is the reason of record: **the figure is one-dimensional,
algorithm against size, where Table `tab:Guidance` is quasi-two-dimensional.** That matters because
§`sec:usecase` argues size is the *last* axis to consult --- it eliminates two cases outright before
size is mentioned, and says size "enters only once those are settled, and then only near the bottom
of the range". A chart organised by size foregrounds the least decisive variable, and contradicts the
section containing it.

So this is not deferred to camera-ready, where there will be room. `paper/UseCaseGuidance.pdf` stays
in the repository --- it is still a good picture, and useful in slides --- but it should not go back
into the paper without answering the dimensionality objection first.

## The remaining slack is slack

Half a page, six days out, against a non-monotonic metric. It is worth more as margin than as
content, and every further edit needs re-measuring rather than trusting 11.50 to hold.

---

# 0w. Terms of art, settled — read before any prose pass — 2026-09-10, current to 09-11

Robin is making a pass to humanize the text and asked to know which terms are deliberate, so as not
to undo them. Each of these cost real effort to make consistent; rewording around them is welcome,
renaming them is not.

| term | means | not |
| --- | --- | --- |
| **RHSort** | RadixHuskySort, in table captions, column headers and figures, where the full name will not fit | `radix`, which could mean any radix sort; `rhs`; the name spelled out in a caption |
| **RadixHuskySort** | the same algorithm, in prose | |
| **QuickHuskySort** | the original algorithm specifically, the one the 2020 paper introduced | plain "HuskySort", which was the ambiguity the 2026-09-08 audit removed from fifteen sites |
| **HuskySort** | the family: both variants, the shared three-step strategy, the encoding idea. Also the 2020 paper, `paper/HuskySort-original-2020.tex`, and Figure 1's `HuskySortFlow.png`, which depicts the family's strategy | either variant specifically. 20 bare uses remain, every one family-level by intent |
| **ParallelRadixHuskySort** | the parallel variant | |
| **code-point order** | sorting Chinese by Unicode code point | "natural order", "natural Unicode order" --- the paper had all three names for this until 0t |
| **pinyin order** | the collation a directory of people actually wants | |
| **husky code** | the 64-bit order-preserving proxy key | "hash code", which is what the 2020 paper had to disclaim: the name Hash Sort was taken |
| **cleanup pass** | step 3, repairing whatever inversions the encoding left | "second pass", which collides with step 2 |
| **perfect** (of an encoding) | order-preserving with no ties, so the cleanup pass can be skipped entirely; noun form **perfection**, negative **imperfect** | "exact", "exactness", "inexact". The paper defines *perfect* at \S~\ref{sec:overview}, where step 3 first needs it, and restates it precisely as $p=0$ at \S~\ref{sec:coding}; and the code's own flag is `perfect()`, so "exact" was a synonym with no warrant. Twelve sites corrected 2026-09-10 |
| **dual-pivot quicksort** | step 2's partitioning: two pivots, three-way scan, three recursive calls --- Yaroslavskiy. Say so wherever the analysis depends on it, which A.1 does | "Introsort" used as though it named the partitioning. Introsort is the right name for the *hybrid strategy*, the depth-limited heapsort fallback and the small-subarray cutoff, and stays as shorthand for step 2's sorter --- but it implies nothing about pivots, and A.1 draws on Wild and Nebel's dual-pivot analysis |
| **`RadixHuskySort.tex`** | the paper, and its build products, renamed 2026-09-11 | `HuskySort.tex`. That name now belongs to the 2020 work alone |
| **husky**, lowercase | the adjective: husky code, husky coding, husky encoding, husky-based, husky-sorting. Capitalised only where any word would be --- sentence-initial, or title case in a heading | mid-sentence "Husky". The paper mixed the two until 2026-09-11; lowercase was already the majority, so that is the convention. `HuskyCoder` and `HuskySort` keep their capitals, being an identifier and a name |
| **`compareTo`, not `equals`** | the relation a husky code must respect. Elements that compare equal must code equal | `equals`, which the paper said until 2026-09-11 and which differs on a type we benchmark: `bigDecimalCoder` is `BigDecimal::longValue`, so 1.0 and 1.00 code alike while `equals` is false |
| **machines A, B and C** | the Intel, the M1 and the Graviton3; C is the machine of record and the only source of quoted figures | naming them by date or by owner, which is how the paper used to distinguish them and which read as a resubmission |

Four further conventions that are easy to undo by accident:

- **Counts versus estimates.** Where the paper says a figure is estimated rather than measured --- the
  0.6 cache factor of A.1 above all --- that hedge is load-bearing and is the reason the appendix is
  publishable as it stands. Do not tidy it away.
- **`\ifanonymous` guards.** Three of them, and each wraps something that identifies us: the author
  block, the repository URL, and the acknowledgments, which name three people. Editing inside a guard
  is fine; unwrapping one puts that material into the review PDF.
- **Citations carry a tie, `~\cite{...}`.** 21 of the 22 do; the odd one begins its own line. SIAM's
  own example spaces every citation, and the tie additionally stops one being orphaned at a line
  break. A rewrap is what would silently drop these.
- **`\log` and `\ln`, never bare `log` or `ln` in math.** Bare ones set as juxtaposed italic
  variables rather than as upright operators. Nine were fixed on 2026-09-11; there are none left.

After any prose pass, the checks worth re-running are: every table caption and column header for
ambiguous algorithm names; all bare `HuskySort` uses, classified family-level or specific; the four
collation terms above; citation and reference integrity; and `./paper/build.sh`, reading the length
off the **anonymous** PDF.

One audit is deliberately being held until the pass is finished, because the pass can move its
subject matter: **every numeral against its source.** Table figures against the JMH JSON, multipliers
against the tables they derive from, counts like "three lines of work" or "eleven rows" against what
is actually listed, and corpus sizes against the data-source section. The defects that prompted it
all had one shape --- an edit landed and the number beside it did not follow: "two well-known lines of
work" after a third was added, the worked example still quoting pre-PR64 timings, machine B's cache
row taken from the wrong `sysctl`, and the 16.5-page figure left in Sai Vineeth's task list for days
after it became false.
