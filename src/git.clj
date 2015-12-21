(ns git
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string     :as string :refer [split-lines]]))


(defn git-log-cmd [file git-repo]
  ["git" "--no-pager" "log" "--format='%h %s'" "--no-merges"
   file
   :dir git-repo])

(defn log [git-repo file & [path-prefix]]
  (let [prefix (or path-prefix "src/main/java/")
        cmd (git-log-cmd (str prefix file) git-repo)]
    (->> (apply sh cmd)
         :out
         string/split-lines)))
