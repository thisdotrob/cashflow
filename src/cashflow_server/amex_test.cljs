(ns cashflow-server.amex-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer (deftest is testing async)]
            [cljs.core.async :refer [<!]]
            [cashflow-server.amex :as sut]
            [cashflow-server.utils :as utils]))

(deftest retrieving-amex-transactions-from-file
  (testing "transactions returned from the filename given by the AMEX_TRANSACTIONS_FILENAME env var"
    (async
      done
      (go
        (let [env-vars {:AMEX_TRANSACTIONS_FILENAME "amex-test-transactions.csv"}
              result (<! (sut/transactions env-vars))
              expected [{:id "AT181410028000010061619"
                         :date "2018-05-20"
                         :narrative "THE NATIONAL TRUST - GR HENLEY ON THAME NT GREYS COURT COWSHED TEAROOM Process Date 20/05/2018  NT GREYS COURT COWSHED TEAROOM"
                         :amount "4.25"}
                        {:id "AT181330035000010097120"
                         :date "2018-05-13"
                         :narrative "UBER *TRIP D6PRL HELP.UBER.COM  Process Date 13/05/2018"
                         :amount "12.61"}
                        ]]
          (is (= (:id (first expected)) (:id (first result))))
          (is (= (:date (first expected)) (:date (first result))))
          (is (= (:narrative (first expected)) (:narrative (first result))))
          (is (= (:amount (first expected)) (:amount (first result))))
          (is (= (:id (second expected)) (:id (second result))))
          (is (= (:date (second expected)) (:date (second result))))
          (is (= (:narrative (second expected)) (:narrative (second result))))
          (is (= (:amount (second expected)) (:amount (second result)))))))))
