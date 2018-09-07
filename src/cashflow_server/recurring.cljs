(ns cashflow-server.recurring
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!]]
            [clojure.string :as string]
            ["fs" :as fs]
            [cashflow-server.date :as date]
            [cashflow-server.utils :as utils]))

(defn range-months-to-add [transaction today]
  (if (< (:day transaction) (date/get-day today))
    (range 1 11)
    (range 0 10)))

(defn future-transaction [today recurring-transaction months-to-add]
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

(defn future-transactions [today transaction]
  (map #(future-transaction today transaction %)
       (range-months-to-add transaction today)))

(defn json->clj [json]
  (js->clj (js-invoke js/JSON "parse" json) :keywordize-keys true))

(defn read-file-async [filename]
  (utils/js-invoke-async fs "readFile" filename "utf8"))

(defn transactions [{:keys [RECURRING_TRANSACTIONS_FILENAME]}]
  (go (->> RECURRING_TRANSACTIONS_FILENAME
           read-file-async
           <!
           json->clj
           (map #(future-transactions (date/today) %))
           flatten
           (sort-by :date))))
