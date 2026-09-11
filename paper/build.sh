#!/bin/bash
# Build both variants of the paper from the single source, RadixHuskySort.tex.
#
#   RadixHuskySort.pdf       identified, draft revision stamp in the footer, acknowledgments
#                            present. For us, and the basis of the camera-ready copy.
#
#   RadixHuskySort-anon.pdf  the review copy: no authors, no affiliations, no emails, no
#                            acknowledgments and no build timestamp. This is the one to judge
#                            length by -- the front matter the other one carries is worth about
#                            half a page, and it is not in the submitted PDF.
#
# The variant is selected by \ANONYMOUS, which RadixHuskySort.tex tests with \ifdefined; neither
# variant needs its own copy of the source.
set -e
cd "$(dirname "$0")"

build() {
  local jobname=$1 prelude=$2
  for pass in 1 2 3; do
    pdflatex -interaction=nonstopmode -jobname="$jobname" "$prelude\\input{RadixHuskySort}" >/dev/null
    [ $pass = 1 ] && bibtex "$jobname" >/dev/null
  done
  printf '%-20s %s pages\n' "$jobname.pdf" "$(pdfinfo "$jobname.pdf" | awk '/^Pages/{print $2}')"
}

build RadixHuskySort ''
build RadixHuskySort-anon '\def\ANONYMOUS{}'

echo
echo "Body length is measured on RadixHuskySort-anon.pdf, up to where the bibliography starts."
