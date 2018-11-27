(ns cashflow-server.recurring
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!]]
            [clojure.string :as string]
            ["fs" :as fs]
            [cashflow-server.date :as date]
            [cashflow-server.utils :as utils]))

(def NUM-FUTURE-WEEKLY-TRANSACTIONS 40)
(def NUM-FUTURE-MONTHLY-TRANSACTIONS 10)

(defn range-months-to-add [transaction today]
  (if (< (:day transaction) (date/get-day today))
    (range 1 (+ 1 NUM-FUTURE-MONTHLY-TRANSACTIONS))
    (range 0 (NUM-FUTURE-MONTHLY-TRANSACTIONS))))

(defn future-monthly-transaction [today recurring-transaction months-to-add]
  (let [{:keys [narrative amount day]} recurring-transaction
        date (-> today
                 (date/add-months months-to-add)
                 (date/set-day day)
                 (str "T23:59:59.999Z"))
        id (str narrative amount day date)]
    {:source "Recurring"
     :narrative narrative
     :amount amount
     :date date
     :id id}))

(defn future-monthly-transactions [today transaction]
  (map #(future-monthly-transaction today transaction %)
       (range-months-to-add transaction today)))

(defn json->clj [json]
  (js->clj (js-invoke js/JSON "parse" json) :keywordize-keys true))

(defn read-file-async [filename]
  (utils/js-invoke-async fs "readFile" filename "utf8"))

(defn monthly-transactions [recurring-transactions]
  (->> recurring-transactions
       (filter #(= "monthly" (:frequency %)))
       (map #(future-monthly-transactions (date/today) %))
       flatten))

(defn future-weekly-transaction-dates [start-date]
  (map #(str (date/add-weeks start-date %) "T23:59:59.999Z")
       (range 0 NUM-FUTURE-WEEKLY-TRANSACTIONS)))

(defn future-weekly-transaction [transaction date]
  (let [{:keys [narrative amount day]} transaction
        id (str narrative amount day date)]
    {:source "Recurring"
    :narrative narrative
    :amount amount
    :date date
    :id id}))

(defn future-weekly-transactions [transaction]
  (let [day-of-week (:day transaction)
        start-date (date/next-day-of-week day-of-week)
        transaction-dates (future-weekly-transaction-dates start-date)]
    (map #(future-weekly-transaction transaction %) transaction-dates)))

(defn weekly-transactions [recurring-transactions]
  (->> recurring-transactions
       (filter #(= "weekly" (:frequency %)))
       (map #(future-weekly-transactions %))
       flatten))

(defn transactions [_]
  (go (let [recurring-transactions (->> "recurring.json"
                                        read-file-async
                                        <!
                                        json->clj
                                        (sort-by :date))
            monthly-transactions (monthly-transactions recurring-transactions)
            weekly-transactions (weekly-transactions recurring-transactions)]
        (sort-by :date (concat weekly-transactions monthly-transactions)))))
