(ns test-mate.analysis.test-need
  (:require [cover.aggregate.jacoco :as jacoco]
            [git                    :as git]
            [clojure.tools.cli      :as cli]
            [clojure-csv.core       :as csv]
            [clojure.string         :as string]))

(defn- coverage-per-line [[file {:keys [lines covered]}]]
  {:class file
   :uncovered (- lines covered)
   :lines lines})

(defn analyse-test-need-coverage [coverage-file]
  (->> coverage-file
       jacoco/aggregate-class-coverage
       (map coverage-per-line)
       (sort-by :uncovered)
       reverse))

(defn bugfix-commit? [log-line]
  (.contains (.toLowerCase log-line) "fix"))

(defn- sanitise-prefix [prefix]
  (if (string/ends-with? prefix "/")
    prefix
    (str prefix "/")))

(defn- add-commit-data [git-repo prefix coverage-data]
  (let [file (str (sanitise-prefix prefix) (:class coverage-data) ".java")
        log  (git/log git-repo file)
        last-change (git/last-commit-date git-repo file)]
    (assoc coverage-data
           :commits (count log)
           :bugfixes (count (filter bugfix-commit? log))
           :last-change last-change)))

(defn- join-bugfix-commit-data [git-repo n prefix coverage-data]
  "Joins commit info to coverage data, takes only the first n coverage data since the commit data retrieval
  is very expensive"
  (->> coverage-data
       (map (partial add-commit-data git-repo prefix))
       (take n)))

(def +csv-header+ [["class" "commits" "bugfixes" "uncovered" "lines" "last-changed" "'=>" "coverage" "bugfix/uncovered" "bugfix/commit" "bugfix/lines" "days-last-update"]])

(defn- round-to-precision
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn- last-changed [now entry]
  (if (= (:last-change entry) 0)
    now
    (:last-change entry)))

(defn- calculate-derived-data [now {:keys [lines uncovered bugfixes commits] :as entry}]
  ["" ; => field (as separator in csv file)
   (if (zero? lines) "-" (.toString (round-to-precision 4 (/ (- lines uncovered) lines)))) ;coverage
   (if (zero? uncovered) "-" (.toString (round-to-precision 4 (/ bugfixes uncovered))))
   (if (zero? commits) "-" (.toString (round-to-precision 4 (/ bugfixes commits))))
   (if (zero? lines) "-" (.toString (round-to-precision 4 (/ bugfixes lines))))
   (.toString (round-to-precision 0 (/ (- now (last-changed now entry)) (* 60 60 24))))])

(defn- csv-fields [now entry]
  (let [csv-data [(or (:class entry) "unknown") (.toString (:commits entry))
                  (.toString (:bugfixes entry)) (.toString (:uncovered entry))
                  (.toString (:lines entry)) (.toString (:last-change entry))]
        derived-data (calculate-derived-data now entry)]
    (vec (concat csv-data derived-data))))

(defn- result-as-csv [now result]
  (vec (map (partial csv-fields now) result)))

(defn- do-analyse [opts]
  (let [coverage-file (:coverage-file opts)
        git-repo (:git-repo opts)
        output-file (:output opts)
        prefix (:prefix opts)
        now (/ (System/currentTimeMillis) 1000)]
    (spit output-file (csv/write-csv
                         (concat +csv-header+
                            (->> coverage-file
                                 analyse-test-need-coverage
                                 (join-bugfix-commit-data git-repo (:num-commits opts) prefix)
                                 (result-as-csv now)))))))

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
    :validate [#(>= % 0) "Must be a number > 0"]]
   ["-p" "--prefix PREFIX" "prefix used to put before class names in the emma report to get a valid file in the git repo"
    :default "src/main/java/"]])


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
