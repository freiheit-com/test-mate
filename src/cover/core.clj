(ns cover.core
  (:use cover.reader.jacoco cover.aggregate.jacoco))

(defn -main [& args]
  (println "aggregate: " (aggregate (rest args) (first args))))
