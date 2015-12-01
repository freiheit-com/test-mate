(ns cover.aggregate.jacoco_test
  (:require [clojure.test :refer :all] [cover.aggregate.jacoco :refer :all]))

(def ^:const minimal "test/cover/testfiles/minimal.xml" )

(deftest should-aggregate-empty-map-if-package-not-found
  (is (= (aggregate (list "does/not/exist") minimal) {})))

(deftest should-aggregate-single-method-coverage-if-package-matches-exactly
  (is (= (aggregate (list "com/freiheit/my/package") minimal) {"com/freiheit/my/package" 0.5})))
