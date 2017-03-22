(ns cover.aggregate.cobertura
  (:require [cover.reader.xml-non-validate :as reader]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as zip]))

(defn make-coverage
  "Construct a coverage object from parameters."
  [covered lines percentage]
  {:covered covered
   :lines lines
   :percentage percentage})

(defn parse-root
  "parse coverage from node"
  [node]
  (let [attrs (:attrs node)]
    (make-coverage (Integer/parseInt (:lines-covered attrs))
                 (Integer/parseInt (:lines-valid attrs))
                 (Float/parseFloat (:line-rate attrs)))))

(defmacro tryCatch [& body] `(try (do ~@body) (catch Exception e# nil)))

(defn get-counts
  "Get line hits from lines ZIP"
  [zip]
  (Integer/parseInt (zip/attr zip :hits)))

(defn hit?
  "Tests if LINE was hit."
  [line]
  (true? (tryCatch (pos? (get-counts line)))))

(defn- percentage [lines covered]
  (if (zero? lines)
    1
    (double (/ covered lines))))

(defn get-lines
  "Get all lines from ROOT"
  [root]
  (zip/xml-> root :packages :package :classes :class :lines :line))

(defn get-summary
  "Get summed coverage from LINES"
  [lines]
  (let [lines# (count lines)
        covered# (count (filter hit? lines))]
    (make-coverage covered#
                   lines#
                   (percentage lines# covered#))))

(defn get-name-attr
  "Gets the name attribute from ZIP"
  [zip]
  (zip/attr zip :name))

(defn get-class
  "get class from LINE zip"
  [line]
  (-> line
      z/up
      z/up
      get-name-attr))

(defn get-package
  "get package from LINE zip"
  [line]
  (-> line
      z/up
      z/up
      z/up
      z/up
      get-name-attr))

;TODO rename to overall-coverage
(defn stats
  "Returns basic statistics from REPORT. Produces a overall coverage information."
  [report]
  (let [xml (reader/read-report report)]
    (or (tryCatch (parse-root xml))
        (get-summary (get-lines (z/xml-zip xml))))))

(defn update-values
  "Updates MAP values with FUNC"
  [func map & args]
  (reduce-kv (fn [m k v] (assoc m k (apply func v args))) {} map))


(defn aggregate
  "Aggregate all coverage data for PACKAGES in FILE."
  [packages file]
  (let [xml (reader/read-report file)
        lines (get-lines (z/xml-zip xml))
        by-package (group-by get-package lines)
        wanted-packages (if (empty? packages) by-package (select-keys by-package packages))]
    (update-values get-summary wanted-packages)))
