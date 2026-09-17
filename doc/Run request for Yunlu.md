# Benchmark run requests for Yunlu — HuskySort paper

| request | what | state |
| --- | --- | --- |
| 1 | English string baselines, with replacement | **done** — PR #63, thank you |
| 2 | The same over wholly distinct words | **done** — PR #63 |
| 3 | Real data: San Francisco building permits | **done** — PR #63 |
| 4 | The full suite at the current commit | **done** — PR #63, 20h30m unattended |
| 5 | The small-N crossover | **done** — PR #63 |
| 6 | chinesenames against a pinyin-*correct* system sort | **done** — PR #64, `doc/pinyin.json` |
| 7 | the adversarial sweep, with the dual-pivot baseline no longer crashing | **done** — PR #64, `doc/adversarial.json` |
| 8 | cache behaviour of the object-reference swap | **closed, not pursued** — step 0 found no `perf` binary on the instance, so the request was never runnable there |
| 9 | `Arrays.parallelSort` as a baseline: strings, `Long[]`, and the permits | **done 2026-09-13** — `doc/Run results from Yunlu 2026-09-13.md` |
| 10 | the optimised `ParallelRadixHuskySort`, on permits (short) and on strings (optional, longer) | **done 2026-09-17** — PR #66, `doc/Run results from Yunlu 2026-09-17.md`; thank you, and the thread-asymmetry hypothesis did not survive |
| 11 | the cleanup pass sort choice (short), and a full-suite re-run (long) | **requested 2026-09-17** — see below |

**Requests 1 to 7 are all answered.** Requests 6 and 7 both arrived in PR #64, whose commit reads
"pinyin and adversarial included"; this table had not been updated to say so, which is corrected here.
Both datasets are in the paper: `pinyin.json` supplies the pinyin-correct baseline now quoted in the
abstract and Table `HS_BM`, and `adversarial.json` supplies both columns of the guarded/unguarded
dual-pivot comparison in the appendix.

**Request 11 is the only outstanding one.** Requests 1--7, 9 and 10 are answered; request 8 is
closed unrun, its step 0 having established that the instance has no `perf` binary. Request 11 is set
out immediately below, ahead of the answered requests that follow it: part (a) is about forty minutes
and answers a question we have only smoke-tested, and part (b) is the long one --- essentially
request 4 again, because the sorter's internals have changed underneath every figure we hold.

Your results are merged as `doc/Run results from Yunlu 2026-09-01.md`, and likewise for
`...2026-09-02`, `...2026-09-03`, `...2026-09-06`, `...2026-09-13` and `...2026-09-17`. What
requests 1 and 2 settled is summarised in Appendix A.

**What has changed since.** Robin has decided that every figure quoted in the paper should come from
one machine, and that machine should be yours. The paper currently mixes three: its original
comparison-sort results were measured on a 2017 Intel MacBook Pro running **Java 1.8.0_152**, and its
radix-sort results on an Apple M1. Yours becomes the single source of record; the other two survive
only as qualitative cross-checks, with no figures quoted from them. Your request-4 run is what makes
that possible.

Requests 3, 4 and 5 and their reasoning are in Appendix B; nothing there needs acting on.

---

## Request 11 — the cleanup pass, and then everything again

Requested 2026-09-17. Two parts, and **(b) is the one that matters more, though (a) is much cheaper.**

Request 10's answer sent us looking at where the time actually goes, and the answer was not the
parallelism at all: it is the cleanup pass, step 3. Chasing that turned up two defects in the husky
coders and one in how the cleanup was chosen, and the upshot is that **most of the figures we hold are
now measurements of superseded code.**

### What changed since the jar you built for request 10 (`dbb0cad`)

Eight commits, of which these matter to the numbers:

- **`RadixHuskySort` internals** (`0f22d44`, `350c99b`): the helper's long array is no longer
  permuted at the end (nothing read it); the final digit pass no longer writes keys (nothing read
  them); the sign-bias and identity-index setup passes are folded into the first digit pass; and the
  digit width can now be derived from n. **This affects every `RadixHuskySort` row in the suite** ---
  strings, permits, dates, numerics, tuples, adversarial --- not just the string ones.
- **`ParallelRadixHuskySort`** (`a6f8eaf`, `0f22d44`): as request 10 already measured, plus the same
  long-array and final-pass changes.
- **The english corpus's coder** (`98a4c63`): `StringSortBenchmarks` gave it `UNICODE_CODER`, which
  packs four 16-bit characters and collapses its 275,333-word vocabulary into 68,512 codes. It now
  uses `englishSaturatingCoder`, ten characters at 6 bits, which resolves that vocabulary almost
  uniquely. Natural runs left for the cleanup pass at n = 1,000,000 fall from **365,958 to 16,641**,
  a factor of 22, and the residual inversion probability p falls by a factor of **245**. Every
  english row moves, and so does commonwords.
- **`systemSortParallel` for chinesenames** (`24b16b6`): now sorts by pinyin rather than by raw code
  point, so it does the same job as the husky sorts it sits beside. Its chinesenames figures from
  request 9 are superseded.

So: **please treat the string and permits tables as needing complete replacement rather than
extension.** We would rather re-measure everything on one jar than splice old and new rows.

### 11a — which sort should the cleanup pass use? (about forty minutes)

```
java -jar target/benchmarks.jar "CleanupPassBenchmarks.(timsortCleanup|adaptiveInsertionCleanup)$" -f 5 -wi 5 -i 10 -rf json -rff cleanup.json
```

That is 2 methods x 3 coders x 2 sizes. `CleanupPassBenchmarks` times the three candidate cleanup
sorts on the array the radix phase actually hands over, rather than inferring the cleanup by
subtracting two whole sorts.

The question: the paper's §A.5 chose Timsort for step 3 on the strength of beating "insertion sort"
by 4.5x, but the repository's `InsertionSort` locates each element by **binary search over the sorted
prefix**, so it costs n log n comparisons however nearly ordered its input is --- which is not the
algorithm the paper's `k(N + pX)` cleanup term describes. A genuinely adaptive insertion sort has
been added, and on a smoke run it beats Timsort by up to 2.6x in the middle of the range and loses by
up to 15x at the top of it.

