# Run results from Yunlu — request 11, appendix: every full-suite row, 2026-09-21

Companion to `Run results from Yunlu 2026-09-21.md`. Every cell is `score ± error` in ms/op exactly as JMH prints it (3
decimals, HALF_UP), read from `req11-full-suite.json` (`-f 5 -wi 5 -i 10 -r 2s -w 2s -e CleanupPassBenchmarks`, Cnt = 50
per row, ± = 99.9 % CI half-width). ‡ after a cell = that row's warm-up detector fired (mean over forks of
mean(iterations 1–5) ÷ mean(iterations 6–10) > 1.10; the row was still speeding up inside the measurement window, so its
score is above steady state — see the main document, "Warm-up convergence"). "—" = the (method, params) row is absent
because the benchmark throws for that parameter (12 rows, listed in the main document). Methods are columns; the
main document carries the ratios and the reading.

## AdversarialSortBenchmarks (124 rows)

### collapsedBits* (fixedHighBits × n; 84 rows)

| fixedHighBits, n | `collapsedBitsDualPivotQuicksort` | `collapsedBitsQuickHuskySort` | `collapsedBitsRadixHuskySort11` | `collapsedBitsRadixHuskySort16` | `collapsedBitsRadixHuskySort8` | `collapsedBitsSystemSort` |
|---|---:|---:|---:|---:|---:|---:|
| fixedHighBits=0, n=200,000 | 29.847 ± 0.080 | 31.801 ± 0.355 | 8.185 ± 0.136 | 8.437 ± 0.170 | 9.097 ± 0.123 | 40.241 ± 0.292 |
| fixedHighBits=16, n=200,000 | 29.985 ± 0.081 | 31.558 ± 0.250 | 8.516 ± 0.120 | 8.325 ± 0.191 | 9.734 ± 0.114 | 40.565 ± 0.545 |
| fixedHighBits=32, n=200,000 | 29.471 ± 0.159 | 31.370 ± 0.240 | 8.749 ± 0.122 | 8.007 ± 0.208 | 9.993 ± 0.139 | 40.201 ± 0.195 |
| fixedHighBits=48, n=200,000 | 30.464 ± 0.102 | 31.365 ± 0.358 | 8.940 ± 0.086 | 7.757 ± 0.118 | 10.726 ± 0.115 | 41.764 ± 0.718 |
| fixedHighBits=56, n=200,000 | 78.030 ± 0.793 | 15.150 ± 0.092 | 8.949 ± 0.120 | 6.799 ± 0.106 | 10.778 ± 0.101 | 26.274 ± 0.700 |
| fixedHighBits=60, n=200,000 | 123.882 ± 2.254 | 7.973 ± 0.062 | 7.904 ± 0.082 | 5.963 ± 0.076 | 9.779 ± 0.088 | 16.736 ± 0.046 |
| fixedHighBits=63, n=200,000 | 43.778 ± 0.178 | 1.875 ± 0.019 | 6.511 ± 0.030 | 4.766 ± 0.046 | 8.298 ± 0.033 | 7.588 ± 0.025 |
| fixedHighBits=0, n=1,000,000 | 323.140 ± 7.969 | 400.255 ± 12.876 | 67.766 ± 2.239‡ | 67.287 ± 1.604 | 78.844 ± 2.333‡ | 502.507 ± 12.786 |
| fixedHighBits=16, n=1,000,000 | 304.346 ± 8.013 | 439.867 ± 6.288 | 66.148 ± 2.355 | 68.389 ± 1.949 | 81.885 ± 2.439 | 509.114 ± 11.792 |
| fixedHighBits=32, n=1,000,000 | 324.098 ± 5.135 | 464.714 ± 7.949 | 66.845 ± 2.185‡ | 65.901 ± 1.543 | 82.445 ± 2.344 | 510.645 ± 12.837 |
| fixedHighBits=48, n=1,000,000 | 323.950 ± 7.918 | 375.203 ± 6.461 | 64.109 ± 2.472‡ | 60.362 ± 1.178 | 82.388 ± 2.609‡ | 515.169 ± 12.835 |
| fixedHighBits=56, n=1,000,000 | 693.772 ± 12.182 | 209.020 ± 3.682 | 57.808 ± 2.302 | 52.122 ± 1.656 | 78.534 ± 2.776 | 175.950 ± 0.592 |
| fixedHighBits=60, n=1,000,000 | 845.393 ± 26.894 | 76.286 ± 1.406 | 47.113 ± 2.295 | 40.440 ± 1.505 | 64.135 ± 2.491‡ | 115.298 ± 0.913 |
| fixedHighBits=63, n=1,000,000 | 181.620 ± 2.172 | 14.743 ± 0.623 | 39.401 ± 2.208 | 31.379 ± 0.690 | 56.281 ± 2.396 | 44.303 ± 0.874 |

### sharedPrefix* (prefixLength × n; 40 rows)

| n, prefixLength | `sharedPrefixQuickHuskySort` | `sharedPrefixRadixHuskySort11` | `sharedPrefixRadixHuskySort16` | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` |
|---|---:|---:|---:|---:|---:|
| n=200,000, prefixLength=0 | 74.374 ± 1.071 | 41.681 ± 2.051‡ | 40.770 ± 2.500‡ | 43.853 ± 1.995‡ | 162.300 ± 2.924 |
| n=200,000, prefixLength=10 | 90.756 ± 0.917 | 98.459 ± 0.974 | 97.715 ± 0.638 | 99.581 ± 0.961 | 86.309 ± 1.249 |
| n=200,000, prefixLength=20 | 93.673 ± 1.201 | 102.575 ± 1.014 | 101.299 ± 1.307 | 105.092 ± 1.451 | 90.835 ± 1.305 |
| n=200,000, prefixLength=40 | 106.028 ± 1.204 | 114.423 ± 1.023 | 114.332 ± 1.229 | 116.131 ± 1.255 | 101.613 ± 2.211 |
| n=1,000,000, prefixLength=0 | 606.427 ± 7.708 | 258.681 ± 5.079 | 230.328 ± 4.641 | 269.087 ± 4.576 | 1423.940 ± 26.205 |
| n=1,000,000, prefixLength=10 | 914.440 ± 18.252 | 922.287 ± 10.275 | 910.590 ± 6.737 | 939.154 ± 12.325 | 906.164 ± 16.458 |
| n=1,000,000, prefixLength=20 | 929.602 ± 21.442 | 939.924 ± 9.571 | 924.529 ± 9.582 | 943.507 ± 9.357 | 886.585 ± 18.381 |
| n=1,000,000, prefixLength=40 | 960.580 ± 15.667 | 990.455 ± 12.362 | 971.720 ± 9.032 | 986.464 ± 11.912 | 931.236 ± 14.236 |

## DateSortBenchmarks (6 rows)

### all methods

| n | `dutchHuskySort` | `dutchHuskySortWithInsertion` | `radixHuskySort11` | `radixHuskySort16` | `radixHuskySort8` | `systemSort` |
|---|---:|---:|---:|---:|---:|---:|
| n=20,000 | 4.383 ± 0.056 | 4.352 ± 0.041 | 0.782 ± 0.003 | 0.841 ± 0.004 | 0.876 ± 0.001 | 3.790 ± 0.020 |

## NumericSortBenchmarks (123 rows)

### `integer[]`

| n | `integerDualPivotQuicksort` | `integerQuickHuskySort` | `integerRadixHuskySort11` | `integerRadixHuskySort16` | `integerRadixHuskySort8` | `integerRawQuicksort` | `integerRawRadixSort` | `integerSinglePivotQuicksort` | `integerSystemSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| n=20,000 | 1.947 ± 0.006 | 2.101 ± 0.012 | 0.572 ± 0.002 | 0.629 ± 0.004 | 0.680 ± 0.001 | 1.139 ± 0.002 | 0.634 ± 0.001 | 1.855 ± 0.007 | 2.876 ± 0.010 |
| n=100,000 | 13.160 ± 0.053 | 13.430 ± 0.066 | 3.381 ± 0.038 | 3.083 ± 0.029 | 4.106 ± 0.025 | 6.657 ± 0.023 | 2.526 ± 0.004 | 11.853 ± 0.092 | 18.379 ± 0.400 |
| n=500,000 | 85.196 ± 0.649 | 86.574 ± 1.982 | 27.604 ± 0.214 | 23.962 ± 0.311 | 32.801 ± 0.224 | 38.189 ± 0.052 | 12.783 ± 0.023 | 72.462 ± 0.477 | 125.252 ± 1.366 |

