(ns cover.aggregate.cobertura
  (:require [cover.reader.xml-non-validate :as reader]))

(defn stats
  "Returns basic statistics from REPORT."
  [report]
  (let [attrs (:attrs (reader/read-report report))]
    {:covered (Integer/parseInt (:lines-covered attrs))
     :lines (Integer/parseInt (:lines-valid attrs))
     :percentage (Float/parseFloat (:line-rate attrs))}))

(defn aggregate
  "Aggregate all coverage data for PACKAGES in FILE."
  [packages file]
  (throw (Exception. "Not implemented yet")))
