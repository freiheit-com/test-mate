(ns cover.aggregate.go
  (:require [clojure.java.io :refer [reader]]
            [cover.aggregate.common :as common]))

(def go-coverage-regex #"^([^:]*):(\d+)\.\d+,(\d+)\.\d+ \d+ (\d+)$")

; produces a vector of [file-name start end covered]
(defn parse-line [line]
  (let [matcher (re-matcher go-coverage-regex line)
        found (re-find matcher)]
    (if found
      (let [groups (re-groups matcher)]
        [(nth groups 1)
         (Integer/parseInt (nth groups 2))
         (Integer/parseInt (nth groups 3))
         (not (= (nth groups 4) "0"))]))))

(defn covered-lines [from to covered]
  (if covered
    (+ 1 (- to from))
    0))

(defn add-coverage [old [_ from to covered]]
  (let [cl (covered-lines from to covered)]
    (if (nil? old)
      [to cl]
      [(max (nth old 0) to) (+ (nth old 1) cl)])))

(defn add-coverage-info-for-file [m info]
  (update m (first info) add-coverage info))

(defn covered-lines-per-file [lines]
  (reduce add-coverage-info-for-file {} (map parse-line lines)))

(defn add-vec2 [[a1 a2] [b1 b2]]
  [(+ a1 b1) (+ a2 b2)])

(defn summarise [file-stats]; file-stats = {"file" [max-line covered]}
  (let [fold (reduce add-vec2 [0 0] (vals file-stats))
        lines (nth fold 0)
        covered (nth fold 1)]
    {:lines lines :covered covered :percentage (common/percentage lines covered)}))

(defn stats [file]
  (with-open [rdr (reader file)]
    (summarise
      (covered-lines-per-file (rest (line-seq rdr))))))
