(ns cashflow-app.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  :initialising?
  (fn [db _]
    (if db
      false
      true)))

(rf/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))


(rf/reg-sub
 :amex-repayment-inline-end-date
 (fn [db _]
   (:amex-repayment-inline-end-date db)))

(rf/reg-sub
 :amex-transaction-inline-start-date
 (fn [db _]
   (:amex-transaction-inline-start-date db)))

(rf/reg-sub
 :amex-transactions-raw
 (fn [db _]
   (:amex-transactions db)))

(rf/reg-sub
 :amex-transactions
 :<- [:amex-transactions-raw]
 :<- [:amex-transaction-inline-start-date]
 (fn [[amex-transactions
       amex-transaction-inline-start-date] _]
   (->> amex-transactions
        (filter #(-> (:narrative %)
                     (not= "PAYMENT RECEIVED - THANK YOU")))
        (filter #(-> (:date %)
                     (subs 0 10)
                     (>= amex-transaction-inline-start-date))))))

(rf/reg-sub
 :starling-transactions-raw
 (fn [db _]
   (:starling-transactions-and-balances db)))

(rf/reg-sub
  :starling-transactions
  :<- [:amex-repayment-inline-end-date]
  :<- [:starling-transactions-raw]
  (fn [[amex-repayment-inline-end-date
        starling-transactions-raw]
       _]
    (filter #(or (-> (:date %)
                     (subs 0 10)
                     (<= amex-repayment-inline-end-date))
                 (-> (:narrative %)
                     (not= "American Express")))
            starling-transactions-raw)))

(rf/reg-sub
 :adjustment-transactions
 (fn [db _]
   (:adjustment-transactions db)))

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
 (fn [[recurring-transactions
       starling-transactions
       amex-transactions
       adjustment-transactions] _]
   (concat recurring-transactions
           starling-transactions
           amex-transactions
           adjustment-transactions)))

(rf/reg-sub
 :all-transactions-sorted
 :<- [:all-transactions]
 (fn [all-transactions _]
   (sort-by :date all-transactions)))

(rf/reg-sub
  :start-date
  (fn [db _]
    (:start-date db)))

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
  :<- [:all-transactions-sorted]
  (fn [all-transactions-sorted _]
    (reduce (fn [transactions transaction]
              (conj transactions
                    (assoc transaction
                           :balance
                           (balance (prev-balance transactions)
                                    transaction))))
            []
            all-transactions-sorted)))
