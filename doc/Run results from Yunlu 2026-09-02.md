# Run results from Yunlu — permits and small-N crossover, 2026-09-02

This answers **requests 3 and 5** of `doc/Run request for Yunlu.md`. Request 4 (the full suite) is
running unattended as I write this and will follow in this PR when it completes — note its true
duration: at `-f 5 -wi 5 -i 10 -r 2s -w 2s` JMH's own ETA for the whole suite is **~17.5 hours**, not
4–5 (the 2:37:37 of 2026-08-17 was at the suite's shallower defaults), so its numbers arrive roughly a
day after these.

Raw JMH outputs committed alongside this file:

- `doc/permits.json` — request 3 (PermitSortBenchmarks, ~54 min)
- `doc/english-crossover.json` — request 5 (small-N crossover, ~2h20m wall)

## Checkout, environment, and the drift questions

Commit **`d3c359f`** (*"HS-10: a real-data case study -- San Francisco building permits"*), branch
`Revisions`. `mvn test` at this commit: **392 tests, 0 failures, 0 errors** — clean, as advertised.

Same instance as 2026-08-17 and as requests 1–2. To your three drift questions, plus one you couldn't
have seen:

| item | answer |
| --- | --- |
| Memory: 30 or 32 GiB? | **30 GiB is right.** `free -h` reports `Mem: total 30Gi`. Yesterday's "32 GiB" in my results doc was the c7g.4xlarge nominal size; the paper should say 30 GiB (the ~2 GiB gap is firmware/kernel reservation). |
| Maven 3.9.9 vs 3.9.16 | Pinned back to **3.9.16** for these runs (user-local install). |
| Kernel 6.12.95 vs 6.12.100 | The AMI has moved on; these runs are on **6.12.100-125.179.amzn2023.aarch64**. Not reversible on this box without a rebuild — recorded rather than reconciled. |
| JDK | The system Corretto auto-patched itself to 21.0.12.1+9 overnight on 2026-09-02. I caught it and **pinned these runs to Corretto 21.0.12.8.1 (build 21.0.12+8-LTS)** — byte-identical JDK version to the August run and to requests 1–2. JMH's log header confirms `VM version: JDK 21.0.12 ... 21.0.12+8-LTS`. |

One further honest drift: this host now has an **8 GiB zram swap device** (`/dev/zram0`), where the
August table says 0 B swap. It is compressed-RAM swap, not disk, and the benchmark JVMs' heaps stay
resident; but the table should say whatever `swapon --show` said on the day, and today that is 8 GiB.
Verbatim `lscpu`, `free -h`, `swapon --show`, `uname -r`, `java -version` and `mvn -v` are in the
appendix below, so the System Environment table can be written from fact.

Load average before the permits run: 0.16. Runs were sequential, never concurrent.

Commands (verbatim from the request):

```
java -jar target/benchmarks.jar "PermitSortBenchmarks" -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff permits.json
java -jar target/benchmarks.jar "StringSortBenchmarks.(insertionSort|systemSort|quickHuskySort|radixHuskySort16)$" -p corpus=english -p n=4,10,20,50,100,200,500,1000,2000,10000 -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-crossover.json
```

## Request 3 — San Francisco building permits (ms/op, Cnt = 50, ± = JMH 99.9% CI)

| n | radix/16 | radix/11 | radix/8 | quickHusky | quickHusky+cleanup | dualPivot | system |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 2.538 ± 0.045 | 2.738 ± 0.076 | 2.751 ± 0.046 | 6.544 ± 0.057 | 7.183 ± 0.046 | 12.345 ± 0.073 | 13.443 ± 0.082 |
| 100,000 | 12.760 ± 0.120 | 14.556 ± 0.336 | 14.542 ± 0.318 | 29.359 ± 0.778 | 36.773 ± 1.469 | 53.655 ± 0.419 | 63.522 ± 0.895 |
| 198,900 | 32.653 ± 2.255 | 37.500 ± 2.313 | 34.829 ± 2.561 | 72.091 ± 1.059 | 85.759 ± 1.721 | 136.566 ± 1.260 | 160.269 ± 2.608 |

Headlines at the full corpus, all with non-overlapping intervals:

- **radix/16 over the system sort: 4.91×** (your preview said 4.40× — slightly stronger here).
- **radix/16 over QuickHuskySort: 2.19×** at 100k and **2.21×** at 198,900 — the `RadixImprovements`
  comparable, agreeing with your 2.19× almost exactly.
- radix/16 over dual-pivot (primitive-array path): 4.18×.

### The pair you asked us to look at

Cleanup-pass cost — `quickHuskySortWithCleanup` over `quickHuskySort`, identical codes, the pass
provably finding nothing to do:

| n | cleanup cost | intervals |
|---:|---:|---|
| 32,000 | **9.8%** | separated |
| 100,000 | **25.3%** | separated |
| 198,900 | **19.0%** | separated |

Two differences from your preview (5.9% / 11.9% / 17.4%): the cost is *larger* here at every size, and
it is **not monotonic in n** — it peaks at 100,000 and falls back at the full corpus. So "grows with n"
does not survive the machine change; "material and never small past 32,000" does. If the $p_{crit}$
discussion leans on monotonicity, it now has a counterexample from the machine the paper quotes;
15–25% at the sizes that matter may be the more defensible phrasing.

## Request 5 — the small-N crossover (µs/op, Cnt = 50)

| n | insertionSort | systemSort | quickHuskySort | radix/16 | winner |
|---:|---:|---:|---:|---:|---|
| 4 | 0.108 ± 0.064 | **0.051 ± 0.001** | 0.087 ± 0.000 | 166.4 ± 0.1 | system |
| 10 | 0.202 ± 0.000 | **0.171 ± 0.001** | 0.222 ± 0.004 | 166.7 ± 0.1 | system |
| 20 | 0.489 ± 0.003 | **0.462 ± 0.009** | 0.515 ± 0.004 | 167.0 ± 0.1 | system (thin) |
| 50 | **1.462 ± 0.010** | 1.668 ± 0.005 | 1.781 ± 0.021 | 168.0 ± 0.1 | insertion |
| 100 | **3.346 ± 0.029** | 4.380 ± 0.041 | 4.543 ± 0.104 | 169.7 ± 0.2 | insertion |
| 200 | **7.996 ± 0.054** | 10.556 ± 0.084 | 9.213 ± 0.042 | 173.1 ± 0.3 | insertion |
| 500 | 38.861 ± 0.400 | 37.467 ± 0.772 | **27.529 ± 0.543** | 185.8 ± 0.2 | quickHusky |
| 1,000 | 138.7 ± 0.6 | 122.2 ± 0.6 | **73.6 ± 1.4** | 205.3 ± 0.4 | quickHusky |
| 2,000 | 407.1 ± 1.4 | 348.2 ± 1.3 | **223.9 ± 3.8** | 253.7 ± 1.0 | quickHusky |
| 10,000 | 5456.1 ± 21.6 | 3157.8 ± 14.7 | 2291.2 ± 24.6 | **1024.8 ± 9.0** | radix/16 |

Crossover points on Graviton3:

- **system sort wins at n ≤ 20** (at 20 it is thin: 0.462 vs insertion's 0.489);
- **insertion sort wins outright from 50 through 200**;
- **QuickHuskySort takes over between 200 and 500** and holds through 2,000;
- **radix/16 overtakes QuickHuskySort between 2,000 and 10,000**. Its curve is a flat **~166 µs floor**
  (allocation/setup of the 2×65,536-entry counting structures, near-constant from n=4 to n=500) that
  the O(n)-ish growth only starts to dominate past a few thousand elements — a concrete number for why
  the paper's guidance should not recommend radix below the thousands.

(At n=4–20 the absolute numbers are harness-dominated, as you predicted; the orderings are still
stable, with intervals mostly separated.)

## Appendix — environment, verbatim

```
$ uname -r
6.12.100-125.179.amzn2023.aarch64

$ java -version
openjdk version "21.0.12" 2026-07-21 LTS
OpenJDK Runtime Environment Corretto-21.0.12.8.1 (build 21.0.12+8-LTS)
OpenJDK 64-Bit Server VM Corretto-21.0.12.8.1 (build 21.0.12+8-LTS, mixed mode, sharing)

$ mvn -v
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: /workplace/liaoyunl/husky-sort/tools/apache-maven-3.9.16
Java version: 21.0.12, vendor: Amazon.com Inc.
OS name: "linux", version: "6.12.100-125.179.amzn2023.aarch64", arch: "aarch64"

$ free -h
               total        used        free      shared  buff/cache   available
Mem:            30Gi        12Gi       948Mi       4.2Gi        17Gi        13Gi
Swap:          8.0Gi       5.8Gi       2.2Gi

$ swapon --show
NAME       TYPE      SIZE USED PRIO
/dev/zram0 partition   8G 5.8G  100

$ lscpu (abridged; full flags available on request)
Architecture: aarch64        Vendor: ARM (Neoverse V1, r1p1)
CPU(s): 16                   Thread(s) per core: 1
Core(s) per socket: 16       Socket(s): 1
L1d: 64 KiB/core             L1i: 64 KiB/core
L2: 1 MiB/core               L3: 32 MiB (shared)
NUMA nodes: 1
```

Instance: AWS EC2 c7g.4xlarge (AWS Graviton3), non-burstable.
