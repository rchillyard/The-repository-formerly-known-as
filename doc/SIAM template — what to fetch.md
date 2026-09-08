# The SIAM proceedings macros — what to fetch, and where to put it

For Sai Vineeth. Everything else on the critical path is done; this is the last thing blocking the
change of document class, and it is a download rather than a judgement call.

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
discretion and it is not printed in the proceedings. Two sections have already been moved there for
that reason (the cleanup-pass scaling analysis and the building-permits case study), and it remains
the cheapest place to put anything that is detail rather than argument.