If you have appetite for one more, this adds the binary-search form so the 4.5x can be checked
directly. It refuses the pinyin coder rather than sorting by the wrong ordering, so exclude that:

```
java -jar target/benchmarks.jar "CleanupPassBenchmarks.binaryInsertionCleanup$" -p coder=englishSaturating,unicode -f 5 -wi 5 -i 10 -rf json -rff cleanup-binary.json
```

**What we expect, so that a surprise is visible as a surprise.** The deciding quantity appears to be
`X/n = pn/4`, where X is the residual inversion count: adaptive should win for roughly
`0.2 < pn < 25` and Timsort outside it. That predicts adaptive winning for `englishSaturating` at
n = 1,000,000 (pn/4 = 0.12) and losing at n = 200,000 (0.024), and Timsort winning for `unicode` at
n = 1,000,000 (28.8) and for `pinyin` at both sizes (35 and 175). If your figures contradict that
pattern we would much rather know.

### 11b — the full suite again (long, and the one that matters)

Essentially request 4 repeated, for the reasons above. Same invocation as then; the suite took you
20h30m unattended.

Please add the two new classes, which did not exist at request 4:

```
java -jar target/benchmarks.jar "ParallelStringSortBenchmarks" -f 5 -wi 5 -i 10 -rf json -rff strings-parallel-full.json
java -jar target/benchmarks.jar "StringSortBenchmarks.huskyEncodeOnlyEnglish" -f 5 -wi 5 -i 10 -rf json -rff encode-masking-vs-saturating.json
```

The second settles a question we could not answer by hand: the saturating coders resolve words the
masking ones cannot, but two hand-rolled harnesses put the **same** masking arithmetic at 41 ms and
69 ms per million, because a call site with four coder implementations measures JIT inlining rather
than `&` against `min`. If saturating turns out materially slower to encode, that is a real argument
against the change and we want it on the record.

### Method, and the commit

Same conditions as request 10, please --- two methods per invocation where a baseline is involved,
`uptime` before and after each, and the `ForkJoinPool` probe. All of that is set out under request 10
below and has not changed.

**The commit to record is `15cc2ff`**, branch `parallel-redesign`. It is the last commit touching
`src/`; the branch tip is later than it, but only by documentation and by the merge of your PR #66,
so `git log 15cc2ff..HEAD -- src/` is empty and building from either gives the same jar.
`mvn -B test` there: 423 tests, 0 failures.

(This supersedes `f92c269`, named here earlier. Since then the encoding phase has been
parallelized in `ParallelRadixHuskySort` --- about 3.5x at a million elements on eight cores --- so
**every ParallelRadixHuskySort figure from request 10 predates it**, which is a further reason 11b
replaces the tables rather than extending them.)

---

## Request 10 — the optimised ParallelRadixHuskySort

Requested 2026-09-16. **Two parts: 10a is short and is the one that matters; 10b is optional and
longer.** If you are short of time, do 10a and stop — 10b answers a reviewer question we do not yet
have, rather than correcting one we have got wrong.

`ParallelRadixHuskySort` was reworked on 2026-09-16. Four changes, each of which strictly removes
work: a per-chunk-per-pass `int[]` clone is gone; the thread pool is created once and shared instead
of per `sort()` call; the sign-bias and identity-index setup passes are folded into the first digit
pass; and the digit width can now be derived from `n` and the chunk count rather than fixed, which is
what the new `Auto` benchmarks use. The fixed-width benchmarks are unchanged and still run at exactly
the width they name.

**Why we are asking you rather than measuring it here.** We tried, on Robin's Mac, and could not get
a usable answer. That machine has 8 cores, one of which is permanently occupied by a
lab-monitoring agent, and the desktop app running the session takes much of another. Two consecutive
runs of an *identical* jar gave `systemSortParallel` 5.112 ms and then 3.514 ms at n = 32,000 — a 31%
swing on code that had not changed. Your machine at load 0.36 on 16 cores is the only place these
numbers mean anything.

### The thread-count problem your request-9 notes exposed

Before the commands: your 09-13 environment note records that the common `ForkJoinPool` behind
`Arrays.parallelSort` had **15 workers on your 16 processors**, and that "no p15/p16 husky row was
requested or run". Every husky row in that request was fixed at p4 or p8.

So request 9's permits headline — `Arrays.parallelSort` 2.77× faster than p8 — compared a 15-thread
sort against an 8-thread one. That is not a like-for-like comparison, and it is very likely most of
why your result and Robin's local one disagree so sharply: on his 8-core Mac, p8 and the system sort
get comparable resources and the husky sort came out 10–24% *ahead* on the same corpus.

A new row, `parallelRadixHuskySortAuto_pAll`, uses `Runtime.getRuntime().availableProcessors()`
instead of a hardcoded count, so it scales to the machine exactly as `Arrays.parallelSort` does. **It
is the row to compare against the system sort.** The fixed p4/p8 rows stay, because a fixed count is
the point for a scaling sweep.

One caveat you are better placed to judge than we are: `availableProcessors()` will report 16 where
the common pool runs 15, and under your `kiro.slice` quota the probe printed `13 14`. If the husky
row should match the pool's 15 rather than the machine's 16, say so and we will parameterise it —
we would rather be one thread generous to the baseline than one short ourselves.

### 10a — permits, the parallel bakeoff (about 20 minutes)

```
java -jar target/benchmarks.jar "PermitSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll)$" -f 5 -wi 5 -i 10 -rf json -rff permits-auto.json
```

Deliberately only **two** methods, so that the baseline and the candidate sit next to each other in
time. Please see the measurement note below for why that matters.

Also please re-run your own `Par.java` probe from 09-13 — the one that printed `15 16` — inside the
same slice as the benchmark, and send its output with the results. It is what tells us how many
threads each side actually got. If it is easier to retype than to find:

