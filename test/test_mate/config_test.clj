(ns test-mate.config-test
  (:require [test-mate.config  :refer :all]
            [midje.sweet      :refer :all]))

(fact "should read empty config if no file in home exists"
  (#'test-mate.config/read-config ".doesNotExist") => {})

(fact "should read from sys-property if set"
  (System/setProperty "fdc.test-mate.config.file" "test/test_mate/testfiles/test_config_via_sys_property")
  (let [prop-content (#'test-mate.config/read-config ".doesNotExist")]
    (:statistic-server-url prop-content) => "https://server-via-property-set")
  (System/clearProperty "fdc.test-mate.config.file"))

(facts "about default properties"
  (fact "gets default props from valid minimal file"
    (binding [*test-mate-config* (#'test-mate.config/read-config-file (java.io.File. "test/test_mate/testfiles/minimal_valid_config"))]
      (allow-decreasing-coverage) => true)))

(facts "about property access"
  (fact "gets default props from valid minimal file"
    (binding [*test-mate-config* (#'test-mate.config/read-config-file (java.io.File. "test/test_mate/testfiles/maximal_valid_config"))]
      (allow-decreasing-coverage) => false)))
