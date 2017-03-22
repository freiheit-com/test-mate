(defproject test-mate "0.9.0"
  :description "mate for testing needs"
  :url "https://github.com/freiheit-com/test-mate"
  :license {:name "GPLv3"
            :url "https://www.gnu.org/licenses/agpl-3.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clojure-csv/clojure-csv "2.0.2"]]
  :profiles {:dev {:dependencies [[midje "1.8.3"]]
                   :plugins [[lein-midje "3.2.1"]]}}
  :aot :all
  :main test-mate.core
  :jvm-opts ["-Xss8m"]) ;workaround: big jacoco files need a lot of stack -> rewrite aggregation with tail recursion
