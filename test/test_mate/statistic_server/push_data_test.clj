(ns test-mate.statistic-server.push-data-test
  (:require [midje.sweet      :refer :all]
            [test-mate.cmd :as command]
            [test-mate.statistic-server.push-data :refer :all]))

(facts "push-data"
  (fact "should do nothing on unknown command"
    (push-data ["unknown-command"]) => irrelevant
    (provided
      (command/exit-with-usage "unknown statistic-server command: unknown-command") => nil :times 1))

  (fact "publish data"
    (push-data ["publish-coverage" "file" "overrides"]) => irrelevant
        (provided
          (publish-statistic-data "file" "overrides") => nil :times 1))

  (fact "should do nothing on unknown command"
    (push-data ["add-project" "file"]) => irrelevant
        (provided
          (add-project "file") => nil :times 1)))
