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
              expected [["20/05/2018"
                         "Reference: AT181410028000010061619"
                         " 4.25"
                         "THE NATIONAL TRUST - GR HENLEY ON THAME"
                         "NT GREYS COURT COWSHED TEAROOM Process Date 20/05/2018  NT GREYS COURT COWSHED TEAROOM"]
                        ["13/05/2018"
                         "Reference: AT181330035000010097120"
                         " 12.61"
                         "UBER *TRIP D6PRL HELP.UBER.COM"
                         " Process Date 13/05/2018"]]]
          (is (= (nth (first expected) 0) (nth (first result) 0)))
          (is (= (nth (first expected) 1) (nth (first result) 1)))
          (is (= (nth (first expected) 2) (nth (first result) 2)))
          (is (= (nth (first expected) 3) (nth (first result) 3)))
          (is (= (nth (first expected) 4) (nth (first result) 4)))
          (is (= (nth (second expected) 0) (nth (second result) 0)))
          (is (= (nth (second expected) 1) (nth (second result) 1)))
          (is (= (nth (second expected) 2) (nth (second result) 2)))
          (is (= (nth (second expected) 3) (nth (second result) 3)))
          (is (= (nth (second expected) 4) (nth (second result) 4))))))))
