(ns dice-and-clocks.action-rolls-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [dice-and-clocks.action-rolls :as action-rolls]))

(deftest combinations-test
  (testing "every position is paired with every effect, exactly once"
    (is (= (* (count action-rolls/positions) (count action-rolls/effects))
           (count action-rolls/combinations)))
    (is (= (set action-rolls/combinations)
           (set (for [p action-rolls/positions e action-rolls/effects]
                  {:position p :effect e}))))))

(deftest generate-dice-results-test
  (testing "size 0 rolls a 2-dice pool and takes the minimum, never a critical"
    (dotimes [_ 20]
      (let [{:keys [pool result size critical]} (action-rolls/generate-dice-results 0)]
        (is (= 0 size))
        (is (= 2 (count pool)))
        (is (= (apply min pool) result))
        (is (false? critical)))))
  (testing "positive size rolls that many dice and takes the maximum"
    (dotimes [_ 20]
      (let [{:keys [pool result size]} (action-rolls/generate-dice-results 3)]
        (is (= 3 size))
        (is (= 3 (count pool)))
        (is (= (apply max pool) result)))))
  (testing "every rolled die is between 1 and 6"
    (dotimes [_ 20]
      (let [{:keys [pool]} (action-rolls/generate-dice-results 4)]
        (is (every? #(<= 1 % 6) pool)))))
  (testing "critical requires more than one six in the pool"
    ;; can't force randomness, so assert the invariant holds across many rolls:
    ;; critical is only ever true when at least two dice show 6
    (dotimes [_ 100]
      (let [{:keys [pool critical]} (action-rolls/generate-dice-results 4)
            sixes (count (filter #(= 6 %) pool))]
        (when critical
          (is (< 1 sixes)))))))

(defn- description-body
  "result-description always returns [:<> body], even when body is nil
  (no cond branch matched) — pull out body so tests can catch that case."
  [result position critical]
  (second (action-rolls/result-description result position critical)))

(deftest result-description-test
  (testing "critical takes precedence over position"
    (is (some? (description-body 6 "Controlled" true))))
  (testing "returns a description for every position and result band"
    (doseq [position ["Controlled" "Risky" "Desperate"]
            result [1 2 3 4 5 6]]
      (is (some? (description-body result position false))
          (str "no description for " position " / " result))))
  (testing "position is case-insensitive"
    (is (= (description-body 6 "risky" false)
           (description-body 6 "Risky" false)))))
