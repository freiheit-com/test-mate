(ns cover.aggregate.jacoco-test
  (:require [clojure.test           :refer :all]
            [cover.aggregate.jacoco :refer :all]
            [midje.sweet      :refer :all]))

(def jacoco-dir "test/cover/testfiles/jacoco/")
(defn _test-path [f] (str jacoco-dir f))
(def _minimal (_test-path "minimal.xml"))
(def _complex (_test-path "some_packages_classes_and_methods.xml"))
(def _class-example (_test-path "class_coverage.xml"))
(def _invalid-no-line-count (_test-path "invalid_no_line_count.xml"))
(def _invalid-no-counter (_test-path "invalid_non_counter_elem_in_method.xml"))
(def _cloverage-file (_test-path "cloverage_file.xml"))


;;parse-legacy-line-counter
(deftest should-aggregate-illegal-as-neutral
  (is (= (#'cover.aggregate.jacoco/parse-legacy-line-counter "no-coverage-data") [0 0])))

(deftest should-aggregate-valid-legacy
  (is (= (#'cover.aggregate.jacoco/parse-legacy-line-counter "96% (186/193)") [186 193])))
;; aggregate files

(deftest should-aggregate-empty-map-if-package-not-found
  (is (= (aggregate (list #"does/not/exist.*") _minimal) {"does/not/exist.*" {}})))

(deftest should-aggregate-single-method-coverage-if-package-matches-exactly
  (is (= (aggregate (list #"com/freiheit/my/package.*") _minimal) {"com/freiheit/my/package.*" {:covered 50 :lines 100 :percentage 0.5}})))

(deftest should-aggregate-to-zero-if-no-line-count-found
  (is (= (aggregate (list #"com/freiheit/my/package.*") _complex) {"com/freiheit/my/package.*" {:covered 657 :lines 1142 :percentage 0.5753064798598949}})))

(deftest should-aggregate-single-method-coverage-if-package-matches-exactly
  (is (= (aggregate (list #"com/freiheit/my/package.*") _invalid-no-line-count) {"com/freiheit/my/package.*" {:covered 0 :lines 0 :percentage 1}})))

(deftest should-aggregate-even-if-invalid-non-counter-in-method-data
  (is (= (aggregate (list #"com/freiheit/my/package.*") _invalid-no-counter) {"com/freiheit/my/package.*" {:covered 50 :lines 100 :percentage 0.5}})))

; test file from lein cloverage --emma-xml
(deftest should-aggregate-cloverage-file
    (is (= (aggregate (list "/") _cloverage-file) {"/" {:covered 186 :lines 193 :percentage 0.9637305699481865}})))

;;; aggregate-class-coverage

(deftest should-aggregate-class-coverage
  (is (= (aggregate-class-coverage _class-example)
         {"com/freiheit/MyClass1" {:covered 0 :lines 10 :percentage 0.0},
          "com/freiheit/foo/Bar" {:covered 10 :lines 100 :percentage 0.1},
          "com/Bar2" {:covered 200 :lines 200 :percentage 1.0}})))

;;; special / handling

(deftest should-aggregate-all-in-minimal
  (is (= (aggregate (list "/") _minimal) {"/" {:covered 50 :lines 100 :percentage 0.5}})))

(deftest should-aggregate-all-in-more-complex-test-file
  (is (= (aggregate (list "/") _complex) {"/" {:covered 657 :lines 1142 :percentage 0.5753064798598949}})))

(deftest should-aggregate-all-in-class-example
  (is (= (aggregate (list "/") _class-example) {"/" {:covered 210 :lines 310 :percentage 0.6774193548387097}})))

(deftest should-allow-combine-of-all-aggregate-with-others
  (is (= (aggregate (list "/" "com/freiheit/my/package.*") _complex)
         {"/" {:covered 657 :lines 1142 :percentage 0.5753064798598949}
          "com/freiheit/my/package.*" {:covered 657 :lines 1142 :percentage 0.5753064798598949}})))

;; stats

(deftest should-aggregate-root
  (is (= {:covered 657 :lines 1142 :percentage 0.5753064798598949}
         (stats _complex))))

(tabular
  (fact "should match package name correctly"
    (#'cover.aggregate.jacoco/package-matches ?regex {:attrs {:name ?name}}) => ?matches)
  ?name            ?regex             ?matches
  "foo"            #"f.*"             true
  "foo"            #"foobar"          false
  ""               #".*"              true
  "foo"            #".*"              true
  "/foo/bar/baz"   #"/foo/.*/baz"     true
  "/foo/bar/baz"   #"/foo/.*"         true
  "/foo/barr"      #"/foo/ba(r)+"     true
  "/foo/bar"       #"/foo/ba(b)+"     false
  "foo"            "/"                true)

(facts "as-pattern"
  (fact "should not convert to pattern if special root"
    (#'cover.aggregate.jacoco/as-pattern "/") => "/")
  (fact "should convert other inputs to pattern"
    (#'cover.aggregate.jacoco/as-pattern ".*") => #".*"))
