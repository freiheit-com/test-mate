(ns git-test
  (:require [midje.sweet   :refer :all]
            [git :refer :all]
            [clojure.java.shell :refer [sh]]))

(facts "about git-log"
  (fact "should git-log on file"
    (git/log ..git-repo.. ..file..) => ["log 1" "log 2"]
    (provided
      (sh "git" "--no-pager" "log" "--format='%h %s'" "--no-merges"
       ..file.. :dir ..git-repo..) => {:out "log 1\nlog 2" :exit 0})
   (fact "should return :fail if command fails"
     (git/log ..git-repo.. ..file..) => :fail
     (provided
       (sh "git" "--no-pager" "log" "--format='%h %s'" "--no-merges"
        ..file.. :dir ..git-repo..) => {:exit 128}))))

(fact "about git-last-commit-date"
  (fact "should return 0 if result is empty"
    (git/last-commit-date ..git-repo.. ..file..) => 0
    (provided
      (sh "git" "log" "-1" "--format=%ct" ..file.. :dir ..git-repo..) => {:out "" :exit 0}))
  (fact "should return unix timestamp"
    (git/last-commit-date ..git-repo.. ..file..) => 1458991995
    (provided
      (sh "git" "log" "-1" "--format=%ct" ..file.. :dir ..git-repo..) => {:out "1458991995" :exit 0}))
  (fact "should return unix timestamp if ends with newline"
    (git/last-commit-date ..git-repo.. ..file..) => 1458991995
    (provided
      (sh "git" "log" "-1" "--format=%ct" ..file.. :dir ..git-repo..) => {:out "1458991995\n" :exit 0}))
  (fact "should return :fail if command fails"
    (git/last-commit-date ..git-repo.. ..file..) => :fail
    (provided
      (sh "git" "log" "-1" "--format=%ct" ..file.. :dir ..git-repo..) => {:exit 128 :out ""})))
