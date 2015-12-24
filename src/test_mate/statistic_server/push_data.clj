(ns test-mate.statistic-server.push-data
  (:require [clj-http.client :as client]
            [test-mate.config :as config]
            [cover.aggregate.jacoco :as jacoco]
            [cheshire.core :as cheshire]
            [clojure.edn :as edn]))

(def publish-url (str (config/statistic-server-url) "/publish/coverage"))

(defn publish-statistic-data [coverage-file project-data]
  (let [coverage (select-keys (get (jacoco/aggregate '("/") coverage-file) "/") [:covered :lines])
        data (merge coverage (config/default-project) (edn/read-string project-data))]
    (client/put publish-url {:body (cheshire/generate-string data)
                             :insecure? true
                             :content-type :json
                             :headers {"auth-token" (config/publish-auth-token)}})))
;TODO
(defn add-project [file]
  (println "adding projects"))

(defn push-data [[cmd & args]]
  "Multiplex function for statistic-server commands"
  (cond (= cmd "publish-coverage") (publish-statistic-data (first args) (second args))
        (= cmd "add-project") (add-project (first args))
        :else (println "unknown statistic-server command: " cmd)))
