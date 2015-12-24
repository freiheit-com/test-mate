(ns test-mate.core
  (:use cover.reader.jacoco cover.aggregate.jacoco analysis.test-need
        test-mate.statistic-server.push-data [git :as git])
  (:gen-class))

(defn -main [& main-args]
  (let [command (first main-args)
        args (rest main-args)]
    (cond (= command "aggregate") (println "aggregate: " (aggregate (rest args) (first args)))
          (= command "test-need") (print-analyse-test-need (first args) (second args))
          (= command "statistic-server") (push-data args)
          :else (println "unknown command: " command))))
