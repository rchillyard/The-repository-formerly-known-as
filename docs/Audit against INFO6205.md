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
| `InversionCounter` | **correct**, verified against brute force; mutation fixed | see below |

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

`ComparatorSortHelper` exists and does hold a `Comparator`, returning
`comparator.compare(v, w)`, which would reopen the question — but it is never
constructed anywhere in the repository, in `main` or in `test`. It is dead code,
and the vestige of a design that was never wired up.

That last sentence used to end "if it is ever brought into use, these three sorts
must be revisited first", which is a warning no refactor will ever read.
`ComparatorIsNotInPlayTest` now enforces it: one test fails if any file other than
the declaration mentions `ComparatorSortHelper`, naming the offender and saying
which three sorts to revisit; a second checks by behaviour that the helper actually
in use compares by the natural ordering. Verified by planting a use and watching it
fail.

**The other two sorts named in the plan are clear for different reasons, checked
2026-09-01.** `PureDualPivotQuicksort` has no helper at all — it compares with
`compareTo` throughout, so there are not two paths to disagree. `MSDStringSort` is
a static utility with no helper and no instrumentation; it cuts over to its own
private `insertionSort` below a cutoff of 15, and that uses
`v.substring(d).compareTo(w.substring(d))`, which agrees with the radix ordering
above it. So the concern recorded in the plan — that MSD might cut over to a sort
ignoring its comparator — does not arise: there is no comparator anywhere in that
class.

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

## `InversionCounter` is correct, and no longer mutates its argument

Checked against brute force over 200 random arrays of up to 40 elements: every
count agrees. Empty and single-element arrays give zero.

It **used to sort its input as a side effect** — `[3, 1, 2]` came back
`[1, 2, 3]`. That was harmless only because nothing outside its own test used it,
and it would not have stayed harmless: a benchmark which counted inversions and
then timed a sort on the same array would have been timing already-sorted data,
and the result would have looked wonderful.

**Fixed**: `getInversions` works on a copy. The cost is nothing that matters, since
the algorithm already allocates a temporary of the same length. Two tests pin it —
that the argument is undisturbed, and that counting twice gives the same answer,
which it did not before.

## The configured cutoff reached only the instrumented path — now fixed

Checked 2026-09-01, at Robin's suggestion, because this is the shape of the defect
that mislabelled Table 8.1 in the book. Fixed the same day.

`ComparisonSortHelper.getCutoff()` is an interface default returning a hardcoded
**7**. Only `InstrumentedComparisonSortHelper` overrode it to read
`[helper] cutoff` from the configuration, and `ComparableSortHelper` had no
constructor taking a `Config` at all — so the uninstrumented path could not see
one. `CountingSortHelper` and `InstrumentedCountingSortHelper` were the same pair.

Three sorts consult it: `QuickSort` (and so its 3-way and dual-pivot subclasses),
`MergeSortBasic`, and `UnicodeMSDStringSort`.

**No published number is affected.** `cutoff` is empty in both config files, so
`getInt("helper", "cutoff", 0)` returned 0 and the instrumented override fell
through to the same 7. Both paths agreed as shipped, and still do.

**But it was a trap, and precisely the one that caught the book.** Set
`cutoff = 32` to explore the parameter and the instrumented counts would move
while the timings did not — so a table could be captioned with a cutoff it was
never measured at.

Fixed by giving both uninstrumented helpers a `Config`-taking constructor and a
`getCutoff()` that reads `[helper] cutoff`, exactly as the instrumented ones do,
and by passing the config down both branches of `HelperFactory.create`. A
constructor given no config behaves as though the cutoff were unset, so the older
call sites are unchanged. `UnicodeMSDStringSort` gained a `(CharacterMap, Config)`
constructor, which is what the two benchmark call sites now use.

`CutoffIsHonouredOnBothPathsTest` pins all of it: with `cutoff = 64` configured,
the instrumented and uninstrumented helpers agree on both hierarchies; with
nothing configured, every helper falls back to the same 7. It tests through
`HelperFactory` rather than the constructors, because the factory is what a
benchmark actually calls — testing the constructors would have passed either way.

**Because the cutoff was unset, the parameter had never done anything on a timed
run. It can now be explored.** That is the point of the fix rather than any
correction to an existing figure.

## `MSDStringSort` was a handicapped baseline — now fixed

