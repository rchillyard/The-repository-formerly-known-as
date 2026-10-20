# Code and corpus notes

Opened 2026-10-20. `TODO.md` had accumulated material with three different destinations: the
ACDA27 paper, a possible second paper, and **neither** --- facts about the code and the corpora that
matter to whoever next runs these benchmarks and to nobody else. The first two now have homes
(`TODO.md` item 43 and `doc/Second paper.md`). This is the third.

`TODO.md` keeps its numbering, because "item 43" and "item 55" are referenced by 143 commit messages
and 47 files and are effectively public identifiers. Items moved here leave a pointer behind rather
than a gap.

---

# The Leipzig corpora

## Mojibake in the english corpus --- checked, and deliberately not repaired

Moved from `TODO.md` item 43C, 2026-10-20. The caution that item carried --- that the damage comes
from the corpus's character repertoire rather than from punctuation --- **is in the paper** as of
2026-10-20, in \S~`sec:data-source`. What follows is the part that is not, and is not going to be.

Chasing this found a real defect in our own tokenizer, now fixed --- the next section. What follows
was re-measured afterwards, against the repaired tokenizer.

**Repairing the mojibake does not help, and costs a little (measured 2026-09-22).** Robin asked
whether repairing the corpus would help, on the reasonable view that the mojibake is an error
and testing against a corrected corpus is defensible if declared. It is repairable, at line
level: repairing each line *before* tokenizing takes words still carrying a mojibake marker from
1,843 to 238 and recovers `Amelie`, `Nurnberger`, `Cliches`, `Souffles`, `Cafe`, `fur` properly
accented. (Word-by-word repair does not work, because the splitter has by then already dropped
the trailing non-letter: `ClichA-tilde-copyright` is left as `ClichA-tilde`, and the
continuation byte is gone.)

But the repair *restores genuine accented characters*, which is exactly what a fixed-width coder
cannot represent, so it moves our numbers the wrong way. At n = 1,000,000:

```
                    words>127    runs    inversions   timsort(ms)  adaptive(ms)
englishMask  before      1,843  17,305  180,352,275         36.9          841.6
englishMask  after       2,536  17,815  187,739,187         37.2          916.9
asciiMask    before      1,843  30,520  185,161,666         42.3          921.0
asciiMask    after       2,536  31,078  259,115,324         45.4        1,199.0
```

Runs rise about 3%, so the Timsort cleanup is unchanged within the noise of an eight-core Mac;
the masking coders' inversions rise 4% and 40%, and the adaptive cleanup degrades accordingly.

So the corruption was, accidentally, flattering our coders --- it was replacing accented
characters with truncated ASCII-ish ones. **Recommendation: do not repair, and say in the paper
that we checked.** One or two sentences forecloses a referee's question and is more interesting
than silence: the mojibake is upstream, our copy is byte-identical to a fresh download (sha256
`475aa01b...`, item 36), a correct repair exists, and applying it moves the Timsort cleanup by
less than the measurement noise, in the unfavourable direction. That supersedes the earlier
"deleting every affected word is worth 2.6%" ceiling, which measured deletion rather than repair.

## The word splitter was discarding 15% of the english corpus and 51% of the chinese one

Moved from `TODO.md` item 43C2, 2026-10-20. Its paper consequence --- that the data-source section
must say how the corpus is tokenized --- **is discharged**: \S~`sec:data-source` now describes the
tokenization, and the stronger statement in point 4 below is what the paper's caution rests on.

### C2. The word splitter was discarding 15% of the english corpus and 51% of the chinese one

Found 2026-09-22 while establishing why word-level mojibake repair failed, and **fixed the same
day** at Robin's instruction ("our tokenizer is wrong. We need to fix that, even if it costs us
time"). `HuskySortBenchmark.REGEX_LEIPZIG` read

```
[~\t]*\t(([\s\p{Punct}\uFF0C]*\p{L}+)*)
```

which requires the captured sentence to be an alternation of ASCII punctuation and Unicode
letters. Java's `\p{Punct}` is POSIX, hence **ASCII-only**, so the group stopped at the first
character that was neither a Unicode letter nor ASCII punctuation, and `String.split` was handed
only that prefix. Digits qualified; so did the pound sign, the copyright sign, and every
non-ASCII dash or quotation mark. The failure was silent and total:

```
eng-uk_web_2002_1M    15.2% of sentence characters discarded
                      275,387 -> 304,959 distinct words, 16.39M -> 18.86M tokens
zho-simp-tw_web_2014  51.5% discarded (U+3002, the ideographic full stop, is not ASCII punct)
                      24,215 -> 50,009 distinct, 25,745 -> 56,134 tokens
```

