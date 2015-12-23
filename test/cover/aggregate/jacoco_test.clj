(ns cover.aggregate.jacoco-test
  (:require [clojure.test           :refer :all]
            [cover.aggregate.jacoco :refer :all]))

(def minimal "test/cover/testfiles/minimal.xml")
(def complex "test/cover/testfiles/some_packages_classes_and_methods.xml")
(def class-example "test/cover/testfiles/class_coverage.xml")

(deftest should-aggregate-empty-map-if-package-not-found
  (is (= (aggregate (list "does/not/exist") minimal) {"does/not/exist" {}})))

(deftest should-aggregate-single-method-coverage-if-package-matches-exactly
  (is (= (aggregate (list "com/freiheit/my/package") minimal) {"com/freiheit/my/package" {:covered 50 :lines 100 :percentage 0.5}})))

(deftest should-aggregate-to-zero-if-no-line-count-found
  (is (= (aggregate (list "com/freiheit/my/package") complex) {"com/freiheit/my/package" {:covered 657 :lines 1142 :percentage 0.5753064798598949}})))

(deftest should-aggregate-single-method-coverage-if-package-matches-exactly
  (is (= (aggregate (list "com/freiheit/my/package") "test/cover/testfiles/invalid_no_line_count.xml") {"com/freiheit/my/package" {:covered 0 :lines 0 :percentage 1}})))

(deftest should-aggregate-even-if-invalid-non-counter-in-method-data
  (is (= (aggregate (list "com/freiheit/my/package") "test/cover/testfiles/invalid_non_counter_elem_in_method.xml") {"com/freiheit/my/package" {:covered 50 :lines 100 :percentage 0.5}})))

(deftest should-aggregate-class-coverage
  (is (= (aggregate-class-coverage class-example)
         {"com/freiheit/MyClass1" {:covered 0 :lines 10 :percentage 0.0},
          "com/freiheit/foo/Bar" {:covered 10 :lines 100 :percentage 0.1},
          "com/Bar2" {:covered 200 :lines 200 :percentage 1.0}})))

; special / handling

(deftest should-aggregate-all-in-minimal
  (is (= (aggregate (list "/") minimal) {"/" {:covered 50 :lines 100 :percentage 0.5}})))

(deftest should-aggregate-all-in-more-complex-test-file
  (is (= (aggregate (list "/") complex) {"/" {:covered 657 :lines 1142 :percentage 0.5753064798598949}})))

(deftest should-aggregate-all-in-class-example
  (is (= (aggregate (list "/") class-example) {"/" {:covered 210 :lines 310 :percentage 0.6774193548387097}})))

(deftest should-allow-combine-of-all-aggregate-with-others
  (is (= (aggregate (list "/" "com/freiheit/my/package") complex) {"/" {:covered 657 :lines 1142 :percentage 0.5753064798598949}
                                                                   "com/freiheit/my/package" {:covered 657 :lines 1142 :percentage 0.5753064798598949}})))
