(ns test-mate.core
  (:require [analysis.test-need :as test-need]
            [cover.parse :as cover]
            [test-mate.statistic-server.push-data :as statistic-server]
            [test-mate.cmd :as cmd])
  (:gen-class))

(defn -main [& main-args]
  (let [command (first main-args)
        args (rest main-args)]
    (cond (= command "aggregate") (println "aggregate: " (cover/aggregate (first args) (rest args)))
          (= command "test-need") (test-need/print-analyse-test-need (first args) (second args))
          (= command "statistic-server") (statistic-server/push-data args)
          :else (cmd/exit-with-usage (str "unknown command: " command)))))
