(ns test-mate.cmd-test
  (:require [midje.sweet   :refer :all]
            [test-mate.cmd :refer :all]))

(facts "exit"
  (fact "nothing happens if exit called"
    (exit-with-usage "message") => irrelevant)) ;ok if no exception thrown
