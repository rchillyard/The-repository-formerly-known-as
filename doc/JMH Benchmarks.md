# Running the JMH benchmarks

As of 2026-07-22, `RadixHuskySort` and the other sort strategies (String/Numeric/Tuple/Date)
can be benchmarked with [JMH](https://openjdk.org/projects/code-tools/jmh/) instead of the
older custom `Benchmark`/`SorterBenchmark` harness, giving proper fork isolation and warmup
discipline. This is what TODO.md item 1 asked for, and it exists specifically to resolve
whether the noisy N=1,000,000 results in
[Radix Sort Benchmark Results.md](Radix%20Sort%20Benchmark%20Results.md) were real or
measurement noise from the old harness.

The old harness (`HuskySortBenchmark` and friends) is unchanged and still works as before;
JMH is additive, gated behind a Maven profile that's off by default.

## Build

```bash
mvn -Pjmh clean package
```

This compiles `src/jmh/java` (a source root only added when the `jmh` profile is active) and
produces a self-contained `target/benchmarks.jar`.

## Run

```bash
java -jar target/benchmarks.jar <regex>
```

`<regex>` matches against `ClassName.methodName`. Examples:

```bash
# List every discovered benchmark without running anything
java -jar target/benchmarks.jar -l

# Just the String sorters (all corpora/sizes)
java -jar target/benchmarks.jar StringSortBenchmarks

# Just the 16-bit radix variant across every category
java -jar target/benchmarks.jar '.*radixHuskySort16.*'

# Override the default digit-width/size parameters
java -jar target/benchmarks.jar StringSortBenchmarks -p corpus=chinese -p n=200000

# Machine-readable output for later analysis
java -jar target/benchmarks.jar -rf csv -rff target/jmh-results.csv
```

Each of the four benchmark classes (`StringSortBenchmarks`, `NumericSortBenchmarks`,
`TupleSortBenchmarks`, `DateSortBenchmarks`) mirrors one of `HuskySortBenchmark`'s existing
categories, comparing System sort / PureHuskySort / (where applicable) a raw quicksort
baseline against `RadixHuskySort` at 8/11/16-bit digit widths.

## Defaults and overriding them

Each class is annotated with `@Fork(2) @Warmup(iterations = 3, time = 1s)
@Measurement(iterations = 5, time = 1s)` — enough to be far more statistically meaningful than
the old un-forked timer loop, while keeping a full run tractable. Override any of these from
the command line with JMH's normal flags (`-f`, `-wi`, `-i`, `-r`, `-w`), e.g. for a quick
smoke test:

```bash
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1 -r 1s -w 1s StringSortBenchmarks
```

A full unrestricted run across all four classes, every parameter combination, at the default
settings will take a long time (tens of minutes or more) — narrow with a regex or reduce
`-f`/`-wi`/`-i` for anything short of a real measurement pass.

## Implementation notes

- All four classes live under `src/jmh/java/edu/neu/coe/huskySort/sort/huskySort/` — same
  package as the classes they benchmark (physically a different source root), mirroring how
  `src/it/java` is already used for integration tests in this pom.
- Getting JMH's annotation processor to actually run required pinning a modern
  `maven-compiler-plugin` version with explicit `annotationProcessorPaths` inside the `jmh`
  profile — the base build has no compiler-plugin version pinned, so it silently falls back to
  Maven's ancient super-POM default (3.1, predating annotation processor path support).
- Building/running from a shaded jar surfaced a latent bug in
  `HuskySortBenchmarkHelper.getWords`: it resolved corpus resources to a filesystem `File` path
  and opened them with `FileReader`, which only works when the resource is on an exploded
  classpath directory. Fixed to read via `getResourceAsStream` instead, so it now works both
  from `target/classes` and from inside a jar. This also fixes an implicit
  platform-default-charset dependency (now explicit UTF-8).
