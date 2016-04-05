(ns git
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string     :as string :refer [split-lines]]))

(def +default-path-prefix+ "src/main/java/")

(defn- git-log-cmd [git-repo file]
  ["git" "--no-pager" "log" "--format='%h %s'" "--no-merges"
   file
   :dir git-repo])

(defn last-commit-date [git-repo file]
  (let [sh-result (sh "git" "log" "-1" "--format=%ct" file :dir git-repo)
        out (string/trim (:out sh-result))]
    (if (= 0 (:exit sh-result))
      (if (= out "") 0 (Integer/parseInt out))
      :fail)))

(defn log [git-repo file]
  (let [cmd (git-log-cmd git-repo file)
        sh-result (apply sh cmd)]
    (if (= 0 (:exit sh-result))
      (string/split-lines (:out sh-result))
      :fail)))
