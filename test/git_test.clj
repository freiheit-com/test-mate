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

(fact "about git-last-commit-date"
  (fact "should return 0 if result is empty"
    (git/last-commit-date ..git-repo.. ..file.. "") => 0
    (provided
      (sh "git" "log" "-1" "--format=%ct" "..file.." :dir ..git-repo..) => {:out ""}))
  (fact "should return unix timestamp"
    (git/last-commit-date ..git-repo.. ..file..) => 1458991995
    (provided
      (sh "git" "log" "-1" "--format=%ct" "src/main/java/..file.." :dir ..git-repo..) => {:out "1458991995"}))
  (fact "should return unix timestamp if ends with newline"
    (git/last-commit-date ..git-repo.. ..file..) => 1458991995
    (provided
      (sh "git" "log" "-1" "--format=%ct" "src/main/java/..file.." :dir ..git-repo..) => {:out "1458991995\n"}))
  (fact "should return last-commit-date with prefix"
    (git/last-commit-date ..git-repo.. ..file.. "prefix/") => 1458991995
    (provided
      (sh "git" "log" "-1" "--format=%ct" "prefix/..file.." :dir ..git-repo..) => {:out "1458991995"})))