```
jshell -q -s - <<'EOF'
System.out.println(java.util.concurrent.ForkJoinPool.commonPool().getParallelism() + " " + Runtime.getRuntime().availableProcessors());
/exit
EOF
```

If you have time for two more, each as its own invocation rather than added to the command above:

```
java -jar target/benchmarks.jar "PermitSortBenchmarks.(parallelRadixHuskySort16_p8|parallelRadixHuskySortAuto_p8)$" -f 5 -wi 5 -i 10 -rf json -rff permits-auto-vs-16.json
java -jar target/benchmarks.jar "PermitSortBenchmarks.(radixHuskySort16|parallelRadixHuskySortAuto_pAll)$" -f 5 -wi 5 -i 10 -rf json -rff permits-parallel-vs-serial.json
```

The first judges the automatic digit width against the fixed 16 bits. The second answers the most
damning line in your 09-13 results — that the parallel husky path was slower than its own serial
radix/16 at every `n` — on a build where the per-pass bookkeeping that probably caused it is gone.

### 10b — strings, parallel against parallel (optional, several hours)

This is the gap request 9 left open. Request 9 settled `Arrays.parallelSort` against us on `Long[]`,
which is the case where husky coding has *least* to offer, because comparing two `Long`s is cheap.
The English and Chinese corpora are the opposite extreme, and until now there was no parallel husky
sort wired into the string benchmarks at all to set against the `systemSortParallel` you measured
there. A new class, `ParallelStringSortBenchmarks`, supplies one.

The full default matrix is three corpora × three sizes × seven methods, which is a long run. Narrowed
to the comparison that answers the question:

```
java -jar target/benchmarks.jar "ParallelStringSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll)$" -p n=1000000 -f 5 -wi 5 -i 10 -rf json -rff strings-parallel.json
```

That is all three corpora at the largest size, two methods. If it is comfortable, the thread-count
sweep is the more interesting result, since it shows whether strings scale with cores differently
from a cheap ordering:

```
java -jar target/benchmarks.jar "ParallelStringSortBenchmarks.parallelRadixHuskySortAuto_p.$" -p n=1000000 -f 5 -wi 5 -i 10 -rf json -rff strings-parallel-sweep.json
```

**One correction that affects your request-9 results.** `StringSortBenchmarks.systemSortParallel`
used to call the no-Comparator `Arrays.parallelSort` for every corpus, so on `chinesenames` it sorted
by raw UTF-16 code point — a cheaper task, and the wrong one, since the husky sorts order that corpus
by pinyin. Both that benchmark and the new class's baseline now use
`HuskyCoderChinesePinyin.NAME_ORDER` for `chinesenames`. You told Robin that the code-point ordering
is almost never used in practice, and that the alternative to pinyin is stroke order rather than code
point, which settles it: a code-point row is not a baseline anyone would recognise.

So **the `chinesenames` row of your request-9 `systemSortParallel` measurement should be treated as
superseded** rather than tabulated beside anything from this request. The `english` and `chinese`
rows are unaffected — their coder supplies no Collator, and natural order is the right order for
them. If 10b is more than you have time for, just re-running `chinesenames` for that one benchmark
would close the gap:

```
java -jar target/benchmarks.jar "StringSortBenchmarks.systemSortParallel$" -p corpus=chinesenames -f 5 -wi 5 -i 10 -rf json -rff parallelsort-pinyin.json
```

### The measurement note — please read before running either part

Three things bit us on 2026-09-16, and the third is a bias rather than noise:

1. **JMH runs a class's methods in lexicographic order.** If load drifts upward across a run,
   whichever method sorts last is systematically penalised — and `systemSortParallel` sorts last in
   `PermitSortBenchmarks`, `StringSortBenchmarks` and the new class alike. In one of our runs it
   scored 30.065 ± 3.580 having scored 22.078 ± 0.605 in another. That is why every command above
   names only two methods. Please don't consolidate them into one invocation.
2. **Antivirus scanning the freshly-built jar.** The benchmarks jar is 76 MB, and on the Mac
   Microsoft Defender was scanning it *during the first benchmark of the following run*. If anything
   equivalent runs on your instance, leave a few minutes between `mvn package` and measuring. At load
   0.36 you may well have nothing to worry about.
3. **`-f 1` is not enough here.** At one fork we measured ±11 ms on a 29 ms score, which is useless
   for a 5–10% effect. Five forks brought it to ±0.2–1.0 ms. Hence `-f 5` throughout.

### The system parameters we would like recorded

The thing that made our own numbers hard to interpret was not having these written down next to them.
Alongside the JSON, please send:

```
uptime
lscpu
free -h
uname -r
java -version
mvn -v
```

`uptime` is the one we most want, and ideally **twice — immediately before and immediately after**
each run. The load average before tells us the machine was quiet to start with; the load average
after, compared against the core count from `lscpu`, tells us whether the benchmark itself was the
only thing running. A run that starts at 0.4 and ends at 9 on 16 cores is clean; one that starts at 4
is not, and we would rather discard it than average it in.

---

## Request 9 — Arrays.parallelSort as a baseline

Requested 2026-09-12. **Unlike request 8, this one bears on a headline claim**, so it is wanted before
the 15th if you can reach it. If you cannot, say so and we will ship the wording change alone, which
is already in.

### Why — the baseline we quote is not the one a practitioner would use at these sizes

Robin asked whether a user sorting more than a million objects would realistically call
`Arrays.sort`. They would not. They would call **`Arrays.parallelSort`**, which is one method call
away and which this repository had never benchmarked.

A probe on Robin's own M1 (four performance cores plus four efficiency cores), English words,
N = 1,000,000, JMH with 2 forks and 5 iterations:

| sorter | ms/op, 99.9% CI |
| --- | ---: |
| `radixHuskySort16` | 203.5 ± 24.3 |
| `systemSort` (`Arrays.sort`) | 586.6 ± 44.1 |
| **`Arrays.parallelSort`** | **168.9 ± 22.0** |

