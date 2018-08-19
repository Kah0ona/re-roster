(ns re-roster.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-roster.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
