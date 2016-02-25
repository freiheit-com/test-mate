(ns test-mate.core-test
  (:require [midje.sweet      :refer :all]
            [test-mate.cmd :as cmd]
            [cover.parse :as cover]
            [analysis.test-need :as test-need]
            [test-mate.statistic-server.push-data :as statistic-server]
            [test-mate.core :refer :all]))

(facts "main commands"
  (fact "should do nothing on unknown main command"
    (-main "unknown-command") => irrelevant
    (provided
      (cmd/exit-with-usage "unknown command: unknown-command") => nil :times 1))

  (fact "should call aggregate on aggregate command"
    (-main "aggregate" ..coverage-file.. ..p1.. ..p2..) => irrelevant
    (provided
      (cover/aggregate ..coverage-file.. [..p1.. ..p2..]) => irrelevant))

  (fact "should call test-need on test-need command"
    (-main "test-need" ..coverage-file.. ..repo-path..) => irrelevant
    (provided
      (test-need/print-analyse-test-need ..coverage-file.. ..repo-path..) => irrelevant))

  (fact "should call push-data on statistic-server command"
    (-main "statistic-server" ..sub-command1.. ..sub-command2..) => irrelevant
    (provided
      (statistic-server/push-data [..sub-command1.. ..sub-command2..]) => irrelevant)))
