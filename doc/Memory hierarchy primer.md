# Memory hierarchy: a short primer

Written 2026-09-10, prompted by the cache factor in the paper's appendix A.1 and by the question of
what a "cache line" is and how it relates to a page. It is a primer, not a survey: enough to reason
about why an algorithm with a good comparison count can still be slow, and no more.

**Provenance of the numbers below.** Page size, cache line size and the cache capacities were
measured on Robin's own M1 with `sysctl` (commands at the end). The TLB capacities are the one
exception: Apple does not document them, so those come from published reverse-engineering of the M1
and should be read as approximate. Where a figure is quoted for the AWS Graviton3 machine of record
it comes from ARM's documentation of Neoverse V1, because that machine does not expose its cache
geometry to the guest at all — which is why the paper's environment table shows a dash for it.

## 1. Virtual addresses, the MMU and pages

Every address a program touches is *virtual* — a fiction private to that process. Real DRAM is
addressed physically, so something has to translate between the two, and that something is the
**MMU** (memory management unit), a hardware block between the core and the memory system.

It translates by consulting **page tables**: a radix tree that the kernel maintains in ordinary
memory, indexed by successive chunks of the virtual address. Memory is managed in fixed-size
**pages** — 16 KB on Apple Silicon, 4 KB by the older x86 convention — so the low 14 bits of an
address on this machine are an offset within a page and the upper bits select which page.

The catch is that walking a multi-level page table costs several dependent memory accesses *for
every single memory access*. Done literally, that would be ruinous.

## 2. The TLB

Hence the **TLB** (translation lookaside buffer): a small, very fast cache of recently used
translations, living inside the MMU. On a hit, translation is effectively free. On a miss, the
hardware performs the page walk described above and installs the result.

The point most worth holding onto is that **this is a separate caching layer from the data caches**.
The TLB caches *where the data is*; L1 and L2 cache *the data itself*. A single load consults both,
and can hit in one and miss in the other.

This is also where the 16 KB page pays off. The M1's per-core data TLB is reported at roughly 160
entries, backed by a shared second level of around 3,000. Three thousand entries at 16 KB each is
about **48 MB of address space reachable without a page walk**; the same TLB with 4 KB pages would
cover a quarter of that. The benchmark arrays in the HuskySort work are a few megabytes, so they sit
well inside TLB reach, and the TLB is essentially never the bottleneck there. That is why the paper's
cache argument is about lines and not about pages.

## 3. The data caches, where the interesting behaviour is

Once an address is translated, the access looks for its data in L1d, then L2, then a system-level
cache, then DRAM. The unit of transfer at every level is the **cache line** — 128 bytes on Apple
Silicon, 64 bytes on most x86-64 and on Neoverse V1. Ask for four bytes and you get all 128; the
neighbours come along at no extra cost.

Approximate costs on this machine, and the reason any of this matters:

| level | latency, order of magnitude |
| --- | --- |
| L1d | 3–4 cycles |
| L2 | 15–20 cycles |
| DRAM | 100+ **nanoseconds** |

That is a factor of a hundred or more between the top and the bottom of the hierarchy. It can
therefore dominate everything a comparison count predicts, and routinely does.

## Robin's machine, measured

| | |
| --- | --- |
| CPU | Apple M1, 8 cores (4 performance + 4 efficiency) |
| Cache line | **128 bytes** |
| Page | 16 KB |
| Performance cores (4) | L1i 192 KB, L1d 128 KB each; L2 12 MB shared across the cluster |
| Efficiency cores (4) | L1i 128 KB, L1d 64 KB each; L2 4 MB shared across the cluster |
| System-level cache | 8 MB, not exposed by `sysctl` |

### Getting these yourself, and one trap

```bash
sysctl -n machdep.cpu.brand_string hw.cachelinesize hw.pagesize
sysctl -a | grep -E 'hw.perflevel[01].(name|physicalcpu|l1[id]cachesize|l2cachesize)'
```

**Use the `perflevel` values, not the plain ones.** On Apple Silicon `hw.l1dcachesize` and
`hw.l1icachesize` report the *efficiency* cluster, with nothing in the output to say so, while
benchmarks run on the performance cores. This is not hypothetical: the paper's environment table
carried "128 KB L1I / 64 KB L1D per core" for this machine until 2026-09-10, which is the efficiency
geometry. The performance cores have 192 KB and 128 KB. Corrected in commit `4e4d115`.

## Why it matters for sorting

If one sentence survives, let it be this:

> An algorithm that walks memory in order gets many references per cache miss; one that jumps around
> pays a miss per reference.

Concretely, on a 64-byte line, a Java reference under compressed oops is 4 bytes, so a single refill
serves **16** references — 32 on this machine's 128-byte lines. Sequential access amortises the miss
across all of them. Random access does not.

Two consequences that show up directly in this project:

- **Quicksort beats heapsort at comparable comparison counts.** Partitioning scans linearly from both
  ends; heapsort jumps by powers of two. This is LaMarca and Ladner's result, cited in the paper's
  introduction.
- **Husky encoding works partly for this reason.** Sorting 8-byte codes packed contiguously in a
  `long[]` touches a small fraction of the lines that chasing object references does, so the same
  number of comparisons costs far less. The paper's array-access model is a deliberately simplified,
  single-level stand-in for exactly this effect — see the introduction, where it is positioned
  against the external-memory and cache-oblivious literature rather than as a replacement for it.

## What this does not cover

Associativity and eviction policy, write-back versus write-through, prefetchers (which are a large
part of why sequential access is fast, and are ignored above), coherence between cores, NUMA, and
huge pages. Any of these can matter; none is needed to understand why an inexact husky code still
beats an object comparison.
