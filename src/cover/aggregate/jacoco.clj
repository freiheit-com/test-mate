(ns cover.aggregate.jacoco
  (:require [cover.reader.xml-non-validate :as reader]))

(def neutral-count [0 0])
(def special-root-package "/")

(def pack-name (comp :name :attrs))

(defn- is-package [elem]
  (= (:tag elem) :package))

(defn- package-matches [re package-xml]
  (or (= re special-root-package) ; legacy support, treat "/" as #".*"
      (boolean (and (is-package package-xml)
                    (re-matches re (pack-name package-xml))))))

(defn- covered-lines [attrs]
  (let [missed (Integer/valueOf (:missed attrs))
        covered (Integer/valueOf (:covered attrs))]
    [covered (+ missed covered)]))

(defn- line-counter [counter]
  (when (and (= (:tag counter) :counter)
             (= (:type (:attrs counter)) "LINE"))
    (covered-lines (:attrs counter))))

(defn- find-line-counter [counters]
  (or (some line-counter counters)
      neutral-count))

(defn- integer-of [val]
  (if val (Integer/valueOf val) 0))

(defn- parse-legacy-line-counter [val]
  (let [match (re-find #"\((\d+)/(\d+)\)" val)]
    [(integer-of (nth match 1)) (integer-of (nth match 2))]))

;<coverage type="line, %" value="96% (186/193)"></coverage>
(defn- line-counter-legacy-format [counter]
  (if (.startsWith (-> counter :attrs :type) "line")
    (parse-legacy-line-counter (-> counter :attrs :value))
    neutral-count))

(def sum-count (partial map +))

(defn- sum-counts [counts]
  (reduce sum-count neutral-count counts))

(defn- aggregate-line-coverage [decl]
  (let [{:keys [tag content]} decl]
    (cond (= tag :method) (find-line-counter content)
          (= tag :coverage) (line-counter-legacy-format decl)
          :else (sum-counts (map aggregate-line-coverage content)))))

(defn- percentage [lines covered]
  (if (= lines 0)
    1
    (double (/ covered lines))))

(defn- readable [[covered lines]]
  {:covered covered
   :lines lines
   :percentage (percentage lines covered)})

(defn- do-aggregate [report-packages aggregation package-pattern]
  (let [filtered (filter (partial package-matches package-pattern) report-packages)]
    (if (not-empty filtered)
      (assoc aggregation
             (.toString package-pattern) (-> {:content filtered}
                                             aggregate-line-coverage
                                             readable))
      (assoc aggregation (.toString package-pattern) {}))))

(defn report-packages [doc]
  "Extract packages defs from file (detects the old and new emma format)"
  (let [first-tag (-> doc :content first :tag)]
    (cond (= :sessioninfo first-tag) (-> doc :content rest vec)
          (= :stats first-tag) (vec (filter is-package (-> doc :content rest first :content first :content))))))

(defn- as-pattern [string]
  (if (= string special-root-package)
    string
    (re-pattern string)))

(defn aggregate [packages file]
  (let [report-packages (report-packages (reader/read-report file))
        package-pattern (map as-pattern packages)]
    (reduce (partial do-aggregate report-packages) {} package-pattern)))

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
