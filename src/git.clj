(ns git
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string     :as string :refer [split-lines]]))

(def +default-path-prefix+ "src/main/java/")

(defn- git-log-cmd [file git-repo]
  ["git" "--no-pager" "log" "--format='%h %s'" "--no-merges"
   file
   :dir git-repo])

(defn last-commit-date [git-repo file & [path-prefix]]
  (let [prefix (or path-prefix +default-path-prefix+)
        out (string/trim (:out (sh "git" "log" "-1" "--format=%ct" (str prefix file) :dir git-repo)))]
    (if (= out "")
      0
      (Integer/parseInt out))))

(defn log [git-repo file & [path-prefix]]
  (let [prefix (or path-prefix +default-path-prefix+)
        cmd (git-log-cmd (str prefix file) git-repo)]
    (->> (apply sh cmd)
         :out
         string/split-lines)))
