(ns cashflow.client.subscriptions
  (:require [re-frame.core :as rf]
            [cashflow.client.date :as date]))

(rf/reg-sub
 :amex-transactions
 (fn [db _]
   (sort-by :date
            (filter #(-> (:narrative %)
                         (not= "PAYMENT RECEIVED - THANK YOU"))
                    (:amex-transactions db)))))

(rf/reg-sub
  :starling-transactions
  (fn [db _]
    (->> db
         :starling-transactions-and-balances
         (filter #(or (date/is-before? (:date %) "2018-12-05")
                      (not= (:narrative %) "American Express")))
         (sort-by :date))))

(rf/reg-sub
 :adjustment-transactions
 (fn [db _]
   (:adjustment-transactions db)))

(rf/reg-sub
 :one-off-transactions
 (fn [db _]
   (:one-off-transactions db)))

(rf/reg-sub
 :recurring-transactions
 (fn [db _]
   (:recurring-transactions db)))

(rf/reg-sub
 :all-transactions
 :<- [:recurring-transactions]
 :<- [:starling-transactions]
 :<- [:amex-transactions]
 :<- [:adjustment-transactions]
 :<- [:one-off-transactions]
 :<- [:filters]
 (fn [[recurring-transactions
       starling-transactions
       amex-transactions
       adjustment-transactions
       one-off-transactions
       filters] _]
   (sort-by :date
            (concat (if (:recurring filters) recurring-transactions)
                    (if (:starling filters) starling-transactions)
                    (if (:amex filters) amex-transactions)
                    (if (:adjustments filters) adjustment-transactions)
                    (if (:one-off filters) one-off-transactions)))))

(defn balance [prev-balance transaction]
  (if (nil? prev-balance)
    (:balance transaction)
    (-> (:amount transaction)
       js/parseFloat
       (+ (js/parseFloat prev-balance))
       (* 100)
       Math/round
       (/ 100)
       str)))

(defn prev-balance [transactions]
  (if (> (count transactions) 0)
    (:balance (peek transactions))
    nil))

(rf/reg-sub
  :cashflow-transactions-and-balances
  :<- [:all-transactions]
  (fn [all-transactions _]
    (reduce (fn [acc transaction]
              (conj acc
                    (assoc transaction
                           :balance
                           (balance (prev-balance acc)
                                    transaction))))
            []
            all-transactions)))

(rf/reg-sub
  :filters
  (fn [db _]
    (:filters db)))
