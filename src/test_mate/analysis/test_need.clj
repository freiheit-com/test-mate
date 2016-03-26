(ns test-mate.analysis.test-need
  (:require [cover.aggregate.jacoco :as jacoco]
            [git                    :as git]
            [clojure.tools.cli      :as cli]
            [clojure-csv.core       :as csv]))

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
  "Joins commit info to coverage data, takes only the first n coverage data since the commit data retrieval
  is very expensive"
  (->> coverage-data
       (map (partial add-commit-data git-repo))
       (take n)))

(def +csv-header+ [["class" "commits" "bugfixes" "uncovered"]])

(defn- extract-fields-for-csv [entry]
  [(:class entry) (.toString (:commits entry)) (.toString (:bugfixes entry)) (.toString (:uncovered entry))])

(defn- result-as-csv [result]
  (map extract-fields-for-csv result))

(defn- do-analyse [opts]
  (let [coverage-file (:coverage-file opts)
        git-repo (:git-repo opts)
        output-file (:output opts)]
    (spit output-file (csv/write-csv
                         (concat +csv-header+
                            (->> coverage-file
                                 analyse-test-need-coverage
                                 (join-bugfix-commit-data git-repo (:num-commits opts))
                                 result-as-csv))))))

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

(defn- exit []
  (System/exit -1))

(defn- exit-with-usage [msg errs]
  (when errs
    (println "error" errs "\n"))
  (println "test-need analysis options:")
  (println msg)
  (exit))

(defn analyse-test-need [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (or (not (empty? arguments)) errors) (exit-with-usage summary errors)
      :else (do-analyse options))))
