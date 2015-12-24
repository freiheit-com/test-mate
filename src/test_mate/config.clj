(ns test-mate.config
  (:require [clojure.edn :as edn]))

(def ^:private +config-file+ ".test-mate-config")

;TODO make config overridable by system property
(defn- read-config []
  (let [config-file (new java.io.File (str (System/getenv "HOME") "/" +config-file+))]
    (if (not (.exists config-file))
      {}
      (edn/read-string (slurp config-file)))))

(def ^:private ^:dynamic *test-mate-config* (read-config))

(def statistic-server-url (partial *test-mate-config* :statistic-server-url))

(defn publish-auth-token []
  (-> *test-mate-config* :auth-tokens :publish))

(def default-project (partial *test-mate-config* :default-project))
