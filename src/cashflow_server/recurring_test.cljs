(ns cashflow-server.recurring-test
  (:require [cljs.test :refer (deftest is testing)]
            [cashflow-server.recurring :as sut]))

(deftest future-transactions-test
  (testing "when the transaction's day is after today"
    (let [transaction {:narrative "Mortgage"
                       :day "16"
                       :amount "300.00"}
          today "2018-08-15"
          result (sut/future-transactions today transaction )]
      (testing "returns 10 future transactions starting next month"
        (is (= 10 (count result)))
        (is (= "2018-08-16" (:date (nth result 0))))
        (is (= "2018-09-16" (:date (nth result 1))))
        (is (= "2018-10-16" (:date (nth result 2))))
        (is (= "2018-11-16" (:date (nth result 3))))
        (is (= "2018-12-16" (:date (nth result 4))))
        (is (= "2019-01-16" (:date (nth result 5))))
        (is (= "2019-02-16" (:date (nth result 6))))
        (is (= "2019-03-16" (:date (nth result 7))))
        (is (= "2019-04-16" (:date (nth result 8))))
        (is (= "2019-05-16" (:date (nth result 9)))))))
  (testing "when the transaction's day is before today"
    (let [transaction {:narrative "Mortgage"
                       :day "13"
                       :amount "300.00"}
          today "2018-08-15"
          result (sut/future-transactions today transaction)]
      (testing "returns 10 future transactions starting this month"
        (is (= 10 (count result)))
        (is (= "2018-09-13" (:date (nth result 0))))
        (is (= "2018-10-13" (:date (nth result 1))))
        (is (= "2018-11-13" (:date (nth result 2))))
        (is (= "2018-12-13" (:date (nth result 3))))
        (is (= "2019-01-13" (:date (nth result 4))))
        (is (= "2019-02-13" (:date (nth result 5))))
        (is (= "2019-03-13" (:date (nth result 6))))
        (is (= "2019-04-13" (:date (nth result 7))))
        (is (= "2019-05-13" (:date (nth result 8))))
        (is (= "2019-06-13" (:date (nth result 9))))))))