(ns test-mate.cmd)

(def available-commands {"statistic-server" "Available statistic-server subcommands: publish-coverage, add-project"
                         "aggregate" "Usage: test-mate aggregate <path/to/coverage_file> package-regex-1 package-regex-2 ..."
                         "analysis" "Available analysis subcommands: test-need"})

(defn build-command-summary []
  (str "\nAvailable commands:\n\n"
       (clojure.string/join "\n"
                            (map #(format "%-20s %s"
                                          (first %)
                                          (second %))
                                 available-commands))))

(defn exit-with-usage [message command]
  (println message)
  (println (get available-commands
                command
                (build-command-summary))))
