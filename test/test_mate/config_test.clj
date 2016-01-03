(ns test-mate.config-test
  (:require [clojure.test      :refer :all]
            [test-mate.config  :refer :all]))

(deftest should-read-empty-config-if-no-file-in-home-existst
  (is (= (#'test-mate.config/read-config ".doesNotExist") {})))

(deftest should-read-from-sys-property-if-set
  (System/setProperty "fdc.test-mate.config.file" "test/test_mate/testfiles/test_config_via_sys_property")
  (let [prop-content (#'test-mate.config/read-config ".doesNotExist")]
    (is (= (:statistic-server-url prop-content) "https://server-via-property-set")))
  (System/clearProperty "fdc.test-mate.config.file"))
