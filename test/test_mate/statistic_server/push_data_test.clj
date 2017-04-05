(ns test-mate.statistic-server.push-data-test
  (:require [midje.sweet      :refer :all]
            [test-mate.cmd :as command]
            [test-mate.config :as config]
            [test-mate.exit :as exit]
            [cover.parse :as parse]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [test-mate.statistic-server.push-data :refer :all :as dut]))

(facts "coverage decreasing"
       (fact "should return true if coverage decreasing"
             (#'dut/decreasing-coverage? ..stats.. ..name..) => true
             (provided
              (percentage-of-pushed ..stats..) => 0.5
              (#'dut/load-latest-data ..name..) => {:overall-coverage {:percentage 0.6}})))

(facts "validating data"
       (fact "should continue if coverage is not decreasing"
             (#'dut/validate-data ..data.. ..name..) => true
             (provided
              (#'dut/decreasing-coverage? ..data.. ..name..) => false
              (exit/terminate -1) => irrelevant :times 0))
       (fact "should terminate if has decreasing coverage"
             (#'dut/validate-data ..data.. ..name..) => false
             (provided
              (#'dut/decreasing-coverage? ..data.. ..name..) => true
              (exit/terminate -1) => true)))


(facts "push-data"
  (fact "should do nothing on unknown command"
    (push-data ["unknown-command"]) => irrelevant
    (provided
      (command/exit-with-usage "unknown statistic-server command: unknown-command" "statistic-server") => nil :times 1))

  (fact "should publish data on command"
    (push-data ["publish-coverage" "file" "overrides"]) => irrelevant
    (provided
      (publish-statistic-data "file" "overrides") => nil :times 1))

  (fact "should add project on command"
    (push-data ["add-project" "file"]) => irrelevant
    (provided
     (add-project "file") => nil :times 1))

  (fact "should check coverage on command"
    (push-data ["check-coverage" "file" "overrides"]) => irrelevant
    (provided
      (check-coverage "file" "overrides") => nil :times 1)))



(defchecker body-with [expected]
  (checker [actual] (= expected (cheshire/parse-string (:body actual) true))))

(facts "check-coverage"
       (fact "should check decreasing coverage"
             (check-coverage ..coverage-file.. "") => irrelevant
             (provided
              (#'dut/read-with-prj-defaults anything) => {:project "test" :subproject "test-sub" :language "clojure"}
              (parse/stats ..coverage-file..) => {:lines 1000 :covered 333}
              (client/get anything anything) => {:body "{\"overall-coverage\": {\"lines\": 1000, \"covered\": 334, \"percentage\": 0.334}}"}
              (exit/terminate -1) => anything)))


(defchecker add-project-body-with [expected]
  (checker [actual] (contains? expected (cheshire/parse-string (:body actual) true))))

(facts "add-project"
  (fact "should add projects from file, ignore skipped ones and ignore additional keys"
    (add-project "test/test_mate/testfiles/add_project_test_file.edn") => irrelevant
    (provided
      (client/put anything (add-project-body-with
                               #{{:project "test" :subproject "sub" :language "clojure"}
                                 {:project "test3" :subproject "sub3" :language "clojure"}
                                 {:project "test4" :subproject "sub4" :language "java"}}))
      => irrelevant :times 3))

  (fact "should handle puts if exception thrown"
    (add-project "test/test_mate/testfiles/add_project_test_file.edn") => irrelevant
    (provided
      (client/put anything anything) =throws=> (ex-info "404" {:status 404}))))

(fact "pushed-coverage"
  (fact "calcs percentage"
    (percentage-of-pushed {:covered 666 :lines 1000}) => 0.666)
  (fact "calcs correctly with zero"
    (percentage-of-pushed {:covered 0 :lines 0}) => 1.0))

(facts "publish-statistic-data"
  (fact "should publish parsed stats with default config if no project supplied"
    (publish-statistic-data ..coverage-file.. nil) => irrelevant
    (provided
      (config/allow-decreasing-coverage) => true
      (config/default-project) => {:project "test" :subproject "test-sub" :language "clojure"}
      (parse/stats ..coverage-file..) => {:lines 1000 :covered 333}
      (client/put anything (body-with {:lines 1000 :covered 333 :project "test" :subproject "test-sub" :language "clojure"})) => irrelevant
      (exit/terminate -1) => anything :times 0))

  (fact "should publish parsed stats with merged project data"
    (publish-statistic-data ..coverage-file.. "{:subproject \"overriden-sub\" :language \"overriden-lang\" :ignored \"value\"}") => irrelevant
    (provided
      (config/allow-decreasing-coverage) => true
      (config/default-project) => {:project "test" :subproject "test-sub" :language "clojure"}
      (parse/stats ..coverage-file..) => {:lines 1000 :covered 333}
      (client/put anything (body-with {:lines 1000 :covered 333 :project "test" :subproject "overriden-sub" :language "overriden-lang"})) => irrelevant
      (exit/terminate -1) => anything :times 0))

  (fact "does not publish data and terminate with non-zero value if decreasing coverage not allowed"
    (binding [config/*test-mate-config* {:allow-decreasing-coverage false}]
      (publish-statistic-data ..coverage-file.. nil) => irrelevant
      (provided
        (config/allow-decreasing-coverage) => false
        (#'dut/read-with-prj-defaults anything) => {:project "test" :subproject "test-sub" :language "clojure"}
        (parse/stats ..coverage-file..) => {:lines 1000 :covered 300}
        (client/get anything anything) => {:body "{\"overall-coverage\": {\"lines\": 1000, \"covered\": 334, \"percentage\": 0.334}}"}
        (exit/terminate -1) => false)))

  (fact "does publish data if new coverage is below threshold but within epsilon allowance"
    (binding [config/*test-mate-config* {:allow-decreasing-coverage false}]
      (publish-statistic-data ..coverage-file.. nil) => irrelevant
      (provided
        (config/allow-decreasing-coverage) => false
        (config/default-project) => {:project "test" :subproject "test-sub" :language "clojure"}
        (parse/stats ..coverage-file..) => {:lines 10000 :covered 3333}
        (client/get anything anything) => {:body "{\"overall-coverage\": {\"lines\": 10000, \"covered\": 3340, \"percentage\": 0.0334}}"}
        (client/put anything (body-with {:lines 10000 :covered 3333 :project "test" :subproject "test-sub" :language "clojure"})) => irrelevant
        (exit/terminate -1) => anything :times 0)))

  (fact "does not publish data and terminate with non-zero value if coverate threshold not met"
    (binding [config/*test-mate-config* {:coverage-threshold 85.0}]
      (publish-statistic-data ..coverage-file.. nil) => irrelevant
      (provided
        (config/coverage-threshold) => 0.85
        (#'dut/read-with-prj-defaults anything) => {:project "test" :subproject "test-sub" :language "clojure"}
        (parse/stats ..coverage-file..) => {:lines 1000 :covered 800}
        (exit/terminate -1) => false)))

  (fact "does publish data if new coverage is above threshold"
    (binding [config/*test-mate-config* {:coverage-threshold 85}]
      (publish-statistic-data ..coverage-file.. nil) => irrelevant
      (provided
        (config/coverage-threshold) => 0.85
        (config/default-project) => {:project "test" :subproject "test-sub" :language "clojure"}
        (parse/stats ..coverage-file..) => {:lines 10000 :covered 8700}
        (client/put anything (body-with {:lines 10000 :covered 8700 :project "test" :subproject "test-sub" :language "clojure"})) => irrelevant
        (exit/terminate -1) => anything :times 0)))
)
