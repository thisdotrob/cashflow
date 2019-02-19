(ns cashflow.server.recurring
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!]]
            [cashflow.server.date :as date]
            [cashflow.server.utils :as utils]))

(defn recurrence-rule->payment-dates
  [{:keys [count frequency interval start-date] :as recurrence-rule}]
  (let [one-off-payment? (and (= 1 count)
                              (nil? frequency)
                              (nil? interval))
        add-fn (if (= "MONTHLY" frequency)
                 date/add-months
                 date/add-weeks)]
    (if one-off-payment?
      [start-date]
      (->> (range 0 count)
           (map #(* interval %))
           (map #(str (add-fn start-date %)))))))

(defn recurrence-rule->future-transactions
  [{:keys [amount narrative source] :as recurrence-rule}]
  (->> recurrence-rule
       recurrence-rule->payment-dates
       (map (fn [date]
              {:source source
               :narrative narrative
               :amount amount
               :date (str date "T01:30:00.000Z") ;; savings goals get taken at about this time
               :id (str narrative
                        amount
                        date)}))))

(defn recurrence-rules->future-transactions
  [recurrence-rules]
  (->> recurrence-rules
       (mapcat recurrence-rule->future-transactions)
       (filter #(date/in-future? (:date %)))))

(defn transactions [_]
  (go (->> "user_data/recurring.json"
           utils/read-file-async
           <!
           utils/json->clj
           (map #(assoc % :source "Recurring"))
           recurrence-rules->future-transactions)))
