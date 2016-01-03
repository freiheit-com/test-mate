(ns test-mate.config
  (:require [clojure.edn :as edn]))

(def ^:private +home-config-file+ ".test-mate-config")
(def ^:private +config-system-property+ "fdc.test-mate.config.file")

(defn- read-config-file [config-file]
   (if (not (.exists config-file))
     {}
     (edn/read-string (slurp config-file))))

(defn- read-config-from-home-dir [home-file-name]
   (let [config-file (new java.io.File (str (System/getenv "HOME") "/" home-file-name))]
     (read-config-file config-file)))

(defn- read-config [home-file-name]
  (let [sys-prop-file (System/getProperty +config-system-property+)]
    (if sys-prop-file
      (read-config-file (new java.io.File sys-prop-file))
      (read-config-from-home-dir home-file-name))))

(def ^:private ^:dynamic *test-mate-config* (read-config +home-config-file+))

(def statistic-server-url (partial *test-mate-config* :statistic-server-url))

(defn publish-auth-token []
  (-> *test-mate-config* :auth-tokens :publish))

(defn meta-auth-token []
  (-> *test-mate-config* :auth-tokens :meta))

(def default-project (partial *test-mate-config* :default-project))
