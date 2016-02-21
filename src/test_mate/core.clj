(ns test-mate.core
  (:use cover.parse
        analysis.test-need
        test-mate.statistic-server.push-data
        [git :as git])
  (:gen-class))

(defn exit-with-usage [message]
  ;TODO print usage :)
  (println message))

(defn -main [& main-args]
  (let [command (first main-args)
        args (rest main-args)]
    (cond (= command "aggregate") (println "aggregate: " (cover.parse/aggregate (first args) (rest args)))
          (= command "test-need") (print-analyse-test-need (first args) (second args))
          (= command "statistic-server") (push-data args)
          :else (println "unknown command: " command))))