### `long[]`

| n | `longDualPivotQuicksort` | `longQuickHuskySort` | `longRadixHuskySort11` | `longRadixHuskySort16` | `longRadixHuskySort8` | `longRawQuicksort` | `longRawRadixSort` | `longSystemSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| n=20,000 | 2.038 ± 0.004 | 2.174 ± 0.008 | 0.517 ± 0.003 | 0.635 ± 0.003 | 0.578 ± 0.003 | 1.113 ± 0.002 | 0.597 ± 0.001 | 2.940 ± 0.006 |
| n=100,000 | 13.964 ± 0.072 | 13.567 ± 0.034 | 3.146 ± 0.036 | 3.215 ± 0.032 | 3.680 ± 0.038 | 6.686 ± 0.015 | 2.410 ± 0.007 | 17.859 ± 0.215 |
| n=500,000 | 92.404 ± 1.090 | 103.107 ± 1.586 | 27.777 ± 0.213 | 26.922 ± 0.248 | 32.114 ± 0.209 | 38.515 ± 0.295 | 12.588 ± 0.051 | 140.149 ± 1.884 |

### `double[]`

| n | `doubleDualPivotQuicksort` | `doubleQuickHuskySort` | `doubleRadixHuskySort11` | `doubleRadixHuskySort16` | `doubleRadixHuskySort8` | `doubleRawQuicksort` | `doubleRawRadixSort` | `doubleSystemSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| n=20,000 | 2.188 ± 0.010 | 2.207 ± 0.008 | 0.611 ± 0.003 | 0.695 ± 0.004 | 0.693 ± 0.005 | 1.248 ± 0.002 | 0.582 ± 0.001 | 3.088 ± 0.013 |
| n=100,000 | 14.029 ± 0.058 | 14.336 ± 0.031 | 3.677 ± 0.054 | 3.619 ± 0.042 | 4.267 ± 0.043 | 7.524 ± 0.037 | 2.320 ± 0.007 | 19.209 ± 0.061 |
| n=500,000 | 92.286 ± 1.051 | 96.349 ± 1.586 | 32.113 ± 0.352 | 30.104 ± 0.270 | 35.970 ± 0.320 | 42.661 ± 0.045 | 12.106 ± 0.046 | 142.815 ± 1.677 |

### `bigInteger[]`

| n | `bigIntegerDualPivotQuicksort` | `bigIntegerQuickHuskySort` | `bigIntegerRadixHuskySort11` | `bigIntegerRadixHuskySort16` | `bigIntegerRadixHuskySort8` | `bigIntegerRawQuicksort` | `bigIntegerRawRadixSort` | `bigIntegerSystemSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| n=20,000 | 3.400 ± 0.027 | 2.666 ± 0.016 | 0.929 ± 0.008 | 1.011 ± 0.007 | 1.006 ± 0.006 | 1.261 ± 0.001 | 0.735 ± 0.001 | 3.960 ± 0.015 |
| n=100,000 | 23.110 ± 0.172 | 16.836 ± 0.244 | 5.827 ± 0.101 | 5.614 ± 0.118 | 6.582 ± 0.131 | 7.524 ± 0.018 | 3.198 ± 0.017 | 25.947 ± 0.374 |
| n=500,000 | 219.782 ± 2.686 | 152.482 ± 1.710 | 54.666 ± 0.357 | 53.114 ± 0.395 | 59.503 ± 0.341 | 43.013 ± 0.256 | 16.661 ± 0.058 | 213.432 ± 2.892 |

### `bigDecimal[]`

| n | `bigDecimalDualPivotQuicksort` | `bigDecimalQuickHuskySort` | `bigDecimalRadixHuskySort11` | `bigDecimalRadixHuskySort16` | `bigDecimalRadixHuskySort8` | `bigDecimalRawQuicksort` | `bigDecimalRawRadixSort` | `bigDecimalSystemSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| n=20,000 | 5.293 ± 0.011 | 2.781 ± 0.013 | 1.044 ± 0.041 | 1.140 ± 0.017 | 1.136 ± 0.056 | 1.456 ± 0.002 | 0.778 ± 0.002 | 5.110 ± 0.023 |
| n=100,000 | 35.446 ± 0.406 | 19.660 ± 0.363 | 7.266 ± 0.185 | 7.103 ± 0.209 | 7.534 ± 0.146 | 8.773 ± 0.032 | 3.525 ± 0.024 | 36.483 ± 0.496 |
| n=500,000 | 250.384 ± 2.395 | 152.474 ± 1.189 | 51.795 ± 0.225 | 50.982 ± 0.431 | 56.154 ± 0.308 | 47.622 ± 0.134 | 17.365 ± 0.062 | 266.187 ± 2.604 |

## ParallelRadixSortBenchmarks (14 rows)

### `Long[]`, 11-bit digits, p = 1 / 2 / 4 / 8

| n | `serialRadixHuskySort11` | `parallelRadixHuskySort11_p1` | `parallelRadixHuskySort11_p2` | `parallelRadixHuskySort11_p4` | `parallelRadixHuskySort11_p8` | `quickHuskySort` | `systemSortParallel` |
|---|---:|---:|---:|---:|---:|---:|---:|
| n=2,000,000 | 157.006 ± 4.679‡ | 195.960 ± 4.944 | 149.612 ± 4.762 | 105.152 ± 1.833 | 98.344 ± 1.601 | 1103.052 ± 16.407 | 87.059 ± 4.528 |
| n=10,000,000 | 957.730 ± 23.269 | 1107.859 ± 23.229 | 809.548 ± 24.574 | 629.169 ± 23.211 | 586.108 ± 26.024 | 5722.351 ± 43.141 | 765.466 ± 9.464 |

