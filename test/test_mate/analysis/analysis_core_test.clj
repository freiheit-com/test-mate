(ns test-mate.analysis.analysis-core-test
  (:require [test-mate.analysis.analysis-core :refer :all]
            [test-mate.analysis.test-need :as test-need]
            [test-mate.cmd :as command]
            [midje.sweet :refer :all]))

(facts "analysis-core"
  (fact "should do nothing on unknown analysis"
    (analysis ["unknown-analysis"]) => irrelevant
    (provided
      (command/exit-with-usage "unknown analysis: unknown-analysis" "analysis") => nil :times 1))
  (fact "should call test-need"
    (analysis ["test-need" ..arg1.. ..arg2..]) => irrelevant
    (provided
      (test-need/analysis-test-need [..arg1.. ..arg2..]) => nil :times 1)))
