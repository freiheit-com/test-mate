(ns test-mate.analysis.analysis-core-test
  (:require [test-mate.analysis.analysis-core :refer :all]
            [test-mate.analysis.test-need :as test-need]
            [test-mate.cmd :as command]
            [midje.sweet :refer :all]))

(facts "analysis-core"
  (fact "should do nothing on unknown analysis"
    (analyse ["unknown-analysis"]) => irrelevant
    (provided
      (command/exit-with-usage "unknown analysis: unknown-analysis") => nil :times 1))
  (fact "should call test-need"
    (analyse ["test-need" ..arg1.. ..arg2..]) => irrelevant
    (provided
      (test-need/analyse-test-need [..arg1.. ..arg2..]) => nil :times 1)))
