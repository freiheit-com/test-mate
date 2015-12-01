(ns cover.core
  (:use cover.in.jacoco))

(defn -main [& args]
  (read-report (first args)))
