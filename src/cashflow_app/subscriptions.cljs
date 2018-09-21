(ns cashflow-app.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(rf/reg-sub
 ::amex-transactions
 (fn [db _]
   (:amex-transactions db)))

(rf/reg-sub
  ::amex-transactions-excluding-repayments
  :<- [::amex-transactions]
  (fn [amex-transactions _]
    (filter #(-> % :narrative (not= "PAYMENT RECEIVED - THANK YOU"))
            amex-transactions)))

(rf/reg-sub
 ::starling-transactions-and-balances
 (fn [db _]
   (:starling-transactions-and-balances db)))

(rf/reg-sub
  ::starling-transactions
  :<- [::starling-transactions-and-balances]
  (fn [starling-transactions-and-balances _]
    (map #(dissoc % :balance) starling-transactions-and-balances)))

(rf/reg-sub
  ::starling-transactions-excluding-amex-repayments
  :<- [::starling-transactions]
  (fn [starling-transactions _]
    (filter #(-> % :narrative (not= "American Express"))
            starling-transactions)))

(rf/reg-sub
 ::recurring-transactions
 (fn [db _]
   (:recurring-transactions db)))

(rf/reg-sub
 ::all-transactions
 :<- [::recurring-transactions]
 :<- [::starling-transactions-excluding-amex-repayments]
 :<- [::amex-transactions-excluding-repayments]
 (fn [[recurring-transactions
       starling-transactions
       amex-transactions] _]
   (concat recurring-transactions
           starling-transactions
           amex-transactions)))

(rf/reg-sub
 ::all-transactions-sorted
 :<- [::all-transactions]
 (fn [all-transactions _]
   (sort-by :date all-transactions)))

(rf/reg-sub
  ::start-date
  (fn [db _]
    (:start-date db)))

(rf/reg-sub
  ::computed-balance-start-id
  (fn [db _]
    (:computed-balance-start-id db)))

(rf/reg-sub
  ::computed-balance-start-date
  :<- [::computed-balance-start-id]
  :<- [::all-transactions]
  (fn [[computed-balance-start-id all-transactions] _]
    (:date (first (filter (fn [{:keys [id]}] (= computed-balance-start-id id))
                          all-transactions)))))

(rf/reg-sub
  ::computed-balance-start-amount
  :<- [::computed-balance-start-id]
  :<- [::starling-transactions-and-balances]
  (fn [[start-id starling-transactions-and-balances] _]
    (:balance (first (filter (fn [{:keys [id]}] (= start-id id))
                             starling-transactions-and-balances)))))

(defn new-computed-balance [prev-balance
                            {:as transaction :keys [amount date id]}
                            computed-balance-start-id
                            computed-balance-start-amount
                            computed-balance-start-date]
  (cond
    (= id computed-balance-start-id)      computed-balance-start-amount
    (>= date computed-balance-start-date) (-> (+ (js/parseFloat prev-balance) (js/parseFloat amount))
                                              (* 100)
                                              Math/round
                                              (/ 100)
                                              str)
    :else prev-balance))

(defn prev-balance [transactions]
  (or (:balance (peek transactions)) "0"))

(defn cashflow-transaction
  [transactions
   transaction
   computed-balance-start-id
   computed-balance-start-amount
   computed-balance-start-date]
  (assoc transaction
         :balance
         (new-computed-balance (prev-balance transactions)
                               transaction
                               computed-balance-start-id
                               computed-balance-start-amount
                               computed-balance-start-date)))

(rf/reg-sub
  ::cashflow-transactions-and-balances
  :<- [::all-transactions-sorted]
  :<- [::computed-balance-start-id]
  :<- [::computed-balance-start-amount]
  :<- [::computed-balance-start-date]
  (fn [[all-transactions-sorted
        computed-balance-start-id
        computed-balance-start-amount
        computed-balance-start-date]
       _]
    (reduce (fn [transactions transaction
                 {:as transaction :keys [date]}]
              (if (>= date computed-balance-start-date)
                (conj transactions
                      (cashflow-transaction transactions
                                            transaction
                                            computed-balance-start-id
                                            computed-balance-start-amount
                                            computed-balance-start-date))
                (conj transactions transaction)))
            []
            all-transactions-sorted)))
