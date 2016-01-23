(ns cover.aggregate.jacoco
  (:require [cover.reader.xml-non-validate :as reader]))

(def neutral-count [0 0])
(def special-root-package "/")

(def pack-name (comp :name :attrs))

(defn- package-starts-with [prefix package-xml]
  (or (= prefix special-root-package)
      (and (= (:tag package-xml) :package)
           (.startsWith (pack-name package-xml) prefix))))

(defn- covered-lines [attrs]
  (let [missed (Integer/valueOf (:missed attrs))
        covered (Integer/valueOf (:covered attrs))]
    [covered (+ missed covered)]))

(defn- line-counter? [counter]
  (when (and (= (:tag counter) :counter)
             (= (:type (:attrs counter)) "LINE"))
    (covered-lines (:attrs counter))))

(defn- find-line-counter [counters]
  (or (some line-counter? counters)
      neutral-count))

(def sum-count (partial map +))

(defn- sum-counts [counts]
  (reduce sum-count neutral-count counts))

(defn- aggregate-line-coverage [{:keys [tag content]}]
  (if (= tag :method)
    (find-line-counter content)
    (sum-counts (map aggregate-line-coverage content))))

(defn- percentage [lines covered]
  (if (= lines 0)
    1
    (double (/ covered lines))))

(defn- readable [[covered lines]]
  {:covered covered
   :lines lines
   :percentage (percentage lines covered)})

(defn- do-aggregate [report-packages aggregation package-name]
  (let [filtered (filter (partial package-starts-with package-name) report-packages)]
    (if (not-empty filtered)
      (assoc aggregation
             package-name (-> {:content filtered}
                              aggregate-line-coverage
                              readable))
      (assoc aggregation package-name {}))))

(def report-packages (comp vec rest :content))


(defn aggregate [packages file]
  (let [report-packages (report-packages (reader/read-report file))]
    (reduce (partial do-aggregate report-packages) {} packages)))

(defn- assoc-class-coverage [aggregation class]
  (assoc aggregation (:name (:attrs class)) (readable (aggregate-line-coverage class))))

(defn- do-aggregate-classes [aggregation package]
  (->> package
       :content
       (reduce assoc-class-coverage aggregation)))

(defn aggregate-class-coverage [file]
  "Aggregate coverage data for each class in report"
  (let [report-packages (report-packages (reader/read-report file))]
    (reduce do-aggregate-classes {} report-packages)))

(defn stats
  "Returns statistics from FILE"
  [file]
  (get (aggregate ["/"] file) "/"))
