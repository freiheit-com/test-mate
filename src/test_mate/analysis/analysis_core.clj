(ns test-mate.analysis.analysis-core
  (:require [test-mate.analysis.test-need :as test-need]
            [test-mate.cmd :as command]))

(defn analysis [[cmd & args]]
  "Multiplex function for analysis commands"
  (condp = cmd
    "test-need" (test-need/analysis-test-need args)
    (command/exit-with-usage (str "unknown analysis: " cmd) "analysis")))
