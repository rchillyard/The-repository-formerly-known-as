# Tasks for Sai Vineeth — ACDA27 submission, due 2026-09-15

Two things, both needing an account or a download, which is why they are not done already. Task 1
blocks the change of document class and is the critical path. Task 2 is required for double-blind
review. A third item is optional and only if there is time.

---

# 1. Fetch the SIAM proceedings macros

Everything else on the critical path is done; this is the last thing blocking the change of document
class, and it is a download rather than a judgement call.

## Why it is needed

ACDA27's submissions page requires it **at submission**, not on acceptance:

> Submissions should use the LaTeX macros at the web page:
> https://www.siam.org/publications/proceedings/

The paper is currently `\documentclass[acmtog]{acmart}` — ACM's class, in its Transactions on Graphics
variant. Every trace of ACM identity has been suppressed so the PDF does not claim to be something it
isn't, but the class itself still has to go.

## What to get

**`soda2e_022817.zip`**, from <https://archive.siam.org/proceedings/macros.php>, or the same package on
CTAN at <https://ctan.org/tex-archive/macros/latex/contrib/siam> (there as `soda2e.all`).

It contains two files:

| file | what it is |
| --- | --- |
| `ltexpprt.sty` | **the macro file — this is the one that matters** |
| `ltexpprt.tex` | an example document and the documentation |

## Two ways to pick the wrong file

1. **`siamart` is not it.** SIAM also publishes `siamart220329.cls` / `siamart190516.cls` for its
   *journals*. Those are single-column and are the wrong series. We want the double-column
   **proceedings** macro, which is `ltexpprt.sty`.
2. **The class name `soda2e.cls` does not exist**, despite being referred to in places. The
   distribution is `soda2e.zip` / `soda2e.all`, and what comes out of it is a `.sty`, used as
   `\documentclass[twoside]{article}` plus `\usepackage{ltexpprt}`. Do not go looking for a `.cls`.

## Where to put it

`paper/`, alongside `acmart.cls`, which is already vendored there — so the repository stays
self-contained and anyone can build the paper without installing anything. Commit `ltexpprt.sty` and
`ltexpprt.tex` both; the example file is worth having when the front matter needs rewriting.

Nothing needs installing into a TeX tree. It is not present in this machine's TeX Live 2020
(`kpsewhich ltexpprt.sty` finds nothing, and there is no `siam` directory under `texmf-dist`), and
`tlmgr` is not available here, which is why it has to come in by hand.

## What happens once it lands

In this order, because the last step depends on the others:

1. Build under the new class and fix what breaks. Expect the acmart-specific front matter to go:
   `\setcopyright`, `\acmJournal`, `\acmVolume`/`\acmNumber`/`\acmArticle`, `\settopmatter`,
   `\footnotetextcopyrightpermission`, and the `\ps@plain` redefinition that prints the draft
   revision stamp.
2. Regenerate the bibliography. **This now works** — `paper/sample-base.bib` was reconstructed on
   2026-09-07 and BibTeX runs clean over it. It did not exist before, which is what had made a class
   change impossible: the committed `.bbl` depends on acmart's own `\bibfield`, `\bibinfo`,
   `\natexlab`, `\showeprint` and `\urldef`, and gives twelve undefined control sequences outside that
   class.
3. Re-measure the page count. The body currently runs 12.46 pages against a limit of 12, but that is
   an acmart number and will not survive the reflow. Measure by the **last body float**, not by where
   `REFERENCES` begins — a `table*` can float past the references and be missed. Concretely: take the
   page of the last `tab:` label in `HuskySort.aux` that belongs to the body, then confirm with
   `pdftotext -f N -l N -bbox` that the acknowledgments start at the top text margin of the next page.
4. Then trim the remainder, once, against the layout we actually submit.

## Also worth knowing

The appendix does not count against the twelve pages — ACDA27 reads it at the program committee's
discretion and it is not printed in the proceedings. Three sections have been moved or written there
for that reason, and it remains the cheapest place to put anything that is detail rather than argument.

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
doc/            run requests and results; they address Yunlu by name and quote Robin's decisions
docs/           Audit against INFO6205.md
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

Send the URL, and the one-line change at `paper/HuskySort.tex:206` can be made — it currently reads
`\url{https://github.com/rchillyard/HuskySort}` inside an `\authornote`, which is going to be rewritten
anyway when the document class changes.

---

# 3. Optional, if there is time before the 15th: rename the package

Robin's view, 2026-09-07: there is no good reason to carry that much identifying information in the
package names, quite apart from the anonymity question. Worth doing on its own merits, but only if
task 1 and task 2 are done and the paper is otherwise ready.

**Scope, measured rather than estimated:**

| what | how many |
| --- | ---: |
| Java files under `src/**/edu/neu/coe/` | 164 |
| `pom.xml` `<groupId>edu.neu.coe.seis</groupId>` | 1 |
| resource or config files referencing the package | **none** |

So it is a package rename plus one line of `pom.xml`, with nothing hiding in properties files or
resources to catch anyone out. Every other reference found was under `target/`, which is regenerated.
An IDE refactor does it in one operation; the risk is not the rename but forgetting to rerun the suite,
which should stay at 396 passing.

**Do not do this on the same commit as anything else**, and do not do it after the SIAM template switch
has been made but before the paper builds cleanly — an unrelated 164-file diff in that window would
make any real problem much harder to see.
