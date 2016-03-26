(ns test-mate.analysis.test-need-test
  (:require [clojure.test :refer :all]
            [test-mate.analysis.test-need :refer :all]
            [midje.sweet :refer :all]))

(def +minimal-ok-args+ ["-c" "coverage-file/path.xml" "-r" "path/to/repo"])

(facts "about analyse-test-need-coverage"
  (fact "should sort by most uncovered lines"
    (analyse-test-need-coverage "test/cover/testfiles/jacoco/class_coverage.xml") =>
      '({:class "com/freiheit/foo/Bar" :uncovered 90} {:class "com/freiheit/MyClass1"  :uncovered 10}
        {:class "com/Bar2" :uncovered 0})))

(facts "about bugfix-commit? classification"
  (fact "should classify log line with fix as bugfix"
    (bugfix-commit? "1234 BugFix: NullPointerException Fixed") => true)
  (fact "should not classify log line as bugfix if there is no fix in it"
    (bugfix-commit? "666 Feature foo added") => false))

(facts "about analysis main function"
  (fact "should print usage if no repo given"
    (analyse-test-need ["-c coverage-file.xml"]) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything) => nil :times 1))
  (fact "should print usage if no coverage file given"
    (analyse-test-need ["-r repo-path"]) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything) => nil :times 1))
  (fact "should print usage if unknown argument is given"
    (analyse-test-need (conj +minimal-ok-args+ "--unkown-argument")) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything) => nil :times 1))
  (fact "should print usage if num-commits < 0"
    (analyse-test-need (conj +minimal-ok-args+ "-n -1")) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/exit-with-usage anything) => nil :times 1))
  (fact "should start analysis if all args are given"
    (analyse-test-need +minimal-ok-args+) => irrelevant
    (provided
      (#'test-mate.analysis.test-need/do-analyse anything) => nil :times 1)))
