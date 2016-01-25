(ns cover.parse
  (:require [cover.aggregate.jacoco :as jacoco]
            [cover.aggregate.cobertura :as cobertura]
            [clojure.java.io :refer [reader]]
            [clojure.string :refer [lower-case]]))

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

(defn discover-type-from-lines
  "Deducts type of data from LINES."
  [lines]
  (let [docstring (find-doctype lines)]
    (cond
      (cobertura? docstring) :cobertura
      (jacoco? docstring) :jacoco
      :else :jacoco)))

(defn discover-type
  "Deducts type of data from FILE."
  [file]
  (with-open [rdr (reader file)]
    (discover-type-from-lines (line-seq rdr))))


(defn aggregate
   "Parses COVERAGE-FILE and returns an object of packages containing coverage data"
  [coverage-file packages]
  (let [type (discover-type coverage-file)]
    (if (= :jacoco type)
      (jacoco/aggregate packages coverage-file)
      (cobertura/aggregate packages coverage-file))))

(defn stats
   "Parses COVERAGE-FILE and returns an object of packages containing coverage statistics"
  [coverage-file]
  (let [type (discover-type coverage-file)]
    (if (= :jacoco type)
      (jacoco/stats coverage-file)
      (cobertura/stats coverage-file))))
