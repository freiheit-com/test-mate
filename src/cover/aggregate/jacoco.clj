(ns cover.aggregate.jacoco
  (:use cover.reader.jacoco))

(defn- pack-name [package-xml]
  (:name (:attrs package-xml)))

(defn- package-starts-with [prefix package-xml]
  (. (pack-name package-xml) startsWith prefix))

(defn- sum-package-coverage [packages]
  0.5) ;TODO actually sum coverage data for package

(defn- do-aggregate [report-packages aggregation package-name]
  (let [filtered (filter (partial package-starts-with package-name) report-packages)]
    (if (not (empty? filtered))
      (assoc aggregation package-name (sum-package-coverage filtered)) aggregation)))

(defn aggregate [packages file]
  (let [report-packages (rest (:content (read-report file)))]
    (reduce (partial do-aggregate report-packages) {} packages)))
