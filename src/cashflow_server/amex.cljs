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
  (str (string/join "-"
                    (reverse (string/split date "/")))
       "T23:59:59.999Z"))

(defn amex-csv-row->transaction [row]
  {:source "Amex"
   :id (amex-reference->id (nth row 1))
   :date (amex-date->date (nth row 0))
   :narrative (nth row 3)
   :amount (amex-amount->amount (nth row 2))})

(defn transactions [{:keys [AMEX_TRANSACTIONS_PATH AMEX_TRANSACTIONS_FILENAME]}]
  (go
    (map amex-csv-row->transaction
         (csv/parse (<! (utils/js-invoke-async fs
                                               "readFile"
                                               (str AMEX_TRANSACTIONS_PATH
                                                    "/"
                                                    AMEX_TRANSACTIONS_FILENAME)
                                               "utf8"))))))