So on eight cores the parallel system sort is about 1.20x faster than *serial* RadixHuskySort. The
intervals overlap between 179.2 and 190.8, so it is not resolved at 99.9% on a contended laptop — but
the direction is plain, and the machine of record has sixteen cores rather than eight.

This does not contradict the paper, whose comparison is serial against serial and where RHSort beats
`Arrays.sort` by 2.88x on the same probe. What it means is that a reviewer with a multicore machine
will try `Arrays.parallelSort`, find it beats our headline algorithm, and ask why it is absent.

### The benchmark exists now

`StringSortBenchmarks.systemSortParallel` was added 2026-09-12 and is in the jar. Nothing for you to
write.

### Step 1 — the serial-vs-parallel picture at the sizes where it matters

```
java -jar target/benchmarks.jar \
  "StringSortBenchmarks.(systemSort|systemSortParallel|radixHuskySort16)$" \
  -p corpus=english -p n=200000,1000000 \
  -f 3 -wi 5 -i 10 -r 2s -w 2s \
  -rf json -rff parallelsort.json
```

### Step 2 — the comparison that actually settles it

The honest pairing is parallel against parallel, on the same data. `ParallelRadixSortBenchmarks`
sorts `Long[]`, not strings, so it now has its own `systemSortParallel` on the same `LongState`
arrays — added 2026-09-12, and the reason the two classes each have one. Running the whole class
gives every column of Table `ParallelRadix` plus the new baseline in a single invocation:

```
java -jar target/benchmarks.jar \
  "ParallelRadixSortBenchmarks" \
  -f 3 -wi 5 -i 10 -r 2s -w 2s \
  -rf json -rff parallel-vs-parallel.json
```

The sizes are the class's own `@Param` defaults, 2,000,000 and 10,000,000, so no `-p n` is needed.

### What to expect, and what would surprise us

We expect `Arrays.parallelSort` to beat serial RHSort at both sizes on sixteen cores, by more than
the 1.20x seen on eight. That is the result we are braced for and the reason for asking.

We expect ParallelRadixHuskySort to beat `Arrays.parallelSort`, on the strength of Table
`ParallelRadix`: eight threads took 172.0 ms to 109.3 ms at N = 2,000,000, about 1.57x over serial. If
that ratio holds it puts the parallel husky sort comfortably ahead. **If it does not, we need to know
before the 15th rather than from a referee.**

Do not tune anything to produce either outcome. A result showing `Arrays.parallelSort` ahead of
ParallelRadixHuskySort is publishable and we would rather print it than discover it in November.

### Step 3 --- the permits, which is the case that matters most

Robin's point, and the strongest of the three runs. The permits pair an expensive three-field
composite ordering with a perfect encoding, so step 3 never runs: it is the case husky coding is built
for. The `Long[]` run of step 2 is the opposite extreme. Benchmarks added 2026-09-12
(`systemSortParallel`, `parallelRadixHuskySort16_p4`, `_p8`):

```
java -jar target/benchmarks.jar \
  "PermitSortBenchmarks" \
  -f 3 -wi 5 -i 10 -r 2s -w 2s \
  -rf json -rff permits-parallel.json
```

A probe on Robin's M1, one fork and three iterations, so the intervals are wide and this is direction
only --- N = 198,900:

| sorter | ms/op |
| --- | ---: |
| `systemSort` | 134.2 |
| `systemSortParallel` | 34.2 |
| `radixHuskySort16` | 34.5 |
| `parallelRadixHuskySort16_p4` | **27.4** |

If that holds up, the parallel husky sort is ahead of `Arrays.parallelSort` on this corpus while
serial RHSort is level with it --- which is the result the argument predicts and the one worth having
measured properly.

N is capped at the corpus's own 198,900, which cannot be extended on real data, an order of magnitude
below the `Long[]` sizes. Parallel speedup follows total work rather than element count, and a
three-field comparator over 198,900 records is a great deal of work, so the size is not the problem it
looks.

### What this does not settle

It is a `Long[]` comparison, because that is what Table `ParallelRadix` measures. Comparing two
`Long`s is cheap, so husky coding has least to offer there --- the gain comes only from avoiding the
boxing indirection, not from replacing an expensive ordering. A win there is therefore a strong
result and a loss is a weaker signal than it looks.

What it leaves open is the parallel comparison on **strings**, where an expensive ordering is what
makes the encoding pay. `ParallelRadixHuskySort` is wired into the benchmarks for `Long[]` only, so
there is nothing to set against `StringSortBenchmarks.systemSortParallel`. That is TODO.md item 33
and post-deadline work; nothing here is asking you for it.

### What it changes in the paper

If the expected result holds, Table `ParallelRadix` gains a column and the parallel section gains a
sentence. If it does not, the paper's claim at large N needs qualifying, which is a bigger edit and
the reason this is urgent rather than deferred.

---

## Request 8 — the cache behaviour of the object-reference swap

Requested 2026-09-09. **Not needed for the 15th.** This one justifies an appendix derivation, not a
headline number, and it is the only claim in the paper that rests on an estimate rather than a count.

### Why — one number in the paper was guessed, and it is labelled as guessed

Appendix A.1 derives QuickHuskySort's array-access cost and arrives at

```
A = 4 + 0.6 x 4 = 6.4
```

where the **0.6** stands for the proportion of the object-reference swap's four array accesses whose
targets are *not* already in cache. Every other quantity in that derivation is arithmetic over
operation counts. That one is a guess, made around 2020, and the appendix says so in as many words:
"one step in it is an estimate rather than a count... We have not measured it."

We would like to measure it. Robin would like the result in the appendix at least, and it connects to
the quicksort and dual-pivot analysis he teaches in DSAIPG, so it has a life beyond this paper.

### Step 0 — thirty seconds, and it may end the request

Hardware performance counters are usually **not exposed on virtualised EC2 instances**, only on
`.metal` ones. We think the machine of record is in that category: the paper's own environment table
records machine C's cache sizes as unknown, because they are not visible to the guest. If the cache
*geometry* is hidden, the counters almost certainly are too.

So before anything else, on `c7g.4xlarge`:

```
perf stat -e cache-misses,cache-references true
```