## ParallelStringSortBenchmarks (72 rows)

### all methods (same rows as the main document's step-5 matrix)

| corpus, n | `serialRadixHuskySortAuto` | `parallelRadixHuskySortAuto_p1` | `parallelRadixHuskySortAuto_p2` | `parallelRadixHuskySortAuto_p4` | `parallelRadixHuskySortAuto_p8` | `parallelRadixHuskySortAuto_pAll` | `parallelRadixHuskySort11_p8` | `systemSortParallel` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| corpus=english, n=32,000 | 3.835 ± 0.208‡ | 3.954 ± 0.208‡ | 3.938 ± 0.238‡ | 4.033 ± 0.238‡ | 4.092 ± 0.216‡ | 4.000 ± 0.231‡ | 4.025 ± 0.272‡ | 4.507 ± 0.050 |
| corpus=english, n=200,000 | 49.360 ± 2.946‡ | 51.315 ± 2.442‡ | 30.203 ± 1.651‡ | 23.237 ± 0.822 | 18.851 ± 0.967 | 19.361 ± 0.777 | 21.098 ± 0.411 | 10.019 ± 0.317 |
| corpus=english, n=1,000,000 | 256.039 ± 5.808 | 258.118 ± 5.906 | 244.657 ± 16.039‡ | 97.662 ± 7.962‡ | 76.658 ± 5.073‡ | 72.663 ± 4.505‡ | 78.626 ± 6.505‡ | 89.415 ± 3.106 |
| corpus=chinese, n=32,000 | 2.040 ± 0.041 | 2.874 ± 0.127 | 2.813 ± 0.114 | 2.856 ± 0.124 | 2.827 ± 0.117 | 2.896 ± 0.112 | 2.764 ± 0.155 | 2.983 ± 0.010 |
| corpus=chinese, n=200,000 | 11.032 ± 0.319 | 11.256 ± 0.104 | 8.635 ± 0.070 | 6.948 ± 0.064 | 6.586 ± 0.070 | 6.657 ± 0.049 | 6.325 ± 0.103 | 5.988 ± 0.024 |
| corpus=chinese, n=1,000,000 | 57.387 ± 1.471 | 59.302 ± 1.254 | 42.370 ± 0.686 | 37.093 ± 0.582 | 33.597 ± 1.339 | 32.610 ± 1.263 | 31.836 ± 0.454 | 41.256 ± 1.760 |
| corpus=chinesenames, n=32,000 | 22.974 ± 0.455 | 23.731 ± 0.461 | 23.719 ± 0.439 | 23.483 ± 0.333 | 23.670 ± 0.422 | 23.343 ± 0.412 | 23.772 ± 0.393 | 21.318 ± 0.128 |
| corpus=chinesenames, n=200,000 | 151.171 ± 1.190 | 153.152 ± 1.342 | 129.895 ± 1.346 | 117.949 ± 0.978 | 111.558 ± 1.345 | 111.003 ± 1.574 | 112.907 ± 1.374 | 47.725 ± 0.789 |
| corpus=chinesenames, n=1,000,000 | 753.382 ± 14.248 | 775.352 ± 21.495 | 644.865 ± 17.479 | 600.327 ± 21.258 | 565.163 ± 9.731 | 558.039 ± 10.631 | 563.224 ± 13.078 | 235.053 ± 4.123 |

## PermitSortBenchmarks (42 rows)

### all 14 methods

| n | `systemSort` | `systemSortParallel` | `dualPivotQuicksort` | `quickHuskySort` | `quickHuskySortWithCleanup` | `radixHuskySort8` | `radixHuskySort11` | `radixHuskySort16` | `radixHuskySortAuto` | `parallelRadixHuskySort16_p4` | `parallelRadixHuskySort16_p8` | `parallelRadixHuskySortAuto_p8` | `parallelRadixHuskySortAuto_p8_chunk4k` | `parallelRadixHuskySortAuto_pAll` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| n=32,000 | 13.417 ± 0.078 | 4.379 ± 0.030 | 12.575 ± 0.075 | 6.437 ± 0.046 | 7.049 ± 0.066 | 2.857 ± 0.027 | 2.674 ± 0.028 | 2.630 ± 0.019 | 2.705 ± 0.030 | 3.183 ± 0.021 | 3.174 ± 0.032 | 2.836 ± 0.012 | 2.786 ± 0.029 | 2.830 ± 0.028 |
| n=100,000 | 56.463 ± 0.994 | 5.420 ± 0.072 | 51.764 ± 1.173 | 27.977 ± 0.731 | 34.537 ± 1.014 | 13.171 ± 0.613 | 13.479 ± 0.652 | 12.386 ± 0.487 | 11.570 ± 0.565 | 6.934 ± 0.158 | 6.818 ± 0.119 | 4.525 ± 0.055 | 4.808 ± 0.131 | 4.789 ± 0.137 |
| n=198,900 | 138.892 ± 2.317 | 12.623 ± 0.179 | 129.818 ± 2.307 | 70.710 ± 1.342 | 85.191 ± 1.901 | 34.981 ± 1.264 | 32.815 ± 0.807 | 32.125 ± 2.254 | 32.337 ± 2.131 | 14.883 ± 0.305 | 12.275 ± 0.230 | 9.782 ± 0.268 | 9.811 ± 0.296 | 8.380 ± 0.079 |

## StringSortBenchmarks (168 rows)

### baselines, comparison sorts, quick husky, encode

