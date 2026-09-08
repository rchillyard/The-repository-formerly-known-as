# Tasks for Sai Vineeth — ACDA27 submission, due 2026-09-15

**Task 1 is done** — Robin downloaded the macros himself on 2026-09-08, and the conversion has been
trialled successfully. **Task 2 is the only thing outstanding for you**: the anonymised code mirror,
which needs an account and so is not something that could be done for you. Task 3 is optional and only
if there is time before the 15th.

---

# 1. Fetch the SIAM proceedings macros — **DONE 2026-09-08, by Robin. Nothing to do.**

Robin found and downloaded the complete package to `paper/siamproceedingsmacros_022425/`:
`siamproceedings.sty`, `siamplain.bst`, `example_doublecolumn.tex` and `.pdf`, the single-column
equivalents, and the example figures and bibliography.

The conversion has since been trialled and **it works** — zero errors, zero undefined references, using
SIAM's own preamble and `siamplain.bst`. The working trial is `doc/HuskySort-siam-trial.tex`, and the
nine changes it took are recorded as §0l of `paper/Paper edits pending.md`.

**The one hard finding from that trial:** the paper is **16.5 pages of body against a limit of 12**.
SIAM's template forbids changing the margins, page size, font or the 10pt size, so there is no denser
configuration to reach for. Four and a half pages have to come out of the main part. That is a decision
about the paper, and Robin has it.

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