* If it prints counts, we are in business — carry on to step 1.
* If it prints `<not supported>` or `<not counted>`, **please stop and tell us.** That is a useful
  answer, not a failure, and it decides between two quite different plans:
  * a short run on a `c7g.metal` instance, which does expose the PMU. The runs below take minutes,
    not the twenty hours request 4 took, so the cost is small — but that is Robin's call, not
    something to spend on unasked.
  * or the timing-only fallback in step 3, which needs no counters at all.

You may also need `sudo sysctl -w kernel.perf_event_paranoid=1` and `perf` itself
(`sudo dnf install perf`). If `perf` is missing entirely, that is worth reporting too.

### The three benchmark methods, which now exist

Committed 2026-09-09; nothing for you to write. Described so you can object if the design looks wrong.

1. `StringSortBenchmarks.quickHuskySortPhase2` — steps 1 and 2 only, codes and object references
   swapped together as the real algorithm does, stopping before the cleanup pass.

2. `StringSortBenchmarks.quickHuskySortPhase2CodesOnly` — the same, except that `swap` exchanges only
   the `long[]` entries and leaves the `Object[]` untouched. Identical comparisons, identical branch
   decisions, identical `long[]` traffic. The partitioning has exactly one implementation and both
   arms run it, so the only difference is the two object reads and two object writes per swap.

3. `NumericSortBenchmarks.integerSinglePivotQuicksort` — a pure single-pivot quicksort on the same
   `Integer[]` arrays the existing `integerDualPivotQuicksort` uses, for step 2 below. Its
   insertion-sort cutoff is matched to `PureDualPivotQuicksort`'s (47) deliberately, so that what
   separates the two is their partitioning rather than where each stops partitioning. The existing
   `QuickSort` could not be used: it is abstract over the instrumented helper framework, and against
   a pure implementation it would measure the instrumentation.

**Neither of the first two is a sort, and both stop before the cleanup pass.** Their times mean
nothing on their own; only the difference does.

#### Why they stop before the cleanup, which we got wrong first

The obvious design — run each variant through the full `sort()` — does not work, and we only found
out by running it. The codes-only variant leaves the payload in random order, so the cleanup pass has
to sort it from scratch instead of repairing a few inversions. Measured on a laptop at
N=32,000: **14.6 ms against 8.7 ms**, the variant doing strictly less work coming out 70 percent
slower. That difference is the cleanup pass, and it drowns the effect being measured.

Timed on phase 2 alone, the direction is what it should be — same laptop, wide intervals, but
unambiguous:

| N | both arrays | codes only |
| --- | ---: | ---: |
| 32,000 | 4.96 ms | 2.79 ms |
| 200,000 | 43.7 ms | 22.2 ms |

So the object-reference swap looks like roughly half of phase 2's *time*. What we need from you is
the same comparison in *cache refills*, which is the quantity the appendix's 0.6 actually refers to.

### Step 1 — the differential run, and the number we actually want

```
java -jar target/benchmarks.jar \
  "StringSortBenchmarks.quickHuskySortPhase2" \
  -p corpus=english -p n=32000,200000,1000000 \
  -f 3 -wi 5 -i 10 -r 2s -w 2s \
  -prof perfnorm \
  -rf json -rff cache-differential.json
```

`-prof perfnorm` reports hardware counters normalised per benchmark operation, which is exactly the
shape we need. If the generic event aliases are unavailable but ARM's own are present, name them
explicitly — on Neoverse V1 the useful ones are `l1d_cache`, `l1d_cache_refill`, `l2d_cache`,
`l2d_cache_refill`:

```
-prof perfnorm:events=l1d_cache,l1d_cache_refill,l2d_cache,l2d_cache_refill,instructions,cycles
```

Please send `perf list | head -60` as well, so we can see what this machine actually offers rather
than guessing from the microarchitecture.

**The arithmetic we will do with it.** Because the two variants differ in exactly one thing, the
difference in cache refills between them is attributable to the object-reference swap. Divide it by
four times the number of swaps and you have the fraction that 0.6 was standing in for:

```
measured factor = delta(cache refills per op) / (4 x swaps per op)
```

We can supply the swap count exactly rather than estimating it — `InstrumentedComparisonSortHelper`
already counts swaps, so a single instrumented run at each N gives the denominator with no error
bars at all. We will do that here; you do not need to.

### Step 2 — single-pivot against dual-pivot, for the teaching material

Same counters, on primitives, where the classic result lives:

```
java -jar target/benchmarks.jar \
  "NumericSortBenchmarks.integer(SinglePivotQuicksort|DualPivotQuicksort|RawQuicksort)$" \
  -p n=500000 \
  -f 3 -wi 5 -i 10 -r 2s -w 2s \
  -prof perfnorm \
  -rf json -rff cache-quicksort.json
```

Dual-pivot quicksort's advantage over single-pivot is widely attributed to cache behaviour rather
than to its comparison count, and the paper currently passes that attribution along on the strength
of a citation. Three sorters on identical arrays with counters attached would let us say it from
measurement instead — and it is the same comparison Robin uses in class, where at present the cache
explanation is asserted rather than shown.

### Step 3 — the fallback, if there is no PMU anywhere we can reach

Cache behaviour can be inferred from timing alone by sweeping N across the hierarchy and watching
where the two arms of step 1 diverge. Below the point where the working set leaves cache, the
object swap should be nearly free; above it, it should cost. The location of the knee is the finding.

```
java -jar target/benchmarks.jar \
  "StringSortBenchmarks.quickHuskySortPhase2" \
  -p corpus=english -p n=1000,4000,16000,64000,256000,1000000,4000000 \
  -f 3 -wi 5 -i 10 -r 2s -w 2s \
  -rf json -rff cache-sweep.json
```

This is weaker evidence than a counter — it shows the effect exists and where it starts, not what
fraction of accesses miss — but it needs no privileged access and it is the method LaMarca and Ladner
used, which the paper cites. It would let the appendix say the estimate is *the right shape* even if
we cannot pin the coefficient.

