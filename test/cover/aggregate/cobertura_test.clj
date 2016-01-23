(ns cover.aggregate.cobertura-test
  (:require [clojure.test           :refer :all]
            [cover.aggregate.cobertura :refer :all]))

(def cobertura-dir "test/cover/testfiles/cobertura/")
(defn test-path [f] (str cobertura-dir f))
(def minimal (test-path "minimal.xml"))

;; (deftest should-aggregate-all-in-minimal
;;   (is (= (aggregate (list "/") minimal) {"/" {:covered 3 :lines 4 :percentage 0.75}})))

(deftest should-stats-minimal
  (is (= (stats minimal) {:covered 3 :lines 4 :percentage 0.75})))
