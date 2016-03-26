(ns test-mate.analysis.test-need
  (:require [cover.aggregate.jacoco :as jacoco]
            [git                    :as git]
            [clojure.tools.cli      :as cli]))

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

(defn- join-bugfix-commit-data [git-repo n coverage-data]
  "Joins commit info to coverage data, takes only the first 1000 coverage data since the commit data retrieval
  is very expensive"
  (->> coverage-data
       (map (partial add-commit-data git-repo))
       (take n)))

(defn- bugfix-per-uncovered-line [{:keys [bugfixes uncovered]}]
  (if (= 0 uncovered)
    0
    (/ bugfixes uncovered)))

(defn print-lines [lines]
  (doseq [line lines]
    (println line)))

#_(defn- test-need-result [result]
    (str (:class result) " has" (:uncovered result) " uncovered lines,\tit took "
         (:commits result) " commits and " (:bugfixes result) " bugfixes to produce this class \tgiving it a bugfix/uncovered line ratio of: " (double (bugfix-per-uncovered-line result)) "\n"))

#_(defn- suspense-dots [limit all-count]
   (str "... only " limit "/" all-count " shown"))

#_(defn print-analyse-test-need [coverage-file git-repo & [result-count]]
   "Same as function without print, but formats result in a readable way"
   (let [n (or result-count 20)]
        result (analyse-test-need coverage-file git-repo)
        by-bugfix (reverse (sort-by bugfix-per-uncovered-line result))
    (print-lines
     [["Ranked purely by most uncovered lines, you should write tests for these classes: "
       (map test-need-result (take n result))
       (suspense-dots n (count result))
       "\n"
       "Considering bugfixes for uncovered lines, you should write tests for these classes: "
       (map test-need-result (take n by-bugfix))
       (suspense-dots n (count by-bugfix))]])))

;; new (work-in-progress)

(defn- do-analyse [opts]
  (let [coverage-file (:coverage-file opts)
        git-repo (:git-repo opts)]
    ;TODO export this result to csv
    (println (->> coverage-file
                  analyse-test-need-coverage
                  (join-bugfix-commit-data git-repo (:num-commits opts))))))

(def cli-options
  [["-c" "--coverage-file FILE" "coverage file (emma/jacoco format)"
    :missing "coverage file"]
   ["-r" "--git-repo REPO" "git repository"
    :missing "git-repo"]
   ["-o" "--output FILE" "output file (csv format)"
    :missing "output file"
    :default "./test_need_out.csv"]
   ["-n" "--num-commits NUM" "number of commits to consider (this affects runtime)"
    :default 1000
    :parse-fn #(Integer/parseInt %)
    :validate [#(>= % 0) "Must be a number > 0"]]])

(defn- exit-with-usage [msg]
  (println "test-need analysis options:")
  (println msg)
  (System/exit -1))

(defn analyse-test-need [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (or (not (empty? arguments)) errors) (exit-with-usage summary)
      :else (do-analyse options))))
