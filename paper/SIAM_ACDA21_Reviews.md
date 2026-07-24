# SIAM ACDA21 Reviews — Submission 60, "Husky Sort"

Verbatim reviews from the original SIAM ACDA21 submission (arXiv:2012.00866), rejected.
Authors: Robin Hillyard, Yunlu Liaozheng, Sai Vineeth Kandappareddigari. Kept here as the
reference for the resubmission rewrite (see [../TODO.md](../TODO.md) and
[../doc/Radix Sort Benchmark Results.md](../doc/Radix%20Sort%20Benchmark%20Results.md)).

## Review 1

This paper introduce Husky Sort - an algorithm which combines quick-sort and TimSort. The
authors implement this algorithm in Java and most of the discussion of the algorithm is one
that applies to objects as found in Java.

The authors note that [Zhang, 2016] showed the quick-sort is the fastest general-purpose
sorting algorithm. While I am not familiar with the paper by Zhang it is worth noting that
other algorithms such as radix can be very competitive with quicksort/mergesort when there is
a small distribution. Furthermore, merge-sort and radix are known to be very scalable as they
can be parallelized across many-threads on both the CPU an GPU.

The literature review in this paper is somewhat limited. The opening paragraph of the abstract
"Much of the copious literature on the subject of sorting has concentrated on minimizing the
number of comparisons and/or exchanges/copies. However, a more appropriate yardstick for the
performance of sorting algorithms is based on the total number of array accesses that are
required (the "work")." The above sentence suggests that the authors are not familiar with the
concept of "external memory" and "cache oblivious sorting". Both of which were hot topics some
15 and 35 years ago, respectively. The literature survey does not capture these very important
papers:
- "The input/output complexity of sorting and related problems", A Aggarwal, JS Vitter
- "Cache-oblivious algorithms", M Frigo, CE Leiserson, H Prokop, S Ramachandran

The authors do show that their new algorithm offers some performance improvement over a
standard quick sort or over TimSort. However, this is not enough to justify publication in
SIAM ACDA.

Other comments:
- The paper is not using the SIAM format.
- References (bibliography) is also using an incorrect format.
- The authors supply both pseudo code as well source code. This isn't necessary.
- There seems to be a broken link to [Jon L. Bentley [n.d.]].
- Figure 4 should not be there as an image but should be text. You need more description of the
  experimental setup.
- The experiment section does not include enough details. Did you time the encoding phase? How
  much time does it account for?
- Table 2-3, measuring performance for such small tables requires very careful experiments,
  otherwise runtime overheads can dominate the time (especially for existing functions as
  quick-sort). As the array size increased in size, the new algorithm lost its performance
  benefit over the existing algorithms.
- Figures 6-9 should be made into LaTeX tables instead of being screenshots.
- Figures 6-9, your measurement of the performance improvement does not do you justice. You are
  showing that your algorithm is 42% faster (for 5.95 over 3.48), however in practice, your
  algorithm is 1.7X faster!
- Figures 6-9 — what is the metric for the time? Seconds? Milliseconds or something else?

Dear authors, the summary of the PC discussion has led to us reject the paper as we did not
find enough algorithmic innovation in the paper. While the paper shows some improvement in
performance, the benchmarking was limited and did not capture enough unique cases where sorting
is necessary.

## Review 2

This paper describes a new sequential sorting algorithm and implementation that the authors
create by using a hybrid combination of quicksort and Timsort. The abstract promises an
analysis of algorithms using number of array accesses, yet this is not delivered in the paper.
The paper is best categorized as an experimental algorithms paper, yet it fails on multiple
counts. For an experimental paper to contain a scientific study of comparison of approaches,
more care must be taken. Only one input is considered, of strings in a collection. For an
algorithm to run correctly and timing considered, more inputs should be used, such as those
that may cause performance issues. For instance, it is assumed that the input is fairly well
balanced, and not skewed in values. The algorithm description is all over the place. There is
no clear presentation of prior algorithms (e.g. merge sort, quicksort, insertion sort, timsort,
etc) and this new approach. Also, the implementation of the algorithm should be separated from
the algorithmic description. In addition, the algorithm assumes 64-bit words, as is common
today, and may not be appropriate for comparison on different key sizes. With the large
literature on sorting, perhaps one of the most studied problems in computer science, the
authors must have a better description of the algorithm, its analysis, and experimental study.

