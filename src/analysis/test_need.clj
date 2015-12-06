(ns analysis.test-need
  (:use cover.aggregate.jacoco [git :as git]))

(defn- coverage-per-line [[file coverage]]
  {:class file :uncovered (- (:lines coverage) (:covered coverage))})

(defn analyse-test-need-coverage [coverage-file]
  (reverse (sort-by :uncovered (map coverage-per-line (aggregate-class-coverage coverage-file)))))

(defn bugfix-commit? [log-line]
  (. (. log-line toLowerCase) contains "fix"))

(defn- add-commit-data [git-repo coverage-data]
  (let [log (git/log git-repo (str (:class coverage-data) ".java"))]
    (assoc coverage-data :commits (count log) :bugfixes (count (filter bugfix-commit? log)))))

;TODO param for 750
(defn- join-bugfix-commit-data [git-repo coverage-data]
  "Joins commit info to coverage data, takes only the first 750 coverage data since the commit data retrieval
  is very expensive"
  (map (partial add-commit-data git-repo) (take 750 coverage-data)))

(defn analyse-test-need [coverage-file git-repo]
  (join-bugfix-commit-data git-repo (analyse-test-need-coverage coverage-file)))

(defn- bugfix-per-uncovered-line [data]
  (let [bugfixes (:bugfixes data)
        uncovered (:uncovered data)]
    (if (= 0 uncovered) 0 (/ bugfixes uncovered))))

;TODO param for count print
(defn print-analyse-test-need [coverage-file git-repo]
  "same as function without print, but formats result in a redable way"
  (let [result (analyse-test-need coverage-file git-repo)
        by-bugfix (reverse (sort-by bugfix-per-uncovered-line result))]
    (println "Ranked purely by most uncovered lines, you should write tests for this classes: ")
    (println (take 25 result))
    (println "Considering bugfixes for uncovered lines, you should write tests for this classes: ")
    (println (take 25 by-bugfix))))
