(ns cover.aggregate.go-test
  (:require [cover.aggregate.go :refer :all]
            [midje.sweet        :refer :all]))


(def go-dir "test/cover/testfiles/go/")
(defn _test-path [f] (str go-dir f))
(def _minimal (_test-path "minimal.out"))
(def _test-file (_test-path "test.out"))

(facts "about stats"
  (fact "summarises minimal file"
    (stats _minimal) => {:covered 3 :lines 35 :percentage 0.08571428571428572})
  (fact "summarise test file"
    (stats _test-file) => {:covered 129 :lines 565 :percentage 0.2283185840707965}))

(facts "about parse-line"
  (fact "parses line non-covered"
    (parse-line "path/to/a.go:23.70,25.2 1 0") => ["path/to/a.go" 23 25 false])
  (fact "parses line covered"
    (parse-line "file.go:28.91,30.16 2 1") => ["file.go", 28 30 true])
  (fact "parse illegal line"
    (parse-line "something.go:illegal") => nil))