| corpus, n | `systemSort` | `systemSortParallel` | `systemSortPinyin` | `insertionSort` | `multikeyQuicksort` | `msdStringSort` | `quickHuskySort` | `quickHuskySortPhase2` | `quickHuskySortPhase2CodesOnly` | `huskyEncodeOnly` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| corpus=english, n=32,000 | 15.926 ± 0.362 | 5.119 ± 0.064 | — | 39.047 ± 0.356 | 7.380 ± 0.079 | 4.264 ± 0.047 | 10.488 ± 0.244 | 9.267 ± 0.209 | 6.400 ± 0.267 | 2.244 ± 0.072 |
| corpus=english, n=200,000 | 159.103 ± 2.937 | 10.525 ± 0.341 | — | 1160.132 ± 6.680 | 72.240 ± 0.814 | 48.010 ± 1.400 | 106.482 ± 1.504 | 85.552 ± 1.537 | 48.118 ± 0.452 | 22.106 ± 1.329‡ |
| corpus=english, n=1,000,000 | 1320.434 ± 24.126 | 99.413 ± 1.920 | — | 26756.221 ± 77.651 | 703.874 ± 10.002 | 278.229 ± 9.375 | 786.362 ± 11.175 | 709.242 ± 8.864 | 271.058 ± 3.732 | 136.906 ± 4.227 |
| corpus=chinese, n=32,000 | 9.828 ± 0.035 | 2.999 ± 0.011 | — | 32.135 ± 0.060 | 5.744 ± 0.058 | — | 5.122 ± 0.039 | 4.363 ± 0.038 | 2.275 ± 0.021 | 0.412 ± 0.007 |
| corpus=chinese, n=200,000 | 71.459 ± 0.509 | 5.922 ± 0.029 | — | 1027.624 ± 2.827 | 35.334 ± 0.247 | — | 31.114 ± 0.144 | 28.435 ± 0.143 | 16.322 ± 0.079 | 2.858 ± 0.048 |
| corpus=chinese, n=1,000,000 | 443.670 ± 2.096 | 41.554 ± 1.538 | — | 25740.719 ± 53.833 | 267.613 ± 2.280 | — | 221.714 ± 1.893 | 213.527 ± 1.492 | 79.118 ± 2.214 | 14.459 ± 0.157 |
| corpus=chinesenames, n=32,000 | 11.883 ± 0.255 | 22.290 ± 0.141 | 73.517 ± 0.441 | 38.098 ± 0.269 | 39.741 ± 0.591 | — | 28.467 ± 0.305 | 10.319 ± 0.180 | 8.390 ± 0.104 | 4.161 ± 0.065 |
| corpus=chinesenames, n=200,000 | 113.753 ± 1.166 | 48.701 ± 0.428 | 567.210 ± 2.708 | 1141.647 ± 3.306 | 299.437 ± 1.988 | — | 188.872 ± 1.241 | 88.220 ± 0.493 | 56.854 ± 0.129 | 38.773 ± 1.003 |
| corpus=chinesenames, n=1,000,000 | 902.895 ± 8.115 | 246.456 ± 5.762 | 3257.845 ± 26.207 | 26453.738 ± 34.795 | 1679.016 ± 9.336 | — | 1294.983 ± 8.106 | 784.411 ± 5.459 | 284.201 ± 5.726 | 209.555 ± 2.321 |

### radix husky family and the two 11b encode methods (the `huskyEncodeOnlyEnglish*` rows on chinese / chinesenames are English-coder-on-Chinese-text controls)

| corpus, n | `radixHuskySort8` | `radixHuskySort10` | `radixHuskySort11` | `radixHuskySort12` | `radixHuskySort13` | `radixHuskySort14` | `radixHuskySort16` | `radixHuskySortAuto` | `huskyEncodeOnlyEnglishMasking` | `huskyEncodeOnlyEnglishSaturating` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| corpus=english, n=32,000 | 4.260 ± 0.296‡ | 4.320 ± 0.299‡ | 4.108 ± 0.291‡ | 4.108 ± 0.302‡ | 3.805 ± 0.281‡ | 3.741 ± 0.258‡ | 3.782 ± 0.263‡ | 4.000 ± 0.300‡ | 1.272 ± 0.076‡ | 2.349 ± 0.069 |
| corpus=english, n=200,000 | 56.222 ± 2.093‡ | 56.770 ± 2.102‡ | 53.769 ± 2.636‡ | 55.808 ± 2.222‡ | 54.030 ± 2.336‡ | 54.863 ± 2.088‡ | 51.862 ± 2.686‡ | 54.843 ± 2.634‡ | 11.103 ± 0.937‡ | 24.831 ± 1.184 |
| corpus=english, n=1,000,000 | 314.524 ± 8.844 | 290.430 ± 7.101 | 280.531 ± 3.858 | 382.103 ± 15.561 | 306.171 ± 5.637 | 295.131 ± 10.743‡ | 269.177 ± 7.083 | 276.778 ± 6.926 | 79.242 ± 2.045 | 152.534 ± 4.030 |
| corpus=chinese, n=32,000 | 2.311 ± 0.047 | 2.113 ± 0.057 | 2.000 ± 0.035 | 2.096 ± 0.016 | 1.950 ± 0.052 | 1.937 ± 0.019 | 1.919 ± 0.035 | 2.117 ± 0.067 | 0.768 ± 0.012 | 1.041 ± 0.015 |
| corpus=chinese, n=200,000 | 13.543 ± 0.154 | 12.684 ± 0.152 | 11.904 ± 0.102 | 11.845 ± 0.129 | 10.930 ± 0.109 | 11.072 ± 0.131 | 10.440 ± 0.105 | 11.431 ± 0.123 | 5.095 ± 0.040 | 7.109 ± 0.062 |
| corpus=chinese, n=1,000,000 | 75.114 ± 1.751 | 71.045 ± 2.062 | 67.207 ± 2.193 | 66.594 ± 2.043 | 63.251 ± 1.945 | 64.112 ± 1.563 | 59.083 ± 1.532 | 59.577 ± 1.593 | 25.795 ± 0.150 | 36.441 ± 0.109 |
| corpus=chinesenames, n=32,000 | 25.580 ± 0.259 | 25.261 ± 0.218 | 25.025 ± 0.294 | 24.983 ± 0.224 | 24.985 ± 0.263 | 25.059 ± 0.282 | 24.630 ± 0.245 | 24.914 ± 0.313 | 0.894 ± 0.008 | 0.901 ± 0.012 |
| corpus=chinesenames, n=200,000 | 154.433 ± 1.136 | 153.718 ± 1.115 | 153.379 ± 1.438 | 151.860 ± 0.303 | 150.689 ± 0.240 | 153.190 ± 1.020 | 150.726 ± 0.276 | 152.537 ± 1.690 | 8.507 ± 0.420 | 10.594 ± 0.475 |
| corpus=chinesenames, n=1,000,000 | 784.902 ± 7.714 | 776.598 ± 10.586 | 761.486 ± 9.953 | 761.247 ± 7.701 | 760.926 ± 6.380 | 760.852 ± 11.024 | 762.765 ± 11.709 | 764.259 ± 12.352 | 60.978 ± 1.217 | 64.552 ± 1.080 |

## TupleSortBenchmarks (18 rows)

### all methods

| n | `dualPivotQuicksort` | `quickHuskySort` | `radixHuskySort11` | `radixHuskySort16` | `radixHuskySort8` | `systemSort` |
|---|---:|---:|---:|---:|---:|---:|
| n=20,000 | 3.339 ± 0.076 | 2.814 ± 0.016 | 1.228 ± 0.009 | 1.307 ± 0.005 | 1.398 ± 0.032 | 3.759 ± 0.020 |
| n=100,000 | 21.369 ± 0.428 | 17.308 ± 0.164 | 6.836 ± 0.052 | 6.507 ± 0.033 | 7.684 ± 0.046 | 23.629 ± 0.176 |
| n=500,000 | 166.185 ± 2.439 | 164.344 ± 2.591 | 67.276 ± 1.288 | 62.506 ± 0.726 | 69.269 ± 0.886 | 227.525 ± 2.953 |

## Rows whose warm-up detector fires (t > 1.10)

t = mean over the five forks of mean(iterations 1–5) ÷ mean(iterations 6–10) from `rawData`; "iterations 8–10" = the mean of
the last three measurement iterations over all forks, the best available estimate of the steady state for these rows.

### `req11-full-suite.json` (2 s iterations): 45 of 567 rows

