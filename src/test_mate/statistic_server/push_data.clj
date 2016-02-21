(ns test-mate.statistic-server.push-data
  (:require [clj-http.client :as client]
            [test-mate.config :as config]
            [test-mate.cmd :as command]
            [cover.parse :as parse]
            [cheshire.core :as cheshire]
            [clojure.edn :as edn]))

(def publish-coverage-url (str (config/statistic-server-url) "/publish/coverage"))
(def add-project-url (str (config/statistic-server-url) "/meta/project"))


(defn- send-data [coverage-stats project-name]
  (let [coverage (select-keys coverage-stats [:covered :lines])
        data (merge coverage project-name)]
    (client/put publish-coverage-url {:body (cheshire/generate-string data)
                                      :insecure? true
                                      :content-type :json
                                      :headers {"auth-token" (config/publish-auth-token)}})
    (println "Successfully pushed " data " to " publish-coverage-url)))

(defn- put-project [project-def]
  (try
    (let [put-result (client/put add-project-url {:body (cheshire/generate-string project-def)
                                                  :insecure? true
                                                  :content-type :json
                                                  :headers {"auth-token" (config/meta-auth-token)}})]
       {:status (:status put-result) :project-def project-def})
    (catch clojure.lang.ExceptionInfo e {:status (:status (ex-data e))
                                         :project-def project-def})))
(defn- handle-project [project-def]
  (if (:skip project-def)
    {:status :skipped :project-def project-def}
    (put-project project-def)))

(defn- print-result [result]
  (when (not (= (:status result) :skipped))
    (println (:status result) " -> "(:project-def result))))

(defn publish-statistic-data [coverage-file project-data]
  (send-data (parse/stats coverage-file)
             (merge (config/default-project) (edn/read-string project-data))))

(defn add-project [project-file]
  (let [projects-to-add (edn/read-string (slurp project-file))]
    (doall (map print-result (map handle-project projects-to-add)))))

(defn push-data [[cmd & args]]
  "Multiplex function for statistic-server commands"
  (cond (= cmd "publish-coverage") (publish-statistic-data (first args) (second args))
        (= cmd "add-project") (add-project (first args))
        :else (command/exit-with-usage (str "unknown statistic-server command: " cmd))))
