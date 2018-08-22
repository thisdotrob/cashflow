(ns cashflow-app.stub-data
  (:require [cashflow-app.data-transformation :as transform]))

(def amex-transactions-clj
  [{:id "AT182010046000010092658"
    :date "2018-07-18"
    :narrative "Amex transaction 1"
    :amount "14.85"}
   {:id "AT182010078000010006952"
    :date "2018-07-19"
    :narrative "Amex transaction 2"
    :amount "40.30"}])

(def amex-transactions (-> amex-transactions-clj
                           transform/clj->json
                           transform/json->clj))