"With Amelie (Cert 15) Jeunet combines the best of his two previous films..." yielded "With
Amelie (Cert". "The figure size must not exceed A4 or 8.5 x 11in..." yielded "The figure size
must not exceed A". Every line with a digit in it lost everything from the digit onwards.

The fix is `[~\t]*\t(.*)` for the line --- a Leipzig line is `<id>\t<sentence>`, so the
sentence is simply everything after the first tab, and any attempt to validate it inside the
pattern can only truncate it --- together with `[^\p{L}]+` for the splitter, replacing an
enumeration of separators that could never be complete. The splitter change is behaviour-neutral
on the text the old line pattern captured; the line pattern is the repair. It is **purely
additive**: no word either corpus produced before is lost.

**Consequences.**

1. Every english and chinese string figure ever measured, by us or by Yunlu, is superseded.
   chinesenames is not affected (it loads via `lineAsList`). This is a second full-suite re-run,
   and Robin accepted that cost explicitly.
2. It makes the masking coders look *worse*, because the recovered vocabulary is more accented:
   `englishCoder`'s inversions at n = 1,000,000 go 85,818,889 -> **180,352,275**, `asciiCoder`'s
   87,797,452 -> **185,161,666**, while every run count moves by less than 4%. The
   masking-versus-saturation verdict does not change --- Timsort still cannot see it --- but the
   discrimination in section A is now 1,637x and 855x rather than 731x and 373x, which makes the
   experiment stronger, not weaker.
3. The paper's data-source section must say how the corpus is tokenized, which it currently does
   not. Given that the bug hid 15% of one corpus and half of the other, "words extracted from
   the Leipzig sentences" is not a sufficient description.
4. It supersedes the claim in section C above that punctuation is not what damages the masking
   coders "because the mojibake dominates". The truer statement is stronger: the splitter emits
   letter-only tokens, so **no token contains punctuation or a digit at all**, and the only
   characters reaching outside a 6- or 7-bit window are letters at or above 128. Both masking
   coders therefore fail on exactly the same words.

---

# Code backlog

Neither of these is paper material. Both are live.

## A third shape for the exactness test: make the window data instead of dispatch

Moved from `TODO.md` item 56, 2026-10-20. **Parked by design** --- see "When to act".

**A third shape for the exactness test: make the window data instead of dispatch (opened
2026-10-19, on Robin's question; deliberately not acted on --- see "When to act").**

Robin asked whether we had considered inlining the test *in place* before hoisting it out, and
whether that is even possible in Java. We had not: request 13's question 1 offered exactly two
shapes, Yunlu's split loop and the hoist, and the hoist shipped. This records the third, which
may be the best of them.

### Literal inlining is not possible, and why

`exactlyEncodable` is virtual. The call site is in `BaseHuskySequenceCoder.huskyEncode(X[])`
and the bodies are in four anonymous subclasses in `HuskyCoderFactory`; a base class cannot
inline a body it does not know, and that polymorphism is the mechanism distinguishing the
coders. Java has no `inline` keyword. JIT inlining is C2's decision, and C2 declined it here
--- `low call site frequency` --- because the test stops at about element 2 of a million.

### What is possible

All four overrides have the identical shape, `super.exactlyEncodable(x) && charactersWithin(x,
(char) LO, (char) 0x7F)`, differing only in `LO` (0 for the ASCII pair, `OFFSET_ENGLISH` for
the English pair). So the window can be two final fields on the base and the predicate can be
non-virtual:

```java
public final boolean exactlyEncodable(final X x) {
    return couldBeExactAtLength(x.length()) && (!narrowing || charactersWithin(x, lo, hi));
}
```

`couldBeExactAtLength` is already `public final` and `charactersWithin` already `protected
static`, so nothing there dispatches virtually. All four overrides then delete and each coder
declares its window as a constructor argument --- the same idea as the `English` / `Ascii` /
`ExtendedAscii` window types built for item 54, which is an argument for doing it on
consistency grounds alone. **The `narrowing` flag is required, not cosmetic**: `unicodeCoder`
has no override today, and without the flag it would begin scanning every character of every
string to no purpose.

### The evidence that it would have worked

`rev` is close to this experiment already. It puts a **non-virtual, trivially inlinable** test
(`couldBeExactAtLength`, which is `final`) at the same point in the same loop, and like the
real check it stops after about two elements, so it executes nearly the same work.

| arm | test at the call site | masking vs old, 200,000 / 1,000,000 |
| --- | --- | --- |
| new | virtual override, not inlined | 1.135 / 1.124 |
| rev | final method, inlined | **0.998 / 0.991** |
| F1, shipped | hoisted out of the loop | 0.975 / 0.977 |

`rev` pays no penalty. The cost therefore attaches to the un-inlined virtual call sitting in
the loop body, not to the testing. De-virtualizing in place should recover it with **no call
site and no double scan of a perfect array**, which would retire the one caveat still attached
to the shipped fix (item 55), and delete code while doing it.

Two reasons not to treat that as settled. The chinese side may not be cured: on the base path
even `CharSequence::length` stays a virtual call with `low call site frequency`, which is a
separate problem, and the 32,000 chinese residual survived the line-50 revert. And F3 ---
which executes what `rev` executes yet reads 1.034 / 1.044 against rev's 0.998 / 0.991 --- is
a standing warning that predicting these effects from the shape of the source is unreliable.
Nothing here is a measurement; `rev` is evidence for the mechanism, not for this variant.

### When to act

**Not before the ACDA27 review comes back.** Any change to the encode path re-invalidates the
205 rows Yunlu ran for request 13 and puts us back in the position item 55 named: the paper's
encode figures and the code that produced them have to describe each other. Two of Yunlu's
runs went on restoring that correspondence, and this is a speculative improvement to a penalty
we have already removed.

Act if any of these becomes true: the paper is accepted and the camera-ready window leaves
room for a re-run; the reviews ask for changes that reopen the encode path anyway; or the
double scan on perfect arrays stops being hypothetical, which it would if a corpus of short
exactly-encodable strings entered the benchmark matrix. If acted on, it needs its own
interleaved two-jar A/B on Yunlu's host --- not an in-JVM comparison, for the reason in 43H
--- and `HoistEquivalenceTest` should be extended to cover it, since collapsing four overrides
into two fields is exactly the kind of change that keeps the output right while altering the
mechanism underneath.

## There is no parallel QuickHuskySort, and it should stay that way

Moved from `TODO.md` item 42, 2026-10-20.

**There is no parallel QuickHuskySort, and it should stay that way --- but for strategic reasons,
not because it would not work.** Robin asked on 2026-09-17 whether such a thing exists. It does
not: `ParallelRadixHuskySort` is the only parallel variant in the repository, and the paper
declines to attempt another.

### It would work, and it has *more* parallel headroom than the radix variant

Parallelizing QuickHuskySort's step 2 is textbook: partition sequentially, fork the two halves,
and after log P levels there are P independent subarrays. The payload-drag swaps that make
QuickHuskySort slower than RHSort are safe under that scheme, forked subranges being disjoint.
With the `doCoding` hook added in item 41, its encoding would parallelize for free.

The headroom is the counterintuitive part. From the paper's own figures at n = 1,000,000 on
English words:

| | total | encoding | step 2 + cleanup |
| --- | ---: | ---: | ---: |
| QuickHuskySort | 697.9 ms | 67.7 (9.7%) | **~630 ms** |
| RHSort | 272.7 ms | 67.7 (24.8%) | ~205 ms |

**QuickHuskySort has about three times the parallelizable work in absolute terms**, its
O(n log n) step 2 being a far larger share of a far larger total. Its Amdahl fraction is
therefore much more favourable than RHSort's: the variant with the *worse* serial performance has
the *better* parallel prospect, because parallelism rewards having work to divide. That is the
mirror image of item 39's argument --- "a sort that wins by doing less total work has less left to
spread across cores" --- read backwards, and it is worth keeping the pair in view, because
together they say the serial win and the parallel prospect trade against each other rather than
reinforcing.

### Why not to build it

- It runs against the decision of item 39 to back off parallel claims generally.
- The paper deliberately de-emphasises QuickHuskySort as the original idea, RHSort being the
  headline (see item 21, the renaming).
- It is real algorithm work --- parallel quicksort with payload drag, plus the stability and
  correctness sweep that `ParallelRadixHuskySortTest` needed --- for a variant that is not the
  contribution.

### The paper's existing sentence is fine, with one caution

The introduction says of QuickHuskySort's comparison-based steps that "parallelizing them well is
a separate, harder problem that this paper does not address". That is a **scope** statement and
stands. The caution is only that "harder" must not be read as "unsolved": the JDK demonstrably
parallelizes Timsort --- `Arrays.parallelSort` *is* that --- and it beats serial Timsort by
1.75--3.8x in the measurements of item 40. If a referee presses, the honest answer is "harder,
and out of scope", not "not known how".