## Review 3

The paper presents one cool idea: by representing objects that are expensive to compare
(strings, dates) by almost-monotonic 64-bit hash codes, we can sort the objects faster. The
idea is to compute a monotonic or almost monotonic code (the authors call it a husky code) for
each object, sort the codes, swapping the reference objects when the codes are swapped, and
then sorting the objects with a sorting algorithm that runs fast on almost-sorted arrays.

The idea is cool, is well explained in the paper (and to some extent, analyzed theoretically),
and it is tested reasonably well (but not very well). The first part of the paper is pretty
well written, the second half is pretty awful (sorry).

The second part that is poorly presented and written reads like a very thorough student project
report, not like a research paper. I will give evidence and some pointers below.

In general, sorting is a very mature area and is not easy to make a significant contribution.
We see this in the citation of the 2016 paper (Zhang et al) that checked this and concluded
that known adaptations of the 1961 quicksort is hard to beat. But the paper also says that the
system sort in Java and Python uses a 2002 algorithm, so it's still worth revisiting once in a
while, and like I wrote, I think that the idea of exploiting an almost monotonic hash is new and
cool.

This could be a really nice paper, if it was well written and with thorough testing, but it is
poorly written (most of it is) and the testing is not as thorough as it should be. There is
also one question that nags me, although it is probably not a major issue.

The question that nags me is why the authors (and the algorithm) do not use radix sort on the
64-bit codes. Is quicksort faster than radix sort on 64-bit integers? This seems strange. Even
if you cannot do the swaps on the objects during radix sort (I am not sure this is not
possible), you can get a representation of the permutation at the end and permute the objects
accordingly. This requires an explanation.

The low quality of the writing is manifested in many ways:
- The title of 3.2 ("Explanation of Working"; this is not English).
- "long is primitive" --> "long is a primitive data type."
- The text explains that hash is a better word than husky for the codes, but does not explain
  why husky is good. I do not get it.
- "As shown in code 1" --> "as shown in Listing 1." (the full stop or period is missing, here
  and in many other places).
- Way too much Java code that not all readers can understand and that sheds no useful light on
  the algorithms.
- The first thing the "Implementation" section talks about is not the implementation but the
  data sets that were used for evaluation. There is actually another section (5) entitled "Test
  Case and Analysis", but the test cases are described earlier.
- The analysis is split between this section 5 and Section 3.3.
- (in 5.1) "This effect can be seen in the table" but the reference to the table, table 4
  (should be Table 4 with a capital) is given only later in the paragraph, again without a full
  stop.
- Table 1: what is counted? Comparisons? You should say. Also, long numbers should be aligned to
  the right, not centered, to show the magnitude.
- What data led to Table 2?
- What are the units of Table 3? Seconds? You should say.
- The data in Figures 6 to 9 would probably be better presented in some graph or graphs.

Figure 4 documents the environment for the experiments in lots of detail (cache sizes etc). If
these details matter, would the method show similar advantages on computers with other
characteristics? On other versions of Java? The paper would have been stronger with at least
some of the experiments being carried out on multiple platforms, to show that the results are
consistent and do not depend much on this processor and version of Java. Also, Java 1.8 is
really old. It would have been better to use a more recent version, maybe a long-term support
version (I think 11 is the most recent LTS version).

Also, research papers usually do not specify the contribution of each author in the body of the
paper (some journals, most in biology, require a separate statement of contribution, but not as
part of the text). I am referring to the text in Page 2, just before Section 3, stating that the
key innovation is due to Author 2.

## Review 4

This paper observes that, if we can construct an order-preserving (or nearly order preserving)
hash function, then we can sort very quickly. The paper demonstrates good speedups on inputs
with a known distribution and a hand-selected hash function.

I think it is well known that if you can accurately bin your items into very small bins, then
it is possible to sort quite quickly.

The problem is the hash function construction. It would be trivial to construct inputs that
cause the proposed scheme to perform poorly.

The paper offers only a few heuristic suggestions why it might in many cases be possible to
construct such a hash function. But it doesn't say how to do so automatically from an input
sample, and it doesn't discuss at all what happens when the input doesn't support such a hash
function. These are the questions at the heart of this approach.
