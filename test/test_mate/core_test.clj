(ns test-mate.core-test
  (:require [midje.sweet      :refer :all]
            [test-mate.cmd :as cmd]
            [cover.parse :as cover]
            [test-mate.analysis.analysis-core :as analysis]
            [test-mate.statistic-server.push-data :as statistic-server]
            [test-mate.core :refer :all]))

(facts "main commands"
  (fact "should do nothing on unknown main command"
    (-main "unknown-command") => irrelevant
    (provided
      (cmd/exit-with-usage "unknown-command") => nil :times 1))

  (fact "should call aggregate on aggregate command"
    (-main "aggregate" ..coverage-file.. ..p1.. ..p2..) => irrelevant
    (provided
      (cover/aggregate ..coverage-file.. [..p1.. ..p2..]) => irrelevant))

  (fact "should call analysis core on analysis command"
    (-main "analysis" ..sub-command1.. ..arg1.. ..arg2..) => irrelevant
    (provided
      (analysis/analysis [..sub-command1.. ..arg1.. ..arg2..]) => irrelevant))

  (fact "should call push-data on statistic-server command"
    (-main "statistic-server" ..sub-command1.. ..sub-command2..) => irrelevant
    (provided
      (statistic-server/push-data [..sub-command1.. ..sub-command2..]) => irrelevant)))
