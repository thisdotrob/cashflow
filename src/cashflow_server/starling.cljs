(ns cashflow-server.starling
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.string :as gstring]
            goog.string.format
            [cashflow-server.utils :as utils]
            [cljs.core.async :refer [<!]]
            [clojure.string :as string]
            ["fs" :as fs]))

(defn starling-transaction->transaction [transaction]
  {:id (get transaction "id")
   :date (subs (get transaction "created") 0 10)
   :narrative (get transaction "narrative")
   :amount (gstring/format "%.2f" (get transaction "amount"))})

(defn transactions [{:keys [STARLING_HOST STARLING_TOKEN]}]
  (go
    (->> {:hostname STARLING_HOST
          :path "/api/v1/transactions"
          :headers {:Authorization (str "Bearer " STARLING_TOKEN)}}
         utils/https-get-async
         <!
         (.parse js/JSON)
         js->clj
         (#(get-in % ["_embedded" "transactions"]))
         (map starling-transaction->transaction))))
