(ns cashflow.server.amex
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require ["fs" :as fs]
            [goog.labs.format.csv :as csv]
            [goog.string :as gstring]
            goog.string.format
            [cljs.core.async :refer [<!]]
            [clojure.string :as string]
            [cashflow.server.utils :as utils]))

(defn amex-amount->amount [amount]
  (->> amount
      string/trim
      (js/parseFloat)
      (* -1)
      (gstring/format "%.2f")))

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

(defn transactions [_]
  (go
    (map amex-csv-row->transaction
         (csv/parse (<! (utils/js-invoke-async fs
                                               "readFile"
                                               "./amex_data/amex.csv"
                                               "utf8"))))))
