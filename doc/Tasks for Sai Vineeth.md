# Tasks for Sai Vineeth

Two rounds. **Round 2 is the live one**; round 1 is the submission round, kept below as the record of
how the paper was built and anonymised.

---

# Round 2 — camera-ready, due 2026-11-23

**Contingent on acceptance.** ACDA27 notifies on **2026-11-02**; camera-ready is **2026-11-23**, so
there are three weeks and they are the only three weeks. Nothing below is research. All of it fails
quietly if nobody owns it, which is why it is written down. Recorded as item 57 of `TODO.md`.

**Read first:** `doc/AI use and the SIAM disclosure policy.md`, and item 19 of `TODO.md` for the
requirements. One correction to carry into everything: **the page limit is 12 pages excluding
references**, not the 16 we worked from for weeks. The appendix is explicitly outside the limit —
SIAM says it is "read by the program committee members at their discretion", so length there costs
attention rather than compliance.

## 2.1 Verify all 23 bibliography entries against source — **the one with real risk**

`paper/sample-base.bib` was reconstructed with AI assistance in `1f0697a`, because leaving `acmart`
required regenerating the bibliography and no `.bib` existed. SIAM's AI policy names fabricated
references as the first of its four failure modes, so this is the item a referee could actually
catch us on.

What was done right at the time, so you know what you are and are not re-checking:

- entries were produced **by transcription from the existing `.bbl`**, not from a model's memory of
  the literature;
- the citation keys are identical before and after, so nothing was invented and nothing lost;
- two thin entries were marked INCOMPLETE rather than guessed at — Jugé's *Adaptive Shivers Sort* and
  Zhang, Meng and Liang's *Sort Race* — and both have since been completed.

**What that does not cover, and what you are actually doing.** Transcription guarantees fidelity to
the *previous bibliography*, not to the publications. Any error already in the `.bbl` was carried
forward faithfully. And it gives no coverage at all of SIAM's sharpest wording — references that
"cite results not actually present in the source paper". So for each of the 23 entries:

1. Resolve the DOI and confirm title, authors, venue, year and pages against the publisher's record.
   Crossref (`https://api.crossref.org/works/<doi>`) is the quickest authoritative source and returns
   the publisher's own metadata.
2. Find where the paper cites it, and confirm the source **says what we say it says**. This is
   per-citation, it cannot be done mechanically, and it is the part that matters.

### A first pass has been done — here is what it cleared, and what it did not

Run 2026-10-19 against Crossref. **Do not repeat the DOI resolution; start from step 2.**

Of 25 entries, 13 carry a DOI. **Twelve resolve and their titles match ours.** The thirteenth did
not, and the error is instructive:

> `1064546.1180622` — Sinha and Zobel, *Cache-Conscious Sorting of Large Sets of Strings with
> Dynamic Tries*. The DOI in the entry, `10.1145/1064546.1180622`, resolves to a **different real
> paper**: Sinha, Ring and Zobel, *Using random sampling to build approximate tries for efficient
> string sorting*, JEA vol. 10 (2005) — a neighbouring article in the same journal. Author, title,
> journal, volume, year and article number were all correct; only the identifier was wrong.
> **Corrected** to `10.1145/1005813.1041517`, confirmed through Crossref.

That is exactly the error transcription from a `.bbl` carries forward faithfully, and exactly what
SIAM's first failure mode describes. It is also the reason step 2 below cannot be skipped: a wrong
identifier on an otherwise correct entry is invisible to every check except resolving it.

One further note: `10.5555/313559.313859` (McIlroy, *Optimistic Sorting*) returns 404. The `10.5555`
prefix is ACM's placeholder namespace for items without a real DOI, so this is probably not an error
— but confirm the paper exists and that the entry's details are right, and consider dropping a DOI
field that resolves to nothing.

**The twelve entries with no DOI were not cleared and need eyes.** Crossref is the wrong instrument
for most of them — a textbook (CLRS), web pages (Python's `listsort.txt`, Yaroslavskiy's dual-pivot
PDF), arXiv preprints (HuskySort, *Sort Race*, the Hash sort report), a dataset (SF building
permits), a GitHub corpus (Chinese-Names-Corpus), and conference proceedings not indexed there
(Bentley and Sedgewick, LREC). Check each against its actual source by hand. The arXiv ones are
quick; the web pages need a retrieval date that is still accurate.

Two entries added 2026-10-19 — López-Ortiz et al., *JEA* 10 (2005), and Antoshenkov, Lomet and
Murray, ICDE 1996 — were Crossref-resolved when added. They need only the ACM article number for the
first, which Crossref does not carry; ACM's own BibTeX export has it.

## 2.2 Confirm the live SIAM AI policy, and place the disclosure

Our copy of the policy came via Robin, because siam.org returns 403 to automated fetches. Confirm the
wording, and in particular **where the disclosure must appear** — publishers differ between a methods
section, a separate disclosure statement, and the acknowledgments.

A draft statement is in `doc/AI use and the SIAM disclosure policy.md`. Place it according to what
the policy actually says. It is a *disclosure*, not a thank-you: acknowledgment credits a
contributor, disclosure states what tool was used and for what, and only the second is being asked
for.

