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
 ::starling-transactions-and-balances
 (fn [db _]
   (:starling-transactions-and-balances db)))

(rf/reg-sub
  ::starling-transactions
  :<- [::starling-transactions-and-balances]
  (fn [starling-transactions-and-balances _]
    (map #(dissoc % :balance) starling-transactions-and-balances)))

(rf/reg-sub
 ::recurring-transactions
 (fn [db _]
   (:recurring-transactions db)))

(rf/reg-sub
 ::all-transactions
 :<- [::recurring-transactions]
 :<- [::starling-transactions]
 :<- [::amex-transactions]
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

(rf/reg-sub
  ::all-transactions-with-computed-balances
  :<- [::all-transactions-sorted]
  :<- [::computed-balance-start-id]
  :<- [::computed-balance-start-amount]
  :<- [::computed-balance-start-date]
  (fn [[all-transactions-sorted
        computed-balance-start-id
        computed-balance-start-amount
        computed-balance-start-date]
       _]
    (reduce (fn [transactions
                 {:as transaction :keys [id date amount]}]
              (let [prev-balance (or (:balance (peek transactions)) "0")
                    new-balance (cond
                                  (= id computed-balance-start-id) computed-balance-start-amount
                                  (>= date computed-balance-start-date) (str (+ (int prev-balance) ;; NOT INT!!!!
                                                                                (int amount)))
                                  :else prev-balance)]
                (conj transactions
                      (assoc transaction
                             :balance
                             new-balance))))
            []
            all-transactions-sorted)))
