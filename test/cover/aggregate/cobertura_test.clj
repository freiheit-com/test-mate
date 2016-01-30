(ns cover.aggregate.cobertura-test
  (:require [clojure.test           :refer :all]
            [cover.aggregate.cobertura :refer :all]
            [cover.reader.xml-non-validate :as reader]
            [clojure.zip :as zip]
            [clojure.xml :as xml]))

(def _cobertura-dir "test/cover/testfiles/cobertura/")
(defn _test-path [f] (str _cobertura-dir f))
(def _minimal (_test-path "minimal.xml"))
(def _bigger (_test-path "bigger.xml"))
(def _nosummary (_test-path "nosummary.xml"))

(defn _read-test-xml [filename] (reader/read-report filename))
(defn _read-test-zip [filename] (zip/xml-zip (_read-test-xml filename)))

(defn _zip-str [s]
  (zip/xml-zip (xml/parse (new org.xml.sax.InputSource
                               (new java.io.StringReader s)))))

(def _hit-line (_zip-str "<line hits=\"11\"/>"))
(def _uncovered-line (_zip-str "<line hits=\"0\"/>"))
(def _invalid-line (_zip-str "<line hits=\"foo\"/>"))

;; get-counts

(deftest should-get-counts-from-valid-line
  (is (= 11 (get-counts _hit-line)))
  (is (= 0 (get-counts _uncovered-line))))

(deftest should-throw-on-invalid-line
  (is (thrown? Exception (get-counts _invalid-line))))


;; hit?

(deftest should-test-valid-zip
    (is (true? (hit? _hit-line))))

(deftest should-not-throw-invalid
  (is (false? (hit? []))))

;; get-lines

(deftest should-get-all-lines
  (is (= 12 (count (get-lines (_read-test-zip _bigger))))))


;; get-summary

(deftest should-get-bigger-summary
  (is (= (get-summary (get-lines (_read-test-zip _bigger))) {:covered 9 :lines 12 :percentage 0.75})))

(deftest should-get-nosummary
  (is (= (get-summary (get-lines (_read-test-zip _nosummary))) {:covered 3 :lines 4 :percentage 0.75})))

;; parse-root

(deftest should-parse-root
  (is (= (parse-root (_read-test-xml _minimal)))))

(deftest should-not-parse-nosummary
  (is (thrown? Exception (parse-root (_read-test-xml _nosummary)))))

;; stats

(deftest should-stats-minimal
  (is (= (stats _minimal) {:covered 3 :lines 4 :percentage 0.75})))

(deftest should-stats-nosummary
  (is (= (stats _nosummary) {:covered 3 :lines 4 :percentage 0.75})))

;; aggregate

(deftest should-aggregate-all-in-minimal
  (is (= (aggregate ["package"] _minimal) {"package" {:covered 3 :lines 4 :percentage 0.75}})))

(deftest should-aggregate-all-in-bigger
  (is (= (aggregate ["package1" "package2"] _bigger) {"package1" {:covered 6 :lines 8 :percentage 0.75}
                                                      "package2" {:covered 3 :lines 4 :percentage 0.75}})))

(deftest should-aggregate-all-if-no-package
  (is (= (aggregate [] _minimal) {"package" {:covered 3 :lines 4 :percentage 0.75}})))
