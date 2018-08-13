(ns cashflow-server.amex
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require ["fs" :as fs]
            [goog.labs.format.csv :as csv]
            [cljs.core.async :refer [<!]]
            [clojure.string :as string]
            [cashflow-server.utils :as utils]))

(defn amex-amount->amount [amount]
  (string/trim amount))

(defn amex-reference->id [reference]
  (subs reference 11))

(defn amex-date->date [date]
  (string/join "-" (reverse (string/split date "/"))))

(defn amex-csv-row->transaction [row]
  {:id (amex-reference->id (nth row 1))
   :date (amex-date->date (nth row 0))
   :narrative (str (nth row 3) " " (nth row 4))
   :amount (amex-amount->amount (nth row 2))})

(defn transactions [{:keys [AMEX_TRANSACTIONS_FILENAME]}]
  (go
    (map amex-csv-row->transaction
         (csv/parse (<! (utils/js-invoke-async fs
                                               "readFile"
                                               AMEX_TRANSACTIONS_FILENAME
                                               "utf8"))))))