| class.method | params | t | score | iterations 8–10 | iteration 1 |
|---|---|---:|---:|---:|---:|
| String.huskyEncodeOnlyEnglishMasking | corpus=english, n=200,000 | 1.29 | 11.103 ± 0.937 | 9.060 | 13.367 |
| String.radixHuskySort8 | corpus=english, n=32,000 | 1.25 | 4.260 ± 0.296 | 3.756 | 5.267 |
| String.radixHuskySortAuto | corpus=english, n=32,000 | 1.25 | 4.000 ± 0.300 | 3.536 | 4.946 |
| String.radixHuskySort13 | corpus=english, n=32,000 | 1.24 | 3.805 ± 0.281 | 3.395 | 4.755 |
| String.radixHuskySort11 | corpus=english, n=32,000 | 1.23 | 4.108 ± 0.291 | 3.667 | 4.881 |
| ParallelString.parallelRadixHuskySort11_p8 | corpus=english, n=32,000 | 1.23 | 4.025 ± 0.272 | 3.607 | 4.977 |
| Adversarial.sharedPrefixRadixHuskySort16 | n=200,000, prefixLength=0 | 1.23 | 40.770 ± 2.500 | 34.696 | 48.084 |
| String.radixHuskySort10 | corpus=english, n=32,000 | 1.22 | 4.320 ± 0.299 | 3.804 | 5.108 |
| String.huskyEncodeOnlyEnglishMasking | corpus=english, n=32,000 | 1.22 | 1.272 ± 0.076 | 1.080 | 1.450 |
| String.radixHuskySort12 | corpus=english, n=32,000 | 1.22 | 4.108 ± 0.302 | 3.680 | 5.051 |
| ParallelString.parallelRadixHuskySortAuto_p4 | corpus=english, n=1,000,000 | 1.22 | 97.662 ± 7.962 | 86.926 | 137.194 |
| String.radixHuskySort14 | corpus=english, n=32,000 | 1.22 | 3.741 ± 0.258 | 3.352 | 4.612 |
| ParallelString.parallelRadixHuskySortAuto_p2 | corpus=english, n=1,000,000 | 1.21 | 244.657 ± 16.039 | 207.453 | 260.152 |
| ParallelString.parallelRadixHuskySortAuto_p2 | corpus=english, n=32,000 | 1.20 | 3.938 ± 0.238 | 3.576 | 4.729 |
| String.radixHuskySort16 | corpus=english, n=32,000 | 1.19 | 3.782 ± 0.263 | 3.443 | 4.817 |
| ParallelString.parallelRadixHuskySort11_p8 | corpus=english, n=1,000,000 | 1.18 | 78.626 ± 6.505 | 71.716 | 113.588 |
| ParallelString.serialRadixHuskySortAuto | corpus=english, n=200,000 | 1.18 | 49.360 ± 2.946 | 43.392 | 55.656 |
| ParallelString.parallelRadixHuskySortAuto_pAll | corpus=english, n=32,000 | 1.18 | 4.000 ± 0.231 | 3.676 | 4.808 |
| ParallelString.parallelRadixHuskySortAuto_p4 | corpus=english, n=32,000 | 1.18 | 4.033 ± 0.238 | 3.705 | 4.827 |
| Adversarial.sharedPrefixRadixHuskySort11 | n=200,000, prefixLength=0 | 1.17 | 41.681 ± 2.051 | 36.632 | 47.028 |
| ParallelString.parallelRadixHuskySortAuto_p1 | corpus=english, n=32,000 | 1.17 | 3.954 ± 0.208 | 3.641 | 4.660 |
| String.radixHuskySort16 | corpus=english, n=200,000 | 1.17 | 51.862 ± 2.686 | 45.705 | 57.299 |
| ParallelString.parallelRadixHuskySortAuto_p1 | corpus=english, n=200,000 | 1.17 | 51.315 ± 2.442 | 45.017 | 57.246 |
| ParallelString.serialRadixHuskySortAuto | corpus=english, n=32,000 | 1.16 | 3.835 ± 0.208 | 3.532 | 4.570 |
| String.radixHuskySort11 | corpus=english, n=200,000 | 1.16 | 53.769 ± 2.636 | 47.317 | 60.023 |
| ParallelString.parallelRadixHuskySortAuto_p8 | corpus=english, n=32,000 | 1.16 | 4.092 ± 0.216 | 3.773 | 4.736 |
| ParallelString.parallelRadixHuskySortAuto_p8 | corpus=english, n=1,000,000 | 1.15 | 76.658 ± 5.073 | 71.089 | 101.345 |
| String.radixHuskySort14 | corpus=english, n=1,000,000 | 1.15 | 295.131 ± 10.743 | 275.266 | 314.728 |
| String.radixHuskySort13 | corpus=english, n=200,000 | 1.15 | 54.030 ± 2.336 | 48.015 | 59.059 |
| Adversarial.sharedPrefixRadixHuskySort8 | n=200,000, prefixLength=0 | 1.14 | 43.853 ± 1.995 | 39.856 | 48.526 |
| ParallelString.parallelRadixHuskySortAuto_p2 | corpus=english, n=200,000 | 1.14 | 30.203 ± 1.651 | 28.178 | 36.150 |
| String.radixHuskySortAuto | corpus=english, n=200,000 | 1.13 | 54.843 ± 2.634 | 49.525 | 59.237 |
| Adversarial.collapsedBitsRadixHuskySort8 | fixedHighBits=60, n=1,000,000 | 1.13 | 64.135 ± 2.491 | 59.770 | 69.839 |
| String.radixHuskySort12 | corpus=english, n=200,000 | 1.13 | 55.808 ± 2.222 | 50.322 | 60.728 |
| String.radixHuskySort14 | corpus=english, n=200,000 | 1.13 | 54.863 ± 2.088 | 49.524 | 60.226 |
| String.radixHuskySort8 | corpus=english, n=200,000 | 1.12 | 56.222 ± 2.093 | 50.957 | 62.288 |
| Adversarial.collapsedBitsRadixHuskySort8 | fixedHighBits=0, n=1,000,000 | 1.12 | 78.844 ± 2.333 | 74.314 | 83.599 |
| String.radixHuskySort10 | corpus=english, n=200,000 | 1.12 | 56.770 ± 2.102 | 52.018 | 62.757 |
| ParallelString.parallelRadixHuskySortAuto_pAll | corpus=english, n=1,000,000 | 1.12 | 72.663 ± 4.505 | 68.212 | 95.347 |
| Adversarial.collapsedBitsRadixHuskySort11 | fixedHighBits=48, n=1,000,000 | 1.11 | 64.109 ± 2.472 | 60.971 | 69.623 |
| Adversarial.collapsedBitsRadixHuskySort11 | fixedHighBits=0, n=1,000,000 | 1.11 | 67.766 ± 2.239 | 64.768 | 73.075 |
| ParallelRadix.serialRadixHuskySort11 | n=2,000,000 | 1.11 | 157.006 ± 4.679 | 148.711 | 167.174 |
| Adversarial.collapsedBitsRadixHuskySort8 | fixedHighBits=48, n=1,000,000 | 1.10 | 82.388 ± 2.609 | 77.510 | 86.146 |
| String.huskyEncodeOnly | corpus=english, n=200,000 | 1.10 | 22.106 ± 1.329 | 21.105 | 23.620 |
| Adversarial.collapsedBitsRadixHuskySort11 | fixedHighBits=32, n=1,000,000 | 1.10 | 66.845 ± 2.185 | 64.021 | 72.384 |

