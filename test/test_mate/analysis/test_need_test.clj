(ns test-mate.analysis.test-need-test
  (:require [clojure.test :refer :all]
            [analysis.test-need :refer :all]))

(deftest should-sort-by-most-uncovered-lines
  (is (= (analyse-test-need-coverage "test/cover/testfiles/jacoco/class_coverage.xml")
         '({:class "com/freiheit/foo/Bar" :uncovered 90} {:class "com/freiheit/MyClass1"  :uncovered 10}
           {:class "com/Bar2" :uncovered 0}))))

(deftest should-classify-log-line-with-fix-as-bugfix
  (is (bugfix-commit? "1234 BugFix: NullPointerException Fixed")))

(deftest should-not-classify-log-line-as-bugfix-if-there-is-no-fix-in-it
  (is (not (bugfix-commit? "666 Feature foo added"))))
