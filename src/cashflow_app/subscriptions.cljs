(ns cashflow-app.subscriptions
  (:require [re-frame.core :as rf]))

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
    (sort-by :date
             (filter #(-> (:narrative %) (not= "American Express"))
                     (:starling-transactions-and-balances db)))))

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
   (sort-by :date
            (concat recurring-transactions
                    starling-transactions
                    amex-transactions
                    adjustment-transactions))))

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
