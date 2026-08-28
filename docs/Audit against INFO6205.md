# Audit of this repository against the INFO6205 findings

Written 2026-08-28, before the paper's resubmission.

A long audit of the sibling `INFO6205` repository turned up a number of defects in
its sorting instrumentation, several of which had changed published figures. The
two lineages diverged long ago, so each defect had to be checked against this tree
separately rather than assumed present.

**Conclusion: the paper's statistics are not affected by any of them.** The
detail matters, though, because in three cases the reason is structural — this
tree cannot have the defect — and in the rest it was checked and found absent.

## The findings, one at a time

| defect found in INFO6205 | here? | why |
| --- | --- | --- |
| `copyBlock` charged `n+1` hits for a block move where the true cost is `2n` | **no** | there is no `copyBlock` in this tree |
| `MSDCutoff` overridden only on the instrumented Helper, so MSD cut over at 20 uninstrumented and 256 instrumented | **no** | there is no `MSDCutoff` in this tree |
| `swapIntoSorted` computed the wrong insertion point for `from != 0`, and was not stable | **fixed here first**, and ported to INFO6205 | — |
| Five sorts ignored the Helper's comparator on their uninstrumented path, giving a *wrong answer* | **no** — see below | structural |
| `swapInto` hit accounting | **correct** | see below |
| TimSort's instrumentation was never finished, so it under-reported comparisons by 58% and reported **zero** on sorted input | **no** — see below | structural |
| `InversionCounter` | **correct**, verified against brute force | see below |

## The comparator bug cannot occur here

This is the one that mattered most, because it produces a wrong answer rather
than a wrong count. Three sorts do have the same dual-path shape —
`QuickSort_3way`, `QuickSort_DualPivot` and `IntroSort` all read

```java
if (helper.instrumented()) ... helper.compare(...) ... else ... x.compareTo(y) ...
```

but the two branches cannot disagree, because

```java
public int compare(final X v, final X w) {
    instrumenter.incrementCompares();
    return v.compareTo(w);
}
```

The instrumented path *is* `compareTo`. There is no comparator to be ignored.

`ComparatorSortHelper` exists and does hold a `Comparator`, which would reopen the
question — but it is never constructed anywhere in the repository, in `main` or in
`test`. It is dead code, and the vestige of a design that was never wired up. If
it is ever brought into use, these three sorts must be revisited first.

## `swapInto`'s hit accounting is right

`InstrumentedComparisonSortHelper.swapInto` charges `(j - i + 1) * 2`. The
operation is

```java
final X x = xs[j];                            // 1 read
System.arraycopy(xs, i, xs, i + 1, j - i);    // (j-i) reads + (j-i) writes
xs[i] = x;                                    // 1 write
```

which is `1 + 2(j-i) + 1 = 2(j-i+1)`. Exact, under the model the paper uses —
"we count raw array reads and writes as a proxy for real cost".

## TimSort here is a wrapper, not a reimplementation

INFO6205 reimplemented Timsort from the JDK expressly so it could be
instrumented, and then wired up exactly one of its nine array-touching methods.
This repository's `sort/simple/TimSort.java` is 54 lines which delegate to
`Arrays.sort`, with no Helper calls at all. It therefore reports **nothing**
rather than reporting something wrong — and the paper's Timsort figures are
normalised times from the Java system sort, which is what the text says they are.

`InstrumentationIsCompleteTest.timSortReportsNoComparisonsAtAll` records this, so
that anyone who later instruments it is told the recording is out of date.

## `InversionCounter` is correct, but mutates its argument

Checked against brute force over 200 random arrays of up to 40 elements: every
count agrees. Empty and single-element arrays give zero.

It does **sort its input as a side effect** — `[3, 1, 2]` comes back `[1, 2, 3]`.
That is currently harmless, because nothing outside its own test uses it. It
would not stay harmless: a benchmark which counted inversions and then timed a
sort on the same array would be timing an already-sorted array, and the result
would look wonderful. Worth a copy at the top of `getInversions` before anything
else calls it.

## What was added

`InstrumentationIsCompleteTest` — the ground-truth check that the INFO6205 defect
was found with, adapted to this tree. The Helper counts what it is *asked* to
count, so a sort which compares without going through it is under-reported and
nothing notices: the figure comes out low, looks plausible, and gets published.

The way to see past that is to count comparisons where they cannot be bypassed —
inside the element type. `Counted.compareTo` increments a counter, so a comparison
is seen whether it went through the Helper, through a raw `compareTo`, or through
a JDK sort the class delegated to.

Result: `InsertionSort`, `MergeSortBasic`, `QuickSort_3way`, `QuickSort_DualPivot`
and `IntroSort` each report **every** comparison they make, on random and on
already-sorted input. The sorted case is the one that caught INFO6205's TimSort,
where the only instrumented method was never reached.

The test also demonstrates its own teeth: the TimSort case shows a real
divergence being detected — actual comparisons above zero, reported comparisons
exactly zero.

## One robustness defect found in passing

`MergeSortBasic.sort(xs, from, to)` throws `NullPointerException` if `preSort` has
not been called, because `aux` is allocated there and the sort reads it without
checking. Reached only by calling the sub-array sort directly rather than through
the lifecycle, so no benchmark hits it. INFO6205 had the same defect, where it has
been fixed. Not fixed here, since it does not touch the paper and the
resubmission is close.