### What to expect, and what would surprise us

We expect the measured fraction to come out **well below 0.6**, possibly by a lot. A single cache
refill brings in a 64-byte line, which holds eight object references, and quicksort's partitioning
scans inward from both ends — so consecutive swaps tend to touch lines already resident. If the
measurement says 0.1 rather than 0.6, that is a plausible result and not a mistake.

A figure at or above 0.6 would be the surprise, and would mean the 2020 guess was better than we
think it was.

Do not tune anything to make it land near 0.6. If it comes back at 0.05 we will report 0.05.

### What this could change, which is why it is not urgent

If the factor is really nearer 0.1 then A is about 4.4 rather than 6.4, and Table `Comparison`'s
QuickHuskySort column changes — in RHSort's favour, incidentally, since it would mean the paper has
been *overstating* the cost of the comparison-based baseline's own advantage. That is a table edit we
would rather make calmly after the 15th than hurriedly on the 14th.

Which is the real reason this is request 8 and not request 6: the appendix currently labels the 0.6 as
an estimate, and an honest label is a perfectly publishable state. A half-integrated new number would
be worse than the label. So please treat this as post-deadline work unless step 0 happens to be quick
and interesting.

---

## Request 6 — chinesenames against a pinyin-*correct* system sort

The checkout differs from the earlier requests: `systemSortPinyin` is new, so the commit is new too.

Twenty minutes, and it revises a conclusion from your 2026-09-03 report rather than adding to it.

```
git fetch origin Revisions
git checkout origin/Revisions
mvn -Pjmh package -DskipTests
java -jar target/benchmarks.jar "StringSortBenchmarks.(systemSort|systemSortPinyin|quickHuskySort|radixHuskySort16|multikeyQuicksort)$" -p corpus=chinesenames -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff pinyin.json
```

**Use the branch tip, not a named commit.** The code freeze for this run is `5ed60a0` — the last commit
that touched anything under `src/`, and the one to record in the paper — but everything after it is
documentation, including your own merged results and the corrections below. Checking out `5ed60a0`
itself would give you a copy of *this file* that still names the previous hash, which is more confusing
than helpful. Any commit from `5ed60a0` onwards compiles and measures identical code; the tip is
simply the tidiest of them.

Do **not** use `d3c359f`, the commit for requests 3 to 5: `systemSortPinyin` does not exist there, and
neither does the encoder fix described below.

### Why — your chinesenames finding needs an asterisk

You reported that on chinesenames the system sort beats every Husky variant, at 823.8 ms/op against
radix/16's 960.2 at 1M, and concluded the corpus is adverse to the mechanism. The measurement is right
but the comparison is not what it appears.

`systemSort` calls `Arrays.sort(copy)` **with no comparator**. On this corpus that sorts by raw UTF-16
code point and performs **no pinyin lookup at all**. The Husky variants sort by pinyin, because that is
the correct order for personal names. So the two sides are not doing the same work: one is solving a
cheaper and, for this data, wrong problem. The same objection applies to the August run's chinesenames
row, and to a remark in the paper itself.

We had already made `multikeyQuicksort` pinyin-aware for exactly this reason. The system sort was
never given the same treatment, so the suite has had no fair pinyin baseline.

`systemSortPinyin` is `Arrays.sort(copy, HuskyCoderChinesePinyin.NAME_ORDER)` — the same three-level
syllable/tone/code-point comparator `MultikeyQuicksort.sortByPinyin` already uses as its fallback.

### It is exactly comparable, and this was checked

On 200,000 names, `Arrays.sort(NAME_ORDER)`, `radixHuskySort/16` and `quickHuskySort` with the pinyin
coder produce **byte-identical output** — zero positions differing, zero inversions under
`NAME_ORDER`. Whatever the timings say, the three are performing the same task.

### What to expect, and what would surprise us

At n = 1,000,000 the system sort performs roughly 20 million pinyin comparisons where the Husky
variants perform one million encodings. That is the mechanism's entire premise — pay the expensive key
extraction once per element rather than once per comparison — and chinesenames is the only corpus in
the suite where the key extraction is expensive enough for it to show in isolation. We therefore
expect `systemSortPinyin` to be *far* slower than everything else, and the corpus to turn from the
paper's weakest result into its clearest demonstration.

**One thing changed since you last ran this corpus, and it is why the commit moved.** Your 408.4 ms/op
`huskyEncodeOnly` figure prompted us to look at the pinyin coder, and the key extraction turned out to
be doing two substring allocations, a string-keyed table search and a string parse *per character, per
element*, uncached — while the comparator's equivalent was already memoized. So the once-per-element
path was the expensive one and the per-comparison path the cheap one, which is backwards for the whole
premise. The per-character value is now cached, worth **5.7x** on the encoding pass in our own
measurement (133 ns per name down to 23 ns), and verified bit-identical over all 1,145,009 names in
the corpus. Expect `huskyEncodeOnly` to come in far below 408 ms this time; if it does not, please tell
us, because then we have misdiagnosed it.

Please keep `systemSort` in the command as well: having both, side by side at the same sizes, is what
lets the paper state plainly what the difference between them is.

## Request 7 — the adversarial sweep, with a baseline that no longer crashes

About forty minutes: one class, no parameters.

**Use the branch tip, as for request 6** — but note that the two requests do *not* have the same
earliest usable commit. Request 6 says any commit from `5ed60a0` onwards measures identical code, and
that is true of `systemSortPinyin`. It is not true here: the depth guard landed later, in `e92610f`.
Run request 7 on `e92610f` or later, or it will simply crash again exactly as before. The tip is at or
past that point, so one checkout at the tip serves both requests.

```
java -jar target/benchmarks.jar "AdversarialSortBenchmarks" -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff adversarial.json
```

### Why — the crash was ours, not the algorithm's

Your full-suite run recorded `collapsedBitsDualPivotQuicksort` dying with `StackOverflowError` on all
five forks at three combinations. Robin's question was the right one: degrading to quadratic time on
adversarial input is expected, but *crashing* is not.

