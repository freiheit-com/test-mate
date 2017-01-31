(ns test-mate.analysis.test-need-test
  (:require [clojure.test :refer :all]
            [test-mate.analysis.test-need :refer :all]
            [midje.sweet :refer :all]))

(def +minimal-ok-args+ ["-c" "coverage-file/path.xml" "-r" "path/to/repo"])
(def +optional-args+ ["-n" "100" "-o" "output/file.csv" "-p" "src/java"])

(facts "about analysis-test-need-coverage"
  (fact "should sort by most uncovered lines"
    (analysis-test-need-coverage "test/cover/testfiles/jacoco/class_coverage.xml") =>
      '({:class "com/freiheit/foo/Bar" :uncovered 90 :lines 100}
        {:class "com/freiheit/MyClass1" :uncovered 10 :lines 10}
        {:class "com/Bar2" :uncovered 0 :lines 200})))

(facts "about bugfix-commit? classification"
  (fact "should classify log line with fix as bugfix"
    (bugfix-commit? "1234 BugFix: NullPointerException Fixed") => true)
  (fact "should not classify log line as bugfix if there is no fix in it"
    (bugfix-commit? "666 Feature foo added") => false))

(facts "about exit-with-usage"
  (fact "should exit"
    (#'test-mate.analysis.test-need/exit-with-usage ..message.. ..error..) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit) => nil :times 1)))

(def +dummy-log+ ["line 1" "line 2" "bugfix" "fix" "line 5"])

(facts "about add-commit-data"
  (fact "should add empty data if git log is empty"
    (#'test-mate.analysis.test-need/add-commit-data ..git-repo.. ..prefix.. {:class "TestClass"}) => {:class "TestClass" :commits 0 :bugfixes 0 :last-change 0}
    (provided
      (git/log ..git-repo.. "..prefix../TestClass.java") => []
      (git/last-commit-date ..git-repo.. "..prefix../TestClass.java") => 0))
  (fact "should not add an / if already on end"
    (#'test-mate.analysis.test-need/add-commit-data ..git-repo.. "has/atend/" {:class "TestClass"}) => {:class "TestClass" :commits 0 :bugfixes 0 :last-change 0}
    (provided
      (git/log ..git-repo.. "has/atend/TestClass.java") => []
      (git/last-commit-date ..git-repo.. "has/atend/TestClass.java") => 0))
  (fact "should add data from log call"
    (#'test-mate.analysis.test-need/add-commit-data ..git-repo.. ..prefix.. {:class "TestClass"}) => {:class "TestClass" :commits 5 :bugfixes 2 :last-change 1234}
    (provided
      (git/log ..git-repo.. "..prefix../TestClass.java") => +dummy-log+
      (git/last-commit-date ..git-repo.. "..prefix../TestClass.java") => 1234))
  (fact "should add zeros if git-log fails"
    (#'test-mate.analysis.test-need/add-commit-data ..git-repo.. ..prefix.. {:class "TestClass"}) => {:class "TestClass" :commits 0 :bugfixes 0 :last-change 0}
    (provided
      (git/log ..git-repo.. anything) => :fail
      (git/last-commit-date ..git-repo.. anything) => 1234))
  (fact "should not add something if git-last-commit fails"
    (#'test-mate.analysis.test-need/add-commit-data ..git-repo.. ..prefix.. {:class "TestClass"}) => {:class "TestClass" :commits 0 :bugfixes 0 :last-change 0}
    (provided
      (git/log ..git-repo.. anything) => +dummy-log+
      (git/last-commit-date ..git-repo.. anything) => :fail)))


(facts "about join-bugfix-commit-data"
  (fact "should return empty if num is 0"
    (#'test-mate.analysis.test-need/join-bugfix-commit-data ..git-repo.. 0 ..prefix..
      [..commit-data-1.. ..commit-data-2.. ..commit-data-3..]) => []
    (provided
      (#'test-mate.analysis.test-need/add-commit-data ..git-repo.. ..prefix.. anything) => ..added-commit-data.. :times 0))
  (fact "should return first two logs if num is 2"
    (#'test-mate.analysis.test-need/join-bugfix-commit-data ..git-repo.. 2 ..prefix..
      [..commit-data-1.. ..commit-data-2.. ..commit-data-3..]) => [..added-commit-data.. ..added-commit-data..]
    (provided
      (#'test-mate.analysis.test-need/add-commit-data ..git-repo.. ..prefix.. anything) => ..added-commit-data.. :times 3)))

(facts "about result-as-csv"
  (fact "should be empty if no result"
    (#'test-mate.analysis.test-need/result-as-csv ..now.. []) => [])
  (fact "should convert to strings and add derived data"
    (#'test-mate.analysis.test-need/result-as-csv 1458817200
        [{:class "class" :commits 50 :bugfixes 4 :uncovered 666 :lines 1000 :last-change 1458991995}
         {:class "class2" :commits 1 :bugfixes 0 :uncovered 89 :lines 113 :last-change 0}
         {:class "class3" :commits 103 :bugfixes 26 :uncovered 230 :lines 233 :last-change 1255691895}]) =>
                 [["class" "50" "4" "666" "1000" "1458991995" "" "0.334" "0.006" "0.08" "0.004" "-2.0"]
                  ["class2" "1" "0" "89" "113" "0" "" "0.2124" "0.0" "0.0" "0.0" "0.0"]
                  ["class3" "103" "26" "230" "233" "1255691895" "" "0.0129" "0.113" "0.2524" "0.1116" "2351.0"]]))

(facts "about do-analysis"
  (fact "should spit analysis output to file"
    (#'test-mate.analysis.test-need/do-analysis {:coverage-file ..coverage-file..
                                                 :git-repo ..git-repo..
                                                 :output ..output-file..
                                                 :num-commits ..num-commits..
                                                 :prefix ..prefix..}) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/analysis-test-need-coverage ..coverage-file..) => ..coverage-data.. :times 1
      (#'test-mate.analysis.test-need/join-bugfix-commit-data ..git-repo.. ..num-commits.. ..prefix.. ..coverage-data..) => ..added-coverage-data.. :times 1
      (#'test-mate.analysis.test-need/result-as-csv anything ..added-coverage-data..) => [["class" "50" "4" "666" "987" "1458991995" "" "0.66" "0.01" "0.033" "1.89" "33.0"]] :times 1
      (spit ..output-file.. "class,commits,bugfixes,uncovered,lines,last-changed,'=>,coverage,bugfix/uncovered,bugfix/commit,bugfix/lines,days-last-update\nclass,50,4,666,987,1458991995,,0.66,0.01,0.033,1.89,33.0\n") => irrelevant :times 1)))

(facts "about analysis main function"
  (fact "should print usage if no repo given"
    (analysis-test-need ["-c coverage-file.xml"]) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything anything) => nil :times 1))
  (fact "should print usage if no coverage file given"
    (analysis-test-need ["-r repo-path"]) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything anything) => nil :times 1))
  (fact "should print usage if unknown argument is given"
    (analysis-test-need (conj +minimal-ok-args+ "--unkown-argument")) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything anything) => nil :times 1))
  (fact "should print usage if num-commits < 0"
    (analysis-test-need (conj +minimal-ok-args+ "-n -1")) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything anything) => nil :times 1))
  (fact "should use default prefix if none given"
    (analysis-test-need +minimal-ok-args+) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/do-analysis
        (checker [actual] (= (:prefix actual) "src/main/java/"))) => nil :times 1))
  (fact "should start analysis if minimal set of args are given"
    (analysis-test-need +minimal-ok-args+) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/do-analysis anything) => nil :times 1))
  (fact "should start analysis if all args are given"
    (analysis-test-need (concat +minimal-ok-args+ +optional-args+)) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/do-analysis anything) => nil :times 1)))