Found 2026-09-01 while establishing whether the paper's comparisons are fair. This
one is not an INFO6205 finding; it is local, and it mattered more than any of them.

`MSDStringSort` is a **timed baseline** — the `msdstringsort` benchmark runs it
through `doPureBenchmark` against the HuskySort variants. Its below-cutoff
insertion sort compared like this:

```java
return v.substring(d).compareTo(w.substring(d)) < 0;
```

Two `String` allocations per comparison. A competent MSD compares in place from
offset `d` and allocates nothing, so some of any margin over MSD was the allocator
rather than the algorithm. That is a comparison which flatters the subject of the
paper, which is the direction that matters.

`less` now walks the characters from `d` without allocating. It agrees with the
substring expression wherever that expression is legal, and is additionally defined
for `d` beyond a string's length, where `substring` would throw.

### What it was worth

Best of 12 after 20 warm-up runs, fresh shuffle each run, every result checked
against `Arrays.sort`; Apple M1, JDK 21; distinct words from
`eng-uk_web_2002_100K-sentences.txt` (81,546 of them). Two independent JVM passes,
both reported because they disagree by more than the effect at some sizes:

| n | substring `less` | in-place `less` | speedup |
| ---: | ---: | ---: | ---: |
| 1,000 | 0.271 / 0.265 | 0.236 / 0.237 | 1.15× / 1.12× |
| 4,000 | 0.506 / 0.443 | 0.526 / 0.306 | 0.96× / 1.45× |
| 16,000 | 2.006 / 2.020 | 1.420 / 1.345 | 1.41× / 1.50× |
| 64,000 | 11.700 / 11.706 | 8.551 / 10.345 | 1.37× / 1.13× |
| 81,546 | 15.060 / 15.957 | 11.538 / 13.904 | 1.31× / 1.15× |

The old column is stable across passes to within 6%; the new one is not, which is
what widens the range. Read the effect as **roughly 1.3× at the sizes the paper
reports, and not below 1.1× anywhere except one noisy small row**. Any table
comparing a husky variant against `MSDStringSort` on time should be re-run.

### Reproducing it

Revert the `less` change and re-run the `msdstringsort` benchmark. The measurement
above was taken with a throwaway harness holding both versions in one JVM; it is
not kept, because a copy of deleted code in the tree is worse than a recipe for
recreating it.

## The Unicode alphabet is not the cost I suspected

Also 2026-09-01. Recorded because the suspicion was wrong, and a wrong suspicion
that goes unrecorded gets raised again.

`MSDStringSort` is constructed with `new Alphabet(Alphabet.RADIX_UNICODE)` even for
the English corpus, which looked like it would allocate a 65,536-int count array at
every recursion node. It does not: `Alphabet` sizes the array
`RADIX_ASCII + spare + 2`, so RADIX_UNICODE gives **514** ints against ASCII's 258,
and characters beyond 255 are mapped lazily into the spare region. A factor of two,
not 256.

Measured, the difference does not survive the noise. Across the two passes above,
`Alphabet.ASCII` came out 12% faster at 64,000 in one pass and 8% *slower* in the
other. **No effect established.**

And switching would be unsafe. `getCountIndex` throws `SortException` for any
character at or beyond its radix, and an ASCII alphabet has no spare region to
absorb one — which is presumably why the call site wraps the run in a
`catch (SortException)` that prints the alphabet. This corpus contains 3 words with
a character beyond 255 out of 81,546. The ASCII runs above did not throw, but only
because those characters sit deep enough in their words that the recursion had
already handed those words to insertion sort. That is luck about the data, not a
property of the code.

**Left alone deliberately.** RADIX_UNICODE is the safe choice, it costs nothing
measurable, and making ASCII safe would mean giving it a spare region — at which
point it is no longer narrower.

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

## One robustness defect found in passing, also fixed

`MergeSortBasic.sort(xs, from, to)` threw `NullPointerException` if `preSort` had
not been called, because `aux` is allocated there and the merge read it without
checking. It is the method the `Sort` interface requires, so a caller may
reasonably reach it directly. `aux` is now allocated lazily, which costs nothing
on the recursive calls since it is already big enough by then — the same fix
INFO6205 uses for the identical defect. Two tests cover it, including a proper
sub-range to confirm nothing outside `from..to` is touched.