They turned out to be separate faults. Two-way partitioning around a heavily duplicated pivot goes
maximally unbalanced, which costs quadratic time — that is the appendix's actual point and it stands.
But the implementation also recursed on both partitions with no bound, so the depth reached order *N*
and the stack was exhausted. Three-way partitioning cures the first; bounding the depth cures the
second. A sort with the second and not the first goes quadratic and survives.

Two things about that baseline had never been disclosed, and both matter:

- **The JDK never applies dual-pivot quicksort to objects.** `Arrays.sort(Object[])` dispatches to
  `ComparableTimSort`; the JDK's `DualPivotQuicksort` mentions neither `Comparable` nor `Object[]`.
  Our class is the primitive algorithm hand-adapted to objects.
- **Later JDKs added this exact guard.** JDK 21's version carries `MAX_RECURSION_DEPTH = 64 * DELTA`
  with a `heapSort` fallback. Ours was a copy of the 2011 code, which predates it. So the baseline
  failed where the algorithm it copies would now merely slow down — a straw man in the one place the
  paper leans on it hardest.

`PureDualPivotQuicksort` now carries a sixty-four-level guard with a heapsort fallback, matching the
JDK's effective limit. Well-balanced input needs about twenty levels at *N* = 1,000,000, so nothing
below the limit changes: the `fixedHighBits` 0 through 56 rows should reproduce your previous numbers
within noise. **The rows to look at are 60 and 63**, which should now be timings rather than blank.

### Checked before asking

On identical data with a deliberately small 1MB stack: two `StackOverflowError`s at 60 and 63 without
the guard, zero with it, across 3,024 trials covering the adversarial shapes, duplicate-heavy random
input, all-equal, sorted, reversed, and the sizes either side of both internal thresholds.
`PureDualPivotQuicksortDepthTest` pins all of it; `mvn test` is now **396** tests.

### What we expect

The 21x degradation at 56 fixed bits (7097.7ms against 335.7 at fhb=0) is the finding and should be
unchanged — it is a property of the partitioning, not of the crash. At 60 and 63 expect the baseline
to be slow but finite. If instead it is *fast* there, tell us: that would mean the heapsort fallback
is being entered far earlier than intended, and the guard needs re-tuning rather than celebrating.

## A note on the Chinese corpora

`msdStringSort` cannot run on them at all — its alphabet holds 256 characters beyond ASCII and the
Chinese corpora hold 3,813 and 2,270 — and the two string baselines we repaired are not used for
pinyin ordering. So requests 1, 2 and 5 are English-only and take `-p corpus=english`.

**Request 4 is the exception**: it runs every corpus, because Table `RadixImprovements` has Chinese
rows and those figures now need to come from your machine too. It selects corpora itself; pass no
flags.

---

# Appendix A — requests 1 and 2, answered 2026-09-01

Kept for the record. **Nothing here needs running.**

## What prompted them

Two things had changed in the string-sort comparison and both needed measuring on hardware we trust:

1. **MSD radix sort was added as a second baseline**, after three correctness defects were fixed that
   had prevented it being benchmarked at all. On our workstation it was *beating* RadixHuskySort at the
   larger sizes.
2. **Both baselines' small-range fallbacks were handicapped** in the same way and both were repaired.
   Multikey quicksort's fallback alone cost it 15–18%, so the paper's published margin over multikey
   was partly measuring our own overhead.

Our own measurements came from a shared MacBook that spent the day fighting Spotlight, an IT
monitoring agent and, at one point, a closed lid: intervals from 1% to 23%, and one benchmark
returning 232, 259 and 330 ms on three occasions.

## What they settled

- **MSD does beat RadixHuskySort at 200,000 and 1,000,000**, with non-overlapping intervals, on a
  second architecture. But the margin's shape is machine-dependent: 1.34x at 200,000 and 1.09x at
  1,000,000 on Graviton3, against roughly 1.10x and 1.38x on the M1.
- **The tie at 32,000 is real** and not an M1 cache artefact — it reproduces on a server-class ARM core
  with an entirely different cache hierarchy.
- **The margin over the repaired multikey is 1.66x / 1.39x / 2.26x**, so the paper's stated 1.3–1.75x
  breaks at the top end on Graviton3 — the opposite end from the M1, which broke it at the bottom.
- **Duplicate density does not explain MSD's advantage.** Drawing without replacement leaves the
  200,000 margin at 1.31x against 1.34x with replacement.

## Why request 2 could not simply use a larger array

Past 1,000,000 the English corpus stops being able to supply distinct words — it holds 275,333 — and
the benchmark becomes a test of duplicate handling. Average copies of each word run 1.06 at 32,000,
1.41 at 200,000 and 3.73 at 1,000,000. The codebase already makes this objection about the
`commonwords` corpus, whose ~3,000 words sampled into a 200,000-element array it calls "artificial
duplicate-heavy skew"; at 1,000,000 the English corpus approaches the same problem from the same
direction. Drawing without replacement bounds n by the corpus size, which is why request 2 stopped at
250,000.


---

# Appendix B — requests 3, 4 and 5, answered 2026-09-02 and 09-03

Kept for the record. **Nothing here needs running.** The shared checkout they used was `d3c359f`;
request 6 above uses a later commit.

## Request 3 — real data: San Francisco building permits

About forty minutes. **Subsumed by request 4**, so skip it if you go straight to that; it stands
separately only because it is the result Robin most wants to see first.

```
java -jar target/benchmarks.jar "PermitSortBenchmarks" -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff permits.json
```

No `-p` flags: the sizes and the corpus are the benchmark's own.

### Why

Every favourable case in the paper is synthetic — `Tuple.create()` generates composite keys,
`generateRandomLocalDateTimeArray` generates dates — and the largest margin we report (4.5x, on dates)
rests on generated data. This is the same shape of case on real data: San Francisco's published
building permit record, 198,900 permits from 2013 to 2018, sorted by Assessor's block, then lot, then
filing date, which is the order the records are actually browsed in.

It is favourable on all three counts that decide the mechanism's advantage, which no other case in the
paper manages simultaneously:

