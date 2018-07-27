(ns cashflow-server.main-test
  (:require [cljs.test :refer (deftest is testing async)]
            ["supertest" :as supertest]
            [cashflow-server.main :as server]))

(deftest test-the-server-is-up
  (testing "should get the status message"
    (async done
           (-> (supertest server/app)
               (.get "/status")
               (.expect (fn [res]
                          (is (= 200 res.status))
                          (is (= "cashflow-server" res.body.service))
                          (is (= "green" res.body.status))))
               (.end done)))))

(deftest retrieving-starling-transactions
  (testing "should return the transactions"
    (async done
           (-> (supertest server/app)
               (.get "/transactions")
               (.expect (fn [res]
                          (is (= 200 res.status))
                          (is (= "transactions" res.body))))
               (.end done)))))
