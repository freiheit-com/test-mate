(ns cover.aggregate.jacoco_test
  (:require [clojure.test :refer :all] [cover.aggregate.jacoco :refer :all]))

(def ^:const minimal "test/cover/testfiles/minimal.xml")

(deftest should-aggregate-empty-map-if-package-not-found
  (is (= (aggregate (list "does/not/exist") minimal) {})))

(deftest should-aggregate-single-method-coverage-if-package-matches-exactly
  (is (= (aggregate (list "com/freiheit/my/package") minimal) {"com/freiheit/my/package" {:covered 50 :lines 100 :percentage 0.5}})))

(deftest should-aggregate-to-zero-if-no-line-count-found
  (is (= (aggregate (list "com/freiheit/my/package") "test/cover/testfiles/some_packages_classes_and_methods.xml") {"com/freiheit/my/package" {:covered 657 :lines 1142 :percentage 0.5753064798598949}})))

(deftest should-aggregate-single-method-coverage-if-package-matches-exactly
  (is (= (aggregate (list "com/freiheit/my/package") "test/cover/testfiles/invalid_no_line_count.xml") {"com/freiheit/my/package" {:covered 0 :lines 0 :percentage 1}})))

(deftest should-aggregate-even-if-invalid-non-counter-in-method-data
  (is (= (aggregate (list "com/freiheit/my/package") "test/cover/testfiles/invalid_non_counter_elem_in_method.xml") {"com/freiheit/my/package" {:covered 50 :lines 100 :percentage 0.5}})))
