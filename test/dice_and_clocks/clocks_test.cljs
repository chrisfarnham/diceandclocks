(ns dice-and-clocks.clocks-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [dice-and-clocks.clocks :as clocks]))

(deftest get-faces-test
  (testing "returns the full face list for a known clock type"
    (is (= ["4b0.png" "4b1.png" "4b2.png" "4b3.png" "4b4.png"]
           (clocks/get-faces :four-b))))
  (testing "unknown clock type returns nil"
    (is (nil? (clocks/get-faces :not-a-real-clock)))))

(deftest get-face-test
  (testing "returns the face at a given tic"
    (is (= "4b0.png" (clocks/get-face :four-b 0)))
    (is (= "4b4.png" (clocks/get-face :four-b 4))))
  (testing "accepts a string key, not just a keyword"
    (is (= "4b0.png" (clocks/get-face "four-b" 0))))
  (testing "tic beyond the clock's segment count returns nil"
    (is (nil? (clocks/get-face :four-b 5)))))

(deftest max-index-test
  (testing "matches (face count - 1) for every defined clock type"
    (doseq [{:keys [key faces]} clocks/clocks]
      (is (= (dec (count faces)) (clocks/max-index key))
          (str "max-index mismatch for " key)))))

(deftest clock-types-test
  (testing "every clock type is represented exactly once"
    (is (= (count clocks/clocks) (count clocks/clock-types))))
  (testing "each clock-type's face is that clock's second face"
    (doseq [{:keys [key face]} clocks/clock-types]
      (is (= face (clocks/get-face key 1))
          (str "clock-types face mismatch for " key)))))