### `req11-strings-parallel-full.json` (step 4, 1 s iterations): 16 of 72 rows

| class.method | params | t | score | iterations 8–10 | iteration 1 |
|---|---|---:|---:|---:|---:|
| ParallelString.parallelRadixHuskySortAuto_pAll | corpus=english, n=1,000,000 | 1.67 | 111.772 ± 17.865 | 72.570 | 115.744 |
| ParallelString.parallelRadixHuskySortAuto_p8 | corpus=english, n=1,000,000 | 1.53 | 110.818 ± 15.042 | 79.068 | 135.392 |
| ParallelString.parallelRadixHuskySort11_p8 | corpus=english, n=1,000,000 | 1.44 | 117.742 ± 16.729 | 84.354 | 108.260 |
| ParallelString.parallelRadixHuskySortAuto_p4 | corpus=english, n=1,000,000 | 1.33 | 119.731 ± 10.893 | 93.428 | 124.008 |
| ParallelString.parallelRadixHuskySortAuto_p4 | corpus=english, n=200,000 | 1.24 | 24.333 ± 1.727 | 20.816 | 29.015 |
| ParallelString.parallelRadixHuskySortAuto_p8 | corpus=english, n=200,000 | 1.21 | 21.864 ± 1.289 | 18.972 | 26.353 |
| ParallelString.parallelRadixHuskySort11_p8 | corpus=english, n=200,000 | 1.19 | 23.895 ± 1.346 | 20.703 | 28.133 |
| ParallelString.parallelRadixHuskySortAuto_pAll | corpus=english, n=200,000 | 1.18 | 20.994 ± 0.987 | 19.065 | 23.930 |
| ParallelString.parallelRadixHuskySort11_p8 | corpus=english, n=32,000 | 1.13 | 5.336 ± 0.308 | 4.961 | 6.271 |
| ParallelString.parallelRadixHuskySortAuto_p1 | corpus=chinese, n=1,000,000 | 1.11 | 59.971 ± 2.908 | 57.767 | 61.974 |
| ParallelString.parallelRadixHuskySortAuto_p1 | corpus=english, n=32,000 | 1.11 | 4.832 ± 0.158 | 4.495 | 5.261 |
| ParallelString.parallelRadixHuskySortAuto_p2 | corpus=english, n=200,000 | 1.11 | 34.283 ± 1.374 | 31.765 | 35.590 |
| ParallelString.parallelRadixHuskySortAuto_p2 | corpus=english, n=32,000 | 1.10 | 4.738 ± 0.150 | 4.422 | 5.129 |
| ParallelString.serialRadixHuskySortAuto | corpus=english, n=32,000 | 1.10 | 4.558 ± 0.152 | 4.239 | 5.012 |
| ParallelString.parallelRadixHuskySortAuto_pAll | corpus=english, n=32,000 | 1.10 | 4.754 ± 0.155 | 4.418 | 5.156 |
| ParallelString.parallelRadixHuskySortAuto_p8 | corpus=english, n=32,000 | 1.10 | 4.597 ± 0.147 | 4.275 | 5.015 |

## Step 4 (`req11-strings-parallel-full.json`, 1 s iterations): per-row speed-ups

Baseline ÷ candidate from unrounded scores; > 1 = candidate faster; † = the two 99.9 % CIs overlap; ‡ = candidate row not converged.
chunks = max(1, min(p, n / 16384)); bits from `chooseDigitBits(n, chunks)`; passes = ⌈64 / bits⌉.

