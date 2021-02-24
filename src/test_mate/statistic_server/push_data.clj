(ns test-mate.statistic-server.push-data
  (:require [clj-http.client :as client]
            [test-mate.config :as config]
            [test-mate.cmd :as command]
            [test-mate.exit :as exit]
            [cover.parse :as parse]
            [cheshire.core :as cheshire]
            [clojure.edn :as edn]))

(def publish-coverage-url (str (config/statistic-server-url) "/publish/coverage"))
(def load-latest-url (str (config/statistic-server-url) "/statistics/coverage/latest/"))
(def add-project-url (str (config/statistic-server-url) "/meta/project"))

(def fail-epsilon 0.0001) ; allow some variance in the percentag coverage value

(defn- only-project-data [m]
  (select-keys m [:project :subproject :language]))

(defn- latest-coverage-url [project-def]
  (str load-latest-url (:project project-def) "/" (:subproject project-def) "/" (:language project-def)))

(defn- load-latest-data [project-def]
  (cheshire/parse-string
     (:body (client/get (latest-coverage-url project-def)
               {:insecure? true
                :content-type :json
                :headers {"Authorization" (str "Bearer" (config/statistics-auth-token))}}))
    true))

(defn percentage-of-pushed [pushed-data]
  (if (<= (:lines pushed-data) 0)
    1.0
    (double (/ (:covered pushed-data) (:lines pushed-data)))))

(defn- decreasing-coverage? [push-stats project-name]
  (let [latest (load-latest-data project-name)
        provided-percentage (percentage-of-pushed push-stats)
        pushed-percentage (:percentage (:overall-coverage latest))]
    (println (format "Stats-Server: %s Provided: %s"(* 100 pushed-percentage) (* 100 provided-percentage)))
    (< provided-percentage (- pushed-percentage fail-epsilon))))

(defn- post-data-to-server [coverage-stats project-name]
  (let [coverage (select-keys coverage-stats [:covered :lines])
        data (merge coverage project-name)]
    (client/put publish-coverage-url {:body (cheshire/generate-string data)
                                      :insecure? true
                                      :content-type :json
                                      :headers {"Authorization" (str "Bearer" (config/publish-auth-token))}})
    (println "Successfully pushed " data " to " publish-coverage-url)))

(defn- validate-data [coverage-stats project-name]
  (if (decreasing-coverage? coverage-stats project-name)
    (do
      (println "Decreasing coverage detected, not allowed by configuration, terminating.")
      (exit/terminate -1)
      false)
    true))

(defn- validate-threshold [coverage-stats threshold-percentage]
  (if (< (percentage-of-pushed coverage-stats) threshold-percentage)
    (do
      (println (format "Decreasing coverage detected, not allowed by configured threshold, terminating. %s < %s" (percentage-of-pushed coverage-stats) threshold-percentage))
      (exit/terminate -1)
      false)
    (do
      (println (format "Coverage ok! %s >= %s" (percentage-of-pushed coverage-stats) threshold-percentage))
      true)))

(defn- send-data [coverage-stats project-name]
  (when
    (and
      (or (config/allow-decreasing-coverage) ; defaults to true
          (validate-data coverage-stats project-name))
      (validate-threshold coverage-stats (config/coverage-threshold))) ; defaults to 0 percent
    (post-data-to-server coverage-stats project-name)))

(defn- put-project [project-def]
  (let [put-data (only-project-data project-def)]
    (try
      (let [put-result (client/put add-project-url {:body (cheshire/generate-string put-data)
                                                    :insecure? true
                                                    :content-type :json
                                                    :headers {"auth-token" (config/meta-auth-token)}})]
          {:status (:status put-result) :project-def project-def})
      (catch clojure.lang.ExceptionInfo e {:status (:status (ex-data e))
                                           :project-def project-def}))))
(defn- handle-project [project-def]
  (if (:skip project-def)
    {:status :skipped :project-def project-def}
    (put-project project-def)))

(defn- print-result [result]
  (when (not (= (:status result) :skipped))
    (println (:status result) " -> "(:project-def result))))

(defn- read-with-prj-defaults
  "Reads a project identifier string and constructs a project object with defaults filled in if omitted."
  [project-data]
  (let [project-def-supplied (only-project-data (edn/read-string project-data))]
    (merge (config/default-project) project-def-supplied)))

(defn publish-statistic-data [coverage-file project-data]
  (send-data (parse/stats coverage-file) (read-with-prj-defaults project-data)))

(defn check-coverage
  "checks coverage on server and fails if higher than provided"
  [coverage-file project-data]
  (validate-data (parse/stats coverage-file) (read-with-prj-defaults project-data)))

(defn add-project [project-file]
  (let [projects-to-add (edn/read-string (slurp project-file))]
    (doall (map print-result (map handle-project projects-to-add)))))

(defn push-data [[cmd & args]]
  "Multiplex function for statistic-server commands"
  (condp = cmd
    "publish-coverage" (publish-statistic-data (first args) (second args))
    "add-project" (add-project (first args))
    "check-coverage" (check-coverage (first args) (second args))
    (command/exit-with-usage (str "unknown statistic-server command: " cmd) "statistic-server")))
