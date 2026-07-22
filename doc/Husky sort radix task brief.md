# Task Brief: Prototype a Radix-Sort Variant of Huskysort

## Background

This repo implements **Huskysort**, described in this paper: https://arxiv.org/abs/2012.00866
(originally submitted to SIAM ACDA21 and rejected — reviewer comments below).

The algorithm sorts arrays of expensive-to-compare objects (Strings, dates, BigDecimal, etc.)
by:

1. Encoding each object to a 64-bit `long` ("husky code") that is order-preserving (or nearly so).
2. Sorting the array of `long`s, swapping the corresponding object references in lockstep at
   every swap (currently via Introsort — quicksort with a heapsort fallback for pathological
   cases, per Musser 1997).
3. If the encoding wasn't provably "perfect" for every element, running a final cleanup pass
   (Timsort or insertion sort) on the object array to fix any remaining inversions.

## The idea to test

One of the paper's four SIAM ACDA21 reviewers asked a pointed question that was never answered:

> "The question that nags me is why the authors (and the algorithm) do not use radix sort on
> the 64-bit codes. Is quicksort faster than radix sort on 64-bit integers? This seems
> strange. Even if you cannot do the swaps on the objects during radix sort (I am not sure
> this is not possible), you can get a representation of the permutation at the end and
> permute the objects accordingly. This requires an explanation." — Reviewer 3

This is worth taking seriously for two reasons:
- Radix sort on fixed-width keys is **O(N)**, not O(N log N), so it should scale better as N
  grows — which is exactly Huskysort's stated goal (move work from linearithmic to linear).
- The reviewer's specific suggestion — don't swap the (possibly heavy) object references
  during the digit-by-digit passes; carry only a cheap `int[]` index array through radix sort,
  then apply the resulting permutation to the object array **once**, in a single O(N) pass —
  avoids the current design's cost of moving object references at every swap during the
  linearithmic phase.

## What's already been done (starting point, not a finished answer)

I built a standalone, repo-independent prototype to sanity-check the idea before touching
real code. It's attached as `RadixVsQuickBenchmark.java`. It compares, on synthetic random
`long[]` keys with a `String` payload:

- **A: Introsort-with-payload-swap** — a from-scratch quicksort/heapsort hybrid that swaps
  the payload reference at every key swap (structurally the same idea as the repo's current
  approach, though not literally the repo's code).
- **B: LSD radix sort, 8-bit digits (8 passes), deferred permutation** — sorts a biased copy
  of the keys (sign bit flipped so unsigned digit order matches signed numeric order),
  carrying only an `int[]` index array through the passes, then builds the final object array
  in one O(N) pass using that index.
- **C: same as B but with 16-bit digits (4 passes)** — trades more memory per pass (65,536
  buckets vs. 256) for fewer passes.

Results on a small, noisy sandbox (1 vCPU, ~4GB RAM — treat as directional only):

| N | quicksort (ms) | radix8 (ms) | radix16 (ms) | speedup (r8) | speedup (r16) |
|---|---|---|---|---|---|
| 10,000 | 0.20 | 0.00 | 1.40 | n/a | 0.14x |
| 100,000 | 17.0 | 5.4 | 9.4 | 3.15x | 1.81x |
| 1,000,000 | 106.2 | 58.2 | 66.8 | 1.82x | 1.59x |
| 5,000,000 | 610.4 | 555.6 | 344.2 | 1.10x | 1.77x |

Radix won at every size tested, and the win didn't shrink as N grew (unlike some of the
original paper's quicksort-vs-Timsort comparisons, which reviewers flagged for exactly that
shrinking-benefit pattern). The 16-bit-digit variant looks like it starts winning out at
larger N, consistent with fewer/wider passes amortizing better — worth confirming properly
rather than trusting two arbitrary digit widths.

**One bug worth knowing about**: my first draft of the quicksort had an off-by-one in the
Hoare-partition recursion bounds that silently dropped elements near the pivot from both
recursive branches. Large arrays looked "mostly sorted" to a casual glance but weren't. I only
caught it by stress-testing against `isSorted()` on many small random arrays before trusting
timing results on millions of elements. **Recommend doing the same for any new sort
implementation here** — a quick correctness stress test before any performance claim.