| corpus, n | method | chunks / bits / passes | score | serialRadixHuskySortAuto ÷ method | Arrays.parallelSort ÷ method |
|---|---|---|---:|---:|---:|
| corpus=english, n=32,000 | parallelRadixHuskySortAuto_p1 | 1 / 12 / 6 | 4.832 ± 0.158‡ | 0.94×† | 0.96×† |
| corpus=english, n=32,000 | parallelRadixHuskySortAuto_p2 | 1 / 12 / 6 | 4.738 ± 0.150‡ | 0.96×† | 0.98×† |
| corpus=english, n=32,000 | parallelRadixHuskySortAuto_p4 | 1 / 12 / 6 | 4.828 ± 0.150 | 0.94×† | 0.96×† |
| corpus=english, n=32,000 | parallelRadixHuskySortAuto_p8 | 1 / 12 / 6 | 4.597 ± 0.147‡ | 0.99×† | 1.01×† |
| corpus=english, n=32,000 | parallelRadixHuskySortAuto_pAll | 1 / 12 / 6 | 4.754 ± 0.155‡ | 0.96×† | 0.98×† |
| corpus=english, n=32,000 | parallelRadixHuskySort11_p8 | 1 / 11 / 6 | 5.336 ± 0.308‡ | 0.85× | 0.87× |
| corpus=english, n=200,000 | parallelRadixHuskySortAuto_p1 | 1 / 15 / 5 | 47.190 ± 2.508 | 1.09×† | 0.22× |
| corpus=english, n=200,000 | parallelRadixHuskySortAuto_p2 | 2 / 14 / 5 | 34.283 ± 1.374‡ | 1.49× | 0.30× |
| corpus=english, n=200,000 | parallelRadixHuskySortAuto_p4 | 4 / 13 / 5 | 24.333 ± 1.727‡ | 2.11× | 0.42× |
| corpus=english, n=200,000 | parallelRadixHuskySortAuto_p8 | 8 / 12 / 6 | 21.864 ± 1.289‡ | 2.34× | 0.47× |
| corpus=english, n=200,000 | parallelRadixHuskySortAuto_pAll | 12 / 12 / 6 | 20.994 ± 0.987‡ | 2.44× | 0.49× |
| corpus=english, n=200,000 | parallelRadixHuskySort11_p8 | 8 / 11 / 6 | 23.895 ± 1.346‡ | 2.14× | 0.43× |
| corpus=english, n=1,000,000 | parallelRadixHuskySortAuto_p1 | 1 / 16 / 4 | 236.205 ± 5.145 | 1.01×† | 0.38× |
| corpus=english, n=1,000,000 | parallelRadixHuskySortAuto_p2 | 2 / 16 / 4 | 268.135 ± 21.034 (slow throughout — see main doc E11b-2 (ii); converged value 128.152 ± 1.708) | 0.89× | 0.34× |
| corpus=english, n=1,000,000 | parallelRadixHuskySortAuto_p4 | 4 / 15 / 5 | 119.731 ± 10.893‡ | 2.00× | 0.76× |
| corpus=english, n=1,000,000 | parallelRadixHuskySortAuto_p8 | 8 / 14 / 5 | 110.818 ± 15.042‡ | 2.16× | 0.82× |
| corpus=english, n=1,000,000 | parallelRadixHuskySortAuto_pAll | 16 / 13 / 5 | 111.772 ± 17.865‡ | 2.14× | 0.81× |
| corpus=english, n=1,000,000 | parallelRadixHuskySort11_p8 | 8 / 11 / 6 | 117.742 ± 16.729‡ | 2.03× | 0.77× |
| corpus=chinese, n=32,000 | parallelRadixHuskySortAuto_p1 | 1 / 12 / 6 | 2.349 ± 0.183 | 0.88× | 1.27× |
| corpus=chinese, n=32,000 | parallelRadixHuskySortAuto_p2 | 1 / 12 / 6 | 2.341 ± 0.176 | 0.88× | 1.28× |
| corpus=chinese, n=32,000 | parallelRadixHuskySortAuto_p4 | 1 / 12 / 6 | 2.396 ± 0.176 | 0.86× | 1.25× |
| corpus=chinese, n=32,000 | parallelRadixHuskySortAuto_p8 | 1 / 12 / 6 | 2.385 ± 0.180 | 0.86× | 1.25× |
| corpus=chinese, n=32,000 | parallelRadixHuskySortAuto_pAll | 1 / 12 / 6 | 2.400 ± 0.193 | 0.86× | 1.24× |
| corpus=chinese, n=32,000 | parallelRadixHuskySort11_p8 | 1 / 11 / 6 | 2.219 ± 0.176 | 0.93×† | 1.35× |
| corpus=chinese, n=200,000 | parallelRadixHuskySortAuto_p1 | 1 / 15 / 5 | 11.019 ± 0.100 | 0.98×† | 0.54× |
| corpus=chinese, n=200,000 | parallelRadixHuskySortAuto_p2 | 2 / 14 / 5 | 8.407 ± 0.065 | 1.29× | 0.71× |
| corpus=chinese, n=200,000 | parallelRadixHuskySortAuto_p4 | 4 / 13 / 5 | 6.761 ± 0.065 | 1.61× | 0.89× |
| corpus=chinese, n=200,000 | parallelRadixHuskySortAuto_p8 | 8 / 12 / 6 | 6.363 ± 0.046 | 1.71× | 0.94× |
| corpus=chinese, n=200,000 | parallelRadixHuskySortAuto_pAll | 12 / 12 / 6 | 6.527 ± 0.083 | 1.66× | 0.92× |
| corpus=chinese, n=200,000 | parallelRadixHuskySort11_p8 | 8 / 11 / 6 | 6.082 ± 0.096 | 1.78× | 0.99×† |
| corpus=chinese, n=1,000,000 | parallelRadixHuskySortAuto_p1 | 1 / 16 / 4 | 59.971 ± 2.908‡ | 0.97×† | 0.69× |
| corpus=chinese, n=1,000,000 | parallelRadixHuskySortAuto_p2 | 2 / 16 / 4 | 41.891 ± 1.823 | 1.39× | 0.98×† |
| corpus=chinese, n=1,000,000 | parallelRadixHuskySortAuto_p4 | 4 / 15 / 5 | 35.591 ± 0.673 | 1.63× | 1.16× |
| corpus=chinese, n=1,000,000 | parallelRadixHuskySortAuto_p8 | 8 / 14 / 5 | 30.654 ± 0.370 | 1.90× | 1.34× |
| corpus=chinese, n=1,000,000 | parallelRadixHuskySortAuto_pAll | 16 / 13 / 5 | 30.009 ± 0.500 | 1.94× | 1.37× |
| corpus=chinese, n=1,000,000 | parallelRadixHuskySort11_p8 | 8 / 11 / 6 | 30.647 ± 0.506 | 1.90× | 1.34× |
| corpus=chinesenames, n=32,000 | parallelRadixHuskySortAuto_p1 | 1 / 12 / 6 | 21.937 ± 0.490 | 0.97×† | 0.95× |
| corpus=chinesenames, n=32,000 | parallelRadixHuskySortAuto_p2 | 1 / 12 / 6 | 22.117 ± 0.559 | 0.97×† | 0.94× |
| corpus=chinesenames, n=32,000 | parallelRadixHuskySortAuto_p4 | 1 / 12 / 6 | 21.825 ± 0.564 | 0.98×† | 0.96× |
| corpus=chinesenames, n=32,000 | parallelRadixHuskySortAuto_p8 | 1 / 12 / 6 | 21.395 ± 0.461 | 1.00×† | 0.97×† |
| corpus=chinesenames, n=32,000 | parallelRadixHuskySortAuto_pAll | 1 / 12 / 6 | 22.397 ± 0.690 | 0.95×† | 0.93× |
| corpus=chinesenames, n=32,000 | parallelRadixHuskySort11_p8 | 1 / 11 / 6 | 20.848 ± 0.410 | 1.02×† | 1.00×† |
| corpus=chinesenames, n=200,000 | parallelRadixHuskySortAuto_p1 | 1 / 15 / 5 | 150.852 ± 1.120 | 1.00×† | 0.31× |
| corpus=chinesenames, n=200,000 | parallelRadixHuskySortAuto_p2 | 2 / 14 / 5 | 127.748 ± 0.243 | 1.19× | 0.36× |
| corpus=chinesenames, n=200,000 | parallelRadixHuskySortAuto_p4 | 4 / 13 / 5 | 116.614 ± 0.970 | 1.30× | 0.40× |
| corpus=chinesenames, n=200,000 | parallelRadixHuskySortAuto_p8 | 8 / 12 / 6 | 114.232 ± 1.797 | 1.33× | 0.40× |
| corpus=chinesenames, n=200,000 | parallelRadixHuskySortAuto_pAll | 12 / 12 / 6 | 110.294 ± 0.862 | 1.37× | 0.42× |
| corpus=chinesenames, n=200,000 | parallelRadixHuskySort11_p8 | 8 / 11 / 6 | 111.313 ± 1.130 | 1.36× | 0.41× |
| corpus=chinesenames, n=1,000,000 | parallelRadixHuskySortAuto_p1 | 1 / 16 / 4 | 760.059 ± 19.567 | 0.99×† | 0.30× |
| corpus=chinesenames, n=1,000,000 | parallelRadixHuskySortAuto_p2 | 2 / 16 / 4 | 648.079 ± 22.275 | 1.16× | 0.36× |
| corpus=chinesenames, n=1,000,000 | parallelRadixHuskySortAuto_p4 | 4 / 15 / 5 | 590.016 ± 11.645 | 1.28× | 0.39× |
| corpus=chinesenames, n=1,000,000 | parallelRadixHuskySortAuto_p8 | 8 / 14 / 5 | 557.670 ± 5.759 | 1.35× | 0.41× |
| corpus=chinesenames, n=1,000,000 | parallelRadixHuskySortAuto_pAll | 16 / 13 / 5 | 549.370 ± 11.735 | 1.37× | 0.42× |
| corpus=chinesenames, n=1,000,000 | parallelRadixHuskySort11_p8 | 8 / 11 / 6 | 551.322 ± 4.505 | 1.37× | 0.42× |

## Sanity check against request 4 — every row that moved ≥ 10 % with disjoint 99.9 % CIs

