(ns cover.parse
  (:require [cover.aggregate.jacoco :as jacoco]
            [cover.aggregate.cobertura :as cobertura]
            [cover.aggregate.go :as go]
            [clojure.java.io :refer [reader]]
            [clojure.string :refer [lower-case]]
            [test-mate.cmd :as command]))

(def cobertura-doctype "cobertura")
(def jacoco-doctype "jacoco")

(defn doctype?
  "t if a doctype sting"
  [s]
  (.startsWith s "<!DOCTYPE"))

(defn find-doctype
  "Looks for a DOCTYPE line in SEQ."
  [seq]
  (first (filter doctype? (take 2 seq))))

(defn check-docktype-string
  "true if S contains DOCTYPE"
  [s doctype]
  (when s (.contains (lower-case s) doctype)))

(defn cobertura?
  "true if cobertura doctype"
  [s]
  (check-docktype-string s cobertura-doctype))

(defn jacoco?
  "true if jacoco doctype"
  [s]
  (check-docktype-string s jacoco-doctype))

(defn go?
  "true if this a go coverage file"
  [s]
  (.startsWith s "mode:"))


(defn discover-type-from-lines
  "Deducts type of data from LINES."
  [lines]
  (let [first-line (first lines)
        docstring (find-doctype lines)]
    (cond
      (go? first-line) :go
      (cobertura? docstring) :cobertura
      (jacoco? docstring) :jacoco
      :else :jacoco)))

(defn discover-type
  "Deducts type of data from FILE."
  [file]
  (with-open [rdr (reader file)]
    (discover-type-from-lines (line-seq rdr))))

(defn stats
  "Parses COVERAGE-FILE and returns an object of packages containing coverage statistics"
  [coverage-file]
  (let [type (discover-type coverage-file)]
    (condp = type
      :jacoco (jacoco/stats coverage-file)
      :cobertura (cobertura/stats coverage-file)
      :go (go/stats coverage-file))))

(defn aggregate
   "Parses COVERAGE-FILE and returns an object of packages containing coverage data"
  [coverage-file packages]
  (let [type (discover-type coverage-file)]
    (condp = type
      :jacoco (jacoco/aggregate packages coverage-file)
      :cobertura (cobertura/aggregate packages coverage-file)
      :go (if (= packages ["/"])
            (go/stats coverage-file)
            (command/exit-with-usage "aggregate for go files does not support package other than /" "aggregate")))))
