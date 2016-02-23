(ns test-mate.statistic-server.push-data-test
  (:require [midje.sweet      :refer :all]
            [test-mate.cmd :as command]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [test-mate.statistic-server.push-data :refer :all]))

(facts "push-data"
  (fact "should do nothing on unknown command"
    (push-data ["unknown-command"]) => irrelevant
    (provided
      (command/exit-with-usage "unknown statistic-server command: unknown-command") => nil :times 1))

  (fact "should publish data on command"
    (push-data ["publish-coverage" "file" "overrides"]) => irrelevant
    (provided
      (publish-statistic-data "file" "overrides") => nil :times 1))

  (fact "should add project on command"
    (push-data ["add-project" "file"]) => irrelevant
    (provided
      (add-project "file") => nil :times 1)))


(defchecker body-with [expected]
  (checker [actual] (contains? expected (cheshire/parse-string (:body actual) true))))

(facts "add-project"
  (fact "should add projects from file, ignore skipped ones and ignore additional keys"
    (add-project "test/test_mate/testfiles/add_project_test_file.edn") => irrelevant
    (provided
      (client/put anything (body-with #{{:project "test" :subproject "sub" :language "clojure"}
                                        {:project "test3" :subproject "sub3" :language "clojure"}
                                        {:project "test4" :subproject "sub4" :language "java"}})) => irrelevant :times 3))

  (fact "should handle puts if exception thrown"
    (add-project "test/test_mate/testfiles/add_project_test_file.edn") => irrelevant
    (provided
      (client/put anything anything) =throws=> (ex-info "404" {:status 404}))))