Baseline `rerun/full-suite.json` (request 4 rerun, 2026-09-06, src freeze `e92610f`; 5 forks, 5 × 2 s warm-up, 10 × 2 s, JMH 1.37,
Corretto 21.0.12). 415 rows common; new ÷ old ≤ 0.90 or ≥ 1.10 with the two 99.9 % CIs disjoint → 42 rows
(14 faster, 28 slower). Attribution is in the main document.

| class.method | params | old (e92610f) | new (15cc2ff) | new ÷ old |
|---|---|---:|---:|---:|
| String.radixHuskySort11 | corpus=chinese, n=1,000,000 | 79.971 ± 2.648 | 67.207 ± 2.193 | 0.84 |
| String.radixHuskySort10 | corpus=chinese, n=1,000,000 | 84.312 ± 2.364 | 71.045 ± 2.062 | 0.84 |
| String.radixHuskySort14 | corpus=english, n=32,000 | 4.431 ± 0.186 | 3.741 ± 0.258 | 0.84 |
| Permit.radixHuskySort8 | n=100,000 | 15.494 ± 0.668 | 13.171 ± 0.613 | 0.85 |
| Adversarial.collapsedBitsRadixHuskySort16 | fixedHighBits=0, n=200,000 | 9.740 ± 0.157 | 8.437 ± 0.170 | 0.87 |
| Permit.radixHuskySort16 | n=100,000 | 14.129 ± 0.446 | 12.386 ± 0.487 | 0.88 |
| Adversarial.collapsedBitsRadixHuskySort16 | fixedHighBits=0, n=1,000,000 | 76.239 ± 1.573 | 67.287 ± 1.604 | 0.88 |
| String.radixHuskySort8 | corpus=english, n=1,000,000 | 355.257 ± 24.712 | 314.524 ± 8.844 | 0.89 |
| ParallelRadix.parallelRadixHuskySort11_p4 | n=2,000,000 | 118.475 ± 2.418 | 105.152 ± 1.833 | 0.89 |
| String.radixHuskySort13 | corpus=chinese, n=1,000,000 | 71.219 ± 2.453 | 63.251 ± 1.945 | 0.89 |
| Permit.radixHuskySort11 | n=100,000 | 15.133 ± 0.865 | 13.479 ± 0.652 | 0.89 |
| Adversarial.collapsedBitsRadixHuskySort16 | fixedHighBits=56, n=1,000,000 | 58.421 ± 1.664 | 52.122 ± 1.656 | 0.89 |
| Adversarial.collapsedBitsRadixHuskySort16 | fixedHighBits=56, n=200,000 | 7.580 ± 0.138 | 6.799 ± 0.106 | 0.90 |
| ParallelRadix.parallelRadixHuskySort11_p8 | n=2,000,000 | 109.316 ± 1.802 | 98.344 ± 1.601 | 0.90 |
| Adversarial.collapsedBitsRadixHuskySort8 | fixedHighBits=0, n=1,000,000 | 71.612 ± 2.587 | 78.844 ± 2.333 | 1.10 |
| String.radixHuskySort8 | corpus=chinese, n=32,000 | 2.095 ± 0.026 | 2.311 ± 0.047 | 1.10 |
| Adversarial.collapsedBitsQuickHuskySort | fixedHighBits=56, n=1,000,000 | 189.341 ± 2.891 | 209.020 ± 3.682 | 1.10 |
| Adversarial.collapsedBitsQuickHuskySort | fixedHighBits=60, n=1,000,000 | 69.086 ± 1.054 | 76.286 ± 1.406 | 1.10 |
| Adversarial.collapsedBitsRadixHuskySort8 | fixedHighBits=16, n=1,000,000 | 74.114 ± 2.707 | 81.885 ± 2.439 | 1.10 |
| String.radixHuskySort13 | corpus=chinesenames, n=32,000 | 22.601 ± 0.395 | 24.985 ± 0.263 | 1.11 |
| String.radixHuskySort12 | corpus=chinesenames, n=32,000 | 22.577 ± 0.287 | 24.983 ± 0.224 | 1.11 |
| ParallelRadix.parallelRadixHuskySort11_p2 | n=10,000,000 | 731.573 ± 19.886 | 809.548 ± 24.574 | 1.11 |
| String.systemSort | corpus=chinesenames, n=200,000 | 102.502 ± 2.178 | 113.753 ± 1.166 | 1.11 |
| Adversarial.collapsedBitsRadixHuskySort8 | fixedHighBits=60, n=1,000,000 | 57.783 ± 2.350 | 64.135 ± 2.491 | 1.11 |
| String.multikeyQuicksort | corpus=chinesenames, n=32,000 | 35.480 ± 0.371 | 39.741 ± 0.591 | 1.12 |
| String.quickHuskySort | corpus=english, n=200,000 | 94.737 ± 2.441 | 106.482 ± 1.504 | 1.12 |
| String.systemSort | corpus=english, n=200,000 | 141.395 ± 2.593 | 159.103 ± 2.937 | 1.13 |
| String.quickHuskySort | corpus=english, n=1,000,000 | 697.875 ± 16.151 | 786.362 ± 11.175 | 1.13 |
| Adversarial.collapsedBitsRadixHuskySort8 | fixedHighBits=63, n=1,000,000 | 49.709 ± 2.845 | 56.281 ± 2.396 | 1.13 |
| Tuple.radixHuskySort11 | n=500,000 | 59.415 ± 0.731 | 67.276 ± 1.288 | 1.13 |
| String.systemSort | corpus=chinesenames, n=1,000,000 | 793.883 ± 8.524 | 902.895 ± 8.115 | 1.14 |
| String.msdStringSort | corpus=english, n=200,000 | 41.940 ± 1.449 | 48.010 ± 1.400 | 1.14 |
| Tuple.quickHuskySort | n=500,000 | 142.609 ± 1.827 | 164.344 ± 2.591 | 1.15 |
| String.systemSort | corpus=english, n=32,000 | 13.690 ± 0.193 | 15.926 ± 0.362 | 1.16 |
| Tuple.dualPivotQuicksort | n=500,000 | 142.701 ± 4.980 | 166.185 ± 2.439 | 1.16 |
| String.systemSort | corpus=english, n=1,000,000 | 1120.873 ± 14.433 | 1320.434 ± 24.126 | 1.18 |
| ParallelRadix.parallelRadixHuskySort11_p1 | n=2,000,000 | 165.224 ± 2.169 | 195.960 ± 4.944 | 1.19 |
| String.radixHuskySort12 | corpus=english, n=1,000,000 | 307.375 ± 5.223 | 382.103 ± 15.561 | 1.24 |
| ParallelRadix.parallelRadixHuskySort11_p1 | n=10,000,000 | 884.462 ± 21.556 | 1107.859 ± 23.229 | 1.25 |
| String.huskyEncodeOnly | corpus=english, n=1,000,000 | 67.686 ± 0.824 | 136.906 ± 4.227 | 2.02 |
| String.huskyEncodeOnly | corpus=english, n=200,000 | 8.819 ± 1.128 | 22.106 ± 1.329 | 2.51 |
| String.huskyEncodeOnly | corpus=english, n=32,000 | 0.667 ± 0.063 | 2.244 ± 0.072 | 3.36 |

