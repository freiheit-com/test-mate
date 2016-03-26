(ns git-test
  (:require [midje.sweet   :refer :all]
            [git :refer :all]
            [clojure.java.shell :refer [sh]]))

(facts "about git-log"
  (fact "should git-log on file with default maven-style prefix"
    (git/log ..git-repo.. "test/file.java") => ["log 1" "log 2"]
    (provided
      (sh "git" "--no-pager" "log" "--format='%h %s'" "--no-merges"
       "src/main/java/test/file.java" :dir ..git-repo..) => {:out "log 1\nlog 2"}))
  (fact "should git-log with prefix"
    (git/log ..git-repo.. "test/file.java" "prefix/") => ["log 1" "log 2" "log 3"]
    (provided
      (sh "git" "--no-pager" "log" "--format='%h %s'" "--no-merges"
       "prefix/test/file.java" :dir ..git-repo..) => {:out "log 1\nlog 2\nlog 3"})))