- the native comparison is composite and expensive — two Strings and a date;
- the encoding is **exact**, packing the whole ordering into 60 of 64 bits, so no cleanup pass runs at
  all;
- no sort specialised to municipal permit records exists.

A first measurement on our machine puts RadixHuskySort/16 at **4.40x** over the system sort at the full
corpus and **2.19x** over QuickHuskySort. Table `RadixImprovements` reports the advantage over
QuickHuskySort, so 2.19x is the comparable figure — below the synthetic Dates row's 4.5x rather than
above it. Permits are not the best number in the paper; they are the best number on real data. Full
results in [Permit benchmark results 2026-09-01.md](Permit%20benchmark%20results%202026-09-01.md).

### The pair to look at

`quickHuskySort` and `quickHuskySortWithCleanup` compute **identical codes**. They differ only in
whether the coder declares itself perfect, so one skips the cleanup pass and the other runs it and
finds nothing to do. The gap between them is therefore the cost of the cleanup pass on input where it
is provably unnecessary — the quantity the paper's $p_{crit}$ discussion turns on, and which has never
been isolated, because every other benchmark varies the encoding and the sort together. We measured
5.9% / 11.9% / 17.4%; your run gave 9.8% / 25.3% / 19.0% — larger throughout, and peaking in the
middle rather than growing, so the monotonicity we saw was ours alone. The result stands and is more
useful for being bounded: a tenth to a quarter of the running time, for a pass that corrects nothing.

### On trusting these numbers

`PermitCoderTest` verifies the exactness claim against every one of the 198,900 records — sorting by
code against sorting by the ordering, plus two million random pairs checked for sign agreement — and
`PermitSortCorrectnessTest` checks that every benchmarked sorter actually sorts, including over the
whole corpus. Both run under `mvn test`. The second exists because this repository has already shipped
a benchmarked sort that produced the wrong order at its largest size without anything noticing, and
JMH never checks its subject's output.

---

## Request 4 — the full suite at the current commit

The long one, and the one that makes the paper consistent. Your 2026-08-17 run took 2:37:37; at five
forks and ten iterations expect perhaps four to five hours. It can run unattended.

```
java -jar target/benchmarks.jar -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff full-suite.json
```

No filter and no `-p` flags: every benchmark class, every default parameter, every corpus.

### Why re-run what you already ran

Your 2026-08-17 run is on the right machine but at an older commit. Since then both of the paper's
string-sorting baselines have been repaired — each allocated a sorter per small subarray and compared
whole strings from character zero rather than from the depth the recursion had already established,
worth 15–18% to three-way radix quicksort and about 1.3x to MSD. Neither of those sorts appears in your
2026-08-17 tables, so nothing there is *wrong*; but a paper whose figures come from two commits invites
the question of which one each number belongs to.

### What it replaces

| paper table | currently measured on | will come from this run |
| --- | --- | --- |
| `HSComp` — HuskySort against the system sort | 2017 Intel MacBook Pro, Java 1.8.0_152 | `NumericSortBenchmarks`, `TupleSortBenchmarks`, `StringSortBenchmarks` |
| `RadixImprovements` — radix against QuickHuskySort | Apple M1 | the same three, plus `DateSortBenchmarks` |
| `ParallelRadix` | Apple M1 | `ParallelRadixSortBenchmarks` |
| the adversarial appendix | Apple M1 | `AdversarialSortBenchmarks` |

### Please match your 2026-08-17 environment as closely as you can

This is the environment the paper will describe, so the closer this run is to the one already reported,
the less there is to reconcile. From `doc/JMH Benchmark Results 2026-08-17.md`:

| item | value to match |
| --- | --- |
| Instance | AWS EC2 `c7g.4xlarge` (AWS Graviton3), ARM Neoverse V1, aarch64 |
| vCPUs | 16 — 16 cores × 1 thread/core, no SMT, 1 socket, 1 NUMA node |
| Cache | L1d 64 KiB/core, L1i 64 KiB/core, L2 1 MiB/core, L3 32 MiB shared |
| CPU clock | not exposed to the guest; Graviton3 documented at 2.6 GHz fixed |
| Memory | **30 GiB** total, 0 B swap |
| OS / kernel | Amazon Linux 2023, kernel `6.12.95-124.187.amzn2023.aarch64` |
| JDK | OpenJDK **21.0.12** (2026-07-21 LTS), Amazon Corretto, 64-bit Server VM |
| Maven | Apache Maven **3.9.16** |
| Instance class | non-burstable — no CPU credits, so sustained rather than burst performance |
| Load average at collection | 0.36 / 0.36 / 0.27 on 16 CPUs |

Three small drifts between that run and yesterday's, worth pinning down rather than leaving:

- kernel **6.12.100** yesterday against 6.12.95 in August;
- Maven **3.9.9** against 3.9.16;
- memory reported as **32 GiB** yesterday against **30 GiB** in August. The paper says 30, so we have
  assumed August is right — please confirm which, since it goes in a table.

None of the first two should matter. If the same AMI and Maven are easy to reach, use them; if not,
just tell us which you used and we will record it.

Please send back `full-suite.json`, plus the output of `lscpu`, `free -h`, `uname -r`, `java -version`
and `mvn -v`, so the environment table can be written from fact rather than from memory.

---

## Request 5 — the small-N crossover

Short, perhaps half an hour, and the last gap.

```
java -jar target/benchmarks.jar "StringSortBenchmarks.(insertionSort|systemSort|quickHuskySort|radixHuskySort16)$" -p corpus=english -p n=4,10,20,50,100,200,500,1000,2000,10000 -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-crossover.json
```

The paper's use-case guidance identifies, size by size, which sorter to reach for below ten thousand
elements — where the system sort wins, where plain insertion sort wins outright, and where
QuickHuskySort takes over. Those crossovers were measured on the M1 only, and the paper says so. They
are the one set of figures request 4 will not produce, because the suite's own parameters start at
32,000.

Expect very small absolute numbers at the low end. That is fine: the crossover points are what matter.

---
