(ns git
  (:use [clojure.java.shell :only [sh]] [clojure.string :as string :only [split-lines]]))

;TODO param for src/main/java

(defn log [git-repo file]
  (string/split-lines (:out (sh "git" "--no-pager" "log" "--format='%h %s'" "--no-merges" (str "src/main/java/" file) :dir git-repo))))
