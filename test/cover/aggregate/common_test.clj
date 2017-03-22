(ns cover.aggregate.go-test
  (:require [cover.aggregate.common :refer :all]
            [midje.sweet        :refer :all]))

(facts "about coverage percentage"
  (fact "0% if covered 0"
    (percentage 100 0) => 0)
  (fact "0% if lines 0 (by definition)"
    (percentage 0 3) => 0)
  (fact "30% coverage"
    (percentage 100 30) => 0.3))
