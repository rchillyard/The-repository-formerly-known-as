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

**`siamproceedings.sty`** — version 2.0, dated 15 January 2025, from
<https://www.siam.org/publications/proceedings/>.

> **Corrected 2026-09-08.** An earlier version of this note sent you after `ltexpprt.sty` in
> `soda2e_022817.zip`. Robin found that `siamproceedings.sty` has superseded it, and he is right: the
> new file's own header says it is "based on ltexpprt.sty and siamart250106.cls" and identifies itself
> as a "Revision of SIAM Proceedings macros for use with LaTeX 2e". The package I named is from 2017.
> Get the 2025 one.

The package consists of six files, and we want at least the first three:

| file | what it is |
| --- | --- |
| `siamproceedings.sty` | **the macro file** |
| `siamplain.bst` | **the BibTeX style — we need this too, see below** |
| `example_doublecolumn.tex` | the template to follow; it settles which `\documentclass` and options to use |
| `example_singlecolumn.tex` | the single-column variant, not what we want |
| `example_references.bib`, `example_fig1.eps` | example inputs |

It is a **package**, used with `\usepackage{siamproceedings}`, not a document class — so there is no
`.cls` to look for. That is worth knowing because searching for a class file is how the previous
attempt at this went wrong.

## Why `siamplain.bst` matters as much as the `.sty`

The paper's bibliography is currently produced by `ACM-Reference-Format.bst`, which comes with acmart
and emits a `.bbl` full of acmart-only macros. Under the SIAM package that `.bbl` will not compile, so
the bibliography has to be regenerated with `siamplain.bst`.

**That is possible now and was not a week ago.** `paper/sample-base.bib` did not exist until it was
reconstructed on 2026-09-07 — BibTeX had been failing silently and the committed `.bbl` was the only
copy of the bibliography anywhere. So grab `siamplain.bst` along with the `.sty`, or the references
will disappear at the first `bibtex` run with nothing to rebuild them from.

## One earlier warning in this note was overstated

It said `siamart` is "not it" and is "the wrong series". That was too strong: `siamproceedings.sty` is
itself derived from `siamart250106.cls`. What remains true is that we want the *proceedings* package
rather than the journal class, and that ACDA27's submissions page points at SIAM's proceedings macros
page specifically.

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
