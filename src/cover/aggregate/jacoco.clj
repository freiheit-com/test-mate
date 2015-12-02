(ns cover.aggregate.jacoco
  (:use cover.reader.jacoco))

(def ^:const +neutral-count+ [0 0])

(defn- pack-name [package-xml]
  (:name (:attrs package-xml)))

(defn- package-starts-with [prefix package-xml]
  (and (= (:tag package-xml) :package) (. (pack-name package-xml) startsWith prefix)))

(defn- covered-lines [attrs]
  (let [missed (Integer/valueOf (:missed attrs))
        covered (Integer/valueOf (:covered attrs))]
    [covered (+ missed covered)]))

(defn- line-counter? [counter]
  (when (and (= (:tag counter) :counter) (= (:type (:attrs counter)) "LINE"))
    (covered-lines (:attrs counter))))

(defn- find-line-counter [counters]
  (let [line (some line-counter? counters)]
    (if line line +neutral-count+)))

(defn- sum-count [[c11 c12] [c21 c22]]
  [(+ c11 c21) (+ c12 c22)])

(defn- sum-counts [counts]
  (reduce sum-count +neutral-count+ counts))

(defn- aggregate-line-coverage [elem]
  (if (= (:tag elem) :method)
    (find-line-counter (:content elem))
    (sum-counts (map aggregate-line-coverage (:content elem)))))

(defn- percentage [lines covered]
  (if (= lines 0) 1
    (double (/ covered lines))))

(defn- readable [[covered lines]]
  {:covered covered :lines lines :percentage (percentage lines covered)})

(defn- do-aggregate [report-packages aggregation package-name]
  (let [filtered (filter (partial package-starts-with package-name) report-packages)]
    (if (not (empty? filtered))
      (assoc aggregation package-name (readable (aggregate-line-coverage {:content filtered})))
      aggregation)))

(defn aggregate [packages file]
  (let [report-packages (:content (read-report file))]
    (reduce (partial do-aggregate report-packages) {} packages)))
