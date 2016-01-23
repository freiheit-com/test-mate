(ns cover.reader.xml-non-validate
  (:require [clojure.xml :as xml]))

(defn- startparse-sax-non-validating [s ch]
    (.. (doto (javax.xml.parsers.SAXParserFactory/newInstance)
         (.setValidating false)
         (.setFeature "http://apache.org/xml/features/nonvalidating/load-dtd-grammar" false)
         (.setFeature "http://apache.org/xml/features/nonvalidating/load-external-dtd" false)
         (.setFeature "http://xml.org/sax/features/validation" false)
         (.setFeature "http://xml.org/sax/features/external-general-entities" false)
         (.setFeature "http://xml.org/sax/features/external-parameter-entities" false))

        (newSAXParser) (parse s ch)))

(defn read-report [report]
   (xml/parse report startparse-sax-non-validating))
