(ns analysis.test-need
  (:require [cover.aggregate.jacoco :as jacoco]
            [git                    :as git]))

(defn- coverage-per-line [[file {:keys [lines covered]}]]
  {:class file
   :uncovered (- lines covered)})

(defn analyse-test-need-coverage [coverage-file]
  (->> coverage-file
       jacoco/aggregate-class-coverage
       (map coverage-per-line)
       (sort-by :uncovered)
       reverse))

(defn bugfix-commit? [log-line]
  (.contains (.toLowerCase log-line) "fix"))

(defn- add-commit-data [git-repo coverage-data]
  (let [log (->> (str (:class coverage-data) ".java")
                 (git/log git-repo))]
    (assoc coverage-data
           :commits (count log)
           :bugfixes (count (filter bugfix-commit? log)))))

(defn- join-bugfix-commit-data
  "Joins commit info to coverage data, takes only the first 1000 coverage data since the commit data retrieval
  is very expensive"
  ([git-repo coverage-data & [commit-count]]
   (let [n (or commit-count 1000)]
     (->> coverage-data
          (map (partial add-commit-data git-repo))
          (take n)))))

(defn analyse-test-need [coverage-file git-repo]
  (->> coverage-file
       analyse-test-need-coverage
       (join-bugfix-commit-data git-repo)))

(defn- bugfix-per-uncovered-line [{:keys [bugfixes uncovered]}]
  (if (= 0 uncovered)
    0
    (/ bugfixes uncovered)))

(defn print-lines [lines]
  (doseq [line lines]
    (println line)))

(defn- test-need-result [result]
  (str (:class result) " has" (:uncovered result) " uncovered lines,
        \tit took " (:commits result) " commits and " (:bugfixes result) " bugfixes to produce this class
        \tgiving it a bugfix/uncovered line ratio of: " (double (bugfix-per-uncovered-line result)) "\n"))

(defn- suspense-dots [limit all-count]
  (str "... only " limit "/" all-count " shown"))

(defn print-analyse-test-need [coverage-file git-repo & [result-count]]
  "Same as function without print, but formats result in a readable way"
  (let [n (or result-count 20)
        result (analyse-test-need coverage-file git-repo)
        by-bugfix (reverse (sort-by bugfix-per-uncovered-line result))]
    (print-lines
     [["Ranked purely by most uncovered lines, you should write tests for these classes: "
       (map test-need-result (take n result))
       (suspense-dots n (count result))
       "\n"
       "Considering bugfixes for uncovered lines, you should write tests for these classes: "
       (map test-need-result (take n by-bugfix))
       (suspense-dots n (count by-bugfix))]])))
