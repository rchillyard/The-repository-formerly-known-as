# The second paper — working notes

Opened 2026-10-19. This exists because the RadixHuskySort paper, submitted to ACDA27 on 2026-09-15,
reached its 12-page limit while the measurement programme kept producing findings. Material lands
here when it is **good, and not this paper's to make**: either the referees have not seen it, or it
needs work we have not done.

Nothing here is committed to. The venue question is open, and the first real decision point is
**2026-11-02**, when ACDA27 notifies. A reject or a major revision reopens the option of splitting
the material differently, or of merging this back.

**Rule of thumb for what belongs here.** If adding it to the camera-ready would show the referees an
argument they did not review, it belongs here. If it corrects something the paper states wrongly, it
belongs in the camera-ready (item 43 of `TODO.md`).

## The spine: relaxed order preservation as a design principle

The first paper now **names** the property --- *approximately order-preserving*, or
*$p$-order-preserving* --- but only names it. It is inherited from the 2020 HuskySort work, so the
first paper is right not to claim it. **The second paper can claim what follows from it**, which
nobody has written down: that relaxing exactness is a design lever with a measurable price, and that
choosing where to sit on it is a real engineering decision.

That is a better title than anything the first paper could carry. See the title discussion of
2026-10-19: retitling the first paper around this would claim as novel something largely inherited.

### B2 --- masking versus saturation (from `TODO.md` item 43B, recorded 2026-09-22)

The worked example for the spine, and the item's original content:

- **Masking is the simplest thing that works.** `c & mask` is one instruction and is exactly
  order-preserving *inside* the window --- for `englishCoder` every character in 64..127, which is
  all of A--Z and a--z. Outside it wraps, so an apostrophe (39) collides with `g` and e-acute (233)
  sorts as `i`. Approximately order-preserving, not order-preserving.
- **Saturation** (`clamp(c - offset, 0, width-1)`) is monotonic over all 65,536 characters, verified
  exhaustively, and costs a compare-and-select per character. **But per character only** --- on
  strings it is not, because a tie at one position lets the next decide (item 48). The honest claim
  is that an out-of-window character costs a *tie* rather than a *mis-ordering*, which is cheaper for
  Timsort but is not exactness.
- **The measured trade.** Saturation costs roughly 25 ms per million to encode on an eight-core Mac,
  and 1.7--3.0x the masking encode on Yunlu's Graviton, and buys nothing measurable in cleanup.
  Masking wins: encode-plus-cleanup is 111.1 ms masking against 136.9 saturating.
- **A point that sharpens it.** On the english corpus the two coders fail on *exactly the same words*.
  The splitter emits letter-only tokens, so no token holds an apostrophe, hyphen or digit, and the
  only characters reaching outside either window are letters at or above 128. The "don't"/"dongt"
  collision is real arithmetic that the corpus never exercises. **Say this in terms of the coder's
  window against the corpus's character repertoire, not in terms of punctuation** --- which is also
  item 43C, and that one-sentence version does go in the first paper.
- **The conditional, which is the interesting part.** All of this holds **because the cleanup is
  Timsort**. With an adaptive insertion sort, masking's 86M inversions would cost of the order of a
  second and saturation would be mandatory. **Coder choice and cleanup choice are coupled**, and
  that coupling is a result in its own right --- it is what makes "how approximate should the code
  be?" a question with a non-obvious answer.

### What the first paper leaves as future work, and this one could close

All named in the first paper's conclusion after the 2026-10-19 rewrite, so taking them up is
continuation rather than overlap:

- **Parallelise the permutation.** Three of four phases now divide; the permutation is an independent
  scatter under a bijection and nothing resists it, we simply have not done it.
- **A rule for when the parallel cleanup pays.** Measured: it takes chinese at 200,000 from 0.64x to
  1.10x against `Arrays.parallelSort` and chinese at a million from 1.15x to 1.49x, but leaves
  chinesenames unchanged within intervals. The deciding quantity is residual disorder and $N$ alone
  does not express it, which is why the first paper ships it opt-in with no rule. `CleanupPassBenchmarks`
  parameterises the right thing; the rule should come from its `parallelTimsortCleanup` arm.
- **The sequential histogram-combine.** Its work grows with thread count while the parallel work
  shrinks, so the design should fail wherever buckets times threads approaches $N$ --- and
  \S~`sec:parallel-baseline` finds exactly that on the permits. Repairing it is a concrete piece of work.
- **The pinyin coder.** chinesenames gains most from a parallel cleanup because its coder is worst:
  the radix phase cuts its run count only 3.1x against english's 62x. Items 10 and 11, now
  quantitatively motivated.

### Open questions worth reporting even unresolved

These are good material precisely because they are unexplained, and the first paper has no room:

- **F3.** An arm that executes what `rev` executes on these arrays reads 1.034 / 1.044 of the old jar
  on masking where `rev` reads 0.998 / 0.991. Two variants performing the same instructions differ by
  4 % because C2 compiles the enclosing method differently. The sharpest illustration we have that a
  loop's speed is a property of its compiled form.
- **The chinese 32,000 residual**, which reverting line 50 does not remove.
- **8 chinese rows at 200,000** reading 1.018--1.048 of request 12 with disjoint CIs against baselines
  at 0.993--1.018.
- **The saturating/masking asymmetry**, where both our guess and Robin's were refuted. See item 43H
  and the request-13 report.

### Code work that would have to come first

- **Item 56**, the third shape for the exactness test: make the character window two final fields so
  the predicate stops being virtual. Parked until after the review because it re-invalidates the
  request-13 rows. If the second paper re-measures anyway, the objection disappears.
- **Item 42**, no parallel QuickHuskySort, and the strategic reasons it should stay that way.

## Methodology, which both papers need

Item 43H goes in the first paper as a few sentences. The full account --- the fork as the unit of
replication, why JMH's own interval is too narrow for a per-JVM compilation effect, and why a
two-variant comparison inside one JVM cannot see one at all --- is longer than that and is worth
writing once, properly. The record is item 43H plus `doc/Run results from Yunlu 2026-10-18.md`.

## People

Yunlu Liaozheng has run every benchmark quoted in either paper, on his own hardware, and several of
the findings above are his rather than ours --- the encode penalty, item 48, the gate design. Any
second paper has to settle authorship before it has content, not after.
