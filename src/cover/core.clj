(ns cover.core
  (:use cover.reader.jacoco cover.aggregate.jacoco analysis.test-need [git :as git]))

(defn -main [& main-args]
  (let [command (first main-args)
        args (rest main-args)]
    (cond (= command "aggregate") (println "aggregate: " (aggregate (rest args) (first args)))
          (= command "test-need") (print-analyse-test-need (first args) (second args)))))