## What this prototype does NOT tell us (i.e., what you need to find out)

- It uses **random 64-bit longs directly** — not the actual `huskyEncode`/`Coding` pipeline,
  and not real strings. It says nothing about whether this holds up on the repo's actual
  English/Chinese word corpora (Leipzig Corpora Collection) or numeric types (Integer, Double,
  Long, BigInteger, BigDecimal, Tuples).
- It doesn't touch Step 1 (encoding cost) or Step 3 (cleanup pass) — only Step 2, the sort of
  the longs.
- It doesn't test adversarial/skewed distributions. Reviewer 4 (a separate, quite important
  critique from the same review round) pointed out that it would be "trivial to construct
  inputs that cause the proposed scheme to perform poorly," and that the paper never showed
  what happens when the encoding is bad, or how to construct a good encoding automatically
  from a data sample. If you have time, it's worth testing radix sort's behavior specifically
  under those adversarial conditions too — it may behave differently from quicksort when many
  keys collide in their upper bits (which happens if the husky code has poor entropy in its
  more-significant digits).

## Concrete tasks

1. **Locate the current sort-the-longs step** in the repo (should be in the Introsort/dual-
   pivot-quicksort implementation described in the paper's Section 3 / Algorithm 1). Confirm
   how it currently swaps the `long[]` and the object array together.

2. **Adapt the deferred-permutation radix sort** from `RadixVsQuickBenchmark.java` to the
   repo's actual types (`Coding`, `huskyCode` output, whatever generic `X extends
   Comparable<X>` structure is in use) rather than raw `long[]`/`Object[]`. Handle the sign-bit
   flip correctly for whatever range husky codes actually occupy (check whether the existing
   encodings are already non-negative — if so the flip may be unnecessary, but should be
   verified rather than assumed).

3. **Wire it in as an alternative sort strategy**, not a replacement — the existing
   Introsort path should stay so the two can be benchmarked side by side using the repo's
   existing benchmark harness and existing test data (English words, Chinese words, numeric
   types, Tuples, etc. — see the paper's Section 4.1 and Figures 6-9 for what was tested
   originally).

4. **Stress-test correctness first**, on small arrays, before trusting any timing numbers
   (see the bug note above). Confirm the final object ordering exactly matches
   `Arrays.sort`/a trusted comparator-based sort on the same input, not just that the longs
   ended up sorted.

5. **Sweep digit width** (e.g., 8-bit/8-pass, 11-bit, 16-bit/4-pass) rather than assuming one
   is best — the crossover point likely depends on N and on cache size.

6. **Re-run the existing benchmark suite** (Tables 1-4, Figures 5-9 in the paper) with the
   new radix path included, on real hardware (not a shared sandbox), ideally on a modern LTS
   JDK (11+ — the original paper used the quite old 1.8.0_152, which one reviewer flagged) and
   with proper JMH warmup rather than manual timing loops if that's not already the harness in
   use.

7. **Check stability as a side benefit**: LSD radix sort via counting sort is inherently
   stable, which may simplify or remove the paper's discussion of quicksort's instability
   (Section 6.1) if radix sort becomes the primary path.

8. **If time allows**, construct at least one deliberately adversarial input (e.g., strings
   sharing a long common prefix, or a distribution where the husky code collapses to few
   distinct high-order bits) and compare how gracefully radix sort vs. quicksort degrade —
   this speaks directly to Reviewer 4's unanswered question.

## Deliverable

A short write-up (numbers + a few sentences) on:
- Whether the radix-sort-with-deferred-permutation approach beats the current Introsort
  approach on the repo's real data, across the sizes and types already benchmarked in the
  paper.
- Which digit width wins and at what N.
- Whether it changes the story around adversarial inputs at all.

This is groundwork for a resubmission, not a finished paper section — the goal right now is
just to get a real, repo-grounded answer to Reviewer 3's question.