(ns cover.parse-test
  (:require [clojure.test :refer :all]
            [cover.parse :refer :all]
            [clojure.string :refer [split-lines]]
            [cover.aggregate.cobertura :as cobertura]
            [cover.aggregate.jacoco :as jacoco]
            [cover.aggregate.go :as go]
            [test-mate.cmd :as command]))

;; setup

(def _jacoco-dir "test/cover/testfiles/jacoco/")
(def _cobertura-dir "test/cover/testfiles/cobertura/")
(def _go-dir "test/cover/testfiles/go/")

(def _minimal-jacoco (str _jacoco-dir "minimal.xml"))
(def _minimal-cobertura (str _cobertura-dir "minimal.xml"))
(def _go-testfile (str _go-dir "test.out"))

;; doctype?

(def _valid-doctype "<!DOCTYPE report PUBLIC \"-//JACOCO//DTD Report 1.0//EN\" \"report.dtd\">")
(def _invalid-doctype "not a doctype")

(deftest should-detect-doctype-on-valid-type
  (is (true? (doctype? _valid-doctype))))

(deftest should-not-detect-doctype-on-invalid-type
  (is (false? (doctype? _invalid-doctype))))

;; find-doctype

(defn read-testfile [name] (split-lines (slurp name)))
(def _jacoco-header (read-testfile _minimal-jacoco))
(def _cobertura-header (read-testfile _minimal-cobertura))

(deftest should-find-jacoco-doctype
  (is (string? (find-doctype _jacoco-header))))

(deftest should-find-cobertura-doctype
  (is (string? (find-doctype _cobertura-header))))

;; check-docktype-string

(deftest should-find-string
  (is (true? (check-docktype-string "<!DOCTYPE foo>" "foo"))))

(deftest should-be-nullsafe
  (is (nil? (check-docktype-string nil "foo"))))


;; cobertura?

(def _cobertura-doctype (second _cobertura-header))
(def _jacoco-doctype (second _jacoco-header))
(def _go ())

(deftest should-test-cobertura-doctype
  (is (true? (cobertura? _cobertura-doctype))))
(deftest should-not-test-jacoco-doctype
  (is (false? (cobertura? _jacoco-doctype))))

;; jacoco?


(deftest should-test-jacoco-doctype
  (is (true? (jacoco? _jacoco-doctype))))
(deftest should-not-test-cobertura-doctype
  (is (false? (jacoco? _cobertura-doctype))))


;; discover-type-from-lines

(deftest should-default-to-jacoco
  (is (= :jacoco (discover-type-from-lines ["random string"]))))


;; discover-type

(deftest should-discover-jacoco-filetype
  (is (= :jacoco (discover-type _minimal-jacoco))))

(deftest should-discover-cobertura-filetype
  (is (= :cobertura (discover-type _minimal-cobertura))))

(deftest should-discover-go-filetype
  (is (= :go (discover-type _go-testfile))))

;; stats

(deftest should-call-stats-on-cobertura
  (with-redefs-fn {#'cobertura/stats (fn [& _] :called)}
    #(is (= :called (stats _minimal-cobertura)))))

(deftest should-call-aggregate-on-jacoco
  (with-redefs-fn {#'jacoco/stats (fn [& _] :called)}
    #(is (= :called (stats _minimal-jacoco)))))

(deftest should-call-aggregate-on-go
  (with-redefs-fn {#'go/stats (fn [& _] :called)}
    #(is (= :called (stats _go-testfile)))))

;; aggregate

(defn _root? [package] (= ["/"] package))

(deftest should-call-aggregate-root-on-cobertura
  (with-redefs-fn {#'cobertura/aggregate (fn [p & _] (is (_root? p)) :called)}
    #(is (= :called (aggregate _minimal-cobertura ["/"])))))

(deftest should-call-aggregate-root-on-jacoco
  (with-redefs-fn {#'jacoco/aggregate (fn [p & _] (is (_root? p)) :called)}
    #(is (= :called (aggregate _minimal-jacoco ["/"])))))

(deftest should-error-on-aggregate-go-package ;real functionality not yet implemented
  (with-redefs-fn {#'command/exit-with-usage (fn [_ _] :called)}
    #(is (= :called (aggregate _go-testfile ["/some/"])))))

(deftest should-delegate-to-stats-if-root-is-used ;real functionality not yet implemented
  (with-redefs-fn {#'go/stats (fn [_] :called)}
    #(is (= :called (aggregate _go-testfile ["/"])))))
