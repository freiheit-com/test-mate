[![Build Status](https://travis-ci.org/freiheit-com/test-mate.svg?branch=master)](https://travis-ci.org/freiheit-com/test-mate)

Usage:

## Coverage data Aggregation

    lein run aggregate <path/to/jacoco_coverage.xml> my/package/one my/package/two ...

Note: You can supply the special package "/" to aggregate the overall coverage result.

# Analysis

## test-need

    lein run test-need <path/to/jacoco_coverage.xml> <path/to/git/repo>

TODO:
Some more doc for features
