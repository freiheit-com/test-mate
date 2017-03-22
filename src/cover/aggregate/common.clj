(ns cover.aggregate.common)

(defn percentage [lines covered]
  (if (= lines 0)
    1
    (double (/ covered lines))))