## 2.3 The acknowledgments

They are wrapped in `\ifanonymous`, so they never appeared in the review copy — the camera-ready is
the first build that shows them. Cutting them frees **no page space at all**: measured, the spill
threshold is identical with and without, because they sit after the body prose. So this is purely a
question of who should be credited.

The current line names Darshan Dedhia, Akshay Bhusare and Kartik Kumar. For context: Dedhia's single
2020 commit implemented the first version of the encoded-exactly test in `HuskyCoder` and
`HuskySequenceCoder` — the direct ancestor of the `exactlyEncodable` machinery that items 48, 55 and
56 are about, and that two of Yunlu's benchmark runs were spent on. Bhusare's commit is the maven
profiles still in `pom.xml`. Kartik Kumar has no commits in this repository. Robin's call.

## 2.5 Read the paper for voice: does any paragraph read as machine-written?

Robin's request, 2026-10-19. A substantial part of the text was drafted with Claude. He and Claude
write similarly enough that it mostly blends, but he wants to know whether **any paragraph screams
that a machine wrote it**. This needs someone who is neither of them, which is why it is your task
and not Claude's — self-assessment is exactly what does not work here.

**First, be clear what this is and is not for.** We are *disclosing* the AI use (§2.2), so the goal
is not concealment and nothing here should be done in that spirit. The goal is that the paper reads
as one voice and reads well. A paragraph that is bland, over-signposted or mechanically balanced is
a bad paragraph whoever wrote it; the AI question is just a good lens for finding them.

**Do not use an AI-detector tool.** They are unreliable on technical prose, their false-positive
rate on edited academic writing is high, and a score would tell us nothing we could act on. Read it.

### You do not have to guess which parts are which

The repository records it. 296 of the 580 commits on `parallel-redesign` end with a
`Co-Authored-By: Claude` trailer, so authorship is attributable line by line:

```bash
git blame -w --line-porcelain paper/RadixHuskySort.tex | grep '^commit ' | sort -u
```

Map each blank-line-separated paragraph to the commits touching its lines, then mark the paragraph
AI-involved if any of those commits carries the trailer. That turns the whole exercise from a
guessing game into a targeted read of a known subset. Ask Claude to generate that mapping if it
would save time — producing the list is mechanical and is not the part that needs your judgement.

Expect the newest material to be the most exposed: the parallel-baseline section and the conclusion
were rewritten on 2026-10-19 (`e1ef6ae`), and the $p_{crit}$ and coding sections were edited the same
day (`6322144`). The oldest body text is largely Robin's and derives from the 2020 paper.

### What to look for

Concrete tells, rather than a general impression. Claude's own habits in this repository, which it
named when asked:

- **Em-dash density.** The paper uses `---` heavily. Some is Robin's style; a paragraph with three
  or more is worth a second look.
- **The rule of three.** "Two conclusions follow", "three properties account for it", triads of
  clauses where two would do.
- **Antithesis as a default rhythm.** "not X, but Y", "X rather than Y", "the design working, not
  failing". One is fine. Several in a paragraph is a tic.
- **Signposted conclusions** — "the honest summary", "that is the finding rather than a caveat on
  it", "worth stating plainly" — where the sentence could simply make the point.
- **Uniform paragraph and sentence length.** Human technical prose is lumpier.
- **Hedges that carry no information**: "it is worth noting that", "importantly", "notably".
- **Explanations of the obvious** immediately after a clear statement.

### What to report

A list of paragraph references with a one-line verdict each — fine / slightly off / rewrite — and
for anything flagged, which tell triggered it. Robin rewrites; you diagnose. A short note on whether
the paper reads as one voice overall is worth as much as the individual flags.

## 2.4 Check the camera-ready build and its artifact

The repository link switches automatically with `\ifanonymous`, from
`anonymous.4open.science/r/radix-proxy-key-sort-artifact-D8B1/` to `github.com/rchillyard/HuskySort`,
so there is nothing to edit. The task is to confirm that the public repository **is** public, **does**
contain the artifact the paper describes, and matches the version measured. Round 1's §2 below is the
record of what the anonymous mirror contained and why.

---

# Round 1 — ACDA27 submission, due 2026-09-15 (historical)

**Task 1 is done** — Robin downloaded the macros himself on 2026-09-08, and the conversion has been
trialled successfully. **Task 2 is the only thing outstanding for you**: the anonymised code mirror,
which needs an account and so is not something that could be done for you. Task 3 is optional and only
if there is time before the 15th.

---

# 1. Fetch the SIAM proceedings macros — **DONE 2026-09-08, by Robin. Nothing to do.**

Robin found and downloaded the complete package to `paper/siamproceedingsmacros_022425/`:
`siamproceedings.sty`, `siamplain.bst`, `example_doublecolumn.tex` and `.pdf`, the single-column
equivalents, and the example figures and bibliography.

