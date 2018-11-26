(ns cashflow-server.starling
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.string :as gstring]
            goog.string.format
            [cashflow-server.utils :as utils]
            [cljs.core.async :refer [<!]]
            [clojure.string :as string]
            ["fs" :as fs]))

(defn starling-transaction->transaction-and-balance [transaction]
  {:source "Starling"
   :id (get transaction "id")
   :date (get transaction "created")
   :narrative (get transaction "narrative")
   :amount (gstring/format "%.2f" (get transaction "amount"))
   :balance (gstring/format "%.2f" (get transaction "balance"))})

(defn transactions-and-balances [{:keys [STARLING_TOKEN]}]
  (go
    (->> {:hostname "api.starlingbank.com"
          :path "/api/v1/transactions"
          :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
         utils/https-get-async
         <!
         (.parse js/JSON)
         js->clj
         (#(get-in % ["_embedded" "transactions"]))
         (map starling-transaction->transaction-and-balance))))