**This is done.** The paper itself now uses the class: `paper/RadixHuskySort.tex` is
`siamproceedings` throughout, with `siamproceedings.sty` and `siamplain.bst` committed beside it, and
`paper/build.sh` produces both the identified and the blind PDF from that one source. The changes it
took are recorded as §0l and §0c of `paper/Paper edits pending.md`. The scripted trial that proved
the conversion, and the script itself, have been removed now that the real source does the job.

The length problem that trial exposed — 16.5 pages of body against a limit of 12 — is resolved too:
the body is **11.79 pages**. It came down by moving material to the appendix, which SIAM permits and
does not print, by reframing the paper around RHSort rather than re-explaining HuskySort, and by one
float-placement parameter that was worth 0.61 pages on its own.

Two smaller notes for whoever does the switch:

- `siamproceedings.sty` and `siamplain.bst` sit one directory below `paper/`, where `acmart.cls`
  currently sits. Copy those two files up into `paper/` at switch time, or add the package directory to
  `TEXINPUTS` and `BSTINPUTS`.
- On TeX Live 2020 the package needs a two-line shim: it calls `\AddToHook`, added to the LaTeX kernel
  in 2020-10-01, and TL2020 carries 2020-02-02. See §0l. On a current distribution the shim is
  unnecessary.

---

# 2. Create an Anonymous GitHub mirror, over `src/` only

ACDA27 runs lightweight double-blind review for proceedings papers, and does not merely permit a code
link but encourages one:

> We strongly encourage also making code and data available; please keep in mind the double-blind
> nature of the review process. We recommend including a link to an anonymized version that makes a
> 'best-effort' to avoid revealing the identity of the authors (e.g., using Anonymous Github or an
> anonymous Dropbox/Google Drive folder).

So the link in the paper must be **replaced, not deleted** — the same page says nothing should be done
for anonymity that weakens the submission, and for a paper about an implementation a missing code link
would.

**Decision taken 2026-09-07: Anonymous GitHub over `src/` only.** <https://anonymous.4open.science>
takes a repository plus an exclusion list.

## What to exclude, and why it matters

Anonymous GitHub rewrites a repository's *name and owner*. It does not rewrite file contents. These
paths name Robin, or Northeastern, or both, throughout — the working documents of this revision are the
worst offenders, being written in terms of what Robin asked for:

```
paper/          the paper itself, author block included, plus the working documents
doc/            run requests and results; they address Yunlu by name and quote Robin's decisions,
                and now also Audit against INFO6205.md
logs/
TODO.md
.idea/
target/
dependency-reduced-pom.xml
```

What should remain is `src/` and `pom.xml` — the code the paper is actually about.

## What is already done

Three identifying strings were removed from the Java sources on 2026-09-07: a test that sorted an array
of `Person` objects built from Robin's family and named Yunlu, and two comments naming Robin. `grep`
over `src/**/*.java` for `rchillyard`, `hillyard`, `northeastern` and `robin` now returns nothing, and
the suite is green at 396 tests.

## What is not, and is accepted

`pom.xml` still declares `<groupId>edu.neu.coe.seis</groupId>`, and every source file sits under
`package edu.neu.coe.huskySort` — `neu` and `coe` being Northeastern and its College of Engineering.
That is a hint rather than a signature, and ACDA27's bar is explicitly best-effort: the purpose "is not
to make it impossible for them to discover the authors if they were to try". See task 3.

## Then

Send the URL and it is a one-line change. The link now sits as a sentence at the head of
§Implementation — "The implementation is available at ..." — having been moved there on 2026-09-08 when
the author footnotes were deleted. It carries a comment marking it for anonymisation, because in the
body it will not be caught by a sweep of the front matter.

---

# 3. Optional, if there is time before the 15th: rename the package **and the Maven groupId**

Robin's view, 2026-09-07: there is no good reason to carry that much identifying information in the
package names, quite apart from the anonymity question. Worth doing on its own merits, but only if
task 1 and task 2 are done and the paper is otherwise ready.

**There are two identifiers to change, not one.** An IDE's "rename package" refactor will handle the
first and silently leave the second, which is why it is called out separately here:

| identifier | where | how many |
| --- | --- | ---: |
| `package edu.neu.coe.huskySort` | `src/**/edu/neu/coe/` | **164 Java files**, plus the directory tree and every import |
| `<groupId>edu.neu.coe.seis</groupId>` | `pom.xml` line 10 | **1 line** |

`neu` is Northeastern and `coe` its College of Engineering, so both say the same thing; renaming the
package while leaving the `groupId` would achieve nothing. Note the `groupId` is not even the same
string as the package — it ends `.seis`, not `.huskySort` — so a global find-and-replace of the package
name will miss it. It also propagates into build output (`target/maven-archiver/pom.properties`) and
would appear in any published artefact coordinates.

Nothing else references either: no properties files, no resources, no configuration. Every other
occurrence found was under `target/`, which is regenerated.

The risk is not the rename but forgetting to rerun the suite afterwards, which should stay at **396
passing**.

**Do not do this on the same commit as anything else**, and do not do it after the SIAM template switch
has been made but before the paper builds cleanly — an unrelated 164-file diff in that window would
make any real problem much harder to see.
